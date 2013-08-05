//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.trade.LimitOrder;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.util.ICSAsyncTask;
import de.schildbach.wallet.ui.HelpDialogFragment;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author Alexander Muthmann
 */
public class MarketDepthFragment extends AbstractBitcoinTraderFragment {
  
  private static final String TAG = MarketDepthFragment.class.getSimpleName();
  private BitcoinTraderApplication application;
  private AbstractBitcoinTraderActivity activity;
  private ProgressDialog mDialog;
  private LineGraphView graphView;
  private GraphViewSeries asksSeries;
  private GraphViewSeries bidsSeries;
  
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
      case R.id.bitcointrader_options_help:
        HelpDialogFragment.page(activity.getSupportFragmentManager(), "help_market_depth");
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.market_depth_fragment, container);
    return view;
  }
  
  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    graphView = new LineGraphView(activity, "");
    graphView.setGraphViewStyle(new GraphViewStyle(Color.BLACK, Color.BLACK, Color.BLACK));
    graphView.setHorizontalLabelsOffset(true);
    LinearLayout layout = (LinearLayout) view.findViewById(R.id.market_depth_graph);
    layout.addView(graphView);
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
    inflater.inflate(R.menu.marketdepth_options, menu);
  }
  
  protected void updateView() {
    Log.d(TAG, ".updateView()");
    OrderBook ob = application.getCache().getEntry(OrderBook.class);
    if (ob == null) {
      MarketDepthFragment.GetOrderBookTask tradesTask = new MarketDepthFragment.GetOrderBookTask();
      tradesTask.executeOnExecutor(ICSAsyncTask.SERIAL_EXECUTOR);
    } else {
      updateView(ob);
    }
  }
  
  protected void updateView(OrderBook orderBook) {
    Log.d(TAG, ".updateView");
    if (orderBook != null) {
      List<LimitOrder> asks = orderBook.getAsks();
      List<LimitOrder> bids = orderBook.getBids();
      GraphViewData[] asksData = buildGraphViewData(asks, true);
      if (asksSeries == null) {
        asksSeries = new GraphViewSeries(getString(R.string.market_depth_asks_graph_title), new GraphViewSeries.GraphViewSeriesStyle(Color.rgb(90, 250, 00), 3), asksData);
      } else {
        asksSeries.resetData(asksData);
      }
      graphView.addSeries(asksSeries);
      
      GraphViewData[] bidsData = buildGraphViewData(bids, false);
      if (bidsSeries == null) {
        bidsSeries = new GraphViewSeries(getString(R.string.market_depth_bids_graph_title), new GraphViewSeries.GraphViewSeriesStyle(Color.rgb(200, 50, 00), 3), bidsData);
      } else {
        bidsSeries.resetData(bidsData);
      }
      graphView.addSeries(bidsSeries);
      graphView.setViewPort(bidsData[0].valueX, (asksData[asksData.length-1].valueX)-bidsData[0].valueX);
      graphView.setScalable(true);
      graphView.setScrollable(true);
      graphView.redrawAll();
    }
  }
  
  private GraphViewData[] buildGraphViewData(List<LimitOrder> orders, boolean assending) {
    Map<Double, Double> data = assending ? new TreeMap<Double, Double>() : new LinkedHashMap<Double, Double>();
    Log.d(TAG, "orders.size() == " + orders.size());
    for (int i = 0; i < orders.size(); i++) {
      LimitOrder cd = orders.get(i);
      double xValue = Math.round((cd.getLimitPrice().getAmount().doubleValue()) * 100.0) / 100.0;
      if (!data.containsKey(xValue)) {
        data.put(xValue, cd.getTradableAmount().doubleValue());
      } else {
        data.put(xValue, data.get(xValue) + cd.getTradableAmount().doubleValue());
      }
    }
    GraphViewData[] ret = new GraphViewData[data.size()];
    if (assending) {
      int i = 0;
      double value = 0;
      for (Entry<Double, Double> entry : data.entrySet()) {
        value += entry.getValue();
        GraphViewData gvd = new GraphViewData(entry.getKey(), value);
        ret[i++] = gvd;
        Log.d(TAG, gvd.valueX + "/" + gvd.valueY);
      }
    } else {
      int i = ret.length;
      double value = 0;
      for (Entry<Double, Double> entry : data.entrySet()) {
        value += entry.getValue();
        GraphViewData gvd = new GraphViewData(entry.getKey(), value);
        ret[--i] = gvd;
        Log.d(TAG, gvd.valueX + "/" + gvd.valueY);
      }
    }
    return ret;
  }
  
  private class GetOrderBookTask extends ICSAsyncTask<Void, Void, OrderBook> {
    
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
    protected void onPostExecute(OrderBook orderBook) {
      if (mDialog != null && mDialog.isShowing()) {
        mDialog.dismiss();
        mDialog = null;
      }
      application.getCache().put(orderBook);
      updateView(orderBook);
    }
    
    @Override
    protected OrderBook doInBackground(Void... params) {
      try {
        Log.d(TAG, "Loading orderbook");
        return getExchangeService().getOrderBook();
      } catch (Exception e) {
        activity.sendBroadcast(new Intent(Constants.UPDATE_FAILED));
        Log.e(TAG, "Exception", e);
      }
      return null;
    }
  };
}
