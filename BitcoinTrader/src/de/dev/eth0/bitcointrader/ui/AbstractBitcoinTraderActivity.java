//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui;

import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.service.ExchangeService;
import de.dev.eth0.bitcointrader.util.CrashReporter;
import de.schildbach.wallet.ui.ReportIssueDialogBuilder;
import java.io.IOException;

/**
 * @author Alexander Muthmann
 */
public abstract class AbstractBitcoinTraderActivity extends SherlockFragmentActivity {

  private static final String TAG = AbstractBitcoinTraderActivity.class.getSimpleName();
  private BitcoinTraderApplication application;
  protected boolean hadErrors = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    application = (BitcoinTraderApplication) getApplication();
    super.onCreate(savedInstanceState);
    hadErrors = false;
    if (savedInstanceState == null) {
      checkAlerts();
    }
  }

  public BitcoinTraderApplication getBitcoinTraderApplication() {
    return application;
  }

  protected ExchangeService getExchangeService() {
    return application.getExchangeService();
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
          CrashReporter.appendDeviceInfo(deviceInfo, AbstractBitcoinTraderActivity.this);
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
