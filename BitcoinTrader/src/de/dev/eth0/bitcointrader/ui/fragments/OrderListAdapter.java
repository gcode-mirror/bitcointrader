//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.AlertDialog;

import android.content.DialogInterface;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.Order.OrderType;
import com.xeiam.xchange.dto.trade.LimitOrder;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.views.AmountTextView;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;

/**
 * @author Alexander Muthmann
 */
public class OrderListAdapter extends AbstractListAdapter<Order> {

  public OrderListAdapter(AbstractBitcoinTraderActivity activity) {
    super(activity);
  }

  @Override
  public void bindView(View row, final Order order) {
    // ask or bid
    TextView rowAskBid = (TextView) row.findViewById(R.id.order_row_askbid);
    OrderType theOrderType = order.getType();
    switch (theOrderType){
    case ASK:
        rowAskBid.setText(R.string.capital_ask);
        break;
    case BID:
        rowAskBid.setText(R.string.capital_bid);
        break;
    }

    // date
    TextView rowDate = (TextView) row.findViewById(R.id.order_row_date);
    rowDate.setText(DateUtils.getRelativeDateTimeString(activity, order.getTimestamp().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME));

    // amount
    AmountTextView rowAmount = (AmountTextView) row.findViewById(R.id.order_row_amount);
    rowAmount.setAmount(order.getTradableAmount());
    rowAmount.setPrecision(Constants.PRECISION_BITCOIN);
    // value
    CurrencyTextView rowValue = (CurrencyTextView) row.findViewById(R.id.order_row_value);
    CurrencyTextView rowTotal = (CurrencyTextView) row.findViewById(R.id.order_row_total);
    rowValue.setPrecision(Constants.PRECISION_CURRENCY);
    rowTotal.setPrecision(Constants.PRECISION_CURRENCY);
    if (order instanceof LimitOrder) {
      LimitOrder lo = (LimitOrder) order;
      rowValue.setAmount(lo.getLimitPrice());
      rowTotal.setAmount(lo.getLimitPrice().multipliedBy(order.getTradableAmount()));
    }
    ImageButton deleteButton = (ImageButton) row.findViewById(R.id.order_row_delete);
    deleteButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setPositiveButton(R.string.button_delete_order_confirm, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            application.getExchangeService().deleteOrder(order);
          }
        });
        alertDialogBuilder.setNegativeButton(R.string.button_delete_order_cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
          }
        });
        alertDialogBuilder.setMessage(activity.getString(R.string.button_delete_order_text, order.getType().toString(), order.getTradableAmount()));
        alertDialogBuilder.create().show();
      }
    });
  }

  @Override
  public int getRowLayout() {
    return R.layout.order_row_extended;
  }
}
