//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;
import com.xeiam.xchange.bitcoincharts.BitcoinChartsAdapters;
import com.xeiam.xchange.bitcoincharts.BitcoinChartsFactory;
import com.xeiam.xchange.bitcoincharts.dto.charts.ChartData;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.PriceChartDetailActivity;
import de.dev.eth0.bitcointrader.util.ICSAsyncTask;
import de.schildbach.wallet.ui.HelpDialogFragment;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Muthmann
 */
public class PriceChartDetailFragment extends AbstractBitcoinTraderFragment {

  private static final String TAG = PriceChartDetailFragment.class.getSimpleName();
  private BitcoinTraderApplication application;
  private AbstractBitcoinTraderActivity activity;
  private ProgressDialog mDialog;
  private Spinner selectPeriodSpinner;
  private GraphView graphView;
  private TextView titleView;
  private GraphViewSeries graphViewSeries;
  private String exchange;
  private int selectedPeriod = 1;
  private final Map<String, SparseArray<ChartData[]>> cache = new HashMap<String, SparseArray<ChartData[]>>();

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = (AbstractBitcoinTraderActivity) activity;
    this.application = (BitcoinTraderApplication) activity.getApplication();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    setHasOptionsMenu(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.bitcointrader_options_refresh:
        cache.clear();
        updateView();
        break;
      case R.id.bitcointrader_options_help:
        HelpDialogFragment.page(activity.getSupportFragmentManager(), "help_price_chart_detail");
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.price_chart_detail_fragment, container);
    return view;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    titleView = (TextView) view.findViewById(R.id.price_chart_detail_title);
    graphView = new LineGraphView(activity, "");
    graphView.setScrollable(true);
    //graphView.setScalable(true);
    graphView.setGraphViewStyle(new GraphViewStyle(Color.BLACK, Color.BLACK, Color.BLACK));
    if (selectedPeriod > 2) {
      SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
      df.applyPattern(df.toPattern().replaceAll("y", ""));
      graphView.setFormatter(df, true);
    } else {
      graphView.setFormatter(DateFormat.getTimeInstance(DateFormat.SHORT), true);
    }
    graphView.setHorizontalLabelsOffset(true);
    LinearLayout layout = (LinearLayout) view.findViewById(R.id.price_chart_detail_graph);
    layout.addView(graphView);

    selectPeriodSpinner = (Spinner) view.findViewById(R.id.price_chart_detail_period_spinner);
    selectPeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedPeriod = getResources().getIntArray(R.array.priceChartDetailPeriodValue)[position];
        switchPeriod();
      }

      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

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
    updateView();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    // only add menu if included in pricechartactivity
    if (activity instanceof PriceChartDetailActivity) {
      inflater.inflate(R.menu.pricechart_options, menu);
    }
  }

  protected void switchPeriod() {
    if (cache.containsKey(exchange)) {
      SparseArray<ChartData[]> entry = cache.get(exchange);
      ChartData[] cd = entry.get(selectedPeriod);
      if (cd != null) {
        updateView(cd);
        return;
      }
    }
    if (selectedPeriod > 2) {
      SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
      df.applyPattern(df.toPattern().replaceAll("y", ""));
      graphView.setFormatter(df, true);
    } else {
      graphView.setFormatter(DateFormat.getTimeInstance(DateFormat.SHORT), true);
    }
    updateView();
  }

  protected void updateView() {
    Log.d(TAG, ".updateView()");
    PriceChartDetailFragment.GetChartDataTask tradesTask = new PriceChartDetailFragment.GetChartDataTask();
    tradesTask.executeOnExecutor(ICSAsyncTask.SERIAL_EXECUTOR);
  }

  protected void updateView(ChartData[] chartData) {
    Log.d(TAG, ".updateView");
    if (chartData != null) {
      GraphViewData[] data = new GraphViewData[chartData.length];
      double min = Double.MAX_VALUE;
      double max = Double.MIN_VALUE;
      for (int i = 0; i < chartData.length; i++) {
        ChartData cd = chartData[i];
        Log.d(TAG, "new entry: " + cd.toString());
        data[i] = new GraphViewData(Double.parseDouble(cd.getDate()) * 1000, cd.getWeightedPrice().doubleValue());
        min = Math.min(data[i].valueY, min);
        max = Math.max(data[i].valueY, max);
        Log.d(TAG, "entry: " + data[i].valueX + "/" + data[i].valueY);
      }
      if (graphViewSeries == null) {
        graphViewSeries = new GraphViewSeries(data);
      } else {
        graphViewSeries.resetData(data);
      }
      SparseArray<ChartData[]> entry;
      if (cache.containsKey(exchange)) {
        entry = cache.get(exchange);
      } else {
        entry = new SparseArray<ChartData[]>();
        cache.put(exchange, entry);
      }
      entry.put(selectedPeriod, chartData);
      graphView.addSeries(graphViewSeries);
      graphView.setManualYAxisBounds(max * 1.025, min * 0.975);
      graphView.redrawAll();
    }
  }

  public void update(String exchange) {
    this.exchange = exchange;
    this.titleView.setText(exchange);
    updateView();




  }

  private class GetChartDataTask extends ICSAsyncTask<Void, Void, ChartData[]> {

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
    protected void onPostExecute(ChartData[] chartData) {
      if (mDialog != null && mDialog.isShowing()) {
        mDialog.dismiss();
        mDialog = null;
      }
      Log.d(TAG, "Found " + (chartData != null ? chartData.length : 0) + " ticker entries");
      updateView(chartData);
    }

    @Override
    protected ChartData[] doInBackground(Void... params) {
      try {
        Log.d(TAG, "Loading chartdate for the last " + selectedPeriod + " days");
        ChartData[] chartdata = BitcoinChartsAdapters.adaptChartData(BitcoinChartsFactory.createInstance().getChartData(exchange, selectedPeriod));
        return chartdata == null ? new ChartData[0] : chartdata;
      } catch (Exception e) {
        activity.sendBroadcast(new Intent(Constants.UPDATE_FAILED));
        Log.e(TAG, "Exception", e);
      }
      return null;
    }
  };
}
