//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.xeiam.xchange.dto.Order;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.ui.PlaceOrderActivity;
import de.dev.eth0.bitcointrader.ui.StartScreenActivity;

/**
 * @author Alexander Muthmann
 */
public class BitcoinTraderActionsFragment extends Fragment {

  private Activity activity;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    this.activity = activity;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.bitcointrader_actions_fragment, container);

    Button buyButton = (Button) view.findViewById(R.id.bitcointrader_actions_buy);
    buyButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Intent i = new Intent(activity, PlaceOrderActivity.class);
        i.putExtra(PlaceOrderActivity.INTENT_EXTRA_TYPE, Order.OrderType.BID.name());
        startActivity(i);
      }
    });

    Button sellButton = (Button) view.findViewById(R.id.bitcointrader_actions_sell);
    sellButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Intent i = new Intent(activity, PlaceOrderActivity.class);
        i.putExtra(PlaceOrderActivity.INTENT_EXTRA_TYPE, Order.OrderType.ASK.name());
        startActivity(i);
      }
    });

    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    TextView demoInfoView = (TextView) view.findViewById(R.id.bitcointrader_demo_info);
    demoInfoView.setText(R.string.bitcoin_action_demo_info);
    demoInfoView.setVisibility(prefs.getBoolean(Constants.PREFS_KEY_DEMO, false) ? View.VISIBLE : View.GONE);

    demoInfoView.setOnClickListener(new OnClickListener() {
      public void onClick(final View v) {
        Editor editor = prefs.edit();
        editor.putString(Constants.PREFS_KEY_MTGOX_APIKEY, null);
        editor.putString(Constants.PREFS_KEY_MTGOX_SECRETKEY, null);
        editor.putBoolean(Constants.PREFS_KEY_DEMO, false);
        editor.commit();
        startActivity(new Intent(activity, StartScreenActivity.class));
        activity.finish();
      }
    });
    return view;
  }
}
