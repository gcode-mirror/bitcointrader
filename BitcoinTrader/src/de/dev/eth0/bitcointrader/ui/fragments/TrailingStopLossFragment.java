//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.mtgox.v2.MtGoxAdapters;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxAccountInfo;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.views.CurrencyAmountView;
import de.schildbach.wallet.ui.HelpDialogFragment;
import org.joda.money.BigMoney;

/**
 *
 * @author Alexander Muthmann
 */
public class TrailingStopLossFragment extends AbstractBitcoinTraderFragment {

  private final static String TAG = TrailingStopLossFragment.class.getSimpleName();
  private EditText percentageTextView;
  private EditText priceTextView;
  private EditText updatesTextView;
  private BitcoinTraderApplication application;
  private AbstractBitcoinTraderActivity activity;
  private Button viewGo;
  private Button viewCancel;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = (AbstractBitcoinTraderActivity)activity;
    this.application = (BitcoinTraderApplication)activity.getApplication();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    setHasOptionsMenu(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.trailingstoploss_options, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.bitcointrader_options_help:
        HelpDialogFragment.page(activity.getSupportFragmentManager(), "help_trailing_stop_loss");
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.trailing_stop_loss_fragment, container);
    return view;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {

    percentageTextView = (EditText)view.findViewById(R.id.trailing_stop_dialog_percentage_text);
    priceTextView = (EditText)view.findViewById(R.id.trailing_stop_dialog_price_text);
    updatesTextView = (EditText)view.findViewById(R.id.trailing_stop_dialog_updates_text);

    CurrencyAmountView priceView = (CurrencyAmountView)view.findViewById(R.id.trailing_stop_dialog_price);
    if (getExchangeService() != null && !TextUtils.isEmpty(getExchangeService().getCurrency())) {
      priceView.setCurrencyCode(getExchangeService().getCurrency());
    }
    CurrencyAmountView percentageView = (CurrencyAmountView)view.findViewById(R.id.trailing_stop_dialog_percentage);
    percentageView.setCurrencyCode("%");
    viewGo = (Button)view.findViewById(R.id.trailing_stop_loss_perform);
    viewGo.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        try {
          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
          Float threashold = Float.parseFloat(percentageTextView.getEditableText().toString());
          String price = priceTextView.getEditableText().toString();
          String updates = updatesTextView.getEditableText().toString();
          int numberUpdates = 1;
          try {
            numberUpdates = Integer.parseInt(updates);
          }
          catch (NumberFormatException nfe) {
          }
          if (!TextUtils.isEmpty(price)) {
            String currency = "USD";
            if (getExchangeService() != null && !TextUtils.isEmpty(getExchangeService().getCurrency())) {
              currency = getExchangeService().getCurrency();
            }
            BigMoney priceCurrency = BigMoney.parse(currency + " 0" + price.toString());

            // check if the entered price is higher than the current price (this would trigger a sell):
            MtGoxAccountInfo mtgoxaccountInfo = getExchangeService().getAccountInfo();
            if (mtgoxaccountInfo != null) {
              AccountInfo accountInfo = MtGoxAdapters.adaptAccountInfo(mtgoxaccountInfo);
              if (accountInfo != null && priceCurrency.isGreaterThan(accountInfo.getBalance(priceCurrency.getCurrencyUnit()))) {
                throw new Exception();
              }
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat(Constants.PREFS_TRAILING_STOP_THREASHOLD, threashold);
            editor.putString(Constants.PREFS_TRAILING_STOP_VALUE, priceCurrency.getAmount().toString());
            editor.putInt(Constants.PREFS_TRAILING_STOP_NUMBER_UPDATES, numberUpdates);
            editor.apply();
            Toast.makeText(getActivity(), R.string.trailing_stop_loss_submitted, Toast.LENGTH_LONG).show();
            activity.setResult(Activity.RESULT_OK);
            activity.finish();
          }
        }
        catch (Exception ex) {
          Log.w(TAG, ex);
          Toast.makeText(getActivity(), R.string.trailing_stop_loss_wrong_input, Toast.LENGTH_LONG).show();
        }
      }
    });

    viewCancel = (Button)view.findViewById(R.id.trailing_stop_loss_cancel);
    viewCancel.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        activity.setResult(Activity.RESULT_CANCELED);
        activity.finish();
      }
    });

  }
}
