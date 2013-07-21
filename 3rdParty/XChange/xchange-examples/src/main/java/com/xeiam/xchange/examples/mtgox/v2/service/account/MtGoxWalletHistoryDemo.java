/**
 * Copyright (C) 2012 - 2013 Xeiam LLC http://xeiam.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.xeiam.xchange.examples.mtgox.v2.service.account;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.examples.mtgox.v2.MtGoxV2ExamplesUtils;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWalletHistory;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWalletHistoryEntry;
import com.xeiam.xchange.mtgox.v2.service.polling.MtGoxPollingAccountService;

/**
 * Demo requesting wallet history at MtGox
 */
public class MtGoxWalletHistoryDemo {

  public static void main(String[] args) {

    Exchange mtgox = MtGoxV2ExamplesUtils.createExchange();

    // Interested in the private account functionality (authentication)
    MtGoxPollingAccountService accountService = (MtGoxPollingAccountService)mtgox.getPollingAccountService();

    MtGoxWalletHistory walletHistory = accountService.getMtGoxWalletHistory("BTC").getMtGoxWalletHistory();
    for (MtGoxWalletHistoryEntry entry : walletHistory.getMtGoxWalletHistoryEntries()) {
      System.out.println(entry);
    }

    walletHistory = accountService.getMtGoxWalletHistory("USD").getMtGoxWalletHistory();
    for (MtGoxWalletHistoryEntry entry : walletHistory.getMtGoxWalletHistoryEntries()) {
      System.out.println(entry);
    }

  }
}
