package com.sumzerotrading.marketdata.hyperliquid;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.ws.HyperliquidConfiguration;
import com.sumzerotrading.hyperliquid.ws.HyperliquidWebSocketClient;
import com.sumzerotrading.hyperliquid.ws.HyperliquidWebSocketClientBuilder;
import com.sumzerotrading.marketdata.IOrderBook;
import com.sumzerotrading.marketdata.Level1Quote;
import com.sumzerotrading.marketdata.Level1QuoteListener;
import com.sumzerotrading.marketdata.Level2Quote;
import com.sumzerotrading.marketdata.Level2QuoteListener;
import com.sumzerotrading.marketdata.OrderBookUpdateListener;
import com.sumzerotrading.marketdata.OrderFlow;
import com.sumzerotrading.marketdata.OrderFlowListener;
import com.sumzerotrading.marketdata.QuoteEngine;
import com.sumzerotrading.marketdata.QuoteType;

public class HyperliquidQuoteEngine extends QuoteEngine implements OrderBookUpdateListener, IBBOWebSocketListener,
        IVolumeAndFundingWebsocketListener, IOrderflowUpdateListener {

    protected static Logger logger = LoggerFactory.getLogger(HyperliquidQuoteEngine.class);

    public static final String SLEEP_TIME_PROPERTY_KEY = "sleep.time.in.seconds";
    public static final String INCLUDE_FUNDING_RATE_PROPERTY_KEY = "include.funding.rates";
    public static final String FUNDING_RATE_UPDATE_INTERVAL_PROPERTY_KEY = "funding.rate.update.interval.seconds";

    protected volatile boolean started = false;
    protected boolean threadCompleted = false;

    protected int sleepTimeMS = 500;
    protected int fundingRateUpdateIntervalSeconds = 5;
    protected ArrayList<String> urlStrings = new ArrayList<>();

    protected String wsUrl;
    protected boolean includeFundingRate = true;

    public HyperliquidQuoteEngine() {
        wsUrl = HyperliquidConfiguration.getInstance().getWebSocketUrl();
    }

    @Override
    public Date getServerTime() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isConnected() {
        return started;
    }

    @Override
    public void startEngine() {
        if (threadCompleted) {
            throw new IllegalStateException("Quote Engine was already stopped");
        }
        logger.info("starting engine with " + sleepTimeMS + " ms interval");
        started = true;

    }

    @Override
    public void startEngine(Properties props) {
        startEngine();
    }

    @Override
    public boolean started() {
        return started;
    }

    @Override
    public void stopEngine() {
        started = false;
        // stopFundingRateUpdates();
    }

    @Override
    public void useDelayedData(boolean useDelayed) {
        logger.error("useDelayedData() Not supported for hyperliquid market data");
    }

    @Override
    public void subscribeLevel1(Ticker ticker, Level1QuoteListener listener) {
        if (!super.level1ListenerMap.containsKey(ticker)) {
            startBBOWSClient(ticker);
            startVolumeAndFundingWSClient(ticker);
        }
        super.subscribeLevel1(ticker, listener);
    }

    @Override
    public void unsubscribeLevel1(Ticker ticker, Level1QuoteListener listener) {
        super.unsubscribeLevel1(ticker, listener);
    }

    @Override
    public void subscribeMarketDepth(Ticker ticker, Level2QuoteListener listener) {
        super.subscribeMarketDepth(ticker, listener);
        OrderBookRegistry.getInstance().getOrderBook(ticker).addOrderBookUpdateListener(this);
    }

    @Override
    public void unsubscribeMarketDepth(Ticker ticker, Level2QuoteListener listener) {
        // TODO Auto-generated method stub
        super.unsubscribeMarketDepth(ticker, listener);
    }

    @Override
    public void subscribeOrderFlow(Ticker ticker, OrderFlowListener listener) {
        if (!super.orderFlowListenerMap.containsKey(ticker)) {
            // No order flow client yet for this ticker, so create one.
            startTradesWSClient(ticker);
        }
        super.subscribeOrderFlow(ticker, listener);
    }

    @Override
    public void unsubscribeOrderFlow(Ticker ticker, OrderFlowListener listener) {
        super.unsubscribeOrderFlow(ticker, listener);
    }

    @Override
    public void bestAskUpdated(Ticker ticker, BigDecimal bestAsk, Double askSize, ZonedDateTime timeStamp) {
        Level1Quote quote = new Level1Quote(ticker, timeStamp);
        quote.addQuote(QuoteType.ASK, ticker.formatPrice(bestAsk));
        quote.addQuote(QuoteType.ASK_SIZE, BigDecimal.valueOf(askSize));
        super.fireLevel1Quote(quote);

    }

    @Override
    public void bestBidUpdated(Ticker ticker, BigDecimal bestBid, Double bidSize, ZonedDateTime timeStamp) {
        Level1Quote quote = new Level1Quote(ticker, timeStamp);
        quote.addQuote(QuoteType.BID, ticker.formatPrice(bestBid));
        quote.addQuote(QuoteType.BID_SIZE, BigDecimal.valueOf(bidSize));
        super.fireLevel1Quote(quote);
    }

    @Override
    public void orderBookImbalanceUpdated(Ticker ticker, BigDecimal imbalance, ZonedDateTime timeStamp) {
        Level1Quote quote = new Level1Quote(ticker, timeStamp);
        quote.addQuote(QuoteType.ORDER_BOOK_IMBALANCE, ticker.formatPrice(imbalance));
        super.fireLevel1Quote(quote);
    }

    @Override
    public void onBBOUpdate(Ticker ticker, BigDecimal bestBid, Double bidSize, BigDecimal bestAsk, Double askSize,
            ZonedDateTime timeStamp) {
        Level1Quote quote = new Level1Quote(ticker, timeStamp);
        if (bestBid != null) {
            quote.addQuote(QuoteType.BID, bestBid);
            quote.addQuote(QuoteType.BID_SIZE, BigDecimal.valueOf(bidSize));
        }
        if (bestAsk != null) {
            quote.addQuote(QuoteType.ASK, bestAsk);
            quote.addQuote(QuoteType.ASK_SIZE, BigDecimal.valueOf(askSize));
        }
        super.fireLevel1Quote(quote);
    }

    @Override
    public void volumeAndFundingUpdate(Ticker ticker, BigDecimal volume, BigDecimal volumeNotional,
            BigDecimal fundingRate, BigDecimal markPrice, BigDecimal openInterest, ZonedDateTime timestamp) {
        Level1Quote quote = new Level1Quote(ticker, timestamp);
        quote.addQuote(QuoteType.VOLUME_NOTIONAL, volumeNotional);
        quote.addQuote(QuoteType.VOLUME, volume);
        quote.addQuote(QuoteType.FUNDING_RATE_APR, fundingRate.multiply(new BigDecimal(100.0 * 24 * 365)));
        quote.addQuote(QuoteType.FUNDING_RATE_HOURLY_BPS, fundingRate.multiply(new BigDecimal(10000.0)));
        quote.addQuote(QuoteType.MARK_PRICE, markPrice);
        quote.addQuote(QuoteType.OPEN_INTEREST, openInterest);
        quote.addQuote(QuoteType.OPEN_INTEREST_NOTIONAL, openInterest.multiply(markPrice));
        super.fireLevel1Quote(quote);
    }

    @Override
    public void onOrderflowUpdate(OrderFlow orderFlow) {
        super.fireOrderFlow(orderFlow);
    }

    @Override
    public void orderBookUpdated(Ticker ticker, IOrderBook book, ZonedDateTime timeStamp) {
        Level2Quote quote = new Level2Quote(ticker, book, timeStamp);
        super.fireMarketDepthQuote(quote);
    }

    protected void startBBOWSClient(Ticker ticker) {
        try {
            logger.info("Starting BBO WebSocket client");
            BBOWebSocketProcessor processor = new BBOWebSocketProcessor(ticker, () -> {
                logger.info("BBO WebSocket closed, trying to restart...");
                startBBOWSClient(ticker);
            });
            processor.addBBOListener(this);
            HyperliquidWebSocketClient bboWSClient = HyperliquidWebSocketClientBuilder.buildBBOClient(wsUrl,
                    ticker.getSymbol(), processor);
            bboWSClient.connect();

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    protected void startVolumeAndFundingWSClient(Ticker ticker) {
        try {
            logger.info("Starting Volume and Funding WebSocket client");
            VolumeAndFundingWebSocketProcessor processor = new VolumeAndFundingWebSocketProcessor(ticker, () -> {
                logger.info("Volume and Funding WebSocket closed, trying to restart...");
                startVolumeAndFundingWSClient(ticker);
            });
            processor.add(this);
            HyperliquidWebSocketClient volumeAndFundingWSClient = HyperliquidWebSocketClientBuilder
                    .buildActiveAssetCtxClient(wsUrl, ticker.getSymbol(), processor);
            volumeAndFundingWSClient.connect();

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    protected void startTradesWSClient(Ticker ticker) {
        try {
            logger.info("Starting Trades WebSocket client");
            OrderFlowWebSocketProcessor processor = new OrderFlowWebSocketProcessor(ticker, () -> {
                logger.info("Trades WebSocket closed, trying to restart...");
                startTradesWSClient(ticker);
            });
            processor.addOrderFlowListener(this);
            HyperliquidWebSocketClient tradesWSClient = HyperliquidWebSocketClientBuilder.buildTradesClient(wsUrl,
                    ticker.getSymbol(), processor);
            tradesWSClient.connect();

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

}
