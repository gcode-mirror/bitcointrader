//$URL$
//$Id$
package de.dev.eth0.bitcointrader.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.ui.BitcoinTraderActivity;

/**
 * @author Alexander Muthmann
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {

  private final static int ORDER_EXECUTED_NOTIFICATION_ID = 1;
  private final static int UPDATE_FAILED_NOTIFICATION_ID = 2;
  private final static int TRAILING_STOP_NOTIFICATION_ID = 3;
  private final static int TRAILING_STOP_ALIGNMENT_NOTIFICATION_ID = 4;

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals(Constants.UPDATE_FAILED)) {
      notifyUpdateFailed(context, intent);
    }
    else if (intent.getAction().equals(Constants.UPDATE_SUCCEDED)) {
      notifyUpdateSucceded(context);
    }
    else if (intent.getAction().equals(Constants.TRAILING_LOSS_EVENT)) {
      notifyTrailingStopEvent(context, intent);
    }
    else if (intent.getAction().equals(Constants.TRAILING_LOSS_ALIGNMENT_EVENT)) {
      notifyTrailingStopAlignment(context, intent);
    }
    else if (intent.getAction().equals(Constants.ORDER_EXECUTED)) {
      notifyOrderExecuted(context, intent.getParcelableArrayExtra(Constants.EXTRA_ORDERRESULT));
    }
  }

  private void notifyUpdateSucceded(Context context) {
    NotificationManager notificationmanager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationmanager.cancel(UPDATE_FAILED_NOTIFICATION_ID);
  }

  private void notifyUpdateFailed(Context context, Intent intent) {
    String message = (intent != null && intent.hasExtra(Constants.EXTRA_MESSAGE))
            ? intent.getStringExtra(Constants.EXTRA_MESSAGE)
            : context.getString(R.string.notify_update_failed_text);
    NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_action_warning)
            .setContentTitle(context.getString(R.string.notify_update_failed_title))
            .setContentText(message);
    NotificationCompat.BigTextStyle notificationStyle = new NotificationCompat.BigTextStyle();
    notificationStyle.setBigContentTitle(context.getString(R.string.notify_update_failed_title));
    notificationStyle.bigText(message);
    mBuilder.setStyle(notificationStyle);
    Intent resultIntent = new Intent(context, BitcoinTraderActivity.class);
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    stackBuilder.addParentStack(BitcoinTraderActivity.class);
    stackBuilder.addNextIntent(resultIntent);
    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(resultPendingIntent);
    setupNotificationBuilder(mBuilder, context);
    NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(UPDATE_FAILED_NOTIFICATION_ID, mBuilder.build());
  }

  private void notifyOrderExecuted(Context context, Parcelable[] executedOrders) {
    StringBuilder sb = new StringBuilder();
    String contentText = null;
    for (Parcelable parcelable : executedOrders) {
      if (parcelable instanceof Bundle) {
        Bundle bundle = (Bundle)parcelable;
        if (TextUtils.isEmpty(contentText)) {
          contentText = context.getString(R.string.notify_order_executed_text,
                  bundle.getString(Constants.EXTRA_ORDERRESULT_AVGCOST),
                  bundle.getString(Constants.EXTRA_ORDERRESULT_TOTALAMOUNT),
                  bundle.getString(Constants.EXTRA_ORDERRESULT_TOTALSPENT));
        }
        sb.append(context.getString(R.string.notify_order_executed_text,
                bundle.getString(Constants.EXTRA_ORDERRESULT_AVGCOST),
                bundle.getString(Constants.EXTRA_ORDERRESULT_TOTALAMOUNT),
                bundle.getString(Constants.EXTRA_ORDERRESULT_TOTALSPENT)));
      }
    }
    NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_action_bitcoin)
            .setContentTitle(context.getString(R.string.notify_order_executed_title))
            .setContentText(contentText);
    NotificationCompat.BigTextStyle notificationStyle = new NotificationCompat.BigTextStyle();
    notificationStyle.setBigContentTitle(context.getString(R.string.notify_order_executed_title));
    notificationStyle.bigText(sb.toString());
    mBuilder.setStyle(notificationStyle);
    Intent resultIntent = new Intent(context, BitcoinTraderActivity.class);
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    stackBuilder.addParentStack(BitcoinTraderActivity.class);
    stackBuilder.addNextIntent(resultIntent);
    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(resultPendingIntent);
    setupNotificationBuilder(mBuilder, context);
    NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(ORDER_EXECUTED_NOTIFICATION_ID, mBuilder.build());
  }

  private void notifyTrailingStopEvent(Context context, Intent intent) {
    String text = context.getString(R.string.trailing_stop_notify_text,
            intent.getStringExtra(Constants.EXTRA_TRAILING_LOSS_EVENT_CURRENTPRICE),
            intent.getStringExtra(Constants.EXTRA_TRAILING_LOSS_EVENT_VALUE));
    NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_action_warning)
            .setContentTitle(context.getString(R.string.trailing_stop_notify_title))
            .setContentText(text);
    NotificationCompat.BigTextStyle notificationStyle = new NotificationCompat.BigTextStyle();
    notificationStyle.setBigContentTitle(context.getString(R.string.trailing_stop_notify_title));
    notificationStyle.bigText(text);
    mBuilder.setStyle(notificationStyle);
    Intent resultIntent = new Intent(context, BitcoinTraderActivity.class);
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    stackBuilder.addParentStack(BitcoinTraderActivity.class);
    stackBuilder.addNextIntent(resultIntent);
    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(resultPendingIntent);
    setupNotificationBuilder(mBuilder, context);
    NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(TRAILING_STOP_NOTIFICATION_ID, mBuilder.build());
  }

  private void notifyTrailingStopAlignment(Context context, Intent intent) {
    String text = context.getString(R.string.trailing_stop_alignment_notify_text,
            intent.getStringExtra(Constants.EXTRA_TRAILING_LOSS_ALIGNMENT_OLDVALUE),
            intent.getStringExtra(Constants.EXTRA_TRAILING_LOSS_ALIGNMENT_NEWVALUE));
    NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_action_warning)
            .setContentTitle(context.getString(R.string.trailing_stop_alignment_notify_title))
            .setContentText(text);
    NotificationCompat.BigTextStyle notificationStyle = new NotificationCompat.BigTextStyle();
    notificationStyle.setBigContentTitle(context.getString(R.string.trailing_stop_alignment_notify_title));
    notificationStyle.bigText(text);
    mBuilder.setStyle(notificationStyle);
    Intent resultIntent = new Intent(context, BitcoinTraderActivity.class);
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    stackBuilder.addParentStack(BitcoinTraderActivity.class);
    stackBuilder.addNextIntent(resultIntent);
    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(resultPendingIntent);
    setupNotificationBuilder(mBuilder, context);
    NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(TRAILING_STOP_NOTIFICATION_ID, mBuilder.build());
  }

  private void setupNotificationBuilder(NotificationCompat.Builder pBuilder, Context pContext) {
    pBuilder.setAutoCancel(true);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);
    boolean sound = prefs.getBoolean(Constants.PREFS_KEY_GENERAL_SOUND, false);
    boolean vibrate = prefs.getBoolean(Constants.PREFS_KEY_GENERAL_VIBRATE, false);
    if (sound || vibrate) {
      pBuilder.setDefaults(sound ? (vibrate ? Notification.DEFAULT_ALL : Notification.DEFAULT_SOUND) : Notification.DEFAULT_VIBRATE);
    }
  }
}
