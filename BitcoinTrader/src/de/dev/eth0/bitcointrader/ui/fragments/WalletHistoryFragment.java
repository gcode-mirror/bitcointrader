//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.xeiam.xchange.currency.MoneyUtils;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWallet;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWalletHistory;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWalletHistoryEntry;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.service.ExchangeService;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import de.dev.eth0.bitcointrader.util.ICSAsyncTask;
import de.schildbach.wallet.ui.HelpDialogFragment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.money.BigMoney;

/**
 * @author Alexander Muthmann
 */
public class WalletHistoryFragment extends SherlockListFragment {

  private static final String TAG = WalletHistoryFragment.class.getSimpleName();
  private BitcoinTraderApplication application;
  private AbstractBitcoinTraderActivity activity;
  private WalletHistoryListAdapter adapter;
  private ProgressDialog mDialog;
  private Spinner historyCurrencySpinner;
  private View infoToastLayout;
  private TextView typeView;
  private TextView infoView;
  private CurrencyTextView amountView;
  private CurrencyTextView balanceView;
  private TextView dateView;
  private String mDialogLoadingString;
  private Toast mInfoToast;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
    View layout = super.onCreateView(inflater, container,
            savedInstanceState);
    ListView lv = (ListView) layout.findViewById(android.R.id.list);
    ViewGroup parent = (ViewGroup) lv.getParent();

    // Remove ListView and add CustomView  in its place
    int lvIndex = parent.indexOfChild(lv);
    parent.removeViewAt(lvIndex);
    View view = inflater.inflate(
            R.layout.wallet_history_fragment, container, false);
    parent.addView(view, lvIndex, lv.getLayoutParams());

    historyCurrencySpinner = (Spinner) view.findViewById(R.id.wallet_history_currency_spinner);
    ExchangeService exchangeService = application.getExchangeService();
    Set<String> currencies = new HashSet<String>();
    if (exchangeService != null && exchangeService.getAccountInfo() != null) {
      for (MtGoxWallet wallet : exchangeService.getAccountInfo().getWallets().getMtGoxWallets()) {
        if (wallet != null && wallet.getBalance() != null && !TextUtils.isEmpty(wallet.getBalance().getCurrency())) {
          currencies.add(wallet.getBalance().getCurrency());
        }
      }
    }
    HistoryCurrencySpinnerAdapter spinneradapter = new HistoryCurrencySpinnerAdapter(activity,
            R.layout.spinner_item, currencies.toArray(new String[0]));
    spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    historyCurrencySpinner.setAdapter(spinneradapter);
    historyCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, ".onItemSelected");
        updateView(false);
      }

      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    return layout;
  }

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);
    this.activity = (AbstractBitcoinTraderActivity) activity;
    this.application = (BitcoinTraderApplication) activity.getApplication();
    this.mDialogLoadingString = activity.getString(R.string.loading_info);
  }

  @Override
  public void onPause() {
    super.onPause();
    if (mDialog != null && mDialog.isShowing()) {
      mDialog.dismiss();
    }
    mDialog = null;
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    adapter = new WalletHistoryListAdapter(activity);
    setListAdapter(adapter);
    setHasOptionsMenu(true);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    int text = R.string.wallet_history_empty_text;
    SpannableStringBuilder emptyText = new SpannableStringBuilder(
            getString(text));
    emptyText.setSpan(new StyleSpan(Typeface.BOLD), 0, emptyText.length(), SpannableStringBuilder.SPAN_POINT_MARK);
    setEmptyText(emptyText);
    infoToastLayout = activity.getLayoutInflater().inflate(R.layout.wallet_history_row_info_toast, (ViewGroup) getView().findViewById(R.id.history_row_info_toast));
    typeView = (TextView) infoToastLayout.findViewById(R.id.history_row_info_toast_type);
    infoView = (TextView) infoToastLayout.findViewById(R.id.history_row_info_toast_info);
    amountView = (CurrencyTextView) infoToastLayout.findViewById(R.id.history_row_info_toast_amount);
    balanceView = (CurrencyTextView) infoToastLayout.findViewById(R.id.history_row_info_toast_balance);
    dateView = (TextView) infoToastLayout.findViewById(R.id.history_row_info_toast_date);
    amountView.setPrecision(8);
    balanceView.setPrecision(8);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    MtGoxWalletHistoryEntry entry = adapter.getItem(position);
    if (entry != null) {
      if (mInfoToast != null) {
        mInfoToast.cancel();
      }
      if (entry.getType().equals("out")) {
        typeView.setText(R.string.wallet_history_out);
      } else if (entry.getType().equals("fee")) {
        typeView.setText(R.string.wallet_history_fee);
      } else if (entry.getType().equals("in")) {
        typeView.setText(R.string.wallet_history_in);
      } else if (entry.getType().equals("spent")) {
        typeView.setText(R.string.wallet_history_spent);
      } else if (entry.getType().equals("earned")) {
        typeView.setText(R.string.wallet_history_earned);
      } else if (entry.getType().equals("withdraw")) {
        typeView.setText(R.string.wallet_history_withdraw);
      } else if (entry.getType().equals("deposit")) {
        typeView.setText(R.string.wallet_history_deposit);
      }
      infoView.setText(entry.getInfo());
      if (entry.getInfo().contains("bought")) {
        String[] substrings = entry.getInfo().split(" ");
        if (substrings.length >= 5) {
          infoView.setText(getResources().getString(R.string.wallet_history_info_bought, substrings[3], substrings[5]));
        }
      } else if (entry.getInfo().contains("sold")) {
        String[] substrings = entry.getInfo().split(" ");
        if (substrings.length >= 5) {
          infoView.setText(getResources().getString(R.string.wallet_history_info_sold, substrings[3], substrings[5]));
        }
      }
      BigMoney amount = MoneyUtils.parse(entry.getValue().getCurrency() + " " + entry.getValue().getValue());
      amountView.setAmount(amount);
      BigMoney balance = MoneyUtils.parse(entry.getBalance().getCurrency() + " " + entry.getBalance().getValue());
      balanceView.setAmount(balance);
      dateView.setText(entry.getDate());

      mInfoToast = new Toast(getActivity());
      mInfoToast.setDuration(Toast.LENGTH_SHORT);
      mInfoToast.setView(infoToastLayout);
      mInfoToast.show();
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.bitcointrader_options_refresh:
        updateView(true);
        break;
      case R.id.bitcointrader_options_help:
        HelpDialogFragment.page(activity.getSupportFragmentManager(), "help_wallet_history");
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.wallethistory_options, menu);
  }

  @Override
  public void onResume() {
    super.onResume();
    updateView(true);
  }

  protected void updateView(boolean forceUpdate) {
    GetMtGoxWalletHistoryTask walletTask = new GetMtGoxWalletHistoryTask();
    walletTask.executeOnExecutor(ICSAsyncTask.SERIAL_EXECUTOR, forceUpdate);
  }

  protected void updateView(List<MtGoxWalletHistory> history) {
    Log.d(TAG, ".updateView");
    List<MtGoxWalletHistoryEntry> entries = new ArrayList<MtGoxWalletHistoryEntry>();
    if (history != null) {
      for (MtGoxWalletHistory historyPage : history) {
        entries.addAll(Arrays.asList(historyPage.getMtGoxWalletHistoryEntries()));
      }
    }
    Collections.sort(entries, new Comparator<MtGoxWalletHistoryEntry>() {
      public int compare(MtGoxWalletHistoryEntry lhs, MtGoxWalletHistoryEntry rhs) {
        return Long.valueOf(rhs.getDate()).compareTo(Long.valueOf(lhs.getDate()));
      }
    });

    adapter.replace(entries);
  }

  private class GetMtGoxWalletHistoryTask extends ICSAsyncTask<Boolean, Void, List<MtGoxWalletHistory>> {

    @Override
    protected void onPreExecute() {
      if (mDialog == null) {
        mDialog = new ProgressDialog(activity);
        mDialog.setMessage(mDialogLoadingString);
        mDialog.setCancelable(false);
        mDialog.setOwnerActivity(activity);
        mDialog.show();
      }
    }

    @Override
    protected void onPostExecute(List<MtGoxWalletHistory> history) {
      if (mDialog != null && mDialog.isShowing()) {
        mDialog.dismiss();
        mDialog = null;
      }
      updateView(history);
    }

    @Override
    protected List<MtGoxWalletHistory> doInBackground(Boolean... params) {

      ExchangeService exchangeService = application.getExchangeService();
      String currency = (String) historyCurrencySpinner.getSelectedItem();

      if (exchangeService != null) {
        HistoryCurrencySpinnerAdapter adapter = (HistoryCurrencySpinnerAdapter) historyCurrencySpinner.getAdapter();
        Map<String, List<MtGoxWalletHistory>> histories = exchangeService.getMtGoxWalletHistory(adapter.getEntries(), params[0]);
        return histories.get(currency);
      }
      return null;
    }
  };

  public static class HistoryCurrencySpinnerAdapter extends ArrayAdapter<String> {

    private String[] entries;

    public HistoryCurrencySpinnerAdapter(Context context, int textViewResourceId, String[] objects) {
      super(context, textViewResourceId, objects);
      entries = objects;
    }

    public String[] getEntries() {
      return entries;
    }
  }
}
