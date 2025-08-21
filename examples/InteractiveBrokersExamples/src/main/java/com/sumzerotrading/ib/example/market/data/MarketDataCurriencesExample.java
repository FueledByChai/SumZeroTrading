/**
 * MIT License

Copyright (c) 2015  Rob Terpilowski

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

package com.sumzerotrading.ib.example.market.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.data.CurrencyTicker;
import com.sumzerotrading.interactive.brokers.client.InteractiveBrokersClient;
import com.sumzerotrading.interactive.brokers.client.InteractiveBrokersClientInterface;
import com.sumzerotrading.marketdata.ILevel1Quote;

public class MarketDataCurriencesExample {

    protected static final Logger logger = LoggerFactory.getLogger(MarketDataCurriencesExample.class);

    public void start() {
        logger.info("Starting Market Data Currencies Example...");
        InteractiveBrokersClientInterface ibClient = InteractiveBrokersClient.getInstance("localhost", 7777, 2);
        logger.info("Connecting to Interactive Brokers Client...");
        ibClient.connect();

        logger.info("Connected to Interactive Brokers Client");
        // try {
        // Thread.sleep(30000);
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        CurrencyTicker currencyTicker = new CurrencyTicker();
        currencyTicker.setSymbol("AUD");
        currencyTicker.setCurrency("CAD");

        logger.info("Subscribing to Level 1 Quotes for: " + currencyTicker);
        ibClient.subscribeLevel1(currencyTicker, (ILevel1Quote quote) -> {
            logger.info("Received Quote: " + quote);
        });

    }

    public static void main(String[] args) {
        new MarketDataCurriencesExample().start();
    }
}
