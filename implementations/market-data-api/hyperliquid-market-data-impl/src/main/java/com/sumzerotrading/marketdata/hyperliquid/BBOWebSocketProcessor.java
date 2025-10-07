package com.sumzerotrading.marketdata.hyperliquid;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.websocket.IWebSocketClosedListener;
import com.sumzerotrading.websocket.IWebSocketProcessor;

public class BBOWebSocketProcessor implements IWebSocketProcessor {

    protected static final Logger logger = LoggerFactory.getLogger(BBOWebSocketProcessor.class);
    protected IWebSocketClosedListener listener;
    protected final List<IBBOWebSocketListener> bboListeners = new CopyOnWriteArrayList<>();
    protected Ticker ticker;

    // Thread pool for processing listener notifications asynchronously
    protected final ExecutorService listenerExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "BBO-listener-" + System.nanoTime());
            t.setDaemon(true);
            return t;
        }
    });

    public BBOWebSocketProcessor(Ticker ticker, IWebSocketClosedListener listener) {
        this.ticker = ticker;
        this.listener = listener;
    }

    public void addBBOListener(IBBOWebSocketListener bboListener) {
        bboListeners.add(bboListener);
    }

    public void removeBBOListener(IBBOWebSocketListener bboListener) {
        bboListeners.remove(bboListener);
    }

    @Override
    public void connectionClosed(int code, String reason, boolean remote) {
        logger.info("Disconnected from Hyperliquid WebSocket: {} (code: {}, remote: {})", reason, code, remote);
        listener.connectionClosed();
    }

    @Override
    public void connectionError(Exception error) {
        logger.error("Hyperliquid WebSocket connection error: {}", error.getMessage(), error);
        listener.connectionClosed();
    }

    @Override
    public void connectionEstablished() {
        logger.info("Hyperliquid WebSocket connection established");
    }

    @Override
    public void connectionOpened() {
        logger.info("Hyperliquid WebSocket connection opened");
    }

    @Override
    public void messageReceived(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);

            // Check if this is an L2 book update
            if (!jsonObject.has("channel") || !"bbo".equals(jsonObject.getString("channel"))) {
                logger.debug("Ignoring non-bbo message: {}", message);
                return;
            }

            JSONObject data = jsonObject.getJSONObject("data");
            String coin = data.getString("coin");
            long timestampMillis = data.getLong("time");
            JSONArray bboArray = data.getJSONArray("bbo");

            // Convert timestamp to ZonedDateTime
            ZonedDateTime timestamp = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestampMillis),
                    ZoneId.of("UTC"));

            // Parse the levels - first array is bids, second is asks
            BigDecimal bidPrice = null;
            BigDecimal askPrice = null;
            Double bidSize = null;
            Double askSize = null;

            if (bboArray.length() >= 1) {
                JSONObject bid = bboArray.getJSONObject(0);
                bidPrice = new BigDecimal(bid.getString("px"));
                bidSize = Double.parseDouble(bid.getString("sz"));
            }

            if (bboArray.length() >= 2) {
                JSONObject ask = bboArray.getJSONObject(1);
                askPrice = new BigDecimal(ask.getString("px"));
                askSize = Double.parseDouble(ask.getString("sz"));
            }

            // Make final copies for lambda usage
            final BigDecimal finalBidPrice = bidPrice;
            final BigDecimal finalAskPrice = askPrice;
            final Double finalBidSize = bidSize;
            final Double finalAskSize = askSize;

            // Notify listeners asynchronously
            for (IBBOWebSocketListener bboListener : bboListeners) {
                listenerExecutor.submit(() -> {
                    try {
                        bboListener.onBBOUpdate(ticker, finalBidPrice, finalBidSize, finalAskPrice, finalAskSize,
                                timestamp);
                    } catch (Exception e) {
                        logger.error("Error notifying BBO listener: {}", e.getMessage(), e);
                    }
                });
            }

            logger.debug("Processed BBO update for {} at {}", coin, timestamp);

        } catch (Exception e) {
            logger.error("Error processing Hyperliquid BBO message: {}", message, e);
        }
    }

    /**
     * Shutdown the executor service and cleanup resources. This should be called
     * when the processor is no longer needed to prevent resource leaks.
     */
    public void shutdown() {
        if (listenerExecutor != null && !listenerExecutor.isShutdown()) {
            listenerExecutor.shutdown();
            try {
                // Wait for existing tasks to complete
                if (!listenerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    // Force shutdown if tasks don't complete within timeout
                    listenerExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                // Force shutdown if interrupted
                listenerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
