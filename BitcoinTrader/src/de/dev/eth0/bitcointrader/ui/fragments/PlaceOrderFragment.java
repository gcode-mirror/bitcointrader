//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.MarketOrder;
import com.xeiam.xchange.mtgox.v2.MtGoxAdapters;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxAccountInfo;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.PlaceOrderActivity;
import de.dev.eth0.bitcointrader.ui.views.CurrencyAmountView;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import de.schildbach.wallet.ui.HelpDialogFragment;
import java.math.BigDecimal;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

/**
 * @author Alexander Muthmann
 */
public class PlaceOrderFragment extends AbstractBitcoinTraderFragment {

  private static final String TAG = PlaceOrderFragment.class.getSimpleName();
  private AbstractBitcoinTraderActivity activity;
  private BitcoinTraderApplication application;
  private Spinner orderTypeSpinner;
  private CurrencyAmountView amountView;
  private EditText amountViewText;
  private CheckBox marketOrderCheckbox;
  private CurrencyAmountView priceView;
  private EditText priceViewText;
  private CurrencyTextView totalView;
  private CurrencyTextView estimatedFeeView;
  private Button viewGo;
  private Button viewCancel;
  private BroadcastReceiver broadcastReceiver;
  private LocalBroadcastManager broadcastManager;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    this.activity = (AbstractBitcoinTraderActivity)activity;
    application = (BitcoinTraderApplication)activity.getApplication();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    // only add menu if included in pricechartactivity
    if (activity instanceof PlaceOrderActivity) {
      inflater.inflate(R.menu.placeorder_options, menu);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.bitcointrader_options_help:
        HelpDialogFragment.page(activity.getSupportFragmentManager(), "help_place_order");
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onResume() {
    super.onResume();
    broadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.d(TAG, ".onReceive");
        updateView();
      }
    };
    broadcastManager = LocalBroadcastManager.getInstance(application);
    broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constants.UPDATE_SUCCEDED));
    updateView();
  }

  @Override
  public void onPause() {
    super.onPause();
    if (broadcastReceiver != null) {
      broadcastManager.unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    application.getExchangeService();
    View view = inflater.inflate(R.layout.place_order_fragment, container);
    totalView = (CurrencyTextView)view.findViewById(R.id.place_order_total);
    estimatedFeeView = (CurrencyTextView)view.findViewById(R.id.place_order_estimatedfee);
    amountView = (CurrencyAmountView)view.findViewById(R.id.place_order_amount);
    amountView.setCurrencyCode(Constants.CURRENCY_CODE_BITCOIN);
    amountViewText = (EditText)view.findViewById(R.id.place_order_amount_text);
    amountViewText.addTextChangedListener(valueChangedListener);

    priceView = (CurrencyAmountView)view.findViewById(R.id.place_order_price);
    priceView.setCurrencyCode(application.getCurrency());
    enablePriceViewContextButton();

    orderTypeSpinner = (Spinner)view.findViewById(R.id.place_order_type);
    ArrayAdapter<Order.OrderType> adapter = new ArrayAdapter<Order.OrderType>(activity,
            R.layout.spinner_item, Order.OrderType.values());
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    orderTypeSpinner.setAdapter(adapter);
    orderTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // if ask has been selected, we need a context button on amount to select all
        if (parent.getItemAtPosition(position).equals(Order.OrderType.ASK)) {
          enableAmountViewContextButton();
        } // remove context button from amount if buying bitcoins
        else if (parent.getItemAtPosition(position).equals(Order.OrderType.BID)) {
          amountView.removeContextButton();
        }
        updateView();
      }

      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    priceViewText = (EditText)view.findViewById(R.id.place_order_price_text);
    priceViewText.addTextChangedListener(valueChangedListener);

    marketOrderCheckbox = (CheckBox)view.findViewById(R.id.place_order_marketorder);
    marketOrderCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Ticker ticker = getExchangeService().getTicker();
        if (ticker != null) {
          priceView.setEnabled(!isChecked);
          Order.OrderType type = (Order.OrderType)orderTypeSpinner.getSelectedItem();
          priceViewText.setText(type.equals(Order.OrderType.ASK)
                  ? ticker.getAsk().getAmount().toString()
                  : ticker.getBid().getAmount().toString());
          priceViewText.clearFocus();
          updateView();
        }
      }
    });
    viewGo = (Button)view.findViewById(R.id.place_order_perform);
    viewGo.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        if (everythingValid()) {
          handleGo();
        }
        else {
          Toast.makeText(activity, R.string.place_order_invalid, Toast.LENGTH_LONG).show();
        }
      }
    });

    viewCancel = (Button)view.findViewById(R.id.place_order_cancel);
    viewCancel.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        // only finish activity if the order has been created in a PlaceOrderActivity
        if (activity instanceof PlaceOrderActivity) {
          activity.setResult(Activity.RESULT_CANCELED);
          activity.finish();
        }
        else {
          resetValues();
        }
      }
    });
    return view;
  }

  public void updateView() {
    Log.d(TAG, ".updateView()");
    Editable amount = amountViewText.getEditableText();
    Editable price = priceViewText.getEditableText();
    priceView.setCurrencyCode(application.getCurrency());
    Order.OrderType type = (Order.OrderType)orderTypeSpinner.getSelectedItem();
    if (!TextUtils.isEmpty(amount) && !TextUtils.isEmpty(price)) {
      BigMoney amountBTC = BigMoney.parse("BTC 0" + amount.toString());
      BigMoney amountUSD = BigMoney.parse(application.getCurrency() + " 0" + price.toString());
      BigMoney totalSpend = amountUSD.multipliedBy(amountBTC.getAmount());
      totalView.setAmount(totalSpend);
      if (getExchangeService() != null) {
        MtGoxAccountInfo accountInfo = getExchangeService().getAccountInfo();
        if (accountInfo != null) {
          if (type.equals(Order.OrderType.ASK)) {
            estimatedFeeView.setPrecision(Constants.PRECISION_CURRENCY);
            estimatedFeeView.setAmount(totalSpend.multipliedBy(accountInfo.getTradeFee().scaleByPowerOfTen(-2)));
          }
          else {
            BigMoney fee = amountBTC.multipliedBy(accountInfo.getTradeFee().scaleByPowerOfTen(-2));
            estimatedFeeView.setPrecision(Constants.PRECISION_BITCOIN);
            estimatedFeeView.setAmount(fee);
          }
        }
      }
    }
  }

  private void enableAmountViewContextButton() {
    amountView.setContextButton(R.drawable.ic_input_calculator, new OnClickListener() {
      public void onClick(View v) {
        MtGoxAccountInfo mtgoxaccountInfo = getExchangeService().getAccountInfo();
        if (mtgoxaccountInfo != null) {
          AccountInfo accountInfo = MtGoxAdapters.adaptAccountInfo(mtgoxaccountInfo);
          if (accountInfo != null) {
            amountView.setAmount(accountInfo.getBalance(CurrencyUnit.of("BTC")).getAmount());
          }
        }
      }
    });
  }

  private void enablePriceViewContextButton() {
    priceView.setContextButton(R.drawable.ic_input_calculator, new OnClickListener() {
      public void onClick(View v) {
        Ticker ticker = getExchangeService().getTicker();
        if (ticker != null && ticker.getLast().getAmount() != BigDecimal.ZERO) {
          priceView.setAmount(ticker.getLast().getAmount());
        }
      }
    });
  }

  private void handleGo() {
    Order order;
    Order.OrderType type = (Order.OrderType)orderTypeSpinner.getSelectedItem();
    boolean marketOrder = marketOrderCheckbox.isChecked();
    Double amount = Double.parseDouble(amountViewText.getEditableText().toString());
    Double price = Double.parseDouble(priceViewText.getEditableText().toString());

    if (marketOrder) {
      order = new MarketOrder(type, BigDecimal.valueOf(amount), "BTC", application.getCurrency());
    }
    else {
      order = new LimitOrder(type, BigDecimal.valueOf(amount), "BTC", application.getCurrency(), "", null, BigMoney.of(CurrencyUnit.of(application.getCurrency()), price));
    }
    getExchangeService().placeOrder(order, activity);

  }

  public void resetValues() {
    marketOrderCheckbox.setChecked(false);
    amountViewText.setText(null);
    priceViewText.setText(null);

  }

  private boolean everythingValid() {
    Editable amount = amountViewText.getEditableText();
    Editable price = priceViewText.getEditableText();
    boolean marketOrder = marketOrderCheckbox.isChecked();
    if (!TextUtils.isEmpty(amount) && (!TextUtils.isEmpty(price) || marketOrder)) {
      try {
        Double.parseDouble(amountViewText.getEditableText().toString());
        Double.parseDouble(priceViewText.getEditableText().toString());
      } catch (NumberFormatException nfe) {
        Log.d(TAG, "Could not parse input: amount: " + amount + ", price: " + price, nfe);
        return false;
      }
      return true;
    }
    return false;
  }

  public void update(Order.OrderType ordertype) {
    for (int i = 0; i < orderTypeSpinner.getCount(); i++) {
      if (orderTypeSpinner.getItemAtPosition(i).equals(ordertype)) {
        orderTypeSpinner.setSelection(i);
        switch (ordertype) {
          case ASK:
            enablePriceViewContextButton();
            return;
          case BID:
            amountView.removeContextButton();
            return;
        }
        return;
      }
    }
  }

  private class ValueChangedListener implements TextWatcher {

    @Override
    public void afterTextChanged(Editable s) {
      updateView();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
  }
  private ValueChangedListener valueChangedListener = new ValueChangedListener();
}
