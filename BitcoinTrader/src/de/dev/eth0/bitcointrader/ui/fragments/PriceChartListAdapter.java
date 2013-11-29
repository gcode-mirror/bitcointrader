//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.xeiam.xchange.bitcoincharts.dto.marketdata.BitcoinChartsTicker;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.views.AmountTextView;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import de.dev.eth0.bitcointrader.util.FormatHelper.DISPLAY_MODE;
import org.joda.money.BigMoney;

/**
 * @author Alexander Muthmann
 */
public class PriceChartListAdapter extends AbstractListAdapter<BitcoinChartsTicker> {

  private final static String TAG = PriceChartListAdapter.class.getSimpleName();

  public PriceChartListAdapter(AbstractBitcoinTraderActivity activity) {
    super(activity);
  }

  @Override
  public int getRowLayout() {
    return R.layout.price_chart_row_extended;
  }

  @Override
  public void bindView(View row, BitcoinChartsTicker entry) {

    ImageView trendView = (ImageView)row.findViewById(R.id.chart_row_trend);
    TextView symbolView = (TextView)row.findViewById(R.id.chart_row_symbol);
    CurrencyTextView lastView = (CurrencyTextView)row.findViewById(R.id.chart_row_last);
    AmountTextView volView = (AmountTextView)row.findViewById(R.id.chart_row_vol);
    CurrencyTextView lowView = (CurrencyTextView)row.findViewById(R.id.chart_row_low);
    CurrencyTextView highView = (CurrencyTextView)row.findViewById(R.id.chart_row_high);

    BigMoney last = BigMoney.parse("BTC " + entry.getClose());
    BigMoney low = BigMoney.parse("BTC " + entry.getLow());
    BigMoney high = BigMoney.parse("BTC " + entry.getHigh());
    BigMoney avg = BigMoney.parse("BTC " + entry.getAvg());

    if (last.isLessThan(avg)) {
      trendView.setImageResource(R.drawable.trend_down);
    }
    else if (last.isGreaterThan(avg)) {
      trendView.setImageResource(R.drawable.trend_up);
    }
    else if (last.isEqual(avg)) {
      trendView.setImageResource(R.drawable.trend_stable);
    }

    symbolView.setText(entry.getSymbol());

    volView.setAmount(entry.getVolume());
    volView.setPrecision(2);

    lastView.setAmount(last);
    lastView.setDisplayMode(DISPLAY_MODE.NO_CURRENCY_CODE);
    lastView.setPrecision(2);
    lowView.setAmount(low);
    lowView.setDisplayMode(DISPLAY_MODE.NO_CURRENCY_CODE);
    lowView.setPrecision(2);
    highView.setAmount(high);
    highView.setDisplayMode(DISPLAY_MODE.NO_CURRENCY_CODE);
    highView.setPrecision(2);
  }
}
