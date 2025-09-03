package com.sumzerotrading.marketdata.paradex;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.paradex.common.api.IWebSocketClosedListener;
import com.sumzerotrading.paradex.common.api.IWebSocketProcessor;

public class MarketsSummaryWebSocketProcessor implements IWebSocketProcessor {

    protected static final Logger logger = LoggerFactory.getLogger(MarketsSummaryWebSocketProcessor.class);
    protected IWebSocketClosedListener closedListener;
    protected List<MarketsSummaryUpdateListener> listeners = new ArrayList<>();

    // Thread pool for handling trade notifications
    private final ExecutorService tradeExecutor;

    // Custom ThreadFactory for naming trade processing threads
    private static class TradeThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix = "trade-processor-";

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }

    public MarketsSummaryWebSocketProcessor(IWebSocketClosedListener closedListener) {
        this.closedListener = closedListener;
        // Initialize thread pool with 4 threads for trade processing
        this.tradeExecutor = Executors.newFixedThreadPool(4, new TradeThreadFactory());
        logger.info("Initialized MarketsSummaryWebSocketProcessor with 4 threads for trade processing");
    }

    public void addMarketsSummaryUpdateListener(MarketsSummaryUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeMarketsSummaryUpdateListener(MarketsSummaryUpdateListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void connectionClosed(int code, String reason, boolean remote) {
        logger.info("Disconnected from Paradex WebSocket: " + reason);
        closedListener.connectionClosed();

    }

    @Override
    public void connectionError(Exception error) {
        logger.error(error.getMessage(), error);
        closedListener.connectionClosed();
    }

    @Override
    public void connectionEstablished() {
        logger.info("Connected to Paradex Trades WebSocket");
    }

    @Override
    public void connectionOpened() {
        logger.info("Opened connection to Paradex Trades WebSocket");

    }

    @Override
    public void messageReceived(String message) {
        logger.debug("Trades message received: " + message);
        try {
            JSONObject jsonObject = new JSONObject(message);
            if (!jsonObject.has("method")) {
                return;
            }
            String method = jsonObject.getString("method");

            if ("subscription".equals(method)) {
                JSONObject params = jsonObject.getJSONObject("params");
                JSONObject data = params.getJSONObject("data");
                String ask = data.getString("ask");
                String bid = data.getString("bid");
                long createdAt = data.getLong("created_at");
                String fundingRate = data.getString("funding_rate");
                String lastPrice = data.getString("last_traded_price");
                String markPrice = data.getString("mark_price");
                String symbol = data.getString("symbol");
                String underlyingPrice = data.getString("underlying_price");
                String volume24h = data.getString("volume_24h");
                String openInterest = data.getString("open_interest");

                for (MarketsSummaryUpdateListener listener : listeners) {
                    // Submit trade notification to thread pool to avoid blocking the WebSocket
                    // thread
                    tradeExecutor.submit(() -> {
                        try {
                            listener.newSummaryUpdate(createdAt, symbol, bid, ask, lastPrice, markPrice, openInterest,
                                    volume24h, underlyingPrice, fundingRate);
                        } catch (Exception ex) {
                            logger.warn("Error processing trade notification for listener", ex);
                        }
                    });
                }
            } else {
                logger.warn("Unknown message type: " + method);
            }
        } catch (Exception e) {
            logger.error("Error processing message: " + message, e);
            logger.error(e.getMessage(), e);
        }

    }

    /**
     * Shuts down the trade processing thread pool. Should be called when the
     * processor is no longer needed to prevent resource leaks.
     */
    public void shutdown() {
        if (tradeExecutor != null && !tradeExecutor.isShutdown()) {
            logger.info("Shutting down trade processing thread pool");
            tradeExecutor.shutdown();
        }
    }

    /**
     * Immediately shuts down the trade processing thread pool. Should be called in
     * emergency situations to force immediate shutdown.
     */
    public void shutdownNow() {
        if (tradeExecutor != null && !tradeExecutor.isShutdown()) {
            logger.info("Force shutting down trade processing thread pool");
            tradeExecutor.shutdownNow();
        }
    }

    /**
     * Checks if the trade processing thread pool has been shut down
     * 
     * @return true if the thread pool has been shut down, false otherwise
     */
    public boolean isShutdown() {
        return tradeExecutor == null || tradeExecutor.isShutdown();
    }

}
