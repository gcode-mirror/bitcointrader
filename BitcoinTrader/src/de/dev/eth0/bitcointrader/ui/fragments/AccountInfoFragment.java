//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.mtgox.v2.MtGoxAdapters;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxAccountInfo;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.ui.WalletHistoryActivity;
import de.dev.eth0.bitcointrader.ui.views.AmountTextView;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import org.joda.money.CurrencyUnit;
/**
 * @author Alexander Muthmann
 */
public class AccountInfoFragment extends AbstractBitcoinTraderFragment {

  private static final String TAG = AccountInfoFragment.class.getSimpleName();
  private BitcoinTraderApplication application;
  private TextView viewName;
  private CurrencyTextView viewDollar;
  private CurrencyTextView viewBtc;
  private AmountTextView viewTradeFee;
  private BroadcastReceiver broadcastReceiver;
  private LocalBroadcastManager broadcastManager;

  @Override
  public void onStart() {
    super.onStart();
    updateView();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.application = (BitcoinTraderApplication) activity.getApplication();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view =  inflater.inflate(R.layout.account_info_fragment, container, false);
    view.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        startActivity(new Intent(getActivity(), WalletHistoryActivity.class));
      }
    });
    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    broadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.d(TAG, ".onReceive");
        updateView();
      }
    };
    broadcastManager = LocalBroadcastManager.getInstance(application);
    broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(Constants.UPDATE_SUCCEDED));
    updateView();
  }

  @Override
  public void onPause() {
    super.onPause();
    if (broadcastReceiver != null) {
      broadcastManager.unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    }
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewName = (TextView) view.findViewById(R.id.your_wallet_name);
    viewDollar = (CurrencyTextView) view.findViewById(R.id.your_wallet_dollar);
    viewDollar.setPrecision(Constants.PRECISION_CURRENCY);
    viewBtc = (CurrencyTextView) view.findViewById(R.id.your_wallet_btc);
    viewBtc.setPrecision(Constants.PRECISION_BITCOIN);
    viewTradeFee = (AmountTextView) view.findViewById(R.id.your_wallet_tradefee);
    viewTradeFee.setPostfix("%");
    viewTradeFee.setPrefix(getActivity().getString(R.string.account_info_trade_fee_label));
  }

  public void updateView() {
    Log.d(TAG, ".updateView");
    if (getExchangeService() != null) {
      MtGoxAccountInfo mtgoxaccountInfo = getExchangeService().getAccountInfo();
      if (mtgoxaccountInfo != null) {
        AccountInfo accountInfo = MtGoxAdapters.adaptAccountInfo(mtgoxaccountInfo);
        viewName.setText(accountInfo.getUsername());
        viewDollar.setAmount(accountInfo.getBalance(CurrencyUnit.of(application.getCurrency())));
        viewBtc.setAmount(accountInfo.getBalance(CurrencyUnit.of("BTC")));
        viewTradeFee.setAmount(mtgoxaccountInfo.getTradeFee());
      }
    }
  }
}
