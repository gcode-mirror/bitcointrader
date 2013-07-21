//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.util.CrashReporter;
import de.schildbach.wallet.ui.ReportIssueDialogBuilder;
import java.io.IOException;

/**
 * @author Alexander Muthmann
 * @author Andreas Schildbach
 */
public class StartScreenActivity extends AbstractBitcoinTraderActivity {

  private static final String TAG = StartScreenActivity.class.getSimpleName();
  private ProgressDialog mDialog;
  private BroadcastReceiver broadcastReceiver;
  private LocalBroadcastManager broadcastManager;
  private AlertDialog alertDialog;
  private boolean hadErrors = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.start_activity);
    hadErrors = false;
    if (savedInstanceState == null) {
      checkAlerts();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    // Only do something, if there has been no error before. otherwise wait till crashreport has been finished
    if (!hadErrors) {
      // if there has been no initial setup (or no mtgox keys are set, we need to start the initalSetupActivity
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBitcoinTraderApplication());
      if (!prefs.getBoolean(Constants.PREFS_KEY_DEMO, false) && (TextUtils.isEmpty(prefs.getString(Constants.PREFS_KEY_MTGOX_APIKEY, null))
              || TextUtils.isEmpty(prefs.getString(Constants.PREFS_KEY_MTGOX_SECRETKEY, null)))) {
        startActivity(new Intent(this, InitialSetupActivity.class));
      } else {
        // otherwise we can connect the exchangeservice and start
        connect();
        broadcastReceiver = new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            Log.d(TAG, ".onReceive (" + intent.getAction() + ")");
            if (mDialog.isShowing()) {
              mDialog.dismiss();
            }
            if (intent.getAction().equals(Constants.UPDATE_SUCCEDED)) {
              startActivity(new Intent(StartScreenActivity.this, BitcoinTraderActivity.class));
              StartScreenActivity.this.finish();
            } else if (intent.getAction().equals(Constants.UPDATE_FAILED)) {
              getBitcoinTraderApplication().stopExchangeService();
              // dont create dialog multiple times
              if (alertDialog == null || !alertDialog.isShowing()) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(StartScreenActivity.this);
                alertDialogBuilder.setMessage(R.string.connection_failed);
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton(R.string.button_retry, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                    connect();
                  }
                });
                alertDialogBuilder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                    StartScreenActivity.this.finish();
                  }
                });
                alertDialog = alertDialogBuilder.create();
                alertDialog.show();
              }
            }
          }
        };
        broadcastManager = LocalBroadcastManager.getInstance(getBitcoinTraderApplication());
        broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constants.UPDATE_SUCCEDED));
        broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constants.UPDATE_FAILED));
      }
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (broadcastReceiver != null) {
      broadcastManager.unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    }
  }

  private void connect() {
    mDialog = new ProgressDialog(this);
    mDialog.setMessage(getString(R.string.connecting_info));
    mDialog.setCancelable(false);
    mDialog.show();
    getBitcoinTraderApplication().startExchangeService();
  }

  private void checkAlerts() {
    if (CrashReporter.hasSavedCrashTrace()) {
      final StringBuilder stackTrace = new StringBuilder();
      final StringBuilder applicationLog = new StringBuilder();

      try {
        hadErrors = true;
        CrashReporter.appendSavedCrashTrace(stackTrace);
        CrashReporter.appendSavedCrashApplicationLog(applicationLog);
      } catch (final IOException x) {
        Log.w(TAG, "IO Exception", x);
      }

      final ReportIssueDialogBuilder dialog = new ReportIssueDialogBuilder(this, R.string.report_issue_dialog_title_crash,
              R.string.report_issue_dialog_message_crash) {
        @Override
        protected void onReportFinished() {
          hadErrors = false;
          onResume();
        }

        @Override
        protected CharSequence subject() {
          try {
            if (CrashReporter.hasReason()) {
              return CrashReporter.getReason();
            }
          } catch (IOException ioe) {
            Log.w(TAG, "Exception", ioe);
          }
          return Constants.REPORT_SUBJECT_CRASH + " " + getBitcoinTraderApplication().applicationVersionName();
        }

        @Override
        protected CharSequence collectApplicationInfo() throws IOException {
          final StringBuilder applicationInfo = new StringBuilder();
          CrashReporter.appendApplicationInfo(applicationInfo, getBitcoinTraderApplication());
          return applicationInfo;
        }

        @Override
        protected CharSequence collectStackTrace() throws IOException {
          if (stackTrace.length() > 0) {
            return stackTrace;
          } else {
            return null;
          }
        }

        @Override
        protected CharSequence collectDeviceInfo() throws IOException {
          final StringBuilder deviceInfo = new StringBuilder();
          CrashReporter.appendDeviceInfo(deviceInfo, StartScreenActivity.this);
          return deviceInfo;
        }

        @Override
        protected CharSequence collectApplicationLog() throws IOException {
          if (applicationLog.length() > 0) {
            return applicationLog;
          } else {
            return null;
          }
        }
      };

      dialog.show();
    }
  }
}
