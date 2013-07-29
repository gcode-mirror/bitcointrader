//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.widget.RemoteViews;
import com.xeiam.xchange.dto.marketdata.Ticker;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.R;

import de.dev.eth0.bitcointrader.service.ExchangeService;
import de.dev.eth0.bitcointrader.ui.BitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.StartScreenActivity;
import de.dev.eth0.bitcointrader.util.FormatHelper;
import java.math.RoundingMode;

/**
 * @author Alexander Muthmann
 */
public class PriceInfoWidgetProvider extends AbstractWidgetProvider {

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    BitcoinTraderApplication application = (BitcoinTraderApplication)context.getApplicationContext();
    ExchangeService exchangeService = application.getExchangeService();

    updateWidgets(context, appWidgetManager, appWidgetIds, exchangeService);
  }

  public static void updateWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, ExchangeService exchangeService) {
    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.price_info_widget_content);
    views.setOnClickPendingIntent(R.id.price_info_widget_content,
            PendingIntent.getActivity(context, 0, new Intent(context, StartScreenActivity.class), 0));
    if (exchangeService != null && exchangeService.getTicker() != null) {
      views.setOnClickPendingIntent(R.id.price_info_widget_content,
              PendingIntent.getActivity(context, 0, new Intent(context, BitcoinTraderActivity.class), 0));
      Ticker ticker = exchangeService.getTicker();

      views.setTextViewText(R.id.price_info_widget_low, formatCurrency(FormatHelper.DISPLAY_MODE.CURRENCY_SYMBOL, ticker.getLow()));
      views.setTextViewText(R.id.price_info_widget_current, formatCurrency(ticker.getLast()));
      views.setTextViewText(R.id.price_info_widget_high, formatCurrency(FormatHelper.DISPLAY_MODE.CURRENCY_SYMBOL, ticker.getHigh()));


      views.setTextViewText(R.id.price_info_widget_volume, new SpannableStringBuilder(context.getString(R.string.price_info_volume_label)
              + " "
              + ticker.getVolume().setScale(0, RoundingMode.HALF_EVEN).toString()));
      views.setTextViewText(R.id.price_info_widget_lastupdate, new SpannableStringBuilder(FormatHelper.formatDate(context, ticker.getTimestamp())));

    }
    for (int appWidgetId : appWidgetIds) {
      appWidgetManager.updateAppWidget(appWidgetId, views);
    }
  }
}
