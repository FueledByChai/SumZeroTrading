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
import com.sumzerotrading.marketdata.ILevel1Quote;
import com.sumzerotrading.marketdata.Level1Quote;
import com.sumzerotrading.marketdata.Level1QuoteListener;
import com.sumzerotrading.marketdata.OrderBookUpdateListener;
import com.sumzerotrading.marketdata.OrderFlow;
import com.sumzerotrading.marketdata.OrderFlowListener;
import com.sumzerotrading.marketdata.QuoteEngine;
import com.sumzerotrading.marketdata.QuoteType;
import com.sumzerotrading.paradex.common.IParadexTickerRegistry;
import com.sumzerotrading.paradex.common.ParadexTickerRegistry;

public class ParadexQuoteEngine extends QuoteEngine
        implements OrderBookUpdateListener, TradesUpdateListener, MarketsSummaryUpdateListener {

    protected static final Logger logger = LoggerFactory.getLogger(ParadexQuoteEngine.class);
    protected Map<Ticker, TradesWebSocketClient> tradesClients = new HashMap<>();
    protected Map<Ticker, MarketsSummaryWebSocketClient> marketsSummaryClients = new HashMap<>();

    protected boolean started = false;
    protected IParadexTickerRegistry tickerRegistry;

    public ParadexQuoteEngine() {
        tickerRegistry = ParadexTickerRegistry.getInstance();
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
        MarketsSummaryWebSocketClient marketsSummaryClient = marketsSummaryClients.get(ticker);
        if (marketsSummaryClient == null) {
            marketsSummaryClient = new MarketsSummaryWebSocketClient();
            marketsSummaryClients.put(ticker, marketsSummaryClient);
        }
        marketsSummaryClient.startMarketsSummaryWSClient(ticker, this);
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

        Ticker ticker = tickerRegistry.lookupByBrokerSymbol(market);

        OrderFlow orderFlow = new OrderFlow(ticker, new BigDecimal(price), new BigDecimal(size),
                OrderFlow.Side.valueOf(side), convertToZonedDateTime(createdAtTimestamp));
        super.fireOrderFlow(orderFlow);
    }

    @Override
    public void newSummaryUpdate(long createdAtTimestamp, String symbol, String bid, String ask, String lastPrice,
            String markPrice, String openInterest, String volume24h, String underlyingPrice, String fundingRate) {

        Ticker ticker = tickerRegistry.lookupByBrokerSymbol(symbol);

        double hourlyFundingRate = Double.parseDouble(fundingRate) / (double) ticker.getFundingRateInterval();
        double annualizedFundingRate = hourlyFundingRate * 24 * 365 * 100;
        BigDecimal fundingRateApr = BigDecimal.valueOf(annualizedFundingRate);

        // The bid and the ask from this market summary websocket aren't reliably
        // upddated, better to use thee
        // top of the order book.
        Map<QuoteType, BigDecimal> quoteValues = new HashMap<>();
        quoteValues.put(QuoteType.LAST, new BigDecimal(lastPrice));
        quoteValues.put(QuoteType.MARK_PRICE, new BigDecimal(markPrice));
        quoteValues.put(QuoteType.OPEN_INTEREST, new BigDecimal(openInterest));
        quoteValues.put(QuoteType.VOLUME, new BigDecimal(volume24h));
        quoteValues.put(QuoteType.UNDERLYING_PRICE, new BigDecimal(underlyingPrice));
        quoteValues.put(QuoteType.FUNDING_RATE_APR, fundingRateApr);

        ILevel1Quote quote = new Level1Quote(ticker, convertToZonedDateTime(createdAtTimestamp), quoteValues);
        super.fireLevel1Quote(quote);
    }

    protected ZonedDateTime convertToZonedDateTime(long timestamp) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("GMT"));
    }

}
