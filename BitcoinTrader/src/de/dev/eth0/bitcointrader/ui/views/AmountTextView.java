//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.views;

import android.content.Context;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.widget.TextView;
import de.dev.eth0.bitcointrader.Constants;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Alexander Muthmann
 */
public class AmountTextView extends TextView {

  private BigDecimal amount = null;
  private Integer precision = null;
  private String prefix = null;
  private String postfix = null;

  public AmountTextView(Context context) {
    super(context);
  }

  public AmountTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
    updateView();
  }

  public void setPrecision(int precision) {
    this.precision = precision;
    updateView();
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix + Constants.CHAR_HAIR_SPACE;
    updateView();
  }

  public void setPostfix(String postfix) {
    this.postfix = Constants.CHAR_HAIR_SPACE + postfix;
    updateView();
  }

  private void updateView() {
    Editable text;
    if (amount != null) {
      if (precision != null) {
        text = new SpannableStringBuilder(amount.setScale(precision, RoundingMode.HALF_EVEN).toString());
      } else {
        text = new SpannableStringBuilder(amount.toString());

      }
      if (prefix != null) {
        text.insert(0, prefix);
      }
      if (postfix != null) {
        text.append(postfix);
      }
      setText(text);
    }
  }
}
