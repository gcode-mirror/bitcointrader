//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.PriceChartDetailActivity;
import de.schildbach.wallet.ui.HelpDialogFragment;

/**
 * @author Alexander Muthmann
 */
public class PriceChartDetailFragment extends AbstractBitcoinTraderFragment {

  private static final String TAG = PriceChartDetailFragment.class.getSimpleName();
  private BitcoinTraderApplication application;
  private AbstractBitcoinTraderActivity activity;
  private WebView webview;

  private String exchange;

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
        webview.reload();
        return true;
      case R.id.price_chart_detail_option_new_window:
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Constants.BITCOINCHARTS_URL, exchange))));
        return true;
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
  @SuppressLint("SetJavaScriptEnabled")
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    webview = (WebView) view.findViewById(R.id.price_chart_detail_graph_webview);
    webview.getSettings().setSupportZoom(true);
    webview.getSettings().setBuiltInZoomControls(true);
    webview.getSettings().setJavaScriptEnabled(true);
    webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
    webview.setWebChromeClient(new WebChromeClient() {
      @Override
      public void onProgressChanged(WebView view, int progress) {
        // Activities and WebViews measure progress with different scales.
        // The progress meter will automatically disappear when we reach 100%
        activity.setProgress(progress * 1000);
      }
    });
    webview.setWebViewClient(new WebViewClient() {
      @Override
      public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        Log.w(TAG, description);
      }
    });
  }

  @Override
  public void onPause() {
    super.onPause();
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
      inflater.inflate(R.menu.pricechartdetail_options, menu);
    }
  }

  protected void updateView() {
    Log.d(TAG, ".updateView()");
    webview.loadUrl(String.format(Constants.BITCOINCHARTS_URL, exchange));
  }

  public void update(String exchange) {
    this.exchange = exchange;
    updateView();
  }
}
