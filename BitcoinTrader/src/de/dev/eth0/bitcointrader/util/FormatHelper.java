//$URL$
//$Id$
package de.dev.eth0.bitcointrader.util;

import android.content.Context;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.Date;
import org.joda.money.BigMoney;
import org.joda.money.format.MoneyAmountStyle;
import org.joda.money.format.MoneyFormatter;
import org.joda.money.format.MoneyFormatterBuilder;

/**
 * Helperclass to format money
 *
 * @author Alexander Muthmann
 */
public class FormatHelper {

  public static CharSequence formatBigMoney(DISPLAY_MODE displayMode, BigMoney amount) {
    return displayMode.getFormater().print(amount.withCurrencyScale(RoundingMode.HALF_EVEN));
  }

  public static CharSequence formatBigMoney(DISPLAY_MODE displayMode, BigMoney amount, Integer precision) {
    return precision == null
            ? formatBigMoney(displayMode, amount)
            : displayMode.getFormater().print(amount.withScale(precision, RoundingMode.HALF_EVEN));
  }

  public static CharSequence formatDate(Context context, Date timestamp) {
    DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
    DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
    return dateFormat.format(timestamp) + ", " + timeFormat.format(timestamp);
  }

  public static enum DISPLAY_MODE {

    NO_CURRENCY_CODE(new MoneyFormatterBuilder().
    appendAmount(MoneyAmountStyle.ASCII_DECIMAL_POINT_NO_GROUPING).toFormatter()),
    CURRENCY_CODE(new MoneyFormatterBuilder().appendCurrencyCode().appendLiteral(" ")
    .appendAmount(MoneyAmountStyle.ASCII_DECIMAL_POINT_NO_GROUPING).toFormatter()),
    CURRENCY_SYMBOL(new MoneyFormatterBuilder().appendCurrencySymbolLocalized().appendLiteral(" ")
    .appendAmount(MoneyAmountStyle.ASCII_DECIMAL_POINT_NO_GROUPING).toFormatter());
    private MoneyFormatter formater;

    private DISPLAY_MODE(MoneyFormatter formater) {
      this.formater = formater;
    }

    public MoneyFormatter getFormater() {
      return formater;
    }
  }
}
