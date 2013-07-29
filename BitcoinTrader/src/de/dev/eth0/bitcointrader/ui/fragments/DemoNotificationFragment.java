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
import android.widget.TextView;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.ui.StartScreenActivity;

/**
 * @author Alexander Muthmann
 */
public class DemoNotificationFragment extends Fragment {

  private Activity activity;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = activity;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.demo_notification_fragment, container);

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
