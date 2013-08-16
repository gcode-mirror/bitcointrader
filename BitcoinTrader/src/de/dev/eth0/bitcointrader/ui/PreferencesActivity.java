//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.R;
/**
 * @author Alexander Muthmann
 */
public class PreferencesActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {
  private static final String KEY_DELETE_ACCOUNT = "delete_account";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preferences);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
    sp.registerOnSharedPreferenceChangeListener(this);

    // if api key has not been set yet, add the summary, otherwise the current value
    String mtgoxApiKey = sp.getString(Constants.PREFS_KEY_MTGOX_APIKEY, null);
    if (TextUtils.isEmpty(mtgoxApiKey)) {
      findPreference(Constants.PREFS_KEY_MTGOX_APIKEY).setSummary(R.string.preferences_mtgox_api_key_summary);
    }
    else {
      findPreference(Constants.PREFS_KEY_MTGOX_APIKEY).setSummary(mtgoxApiKey);
    }

    ListPreference listPreference = (ListPreference)findPreference(Constants.PREFS_KEY_GENERAL_UPDATE);
    if (listPreference.getValue() == null) {
      listPreference.setValueIndex(0);
    }
    listPreference.setSummary(listPreference.getEntry());
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    // Update summary for api key changed
    if (TextUtils.equals(key, Constants.PREFS_KEY_MTGOX_APIKEY)) {
      Preference pref = findPreference(key);
      EditTextPreference etp = (EditTextPreference)pref;
      if (TextUtils.isEmpty(etp.getText())) {
        pref.setSummary(R.string.preferences_mtgox_api_key_summary);
      }
      else {
        pref.setSummary(etp.getText());
      }
    }
    if (TextUtils.equals(key, Constants.PREFS_KEY_GENERAL_UPDATE)) {
      Preference pref = findPreference(key);
      ListPreference lp = (ListPreference)pref;
      pref.setSummary(lp.getEntry());
    }
  }

  @Override
  public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
    String key = preference.getKey();
    if (KEY_DELETE_ACCOUNT.equals(key)) {
      Editor editor = preference.getEditor();
      editor.putString(Constants.PREFS_KEY_MTGOX_APIKEY, null);
      editor.putString(Constants.PREFS_KEY_MTGOX_SECRETKEY, null);
      editor.putBoolean(Constants.PREFS_KEY_DEMO, false);
      editor.commit();
      Toast.makeText(this, R.string.preferences_delete_account_title, Toast.LENGTH_LONG).show();
      startActivity(new Intent(this, StartScreenActivity.class));
      finish();
    }
    return false;
  }
}
