//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWallet;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.R;
import de.schildbach.wallet.integration.android.BitcoinIntegration;
import de.schildbach.wallet.ui.HelpDialogFragment;
/**
 * @author Alexander Muthmann
 */
public class BitcoinTraderActivity extends AbstractBitcoinTraderActivity {

  private LocalBroadcastManager broadcastManager;
  private Menu selectCurrencyItem;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.bitcointrader_content);
    broadcastManager = LocalBroadcastManager.getInstance(getBitcoinTraderApplication());
    
    
  }

  @Override
  protected void onResume() {
    super.onResume();
    getBitcoinTraderApplication().startExchangeService();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getSupportMenuInflater().inflate(R.menu.bitcointrader_options, menu);

    selectCurrencyItem = menu.findItem(R.id.bitcointrader_options_select_currency).getSubMenu();
    selectCurrencyItem.clear();
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.bitcointrader_options_select_currency:
        showSelectCurrencyPopup();
        break;
      case R.id.bitcointrader_options_price_chart:
        startActivity(new Intent(this, PriceChartActivity.class));
        break;
      case R.id.bitcointrader_options_wallet_history:
        startActivity(new Intent(this, WalletHistoryActivity.class));
        break;
      case R.id.bitcointrader_options_refresh:
        broadcastManager.sendBroadcast(new Intent(Constants.UPDATE_SERVICE_ACTION));
        break;
      case R.id.bitcointrader_options_about:
        startActivity(new Intent(this, AboutActivity.class));
        break;
      case R.id.bitcointrader_options_preferences:
        startActivity(new Intent(this, PreferencesActivity.class));
        break;
      case R.id.bitcointrader_options_donate:
        BitcoinIntegration.request(this, Constants.DONATION_ADDRESS);
        break;
      case R.id.bitcointrader_options_help:
        HelpDialogFragment.page(getSupportFragmentManager(), "help");
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void showSelectCurrencyPopup() {
    if (selectCurrencyItem.size() == 0) {
      int idx = 0;
      if (getExchangeService() != null && getExchangeService().getAccountInfo() != null) {
        for (MtGoxWallet wallet : getExchangeService().getAccountInfo().getWallets().getMtGoxWallets()) {
          if (wallet != null && wallet.getBalance() != null
                  && !TextUtils.isEmpty(wallet.getBalance().getCurrency())
                  && !wallet.getBalance().getCurrency().equals(Constants.CURRENCY_CODE_BITCOIN)) {
            MenuItem mi = selectCurrencyItem.add(Menu.NONE, idx++, Menu.NONE, wallet.getBalance().getCurrency());
            mi.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
              public boolean onMenuItemClick(MenuItem item) {
                getExchangeService().setCurrency(item.getTitle().toString());
                broadcastManager.sendBroadcast(new Intent(Constants.UPDATE_SERVICE_ACTION));
                return true;
              }
            });
          }
        }
      }
    }
  }
}
