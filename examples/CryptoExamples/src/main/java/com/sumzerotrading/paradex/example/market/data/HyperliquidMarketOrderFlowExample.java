/**
 * MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
and associated documentation files (the "Software"), to deal in the Software without restriction, 
including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.sumzerotrading.paradex.example.market.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.ws.HyperliquidTickerRegistry;
import com.sumzerotrading.marketdata.OrderFlow;
import com.sumzerotrading.marketdata.QuoteEngine;
import com.sumzerotrading.marketdata.hyperliquid.HyperliquidQuoteEngine;

public class HyperliquidMarketOrderFlowExample {

    protected static final Logger logger = LoggerFactory.getLogger(HyperliquidMarketOrderFlowExample.class);

    public void getOrderFlow() {

        Ticker ticker = HyperliquidTickerRegistry.getInstance().lookupByBrokerSymbol("BTC");

        QuoteEngine quoteEngine = QuoteEngine.getInstance(HyperliquidQuoteEngine.class);

        quoteEngine.subscribeOrderFlow(ticker, (OrderFlow orderFlow) -> {
            logger.info("Received Order Flow: {}", orderFlow);
        });
    }

    public static void main(String[] args) {
        HyperliquidMarketOrderFlowExample example = new HyperliquidMarketOrderFlowExample();
        example.getOrderFlow();
    }
}
