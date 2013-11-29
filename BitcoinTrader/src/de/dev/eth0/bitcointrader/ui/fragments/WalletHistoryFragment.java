//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWallet;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWalletHistory;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWalletHistoryEntry;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.service.ExchangeService;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.util.ICSAsyncTask;
import de.schildbach.wallet.ui.HelpDialogFragment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Alexander Muthmann
 */
public class WalletHistoryFragment extends AbstractBitcoinTraderFragment {

  private static final String TAG = WalletHistoryFragment.class.getSimpleName();
  private BitcoinTraderApplication application;
  private AbstractBitcoinTraderActivity activity;
  private WalletHistoryListAdapter adapter;
  private ProgressDialog mDialog;
  private Spinner historyCurrencySpinner;
  private ExpandableListView expandableList;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
    View view = inflater.inflate(
            R.layout.wallet_history_fragment, container, false);
    expandableList = (ExpandableListView)view.findViewById(R.id.wallet_history_expandable_list);
    expandableList.setAdapter(adapter);
    historyCurrencySpinner = (Spinner) view.findViewById(R.id.wallet_history_currency_spinner);
    ExchangeService exchangeService = application.getExchangeService();
    Set<String> currencies = new LinkedHashSet<String>();
    int idxCurrentCurrency = Integer.MIN_VALUE;
    int counter = 0;
    if (exchangeService != null && exchangeService.getAccountInfo() != null) {
      for (MtGoxWallet wallet : exchangeService.getAccountInfo().getWallets().getMtGoxWallets()) {
        if (wallet != null && wallet.getBalance() != null && !TextUtils.isEmpty(wallet.getBalance().getCurrency())) {
          if (exchangeService.getCurrency().equalsIgnoreCase(wallet.getBalance().getCurrency())) {
            idxCurrentCurrency = counter;
          }
          currencies.add(wallet.getBalance().getCurrency());
          counter++;
        }
      }
    }
    HistoryCurrencySpinnerAdapter spinneradapter = new HistoryCurrencySpinnerAdapter(activity,
            R.layout.spinner_item, currencies.toArray(new String[currencies.size()]));

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
    if (idxCurrentCurrency != Integer.MIN_VALUE) {
      historyCurrencySpinner.setSelection(idxCurrentCurrency);
    }
    return view;
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
    setHasOptionsMenu(true);
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

    Map<MtGoxWalletHistoryEntry, List<MtGoxWalletHistoryEntry>> foo = new LinkedHashMap<MtGoxWalletHistoryEntry, List<MtGoxWalletHistoryEntry>>();
    for (MtGoxWalletHistoryEntry mgwhe : entries) {
      foo.put(mgwhe, Arrays.asList(mgwhe));
    }
    adapter.replace(foo);
  }

  private class GetMtGoxWalletHistoryTask extends ICSAsyncTask<Boolean, Void, List<MtGoxWalletHistory>> {

    @Override
    protected void onPreExecute() {
      if (mDialog == null) {
        mDialog = new ProgressDialog(activity);
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
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
        Map<String, List<MtGoxWalletHistory>> histories = exchangeService.getMtGoxWalletHistory(adapter.getEntries(), params[0], mDialog);
        return histories.get(currency);
      }
      return null;
    }
  };

  public static class HistoryCurrencySpinnerAdapter extends ArrayAdapter<String> {

    private final String[] entries;

    public HistoryCurrencySpinnerAdapter(Context context, int textViewResourceId, String[] objects) {
      super(context, textViewResourceId, objects);
      entries = objects;
    }

    public String[] getEntries() {
      return entries;
    }
  }
}
