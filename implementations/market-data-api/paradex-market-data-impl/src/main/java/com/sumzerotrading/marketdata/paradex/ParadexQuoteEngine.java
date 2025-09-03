package com.sumzerotrading.marketdata.paradex;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.Level1Quote;
import com.sumzerotrading.marketdata.Level1QuoteListener;
import com.sumzerotrading.marketdata.OrderBookUpdateListener;
import com.sumzerotrading.marketdata.OrderFlow;
import com.sumzerotrading.marketdata.OrderFlowListener;
import com.sumzerotrading.marketdata.QuoteEngine;
import com.sumzerotrading.marketdata.QuoteType;
import com.sumzerotrading.paradex.common.ParadexTickerRegistry;

public class ParadexQuoteEngine extends QuoteEngine implements OrderBookUpdateListener, TradesUpdateListener {

    protected static final Logger logger = LoggerFactory.getLogger(ParadexQuoteEngine.class);
    protected Map<Ticker, TradesWebSocketClient> tradesClients = new HashMap<>();

    protected boolean started = false;

    public ParadexQuoteEngine() {
        ParadexTickerRegistry.getInstance();
    }

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
    public void subscribeOrderFlow(Ticker ticker, OrderFlowListener listener) {
        super.subscribeOrderFlow(ticker, listener);
        TradesWebSocketClient tradesClient = tradesClients.get(ticker);
        if (tradesClient == null) {
            tradesClient = new TradesWebSocketClient();
            tradesClients.put(ticker, tradesClient);
        }
        tradesClient.startTradesWSClient(ticker, this);
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

    @Override
    public void newTrade(long createdAtTimestamp, String market, String price, String side, String size) {

        Ticker ticker = ParadexTickerRegistry.getInstance().lookupByBrokerSymbol(market);
        ZonedDateTime timestamp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(createdAtTimestamp), ZoneId.of("GMT"));

        OrderFlow orderFlow = new OrderFlow(ticker, new BigDecimal(price), new BigDecimal(size),
                OrderFlow.Side.valueOf(side), timestamp);
        super.fireOrderFlow(orderFlow);
    }
}
