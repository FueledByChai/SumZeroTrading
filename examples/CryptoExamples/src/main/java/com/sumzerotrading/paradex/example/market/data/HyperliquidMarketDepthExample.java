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
import com.sumzerotrading.hyperliquid.websocket.HyperliquidTickerRegistry;
import com.sumzerotrading.marketdata.hyperliquid.HyperliquidQuoteEngine;
import com.sumzerotrading.util.ITickerRegistry;

public class HyperliquidMarketDepthExample {

    protected static final Logger logger = LoggerFactory.getLogger(HyperliquidMarketDepthExample.class);

    public void start() {

        ITickerRegistry registry = HyperliquidTickerRegistry.getInstance();
        HyperliquidQuoteEngine quoteEngine = new HyperliquidQuoteEngine();
        quoteEngine.startEngine();

        Ticker btcTicker = registry.lookupByCommonSymbol("BTC");

        quoteEngine.subscribeMarketDepth(btcTicker, (orderBook) -> {
            logger.info("Order Book Update: " + orderBook);
            logger.info("#############\n\n\n");
        });

    }

    public static void main(String[] args) {
        new HyperliquidMarketDepthExample().start();
    }
}
