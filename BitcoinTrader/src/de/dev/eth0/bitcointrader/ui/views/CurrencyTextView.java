//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.views;

import android.content.Context;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.widget.TextView;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.util.FormatHelper;
import de.dev.eth0.bitcointrader.util.FormatHelper.DISPLAY_MODE;
import java.math.RoundingMode;
import org.joda.money.BigMoney;

/**
 * @author Alexander Muthmann
 */
public class CurrencyTextView extends TextView {

  
  private BigMoney amount = null;
  private String prefix = null;
  private Integer precision = null;
  private DISPLAY_MODE displayMode = DISPLAY_MODE.CURRENCY_CODE;

  public CurrencyTextView(Context context) {
    super(context);
  }

  public CurrencyTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setDisplayMode(DISPLAY_MODE displayMode) {
    this.displayMode = displayMode;
    updateView();
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix + Constants.CHAR_HAIR_SPACE;
    updateView();
  }

  public void setPrecision(int precision) {
    this.precision = precision;
    updateView();
  }

  public void setAmount(BigMoney amount) {
    this.amount = amount;
    updateView();
  }

  private void updateView() {
    if (amount != null) {
      Editable text;
      if (precision != null) {
        text = new SpannableStringBuilder(FormatHelper.formatBigMoney(displayMode, amount, precision));
      } else {
        text = new SpannableStringBuilder(FormatHelper.formatBigMoney(displayMode, amount));
      }
      if (prefix != null) {
        text.insert(0, prefix);
      }
      setText(text);
    }
  }
}
