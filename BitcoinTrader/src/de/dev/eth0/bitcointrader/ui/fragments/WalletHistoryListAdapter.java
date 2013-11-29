//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
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
public class WalletHistoryListAdapter extends AbstractExpandableListAdapter<MtGoxWalletHistoryEntry, MtGoxWalletHistoryEntry> {

  private static final int TYPE_WITH_PRICE = 0;
  private static final int TYPE_WITHOUT_PRICE = 1;

  public WalletHistoryListAdapter(AbstractBitcoinTraderActivity activity) {
    super(activity);
  }

  @Override
  public int getChildType(int groupPosition, int childPosition) {
    MtGoxWalletHistoryEntry entry = getChild(groupPosition, childPosition);
    if (entry.getType().equals("out")
            || entry.getType().equals("in")
            || entry.getType().equals("earned")
            || entry.getType().equals("spent")) {
      return TYPE_WITH_PRICE;
    }
    return TYPE_WITHOUT_PRICE;
  }

  @Override
  public int getChildTypeCount() {
    return 2;
  }

  @Override
  public int getGroupLayout() {
    return R.layout.wallet_history_fragment_list_group;
  }

  @Override
  public int getChildLayout() {
    return R.layout.wallet_history_fragment_list_item;
  }

  public void bindGroupView(View group, MtGoxWalletHistoryEntry entry) {
    TextView headerType = (TextView)group.findViewById(R.id.wallet_history_list_group_type);
    addTypeToView(headerType, entry);

    CurrencyTextView headerAmount = (CurrencyTextView)group.findViewById(R.id.wallet_history_list_group_amount);
    addAmountToView(headerAmount, entry);
  }

  @Override
  public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View childView, ViewGroup parent) {
    int type = getChildType(groupPosition, childPosition);
    ViewHolder viewHolder;
    if (childView == null) {
      switch (type) {
        case TYPE_WITH_PRICE:
          childView = inflater.inflate(R.layout.wallet_history_fragment_list_price_item, null);
          viewHolder = new ViewHolder();
          viewHolder.priceView = (TextView)childView.findViewById(R.id.wallet_history_row_price);
          break;
        case TYPE_WITHOUT_PRICE:
        default:
          childView = inflater.inflate(R.layout.wallet_history_fragment_list_item, null);
          viewHolder = new ViewHolder();
          break;
      }
      viewHolder.balanceView = (CurrencyTextView)childView.findViewById(R.id.wallet_history_row_balance);
      viewHolder.dateView = (TextView)childView.findViewById(R.id.wallet_history_row_date);
      viewHolder.infoView = (TextView)childView.findViewById(R.id.wallet_history_row_info);
      childView.setTag(viewHolder);
    }
    else {
      viewHolder = (ViewHolder)childView.getTag();
    }

    MtGoxWalletHistoryEntry entry = getChild(groupPosition, childPosition);
    if (type == TYPE_WITH_PRICE) {
      // price
      addPriceToView(viewHolder.priceView, entry);
    }
    // balance
    BigMoney balance = MoneyUtils.parse(entry.getBalance().getCurrency() + " " + entry.getBalance().getValue());
    viewHolder.balanceView.setPrecision(8);
    viewHolder.balanceView.setAmount(balance);

    // date
    viewHolder.dateView.setText(DateUtils.getRelativeDateTimeString(activity, Long.parseLong(entry.getDate()) * 1000, DateUtils.MINUTE_IN_MILLIS, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME));

    String[] substrings = entry.getInfo().split(" ");
    if (substrings.length >= 5) {
      if (entry.getInfo().contains("bought")) {
        viewHolder.infoView.setText(activity.getString(R.string.wallet_history_info_bought, substrings[3], substrings[5]));
      }
      else if (entry.getInfo().contains("sold")) {
        viewHolder.infoView.setText(activity.getString(R.string.wallet_history_info_sold, substrings[3], substrings[5]));
      }
      else {
        viewHolder.infoView.setText("");
      }
    }
    else {
      viewHolder.infoView.setText("");
    }
    return childView;
  }

  private void addPriceToView(TextView view, MtGoxWalletHistoryEntry entry) {
    String[] substrings = entry.getInfo().split(" ");
    view.setText(substrings.length >= 5 ? substrings[5] : "");
  }

  private void addAmountToView(CurrencyTextView view, MtGoxWalletHistoryEntry entry) {
    BigMoney amount = MoneyUtils.parse(entry.getValue().getCurrency() + " " + entry.getValue().getValue());
    view.setPrecision(8);
    view.setAmount(amount);
  }

  private void addTypeToView(TextView view, MtGoxWalletHistoryEntry entry) {
    if (entry.getType().equals("out")) {
      view.setText(R.string.wallet_history_out);
      view.setBackgroundColor(activity.getResources().getColor(R.color.history_out));
    }
    else if (entry.getType().equals("fee")) {
      view.setText(R.string.wallet_history_fee);
      view.setBackgroundColor(activity.getResources().getColor(R.color.history_fee));
    }
    else if (entry.getType().equals("in")) {
      view.setText(R.string.wallet_history_in);
      view.setBackgroundColor(activity.getResources().getColor(R.color.history_in));
    }
    else if (entry.getType().equals("spent")) {
      view.setText(R.string.wallet_history_spent);
      view.setBackgroundColor(activity.getResources().getColor(R.color.history_spend));
    }
    else if (entry.getType().equals("earned")) {
      view.setText(R.string.wallet_history_earned);
      view.setBackgroundColor(activity.getResources().getColor(R.color.history_earned));
    }
    else if (entry.getType().equals("withdraw")) {
      view.setText(R.string.wallet_history_withdraw);
      view.setBackgroundColor(activity.getResources().getColor(R.color.history_withdraw));
    }
    else if (entry.getType().equals("deposit")) {
      view.setText(R.string.wallet_history_deposit);
      view.setBackgroundColor(activity.getResources().getColor(R.color.history_deposit));
    }
    else {
      view.setText(entry.getType());
    }
  }

  @Override
  public void bindChildView(View child, MtGoxWalletHistoryEntry entry) {
    // not required
  }

  private static class ViewHolder {

    protected CurrencyTextView balanceView;
    protected TextView dateView;
    protected TextView priceView;
    protected TextView infoView;

  }
}
