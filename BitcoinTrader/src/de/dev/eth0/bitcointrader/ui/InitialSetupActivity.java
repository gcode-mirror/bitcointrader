//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeException;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.mtgox.v2.MtGoxExchange;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.util.ICSAsyncTask;
import de.schildbach.wallet.ui.HelpDialogFragment;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

/**
 * @author Alexander Muthmann
 */
public class InitialSetupActivity extends AbstractBitcoinTraderActivity {

  private static final String TAG = InitialSetupActivity.class.getSimpleName();
  private TextView infoTextView;
  private ImageButton startScanButton;
  private Button demoButton;
  private EditText manualSetupKey;
  private EditText manualSetupSecretKey;
  private Button manualSetupButton;
  private ProgressDialog dialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.initial_setup_activity);

    infoTextView = (TextView)findViewById(R.id.initial_setup_activity_info);
    infoTextView.setMovementMethod(LinkMovementMethod.getInstance());

    startScanButton = (ImageButton)findViewById(R.id.initial_setup_activity_start_scan);
    startScanButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        IntentIntegrator integrator = new IntentIntegrator(InitialSetupActivity.this);
        integrator.setTargetApplications(IntentIntegrator.TARGET_BARCODE_SCANNER_ONLY);
        integrator.initiateScan();
      }
    });
    manualSetupKey = (EditText)findViewById(R.id.initial_setup_activity_manual_key);
    manualSetupKey.clearFocus();
    manualSetupSecretKey = (EditText)findViewById(R.id.initial_setup_activity_manual_secretkey);
    manualSetupButton = (Button)findViewById(R.id.initial_setup_activity_manual_setupbutton);
    manualSetupButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        String key = manualSetupKey.getText().toString();
        String secretKey = manualSetupSecretKey.getText().toString();
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(secretKey)) {
          testAndSaveAccessKeys(key, secretKey);
        }
      }
    });
    demoButton = (Button)findViewById(R.id.initial_setup_activity_demo_button);
    demoButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        // start demo
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBitcoinTraderApplication());
        Editor editor = prefs.edit();
        editor.putBoolean(Constants.PREFS_KEY_DEMO, true);
        editor.apply();
        Toast.makeText(InitialSetupActivity.this, R.string.initial_setup_demo, Toast.LENGTH_LONG).show();
        InitialSetupActivity.this.finish();
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    this.finish();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getSupportMenuInflater().inflate(R.menu.initialsetup_options, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.bitcointrader_options_help:
        HelpDialogFragment.page(getSupportFragmentManager(), "help_setup");
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    if (scanResult != null) {
      GetAPIKeyTask gakt = new GetAPIKeyTask();
      gakt.executeOnExecutor(ICSAsyncTask.SERIAL_EXECUTOR, scanResult.getContents());
    }
    super.onActivityResult(requestCode, resultCode, intent);
  }

  private void testAndSaveAccessKeys(String key, String secretKey) {
    // Test connection
    TestConnectionTask tct = new TestConnectionTask();
    tct.executeOnExecutor(ICSAsyncTask.SERIAL_EXECUTOR, key, secretKey);
  }

  private class GetAPIKeyTask extends ICSAsyncTask<String, Void, Exception> {

    private class ExpiredException extends Exception {
    }

    private class InvalidException extends Exception {
    }

    private class NotEnoughPermissionsException extends Exception {
    }
    private String key;
    private String secretKey;

    @Override
    protected Exception doInBackground(String... params) {
      try {
        getApiKeys(params[0]);
      }
      catch (Exception e) {
        return e;
      }
      return null;
    }

    @Override
    protected void onPreExecute() {
      dialog = new ProgressDialog(InitialSetupActivity.this);
      dialog.setMessage(getString(R.string.initial_setup_testing));
      dialog.setCancelable(false);
      dialog.setOwnerActivity(InitialSetupActivity.this);
      dialog.show();
    }

    @Override
    protected void onPostExecute(Exception exception) {
      if (dialog.isShowing()) {
        dialog.dismiss();
      }
      if (exception == null) {
        testAndSaveAccessKeys(key, secretKey);
      }
      else {
        if (exception instanceof ExpiredException) {
          Toast.makeText(InitialSetupActivity.this, R.string.initial_setup_failed_key_expired, Toast.LENGTH_LONG).show();
        }
        if (exception instanceof InvalidException) {
          Toast.makeText(InitialSetupActivity.this, R.string.initial_setup_failed_key_invalid, Toast.LENGTH_LONG).show();
        }
        if (exception instanceof NotEnoughPermissionsException) {
          Toast.makeText(InitialSetupActivity.this, R.string.initial_setup_failed_not_enough_permissions, Toast.LENGTH_LONG).show();
        }
        else if (exception instanceof Exception) {
          Toast.makeText(InitialSetupActivity.this, R.string.connection_failed, Toast.LENGTH_LONG).show();
        }
      }
    }

    private void getApiKeys(String scannedKey)
            throws UnsupportedEncodingException, IOException, ExpiredException, InvalidException, NotEnoughPermissionsException {
      String urlEncodedKey = URLEncoder.encode(scannedKey, "UTF-8");
      URL query = new URL(Constants.APP_ACTIVATION_URL);
      HttpsURLConnection c = (HttpsURLConnection)query.openConnection();
      c.setRequestMethod("POST");
      c.setRequestProperty("User-Agent", "biTrader");

      c.setDoOutput(true);
      c.setDoInput(true);

      DataOutputStream output = new DataOutputStream(c.getOutputStream());
      String postData = String.format("key=%s&name=biTrader&app=%s",
              urlEncodedKey, Constants.APP_ACTIVATION_ID);
      output.writeBytes(postData);
      output.close();

      DataInputStream input;
      if (c.getResponseCode() >= 400) {
        input = new DataInputStream(c.getErrorStream());
      }
      else {
        input = new DataInputStream(c.getInputStream());
      }

      // Find out charset, default to ISO-8859-1 if unknown
      String charset = "ISO-8859-1";
      String contentType = c.getContentType();
      if (contentType != null) {
        int pos = contentType.indexOf("charset=");
        if (pos != -1) {
          charset = contentType.substring(pos + "charset=".length());
        }
      }

      BufferedReader r = new BufferedReader(new InputStreamReader(input, charset));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = r.readLine()) != null) {
        sb.append(line);
        sb.append("\n");
      }

      r.close();
      input.close();
      String response = sb.toString();

      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> rawJSON = mapper.readValue(response, new TypeReference<Map<String, Object>>() {
      });

      String result = (String)rawJSON.get("result");
      if ("error".equals(result)) {
        String error = (String)rawJSON.get("error");

        if ("Expired key".equals(error)) {
          throw new ExpiredException();
        }
        else if ("Invalid or already used key".equals(error)) {
          throw new InvalidException();
        }
        throw new IOException();
      }

      Map<String, Object> returnObject = (Map<String, Object>)rawJSON.get("return");
      Map<String, Object> rightsObject = (Map<String, Object>)returnObject.get("Rights");
      if (rightsObject == null) {
        throw new NotEnoughPermissionsException();
      }

      String getInfo = (String)rightsObject.get("get_info");
      String trade = (String)rightsObject.get("trade");
      if (getInfo == null || trade == null) {
        throw new NotEnoughPermissionsException();
      }
      String apiKey = (String)returnObject.get("Rest-Key");
      String apiSecret = (String)returnObject.get("Secret");

      if (apiKey == null || apiSecret == null) {
        throw new InvalidException();
      }
      key = apiKey;
      secretKey = apiSecret;
    }
  }

  private class TestConnectionTask extends ICSAsyncTask<String, Void, Exception> {

    private String key;
    private String secretKey;
    private AccountInfo accountInfo;
    private ProgressDialog dialog;

    @Override
    protected void onPreExecute() {
      dialog = new ProgressDialog(InitialSetupActivity.this);
      dialog.setMessage(getString(R.string.initial_setup_testing));
      dialog.setCancelable(false);
      dialog.setOwnerActivity(InitialSetupActivity.this);
      dialog.show();
    }

    @Override
    public Exception doInBackground(String... params) {
      Log.d(TAG, "testing connection...");
      try {
        key = params[0];
        secretKey = params[1];
        ExchangeSpecification exchangeSpec = new ExchangeSpecification(MtGoxExchange.class);
        exchangeSpec.setApiKey(key);
        exchangeSpec.setSecretKey(secretKey);
        exchangeSpec.setSslUri(Constants.MTGOX_SSL_URI);
        exchangeSpec.setPlainTextUriStreaming(Constants.MTGOX_PLAIN_WEBSOCKET_URI);
        exchangeSpec.setSslUriStreaming(Constants.MTGOX_SSL_WEBSOCKET_URI);
        Exchange exchange = (MtGoxExchange)ExchangeFactory.INSTANCE.createExchange(exchangeSpec);
        accountInfo = exchange.getPollingAccountService().getAccountInfo();
      }
      catch (Exception e) {
        Log.i(TAG, "Exception", e);
        return e;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Exception exception) {
      if (dialog.isShowing()) {
        dialog.dismiss();
      }
      if (exception == null) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBitcoinTraderApplication());
        Editor editor = prefs.edit();
        editor.putString(Constants.PREFS_KEY_MTGOX_APIKEY, key);
        editor.putString(Constants.PREFS_KEY_MTGOX_SECRETKEY, secretKey);
        editor.apply();
        Toast.makeText(InitialSetupActivity.this,
                InitialSetupActivity.this.getString(R.string.initial_setup_success, accountInfo.getUsername()), Toast.LENGTH_LONG).show();
        InitialSetupActivity.this.finish();
      }
      else {
        if (exception instanceof IOException) {
          Toast.makeText(InitialSetupActivity.this, R.string.connection_failed, Toast.LENGTH_LONG).show();
        }
        else if (exception instanceof ExchangeException) {
          Toast.makeText(InitialSetupActivity.this, R.string.initial_setup_failed_wrong_credentials, Toast.LENGTH_LONG).show();
        }
      }
    }
  };
}
