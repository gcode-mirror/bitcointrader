//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.xeiam.xchange.bitcoincharts.BitcoinChartsFactory;
import com.xeiam.xchange.bitcoincharts.dto.marketdata.BitcoinChartsTicker;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.PriceChartActivity;
import de.dev.eth0.bitcointrader.ui.PriceChartDetailActivity;
import de.dev.eth0.bitcointrader.ui.views.AmountTextView;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import de.dev.eth0.bitcointrader.util.ICSAsyncTask;
import de.dev.eth0.bitcointrader.util.FormatHelper.DISPLAY_MODE;
import de.schildbach.wallet.ui.HelpDialogFragment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.joda.money.BigMoney;

/**
 * @author Alexander Muthmann
 */
public class PriceChartFragment extends SherlockListFragment {

  private static final String TAG = PriceChartFragment.class.getSimpleName();
  private BitcoinTraderApplication application;
  private AbstractBitcoinTraderActivity activity;
  private PriceChartListAdapter adapter;
  private ProgressDialog mDialog;
  private View infoToastLayout;
  private TextView symbolView;
  private CurrencyTextView lastView;
  private CurrencyTextView avgView;
  private AmountTextView volView;
  private CurrencyTextView lowView;
  private CurrencyTextView highView;
  private CurrencyTextView bidView;
  private CurrencyTextView askView;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = (AbstractBitcoinTraderActivity)activity;
    this.application = (BitcoinTraderApplication)activity.getApplication();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    adapter = new PriceChartListAdapter(activity);
    setListAdapter(adapter);
    setHasOptionsMenu(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.bitcointrader_options_refresh:
        updateView(true);
        break;
      case R.id.bitcointrader_options_help:
        HelpDialogFragment.page(activity.getSupportFragmentManager(), "help_price_chart");
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.price_chart_fragment, container);
    return view;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    infoToastLayout = activity.getLayoutInflater().inflate(R.layout.price_chart_row_info_toast, (ViewGroup)getView().findViewById(R.id.chart_row_info_toast));
    symbolView = (TextView)infoToastLayout.findViewById(R.id.chart_row_info_toast_symbol);
    lastView = (CurrencyTextView)infoToastLayout.findViewById(R.id.chart_row_info_toast_last);
    avgView = (CurrencyTextView)infoToastLayout.findViewById(R.id.chart_row_info_toast_avg);
    volView = (AmountTextView)infoToastLayout.findViewById(R.id.chart_row_info_toast_vol);
    lowView = (CurrencyTextView)infoToastLayout.findViewById(R.id.chart_row_info_toast_low);
    highView = (CurrencyTextView)infoToastLayout.findViewById(R.id.chart_row_info_toast_high);
    bidView = (CurrencyTextView)infoToastLayout.findViewById(R.id.chart_row_info_toast_bid);
    askView = (CurrencyTextView)infoToastLayout.findViewById(R.id.chart_row_info_toast_ask);

    lastView.setDisplayMode(DISPLAY_MODE.NO_CURRENCY_CODE);
    lastView.setPrecision(Constants.PRECISION_CURRENCY);
    avgView.setDisplayMode(DISPLAY_MODE.NO_CURRENCY_CODE);
    avgView.setPrecision(Constants.PRECISION_CURRENCY);
    lowView.setDisplayMode(DISPLAY_MODE.NO_CURRENCY_CODE);
    lowView.setPrecision(Constants.PRECISION_CURRENCY);
    highView.setDisplayMode(DISPLAY_MODE.NO_CURRENCY_CODE);
    highView.setPrecision(Constants.PRECISION_CURRENCY);
    bidView.setDisplayMode(DISPLAY_MODE.NO_CURRENCY_CODE);
    bidView.setPrecision(Constants.PRECISION_CURRENCY);
    askView.setDisplayMode(DISPLAY_MODE.NO_CURRENCY_CODE);
    askView.setPrecision(Constants.PRECISION_CURRENCY);
    volView.setPrecision(Constants.PRECISION_BITCOIN);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    // only add menu if included in pricechartactivity
    if (activity instanceof PriceChartActivity) {
      inflater.inflate(R.menu.pricechart_options, menu);
    }
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    BitcoinChartsTicker entry = adapter.getItem(position);
    if (entry != null) {
      symbolView.setText(entry.getSymbol());
      lastView.setAmount(BigMoney.parse("BTC " + entry.getClose()));
      avgView.setAmount(BigMoney.parse("BTC " + entry.getAvg()));
      lowView.setAmount(BigMoney.parse("BTC " + entry.getLow()));
      highView.setAmount(BigMoney.parse("BTC " + entry.getHigh()));
      bidView.setAmount(BigMoney.parse("BTC " + entry.getBid()));
      askView.setAmount(BigMoney.parse("BTC " + entry.getAsk()));
      volView.setAmount(entry.getVolume());

      Toast toast = new Toast(getActivity());
      toast.setDuration(Toast.LENGTH_LONG);
      toast.setView(infoToastLayout);
      toast.show();

      Object o = activity.getSupportFragmentManager().findFragmentById(R.id.price_chart_detail_fragment);
      if (o != null) {
        PriceChartDetailFragment pcdf = (PriceChartDetailFragment)o;
        pcdf.update(entry.getSymbol());
      }
      else {
        Intent detailsActivity = new Intent(activity, PriceChartDetailActivity.class);
        detailsActivity.putExtra(PriceChartDetailActivity.INTENT_EXTRA_EXCHANGE, entry.getSymbol());
        startActivity(detailsActivity);
      }
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (mDialog != null && mDialog.isShowing()) {
      mDialog.dismiss();
    }
    mDialog = null;
  }

  @Override
  public void onResume() {
    super.onResume();
    updateView(false);
  }

  protected void updateView(boolean forceUpdate) {
    Log.d(TAG, ".updateView()");
    if (!forceUpdate) {
      BitcoinChartsTicker[] ticker = application.getCache().getEntry(BitcoinChartsTicker[].class);
      if (ticker != null) {
        updateView(ticker);
        return;
      }
    }
    GetTickerTask tradesTask = new GetTickerTask();
    tradesTask.executeOnExecutor(ICSAsyncTask.SERIAL_EXECUTOR);
  }

  protected void updateView(BitcoinChartsTicker[] tradesList) {
    Log.d(TAG, ".updateView");
    if (tradesList != null) {
      List<BitcoinChartsTicker> tickers = new ArrayList<BitcoinChartsTicker>();
      for (BitcoinChartsTicker data : tradesList) {
        if (data.getVolume().intValue() != 0) {
          tickers.add(data);
        }
      }
      adapter.replace(tickers);
    }
  }

  private class GetTickerTask extends ICSAsyncTask<Void, Void, BitcoinChartsTicker[]> {

    @Override
    protected void onPreExecute() {
      if (mDialog == null) {
        mDialog = new ProgressDialog(activity);
        mDialog.setMessage(getString(R.string.loading_info));
        mDialog.setCancelable(false);
        mDialog.setOwnerActivity(activity);
        mDialog.show();
      }
    }

    @Override
    protected void onPostExecute(BitcoinChartsTicker[] ticker) {
      if (mDialog != null && mDialog.isShowing()) {
        mDialog.dismiss();
        mDialog = null;
      }
      Log.d(TAG, "Found " + (ticker != null ? ticker.length : 0) + " ticker entries");
      // Sort Tickers by volume
      Arrays.sort(ticker, new Comparator<BitcoinChartsTicker>() {
        @Override
        public int compare(BitcoinChartsTicker entry1,
                BitcoinChartsTicker entry2) {
          return entry2.getVolume().compareTo(entry1.getVolume());
        }
      });
      application.getCache().put(ticker);
      updateView(ticker);
    }

    @Override
    protected BitcoinChartsTicker[] doInBackground(Void... params) {
      try {
        BitcoinChartsTicker[] ticker = BitcoinChartsFactory.createInstance().getMarketData();
        return ticker == null ? new BitcoinChartsTicker[0] : ticker;
      }
      catch (Exception e) {
        activity.sendBroadcast(new Intent(Constants.UPDATE_FAILED));
        Log.e(TAG, "Exception", e);
      }
      return null;
    }
  };
}
