//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui;

import android.content.Intent;
import android.os.Bundle;
import com.actionbarsherlock.app.ActionBar;

import com.actionbarsherlock.view.MenuItem;
import com.xeiam.xchange.dto.Order;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.ui.fragments.PlaceOrderFragment;

/**
 * @author Alexander Muthmann
 */
public class PlaceOrderActivity extends AbstractBitcoinTraderActivity {

  public static final String INTENT_EXTRA_TYPE = "type";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.place_order_activity);
    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    handleIntent(getIntent());
  }

  @Override
  protected void onNewIntent(Intent intent) {
    handleIntent(intent);
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

  private void handleIntent(Intent intent) {
    Order.OrderType ordertype = null;
    if (intent.hasExtra(INTENT_EXTRA_TYPE)) {
      ordertype = Order.OrderType.valueOf(intent.getStringExtra(INTENT_EXTRA_TYPE));
      updatePlaceOrderFragment(ordertype);
    }
  }

  private void updatePlaceOrderFragment(Order.OrderType ordertype) {
    PlaceOrderFragment placeOrderFragment = (PlaceOrderFragment) getSupportFragmentManager().findFragmentById(R.id.place_order_fragment);
    placeOrderFragment.update(ordertype);
  }
}
