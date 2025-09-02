package com.sumzerotrading.marketdata.dydx;

import com.sumzerotrading.data.Exchange;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.ILevel1Quote;
import com.sumzerotrading.marketdata.Level1QuoteListener;
import com.sumzerotrading.marketdata.QuoteEngine;

public class TestApp implements Level1QuoteListener {

    public void startApp() {
        QuoteEngine quoteEngine = new DyDxLevel1QuoteEngine();
        Ticker ticker = new Ticker("BTC-USD").setInstrumentType(InstrumentType.PERPETUAL_FUTURES)
                .setExchange(Exchange.DYDX);
        Ticker eth = new Ticker("ETH-USD").setInstrumentType(InstrumentType.PERPETUAL_FUTURES)
                .setExchange(Exchange.DYDX);
        Ticker xrp = new Ticker("XRP-USD").setInstrumentType(InstrumentType.PERPETUAL_FUTURES)
                .setExchange(Exchange.DYDX);
        Ticker sol = new Ticker("SOL-USD").setInstrumentType(InstrumentType.PERPETUAL_FUTURES)
                .setExchange(Exchange.DYDX);

        quoteEngine.startEngine();
        quoteEngine.subscribeLevel1(ticker, this);
        quoteEngine.subscribeLevel1(eth, this);
        quoteEngine.subscribeLevel1(xrp, this);
        quoteEngine.subscribeLevel1(sol, this);

    }

    @Override
    public void quoteRecieved(ILevel1Quote quote) {
        System.out.println("Got quote: " + quote);

    }

    public static void main(String[] args) throws Exception {
        new TestApp().startApp();
        Thread.sleep(100000);
    }

}
