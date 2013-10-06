//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import com.actionbarsherlock.app.SherlockFragment;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.service.ExchangeService;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;

/**
 * @author Alexander Muthmann
 */
public abstract class AbstractBitcoinTraderFragment extends SherlockFragment {

  private AbstractBitcoinTraderActivity activity;
  private BitcoinTraderApplication application;
  protected String mDialogLoadingString;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = (AbstractBitcoinTraderActivity) activity;
    this.application = this.activity.getBitcoinTraderApplication();
    this.mDialogLoadingString = activity.getString(R.string.loading_info);
  }

  protected ExchangeService getExchangeService() {
    return application.getExchangeService();
  }
}
