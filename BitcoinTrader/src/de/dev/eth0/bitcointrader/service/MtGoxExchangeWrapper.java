//$URL$
//$Id$
package de.dev.eth0.bitcointrader.service;

import android.util.Log;
import com.xeiam.xchange.ExchangeException;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.mtgox.MtGoxUtils;
import com.xeiam.xchange.mtgox.v2.MtGoxExchange;
import com.xeiam.xchange.mtgox.v2.dto.MtGoxException;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxAccountInfo;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxAccountInfoWrapper;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWalletHistory;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWalletHistoryWrapper;
import com.xeiam.xchange.mtgox.v2.dto.trade.polling.MtGoxOrderResult;
import com.xeiam.xchange.mtgox.v2.dto.trade.polling.MtGoxOrderResultWrapper;
import com.xeiam.xchange.mtgox.v2.service.polling.MtGoxPollingAccountService;
import com.xeiam.xchange.mtgox.v2.service.polling.MtGoxPollingMarketDataService;
import com.xeiam.xchange.mtgox.v2.service.polling.MtGoxPollingTradeService;
import java.io.IOException;
import java.util.Locale;

/**
 * @author Alexander Muthmann
 */
public class MtGoxExchangeWrapper extends MtGoxExchange {

  private static final String TAG = MtGoxExchangeWrapper.class.getSimpleName();

  @Override
  public void applySpecification(ExchangeSpecification exchangeSpecification) {

    super.applySpecification(exchangeSpecification);

    // Configure the basic services if configuration does not apply
    this.pollingMarketDataService = new MtGoxPollingMarketDataServiceWrapper(exchangeSpecification);
    this.pollingTradeService = new MtGoxPollingTradeServiceWrapper(exchangeSpecification);
    this.pollingAccountService = new MtGoxPollingAccountServiceWrapper(exchangeSpecification);
  }

  @Override
  public MtGoxPollingMarketDataServiceWrapper getPollingMarketDataService() {
    return (MtGoxPollingMarketDataServiceWrapper) pollingMarketDataService;
  }

  @Override
  public MtGoxPollingTradeServiceWrapper getPollingTradeService() {
    return (MtGoxPollingTradeServiceWrapper) pollingTradeService;
  }

  @Override
  public MtGoxPollingAccountServiceWrapper getPollingAccountService() {
    return (MtGoxPollingAccountServiceWrapper) pollingAccountService;
  }

  public static class MtGoxPollingAccountServiceWrapper extends MtGoxPollingAccountService {

    public MtGoxPollingAccountServiceWrapper(ExchangeSpecification exchangeSpecification) {
      super(exchangeSpecification);
    }

    public MtGoxAccountInfo getMtGoxAccountInfo() {
      try {
        MtGoxAccountInfoWrapper mtGoxAccountInfoWrapper = mtGoxV2.getAccountInfo(exchangeSpecification.getApiKey(), signatureCreator, MtGoxUtils.getNonce());
        if (mtGoxAccountInfoWrapper.getResult().equals("success")) {
          return mtGoxAccountInfoWrapper.getMtGoxAccountInfo();
        } else if (mtGoxAccountInfoWrapper.getResult().equals("error")) {
          throw new ExchangeException("Error calling getAccountInfo(): " + mtGoxAccountInfoWrapper.getError());
        } else {
          throw new ExchangeException("Error calling getAccountInfo(): Unexpected result!");
        }
      } catch (MtGoxException e) {
        Log.d(TAG, Log.getStackTraceString(e), e);
        throw new ExchangeException("Error calling getAccountInfo()", e);
      } catch (IOException e) {
        Log.d(TAG, Log.getStackTraceString(e), e);
        throw new ExchangeException("Error calling getAccountInfo()", e);
      }
    }

    public MtGoxWalletHistory getMtGoxWalletHistory(String currency, Integer page) {
      try {
        MtGoxWalletHistoryWrapper mtGoxWalletHistoryWrapper = mtGoxV2.getWalletHistory(exchangeSpecification.getApiKey(), signatureCreator, MtGoxUtils.getNonce(), currency, page);
        if (mtGoxWalletHistoryWrapper.getResult().equals("success")) {
          return mtGoxWalletHistoryWrapper.getMtGoxWalletHistory();
        } else if (mtGoxWalletHistoryWrapper.getResult().equals("error")) {
          throw new ExchangeException("Error calling getMtGoxWalletHistory(): " + mtGoxWalletHistoryWrapper.getError());
        } else {
          throw new ExchangeException("Error calling getMtGoxWalletHistory(): Unexpected result!");
        }
      } catch (MtGoxException e) {
        Log.d(TAG, Log.getStackTraceString(e), e);
        throw new ExchangeException("Error calling getMtGoxWalletHistory(): " + e.getError());
      } catch (IOException e) {
        Log.d(TAG, Log.getStackTraceString(e), e);
        throw new ExchangeException("Error calling getAccountInfo(): " + e.getLocalizedMessage());
      }
    }
  }

  public static class MtGoxPollingTradeServiceWrapper extends MtGoxPollingTradeService {

    public MtGoxPollingTradeServiceWrapper(ExchangeSpecification exchangeSpecification) {
      super(exchangeSpecification);
    }

    public MtGoxOrderResult getOrderResult(Order order) {
      try {
        MtGoxOrderResultWrapper mtGoxOrderResultWrapper = mtGoxV2.getOrderResult(exchangeSpecification.getApiKey(), signatureCreator, MtGoxUtils.getNonce(),
                order.getType().toString().toLowerCase(Locale.US), order.getId());
        if (mtGoxOrderResultWrapper.getResult().equals("success")) {
          return mtGoxOrderResultWrapper.getMtGoxOrderResult();
        } else if (mtGoxOrderResultWrapper.getResult().equals("error")) {
          throw new ExchangeException("Error calling getMtGoxWalletHistory(): " + mtGoxOrderResultWrapper.getError());
        } else {
          throw new ExchangeException("Error calling getMtGoxWalletHistory(): Unexpected result!");
        }
      } catch (MtGoxException e) {
        Log.d(TAG, Log.getStackTraceString(e), e);
        throw new ExchangeException("Error calling getMtGoxWalletHistory(): " + e.getError());
      } catch (IOException e) {
        Log.d(TAG, Log.getStackTraceString(e), e);
        throw new ExchangeException("Error calling getAccountInfo(): " + e.getLocalizedMessage());
      }
    }
  }

  public static class MtGoxPollingMarketDataServiceWrapper extends MtGoxPollingMarketDataService {

    public MtGoxPollingMarketDataServiceWrapper(ExchangeSpecification exchangeSpecification) {
      super(exchangeSpecification);
    }
  }
}
