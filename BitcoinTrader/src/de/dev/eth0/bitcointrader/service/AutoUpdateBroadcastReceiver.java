//$URL$
//$Id$
package de.dev.eth0.bitcointrader.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Broadcastreceiver to convert a global broadcast to a localbroadcast
 *
 * @author Alexander Muthmann
 */
public class AutoUpdateBroadcastReceiver extends BroadcastReceiver {

  private static final String TAG = AutoUpdateBroadcastReceiver.class.getSimpleName();

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(TAG, ".onReceive");
    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
    lbm.sendBroadcast(intent);
  }
}
