//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.widgets;

import android.appwidget.AppWidgetProvider;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import de.dev.eth0.bitcointrader.util.FormatHelper;
import de.dev.eth0.bitcointrader.util.FormatHelper.DISPLAY_MODE;
import org.joda.money.BigMoney;

/**
 *
 * @author Alexander Muthmann
 */
public abstract class AbstractWidgetProvider extends AppWidgetProvider {

  protected static Editable formatCurrency(BigMoney currency) {
    return formatCurrency(currency, null);
  }

  protected static Editable formatCurrency(BigMoney currency, Integer precision) {
    return formatCurrency(DISPLAY_MODE.CURRENCY_CODE, currency, precision);
  }

  protected static Editable formatCurrency(DISPLAY_MODE mode, BigMoney currency) {
    return formatCurrency(mode, currency, null);
  }

  protected static Editable formatCurrency(DISPLAY_MODE mode, BigMoney currency, Integer precision) {
    return new SpannableStringBuilder(FormatHelper.formatBigMoney(mode, currency, precision));
  }

}
