//$URL$
//$Id$
package de.dev.eth0.bitcointrader.service;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.xeiam.xchange.ExchangeException;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.currency.MoneyUtils;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.marketdata.Trades;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.MarketOrder;
import com.xeiam.xchange.mtgox.v2.MtGoxAdapters;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxAccountInfo;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWalletHistory;
import com.xeiam.xchange.mtgox.v2.dto.trade.polling.MtGoxOrderResult;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.ui.BitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.PlaceOrderActivity;
import de.dev.eth0.bitcointrader.ui.fragments.PlaceOrderFragment;
import de.dev.eth0.bitcointrader.ui.widgets.AccountInfoWidgetProvider;
import de.dev.eth0.bitcointrader.ui.widgets.PriceInfoWidgetProvider;
import de.dev.eth0.bitcointrader.util.FormatHelper;
import de.dev.eth0.bitcointrader.util.ICSAsyncTask;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import si.mazi.rescu.HttpException;

/**
 * Service to cache all data from exchange to prevent multiple calls
 *
 * @author Alexander Muthmann
 */
public class ExchangeService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

  private static final String TAG = ExchangeService.class.getSimpleName();
  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d(TAG, ".onReceive()");
      // only run if currently no running task
      if (exchange != null) {
        executeTask(new UpdateTask(), (Void) null);
      }
    }
  };
  private LocalBroadcastManager broadcastManager;

  public class LocalBinder extends Binder {

    public ExchangeService getService() {
      return ExchangeService.this;
    }
  }
  private MtGoxExchangeWrapper exchange;
  private final Binder binder = new LocalBinder();
  private MtGoxAccountInfo accountInfo;
  private List<LimitOrder> openOrders = new ArrayList<LimitOrder>();
  private Float trailingStopThreadhold;
  private BigDecimal trailingStopValue;
  private BigDecimal[] trailingStopChecks = new BigDecimal[1];
  private boolean notifyOnUpdate;
  private int updateInterval;
  private Ticker ticker;
  private Date lastUpdate;
  private Date lastUpdateWalletHistory;
  private Map<String, List<MtGoxWalletHistory>> walletHistoryCache = new HashMap<String, List<MtGoxWalletHistory>>();

  @Override
  public void onCreate() {
    super.onCreate();
    broadcastManager = LocalBroadcastManager.getInstance(this);
    broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constants.UPDATE_SERVICE_ACTION));

  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    prefs.registerOnSharedPreferenceChangeListener(this);
    notifyOnUpdate = prefs.getBoolean(Constants.PREFS_KEY_GENERAL_NOTIFY_ON_UPDATE, false);
    updateInterval = Integer.parseInt(prefs.getString(Constants.PREFS_KEY_GENERAL_UPDATE, "0"));
    float th = prefs.getFloat(Constants.PREFS_TRAILING_STOP_THREASHOLD, Float.MIN_VALUE);
    if (th != Float.MIN_VALUE) {
      trailingStopThreadhold = th;
    }
    String tvalue = prefs.getString(Constants.PREFS_TRAILING_STOP_VALUE, null);
    if (!TextUtils.isEmpty(tvalue)) {
      trailingStopValue = new BigDecimal(tvalue);
    } else {
      trailingStopValue = null;
    }
    createExchange(prefs);
    return Service.START_STICKY;
  }

  private void createExchange(SharedPreferences prefs) {
    String mtGoxAPIKey, mtGoxSecretKey;
    if (prefs.getBoolean(Constants.PREFS_KEY_DEMO, false)) {
      mtGoxAPIKey = Constants.MTGOX_DEMO_ACCOUNT_APIKEY;
      mtGoxSecretKey = Constants.MTGOX_DEMO_ACCOUNT_SECRETKEY;
    } else {
      mtGoxAPIKey = prefs.getString(Constants.PREFS_KEY_MTGOX_APIKEY, null);
      mtGoxSecretKey = prefs.getString(Constants.PREFS_KEY_MTGOX_SECRETKEY, null);
    }
    if (!TextUtils.isEmpty(mtGoxAPIKey) && !TextUtils.isEmpty(mtGoxSecretKey)) {
      ExchangeSpecification exchangeSpec = new ExchangeSpecification(MtGoxExchangeWrapper.class);
      exchangeSpec.setApiKey(mtGoxAPIKey);
      exchangeSpec.setSecretKey(mtGoxSecretKey);
      exchangeSpec.setSslUri(Constants.MTGOX_SSL_URI);
      exchangeSpec.setPlainTextUriStreaming(Constants.MTGOX_PLAIN_WEBSOCKET_URI);
      exchangeSpec.setSslUriStreaming(Constants.MTGOX_SSL_WEBSOCKET_URI);
      exchange = (MtGoxExchangeWrapper) ExchangeFactory.INSTANCE.createExchange(exchangeSpec);
      broadcastUpdate();
    }
  }

  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (key.equals(Constants.PREFS_KEY_MTGOX_APIKEY) || key.equals(Constants.PREFS_KEY_MTGOX_SECRETKEY)) {
      createExchange(sharedPreferences);
    } else if (key.equals(Constants.PREFS_KEY_GENERAL_NOTIFY_ON_UPDATE)) {
      notifyOnUpdate = sharedPreferences.getBoolean(Constants.PREFS_KEY_GENERAL_NOTIFY_ON_UPDATE, false);
    } else if (key.equals(Constants.PREFS_KEY_GENERAL_UPDATE)) {
      updateInterval = Integer.parseInt(sharedPreferences.getString(Constants.PREFS_KEY_GENERAL_UPDATE, "0"));
    } else if (key.equals(Constants.PREFS_TRAILING_STOP_THREASHOLD)) {
      float th = sharedPreferences.getFloat(Constants.PREFS_TRAILING_STOP_THREASHOLD, Float.MIN_VALUE);
      if (th != Float.MIN_VALUE) {
        trailingStopThreadhold = th;
      }
    } else if (key.equals(Constants.PREFS_TRAILING_STOP_VALUE)) {
      String tvalue = sharedPreferences.getString(Constants.PREFS_TRAILING_STOP_VALUE, null);
      if (!TextUtils.isEmpty(tvalue)) {
        trailingStopValue = new BigDecimal(tvalue);
      } else {
        trailingStopValue = null;
      }
    } else if (key.equals(Constants.PREFS_TRAILING_STOP_NUMBER_UPDATES)) {
      int updates = sharedPreferences.getInt(Constants.PREFS_TRAILING_STOP_NUMBER_UPDATES, 1);
      trailingStopChecks = new BigDecimal[updates];
    }
  }

  @Override
  public void onDestroy() {
    if (broadcastReceiver != null) {
      broadcastManager.unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    }
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent arg0) {
    return binder;
  }

  public void setCurrency(String currency) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    if (!TextUtils.equals(prefs.getString(Constants.PREFS_KEY_CURRENCY, null), currency)) {
      deleteTrailingStopLoss();
      prefs.edit().putString(Constants.PREFS_KEY_CURRENCY, currency).apply();
      sendBroadcast(new Intent(Constants.CURRENCY_CHANGE_EVENT));
    }
  }

  public String getCurrency() {
    return PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PREFS_KEY_CURRENCY, "USD");
  }

  private void deleteTrailingStopLoss() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = prefs.edit();
    editor.remove(Constants.PREFS_TRAILING_STOP_THREASHOLD);
    editor.remove(Constants.PREFS_TRAILING_STOP_VALUE);
    editor.remove(Constants.PREFS_TRAILING_STOP_NUMBER_UPDATES);
    editor.apply();
  }

  public MtGoxExchangeWrapper getExchange() {
    return exchange;
  }

  public OrderBook getOrderBook() {
    return getExchange().getPollingMarketDataService().getPartialOrderBook("BTC", getCurrency());
  }

  public Map<String, List<MtGoxWalletHistory>> getMtGoxWalletHistory(String[] currencies, boolean forceUpdate) {
    boolean update = forceUpdate;
    if (updateInterval > 0 && !forceUpdate) {
      // one minute has 60*1000 miliseconds
      Date now = new Date();
      if (lastUpdateWalletHistory != null && (now.getTime() - lastUpdateWalletHistory.getTime()) >= updateInterval * 60 * 1000) {
        update = true;
      }
    }
    // no update is required and cache contains wallethistory
    if (!update) {
      return Collections.unmodifiableMap(walletHistoryCache);
    }
    walletHistoryCache.clear();
    for (String currency : currencies) {
      try {
        MtGoxWalletHistory walletHistory = exchange.getPollingAccountService().getMtGoxWalletHistory(currency, null);
        List<MtGoxWalletHistory> pages = new ArrayList<MtGoxWalletHistory>();
        if (walletHistory != null) {
          pages.add(walletHistory);
          if (walletHistory.getCurrentPage() < walletHistory.getMaxPage()) {
            for (int page = 2; page <= walletHistory.getMaxPage(); page++) {
              walletHistory = exchange.getPollingAccountService().getMtGoxWalletHistory(currency, page);
              if (walletHistory != null) {
                pages.add(walletHistory);
              }
            }
          }
        }
        if (!pages.isEmpty()) {
          walletHistoryCache.put(currency, pages);
        }
      } catch (ExchangeException ee) {
        Log.i(TAG, "ExchangeException", ee);
        broadcastUpdateFailure();
      } catch (HttpException uhe) {
        Log.e(TAG, "HttpException", uhe);
        broadcastUpdateFailure();
      } catch (RuntimeException iae) {
        Log.e(TAG, "RuntimeException", iae);
        broadcastUpdateFailure();
      }
    }
    lastUpdateWalletHistory = new Date();
    return Collections.unmodifiableMap(walletHistoryCache);
  }

  public Trades getTrades() {
    return exchange.getPollingMarketDataService().getTrades("BTC", getCurrency());
  }

  public MtGoxAccountInfo getAccountInfo() {
    return accountInfo;
  }

  public List<LimitOrder> getOpenOrders() {
    return openOrders;
  }

  public Ticker getTicker() {
    return ticker;
  }

  public Date getLastUpdate() {
    return lastUpdate;
  }

  public void deleteOrder(Order order) {
    Log.d(TAG, ".deleteOrder()");
    executeTask(new DeleteOrderTask(), order);
  }

  public void placeOrder(Order order, FragmentActivity activity) {
    Log.d(TAG, ".placeOrder()");
    executeTask(new PlaceOrderTask(activity), order);
  }

  private <S, T, U> void executeTask(ICSAsyncTask<S, T, U> task, S... params) {
    task.executeOnExecutor(ICSAsyncTask.SERIAL_EXECUTOR, params);
  }

  private void broadcastUpdate() {
    broadcastManager.sendBroadcast(new Intent(Constants.UPDATE_SERVICE_ACTION));
  }

  private void broadcastUpdateSuccess() {
    sendBroadcast(new Intent(Constants.UPDATE_SUCCEDED));
  }

  private void broadcastUpdateFailure() {
    sendBroadcast(new Intent(Constants.UPDATE_FAILED));
  }

  private void broadcastTrailingStopEvent(BigDecimal trailingStopValue, BigDecimal currentPrice) {
    Intent intent = new Intent(Constants.TRAILING_LOSS_EVENT);
    intent.putExtra(Constants.EXTRA_TRAILING_LOSS_EVENT_VALUE, trailingStopValue.toString());
    intent.putExtra(Constants.EXTRA_TRAILING_LOSS_EVENT_CURRENTPRICE, currentPrice.toString());
    sendBroadcast(intent);
  }

  private void broadcastTrailingStopAlignmentEvent(BigDecimal trailingStopValue, BigDecimal currentPrice) {
    Intent intent = new Intent(Constants.TRAILING_LOSS_ALIGNMENT_EVENT);
    intent.putExtra(Constants.EXTRA_TRAILING_LOSS_ALIGNMENT_OLDVALUE, trailingStopValue.toString());
    intent.putExtra(Constants.EXTRA_TRAILING_LOSS_ALIGNMENT_NEWVALUE, currentPrice.toString());
    sendBroadcast(intent);
  }

  private <T extends Order> void broadcastOrderExecuted(Collection<T> openOrders) {
    Intent intent = new Intent(Constants.ORDER_EXECUTED);
    List<Parcelable> extras = new ArrayList<Parcelable>();
    for (Order lo : openOrders) {
      try {
        MtGoxOrderResult result = exchange.getPollingTradeService().getOrderResult(lo);
        Bundle bundle = new Bundle();
        BigMoney amount = MoneyUtils.parse(result.getAvgCost().getCurrency() + " " + result.getAvgCost().getValue());

        bundle.putString(Constants.EXTRA_ORDERRESULT_AVGCOST, FormatHelper.formatBigMoney(
                FormatHelper.DISPLAY_MODE.CURRENCY_CODE, amount, Constants.PRECISION_BITCOIN).toString());
        amount = MoneyUtils.parse(result.getTotalAmount().getCurrency() + " " + result.getTotalAmount().getValue());
        bundle.putString(Constants.EXTRA_ORDERRESULT_TOTALAMOUNT, FormatHelper.formatBigMoney(
                FormatHelper.DISPLAY_MODE.CURRENCY_CODE, amount, Constants.PRECISION_BITCOIN).toString());
        amount = MoneyUtils.parse(result.getTotalSpent().getCurrency() + " " + result.getTotalSpent().getValue());
        bundle.putString(Constants.EXTRA_ORDERRESULT_TOTALSPENT, FormatHelper.formatBigMoney(
                FormatHelper.DISPLAY_MODE.CURRENCY_CODE, amount, Constants.PRECISION_CURRENCY).toString());
        extras.add(bundle);
      } catch (Exception ee) {
        Log.d(TAG, "getting OrderResult failed", ee);
      }
    }
    Log.d(TAG, "Sending out order executed intent");
    if (!extras.isEmpty()) {
      intent.putExtra(Constants.EXTRA_ORDERRESULT, extras.toArray(new Parcelable[extras.size()]));
      sendBroadcast(intent);
    }
  }

  private class UpdateTask extends ICSAsyncTask<Void, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Void... params) {
      Log.d(TAG, "performing update...");
      try {
        accountInfo = exchange.getPollingAccountService().getMtGoxAccountInfo();
        ticker = exchange.getPollingMarketDataService().getTicker(Currencies.BTC, getCurrency());
        checkTrailingStop();

        if (TextUtils.isEmpty(getCurrency())) {
          setCurrency(accountInfo.getWallets().getMtGoxWallets().get(1).getBalance().getCurrency());
        }
        List<LimitOrder> orders = exchange.getPollingTradeService().getOpenOrders().getOpenOrders();
        openOrders.removeAll(orders);
        // Order executed
        if (!openOrders.isEmpty()) {
          broadcastOrderExecuted(openOrders);
        }
        openOrders = orders;
        lastUpdate = new Date();
        broadcastUpdateSuccess();
      } catch (ExchangeException ee) {
        Log.i(TAG, "ExchangeException", ee);
        broadcastUpdateFailure();
        return false;
      } catch (HttpException uhe) {
        Log.e(TAG, "HttpException", uhe);
        broadcastUpdateFailure();
        return false;
      } catch (RuntimeException iae) {
        Log.e(TAG, "RuntimeException", iae);
        broadcastUpdateFailure();
        return false;
      }
      return true;
    }

    private void checkTrailingStop() {
      // Check trailing stop loss
      if (trailingStopThreadhold != null && trailingStopValue != null) {
        Log.d(TAG, "checking trailing stop loss");
        // compare current price from array with last updates
        // first move all items one step to the left
        for (int i = 0; i < trailingStopChecks.length - 1; i++) {
          trailingStopChecks[i] = trailingStopChecks[i + 1];
        }
        trailingStopChecks[trailingStopChecks.length - 1] = ticker.getLast().getAmount();
        // now calculate average
        BigDecimal currentPrice = BigDecimal.ZERO;
        for (int i = trailingStopChecks.length - 1; i >= 0; i--) {
          BigDecimal bd = trailingStopChecks[i];
          if (bd == null) {
            Log.d(TAG, "not enough updates yet...");
            return;  // if a single value is not set yet, we need more updates
          }
          currentPrice = currentPrice.add(bd);
        }
        currentPrice = currentPrice.divide(new BigDecimal(trailingStopChecks.length));
        // check if price has fallen below the limit
        if (currentPrice.compareTo(trailingStopValue) < 0) {
          Log.d(TAG, "selling btc as the price has fallen from " + trailingStopValue.toString() + " to " + currentPrice.toString());
          broadcastTrailingStopEvent(trailingStopValue, currentPrice);
          deleteTrailingStopLoss();
          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ExchangeService.this);
          if (prefs.getBoolean(Constants.PREFS_KEY_TRAILING_STOP_SELLING_ENABLED, false)) {
            Log.d(TAG, "selling is enabled, selling btc");
            AccountInfo accountInfo = MtGoxAdapters.adaptAccountInfo(getAccountInfo());
            Order marketOrder = new MarketOrder(Order.OrderType.ASK, accountInfo.getBalance(CurrencyUnit.of("BTC")).getAmount(), "BTC", getCurrency());
            placeOrder(marketOrder, null);
          }
        }
        if (currentPrice.compareTo(trailingStopValue) > 0) {
          // check if price has risen and a alignment is required
          BigDecimal threshold = new BigDecimal(trailingStopThreadhold).divide(new BigDecimal(100));
          BigDecimal newTrailingStopValue = currentPrice.subtract(currentPrice.multiply(threshold));
          if (newTrailingStopValue.compareTo(currentPrice) < 0 && newTrailingStopValue.compareTo(trailingStopValue) > 0) {
            Log.d(TAG, "updating trailing stop value from " + trailingStopValue.toString() + " to " + newTrailingStopValue.toString());
            broadcastTrailingStopAlignmentEvent(trailingStopValue, newTrailingStopValue);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ExchangeService.this);
            prefs.edit().putString(Constants.PREFS_TRAILING_STOP_VALUE, newTrailingStopValue.toString()).apply();
          }
        }
      }
    }

    @Override
    protected void onPostExecute(Boolean success) {
      if (success) {
        // update widgets
        AppWidgetManager gm = AppWidgetManager.getInstance(ExchangeService.this);
        int[] ids = gm.getAppWidgetIds(new ComponentName(ExchangeService.this, AccountInfoWidgetProvider.class));
        AccountInfoWidgetProvider.updateWidgets(ExchangeService.this, gm, ids, ExchangeService.this);
        ids = gm.getAppWidgetIds(new ComponentName(ExchangeService.this, PriceInfoWidgetProvider.class));
        PriceInfoWidgetProvider.updateWidgets(ExchangeService.this, gm, ids, ExchangeService.this);

        if (notifyOnUpdate) {
          Toast.makeText(ExchangeService.this,
                  R.string.notify_update_success_text, Toast.LENGTH_LONG).show();
          NotificationCompat.Builder mBuilder =
                  new NotificationCompat.Builder(getApplicationContext())
                  .setSmallIcon(R.drawable.ic_action_bitcoin)
                  .setContentTitle(getApplicationContext().getString(R.string.notify_update_success_text))
                  .setContentText(getApplicationContext().getString(R.string.notify_update_success_text));
          Intent resultIntent = new Intent(getApplicationContext(), BitcoinTraderActivity.class);
          TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
          stackBuilder.addParentStack(BitcoinTraderActivity.class);
          stackBuilder.addNextIntent(resultIntent);
          PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
          mBuilder.setContentIntent(resultPendingIntent);
          mBuilder.setAutoCancel(true);
          NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
          mNotificationManager.notify(1234, mBuilder.build());
        }
      } else if (!success) {
        Toast.makeText(ExchangeService.this,
                R.string.notify_update_failed_title, Toast.LENGTH_LONG).show();
      }
    }
  };

  private class DeleteOrderTask extends ICSAsyncTask<Order, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Order... params) {
      Log.d(TAG, "Deleting order");
      try {
        if (params.length == 1) {
          boolean ret = exchange.getPollingTradeService().cancelOrder(params[0].getId());
          lastUpdate = new Date();
          broadcastUpdate();
          return ret;
        }
      } catch (ExchangeException ee) {
        Log.i(TAG, "ExchangeException", ee);
        broadcastUpdateFailure();
      } catch (HttpException uhe) {
        Log.e(TAG, "HttpException", uhe);
        broadcastUpdateFailure();
        return false;
      }
      return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
      Toast.makeText(ExchangeService.this, "Order deleted " + (success ? "successfully" : "unsuccessfully"), Toast.LENGTH_LONG).show();
    }
  };

  private class PlaceOrderTask extends ICSAsyncTask<Order, Void, Boolean> {

    private ProgressDialog mDialog;
    private FragmentActivity activity;
    private boolean demoMode = PreferenceManager.getDefaultSharedPreferences(getApplication()).getBoolean(Constants.PREFS_KEY_DEMO, false);

    public PlaceOrderTask(FragmentActivity activity) {
      super();
      this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
      if (activity != null) {
        mDialog = new ProgressDialog(activity);
        mDialog.setMessage(getString(R.string.place_order_submitting));
        mDialog.setCancelable(false);
        mDialog.setOwnerActivity(activity);
        mDialog.show();
      }
    }

    @Override
    protected void onPostExecute(Boolean success) {
      if (success) {
        Toast.makeText(ExchangeService.this,
                ExchangeService.this.getString(R.string.place_order_success), Toast.LENGTH_LONG).show();
        if (activity != null) {
          PlaceOrderFragment placeOrderFragment = (PlaceOrderFragment) activity.getSupportFragmentManager().findFragmentById(R.id.place_order_fragment);
          if (placeOrderFragment != null) {
            placeOrderFragment.resetValues();
          }
        }
      } else {
        Toast.makeText(ExchangeService.this, demoMode ? R.string.place_order_failed_demo : R.string.place_order_failed, Toast.LENGTH_LONG).show();
      }
      if (mDialog != null && mDialog.isShowing()) {
        try {
          mDialog.dismiss();
        } catch (IllegalArgumentException iae) {
          // not really nice, but works for this case. (#140)
          Log.w(TAG, iae);
        }
      }
    }

    @Override
    protected Boolean doInBackground(Order... params) {
      String orderId = null;
      if (!demoMode) {
        try {
          if (params.length == 1) {
            Order order = params[0];
            if (order instanceof MarketOrder) {
              MarketOrder mo = (MarketOrder) order;
              orderId = exchange.getPollingTradeService().placeMarketOrder(mo);
              List<MarketOrder> list = new ArrayList<MarketOrder>();
              list.add(mo);
              broadcastOrderExecuted(list);
            } else if (order instanceof LimitOrder) {
              LimitOrder lo = (LimitOrder) order;
              orderId = exchange.getPollingTradeService().placeLimitOrder(lo);
            }
            lastUpdate = new Date();
            broadcastUpdateSuccess();
          }
        } catch (ExchangeException ee) {
          Log.i(TAG, "ExchangeException", ee);
          broadcastUpdateFailure();
        } catch (HttpException uhe) {
          Log.e(TAG, "HttpException", uhe);
          broadcastUpdateFailure();
        }
      }
      // only finish activity if the order has been created in a PlaceOrderActivity
      if (activity != null && activity instanceof PlaceOrderActivity) {
        if (!TextUtils.isEmpty(orderId)) {
          activity.setResult(Activity.RESULT_OK);
        }
        activity.finish();
      }
      return !TextUtils.isEmpty(orderId);
    }
  };
}
