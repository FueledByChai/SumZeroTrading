package com.sumzerotrading.marketdata.paradex;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Properties;

import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.Level1Quote;
import com.sumzerotrading.marketdata.Level1QuoteListener;
import com.sumzerotrading.marketdata.OrderBookUpdateListener;
import com.sumzerotrading.marketdata.QuoteEngine;
import com.sumzerotrading.marketdata.QuoteType;

public class ParadexQuoteEngine extends QuoteEngine implements OrderBookUpdateListener {

    protected boolean started = false;

    @Override
    public Date getServerTime() {
        return null;
    }

    @Override
    public boolean isConnected() {
        return started;
    }

    @Override
    public void startEngine() {
        started = true;

    }

    @Override
    public void startEngine(Properties props) {
        started = true;

    }

    @Override
    public boolean started() {
        return started;
    }

    @Override
    public void stopEngine() {
        started = false;

    }

    @Override
    public void useDelayedData(boolean useDelayed) {
        // Not implemented for Paradex

    }

    @Override
    public void subscribeLevel1(Ticker ticker, Level1QuoteListener listener) {
        super.subscribeLevel1(ticker, listener);
        OrderBookRegistry.getInstance().getOrderBook(ticker).addOrderBookUpdateListener(this);
    }

    @Override
    public void unsubscribeLevel1(Ticker ticker, Level1QuoteListener listener) {
        super.unsubscribeLevel1(ticker, listener);
    }

    @Override
    public void bestBidUpdated(Ticker ticker, BigDecimal bestBid) {
        Level1Quote quote = new Level1Quote(ticker);
        quote.addQuote(QuoteType.BID, bestBid);
        super.fireLevel1Quote(quote);

    }

    @Override
    public void bestAskUpdated(Ticker ticker, BigDecimal bestAsk) {
        Level1Quote quote = new Level1Quote(ticker);
        quote.addQuote(QuoteType.ASK, bestAsk);
        super.fireLevel1Quote(quote);

    }
}
