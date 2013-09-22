//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.TrailingStopLossActivity;
import de.dev.eth0.bitcointrader.util.FormatHelper;
import org.joda.money.BigMoney;

/**
 * @author Alexander Muthmann
 */
public class TrailingStopLossActionsFragment extends AbstractBitcoinTraderFragment {

  private static final String TAG = TrailingStopLossActionsFragment.class.getSimpleName();
  private AbstractBitcoinTraderActivity activity;
  private BitcoinTraderApplication application;
  private Button activateStopLossButton;
  private BroadcastReceiver broadcastReceiver;
  private LocalBroadcastManager broadcastManager;

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
    broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constants.TRAILING_LOSS_ALIGNMENT_EVENT));
    broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constants.TRAILING_LOSS_EVENT));
    broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constants.CURRENCY_CHANGE_EVENT));
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
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = (AbstractBitcoinTraderActivity)getActivity();
    this.application = this.activity.getBitcoinTraderApplication();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.trailing_stop_actions_fragment, container);
    activateStopLossButton = (Button)view.findViewById(R.id.trailing_stop_actions_stop_loss_button);
    updateView();
    return view;
  }

  private void updateView() {
    activateStopLossButton.setClickable(false);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    Float threashold = prefs.getFloat(Constants.PREFS_TRAILING_STOP_THREASHOLD, Float.MIN_VALUE);
    String value = prefs.getString(Constants.PREFS_TRAILING_STOP_VALUE, "");
    if (threashold == Float.MIN_VALUE) {
      activateStopLossButton.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          activity.startActivity(new Intent(activity, TrailingStopLossActivity.class));
        }
      });
      activateStopLossButton.setText(R.string.trailing_stop_activate_stop_loss);
    }
    else {
      if (getExchangeService() != null && !TextUtils.isEmpty(getExchangeService().getCurrency())) {
        BigMoney valueBM = BigMoney.parse(getExchangeService().getCurrency() + " " + value);
        activateStopLossButton.setText(getString(R.string.trailing_stop_cancel_stop_loss, threashold, FormatHelper.formatBigMoney(FormatHelper.DISPLAY_MODE.CURRENCY_CODE, valueBM)) + "%)");
        activateStopLossButton.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            prefs.edit().remove(Constants.PREFS_TRAILING_STOP_THREASHOLD).apply();
            updateView();
          }
        });
      }
      else {
        activateStopLossButton.setClickable(false);
      }
    }
  }
}
