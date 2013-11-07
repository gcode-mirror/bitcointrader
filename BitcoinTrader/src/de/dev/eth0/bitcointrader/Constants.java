//$URL$
//$Id$
package de.dev.eth0.bitcointrader;

/**
 * @author Alexander Muthmann
 */
public class Constants {

  // General Settings
  public static final char CHAR_HAIR_SPACE = '\u200a';
  public static final char CHAR_THIN_SPACE = '\u2009';
  public static final String CURRENCY_CODE_BITCOIN = "BTC";
  public static final int PRECISION_BITCOIN = 5;
  public static final int PRECISION_CURRENCY = 5;
  public static final String APP_ACTIVATION_URL = "https://mtgox.com/api/1/generic/api/activate";
  public static final String APP_ACTIVATION_ID = "00287aa0-775a-41b3-b799-2d7a0116bb06";
  public static final String MTGOX_DEMO_ACCOUNT_APIKEY = "cb7897cc-5c08-4e74-82a4-4c5e72457589";
  public static final String MTGOX_DEMO_ACCOUNT_SECRETKEY = "1LrjXFwoNped6Tw+7cQVzIkn6ZHnJdoFUEqWyEAplkDdtUAcREEsUn8xfkvQDOunyPhP2TVNv54660e9CFET7w==";
  // About
  public static final String DONATION_ADDRESS = "1FDLG9HbYAECa9R1o7kE9Rt37XdnWv1teZ";
  public static final String REPORT_EMAIL = "bitcointraderissues@dev-eth0.de";
  public static final String REPORT_SUBJECT_ISSUE = "Reported issue";
  public static final String REPORT_SUBJECT_CRASH = "Crash report";
  public static final String AUTHOR_URL = "http://www.dev-eth0.de";
  public static final String AUTHOR_TWITTER_URL = "https://twitter.com/#!/deveth0";
  public static final String AUTHOR_SOURCE_URL = "https://code.google.com/p/bitcointrader/";
  public static final String CREDITS_BITCOINWALLET_URL = "http://code.google.com/p/bitcoin-wallet/";
  public static final String CREDITS_XCHANGE_URL = "https://github.com/timmolter/XChange";
  public static final String CREDITS_ZXING_URL = "http://zxing.googlecode.com";
  public static final String CREDITS_GRAPHVIEW_URL = "http://www.jjoe64.com/p/graphview-library.html";
  // Prefs
  public static final String PREFS_TRAILING_STOP_THREASHOLD = "trailingStopThreadhold";
  public static final String PREFS_TRAILING_STOP_VALUE = "trailingStopValue";
  public static final String PREFS_KEY_DEMO = "demo";
  public static final String PREFS_KEY_MTGOX_APIKEY = "mtgox_apikey";
  public static final String PREFS_KEY_MTGOX_SECRETKEY = "mtgox_secretkey";
  public static final String PREFS_KEY_GENERAL_UPDATE = "general_update";
  public static final String PREFS_KEY_GENERAL_NOTIFY_ON_UPDATE = "general_notify_on_update";
  public static final String PREFS_KEY_GENERAL_SOUND = "general_sound";
  public static final String PREFS_KEY_GENERAL_VIBRATE = "general_vibrate";
  public static final String PREFS_KEY_CURRENCY = "selected_currency";
  public static final String PREFS_KEY_TRAILING_STOP_SELLING_ENABLED = "trailing_stop_selling_enabled";
  public static final String PREFS_TRAILING_STOP_NUMBER_UPDATES = "trailing_stop_number_updates";
  public static final String MTGOX_SSL_URI = "https://data.mtgox.com";
  public static final String MTGOX_PLAIN_WEBSOCKET_URI = "ws://websocket.mtgox.com";
  public static final String MTGOX_SSL_WEBSOCKET_URI = "ws://websocket.mtgox.com";
  // Broadcast events
  public static final String UPDATE_SUCCEDED = "de.dev.eth0.bitcointrader.UPDATE_SUCCEDED";
  public static final String UPDATE_SERVICE_ACTION = "de.dev.eth0.bitcointrader.UPDATE_SERVICE_ACTION";
  public static final String UPDATE_FAILED = "de.dev.eth0.bitcointrader.UPDATE_FAILED";
  public static final String ORDER_EXECUTED = "de.dev.eth0.bitcointrader.ORDER_EXECUTED";
  public static final String CURRENCY_CHANGE_EVENT = "de.dev.eth0.bitcointrader.CURRENCY_CHANGE_EVENT";
  public static final String TRAILING_LOSS_EVENT = "de.dev.eth0.bitcointrader.TRAILING_LOSS_EVENT";
  public static final String TRAILING_LOSS_ALIGNMENT_EVENT = "de.dev.eth0.bitcointrader.TRAILING_LOSS_ALIGNMENT_EVENT";
  // Extras for intents
  public static final String EXTRA_ORDERRESULT = "orderresults";
  public static final String EXTRA_ORDERRESULT_AVGCOST = "avgcost";
  public static final String EXTRA_ORDERRESULT_TOTALAMOUNT = "totalamount";
  public static final String EXTRA_ORDERRESULT_TOTALSPENT = "totalspent";
  public static final String EXTRA_TRAILING_LOSS_EVENT_CURRENTPRICE = "currentprice";
  public static final String EXTRA_TRAILING_LOSS_EVENT_VALUE = "threashold";
  public static final String EXTRA_TRAILING_LOSS_ALIGNMENT_OLDVALUE = "oldvalue";
  public static final String EXTRA_TRAILING_LOSS_ALIGNMENT_NEWVALUE = "newvalue";
}
