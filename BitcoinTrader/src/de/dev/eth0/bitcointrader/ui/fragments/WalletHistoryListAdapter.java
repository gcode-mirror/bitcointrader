//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;
import com.xeiam.xchange.currency.MoneyUtils;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWalletHistoryEntry;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import org.joda.money.BigMoney;

/**
 * @author Alexander Muthmann
 */
public class WalletHistoryListAdapter extends AbstractListAdapter<MtGoxWalletHistoryEntry> {

  public WalletHistoryListAdapter(AbstractBitcoinTraderActivity activity) {
    super(activity);
  }

  @Override
  public int getRowLayout() {
    return R.layout.wallet_history_row_extended;
  }
  
  public void bindView(View row, MtGoxWalletHistoryEntry entry) {
    // type (out, fee, earned)
    TextView rowType = (TextView) row.findViewById(R.id.wallet_history_row_type);
    if (entry.getType().equals("out")) {
      rowType.setText(R.string.wallet_history_out);
      rowType.setBackgroundColor(activity.getResources().getColor(R.color.history_out));
    } else if (entry.getType().equals("fee")) {
      rowType.setText(R.string.wallet_history_fee);
      rowType.setBackgroundColor(activity.getResources().getColor(R.color.history_fee));
    } else if (entry.getType().equals("in")) {
      rowType.setText(R.string.wallet_history_in);
      rowType.setBackgroundColor(activity.getResources().getColor(R.color.history_in));
    } else if (entry.getType().equals("spent")) {
      rowType.setText(R.string.wallet_history_spent);
      rowType.setBackgroundColor(activity.getResources().getColor(R.color.history_spend));
    } else if (entry.getType().equals("earned")) {
      rowType.setText(R.string.wallet_history_earned);
      rowType.setBackgroundColor(activity.getResources().getColor(R.color.history_earned));
    } else if (entry.getType().equals("withdraw")) {
      rowType.setText(R.string.wallet_history_withdraw);
      rowType.setBackgroundColor(activity.getResources().getColor(R.color.history_withdraw));
    } else if (entry.getType().equals("deposit")) {
      rowType.setText(R.string.wallet_history_deposit);
      rowType.setBackgroundColor(activity.getResources().getColor(R.color.history_deposit));
    } else {
      rowType.setText(entry.getType());
    }

    // date
    TextView rowDate = (TextView) row.findViewById(R.id.wallet_history_row_date);
    rowDate.setText(DateUtils.getRelativeDateTimeString(activity, Long.parseLong(entry.getDate()) * 1000, DateUtils.MINUTE_IN_MILLIS, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME));

    // amount
    CurrencyTextView rowAmount = (CurrencyTextView) row.findViewById(R.id.wallet_history_row_amount);
    BigMoney amount = MoneyUtils.parse(entry.getValue().getCurrency() + " " + entry.getValue().getValue());
    rowAmount.setPrecision(8);
    rowAmount.setAmount(amount);
    // balance
    CurrencyTextView rowBalance = (CurrencyTextView) row.findViewById(R.id.wallet_history_row_balance);
    BigMoney balance = MoneyUtils.parse(entry.getBalance().getCurrency() + " " + entry.getBalance().getValue());
    rowBalance.setPrecision(8);
    rowBalance.setAmount(balance);

  }

}
