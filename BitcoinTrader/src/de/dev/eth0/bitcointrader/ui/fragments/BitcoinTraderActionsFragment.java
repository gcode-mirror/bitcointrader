//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.xeiam.xchange.dto.Order;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.ui.PlaceOrderActivity;

/**
 * @author Alexander Muthmann
 */
public class BitcoinTraderActionsFragment extends Fragment {

  private Activity activity;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    this.activity = activity;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.bitcointrader_actions_fragment, container);

    Button buyButton = (Button) view.findViewById(R.id.bitcointrader_actions_buy);
    buyButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Intent i = new Intent(activity, PlaceOrderActivity.class);
        i.putExtra(PlaceOrderActivity.INTENT_EXTRA_TYPE, Order.OrderType.BID.name());
        startActivity(i);
      }
    });

    Button sellButton = (Button) view.findViewById(R.id.bitcointrader_actions_sell);
    sellButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Intent i = new Intent(activity, PlaceOrderActivity.class);
        i.putExtra(PlaceOrderActivity.INTENT_EXTRA_TYPE, Order.OrderType.ASK.name());
        startActivity(i);
      }
    });
    return view;
  }
}
