package com.sumzerotrading.marketdata.hyperliquid;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.marketdata.IOrderBook;
import com.sumzerotrading.marketdata.OrderBook.PriceLevel;
import com.sumzerotrading.websocket.IWebSocketClosedListener;
import com.sumzerotrading.websocket.IWebSocketProcessor;

public class OrderBookWebSocketProcessor implements IWebSocketProcessor {

    protected static final Logger logger = LoggerFactory.getLogger(OrderBookWebSocketProcessor.class);
    protected final IOrderBook orderBook;
    protected IWebSocketClosedListener listener;

    // Thread pool for processing order book updates asynchronously
    protected final ExecutorService orderBookUpdateExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "OrderBook-update-" + System.nanoTime());
            t.setDaemon(true);
            return t;
        }
    });

    public OrderBookWebSocketProcessor(IOrderBook orderBook, IWebSocketClosedListener listener) {
        this.orderBook = orderBook;
        this.listener = listener;
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

    /**
     * Get the order book being updated by this processor. This provides thread-safe
     * access to the latest order book state.
     * 
     * @return The order book instance
     */
    public IOrderBook getOrderBook() {
        return orderBook;
    }

    /**
     * Check if the order book has been initialized with at least one snapshot.
     * 
     * @return true if order book is initialized, false otherwise
     */
    public boolean isOrderBookInitialized() {
        return orderBook.isInitialized();
    }

    @Override
    public void messageReceived(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);

            // Check if this is an L2 book update
            if (!jsonObject.has("channel") || !"l2Book".equals(jsonObject.getString("channel"))) {
                logger.debug("Ignoring non-l2Book message: {}", message);
                return;
            }

            JSONObject data = jsonObject.getJSONObject("data");
            String coin = data.getString("coin");
            long timestampMillis = data.getLong("time");
            JSONArray levelsArray = data.getJSONArray("levels");

            // Convert timestamp to ZonedDateTime
            ZonedDateTime timestamp = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestampMillis),
                    ZoneId.of("GMT"));

            // Parse the levels - first array is bids, second is asks
            List<PriceLevel> bids = new ArrayList<>();
            List<PriceLevel> asks = new ArrayList<>();

            if (levelsArray.length() >= 1) {
                JSONArray bidsArray = levelsArray.getJSONArray(0);
                for (int i = 0; i < bidsArray.length(); i++) {
                    JSONObject bidLevel = bidsArray.getJSONObject(i);
                    BigDecimal price = new BigDecimal(bidLevel.getString("px"));
                    Double size = Double.parseDouble(bidLevel.getString("sz"));
                    bids.add(new PriceLevel(price, size));
                }
            }

            if (levelsArray.length() >= 2) {
                JSONArray asksArray = levelsArray.getJSONArray(1);
                for (int i = 0; i < asksArray.length(); i++) {
                    JSONObject askLevel = asksArray.getJSONObject(i);
                    BigDecimal price = new BigDecimal(askLevel.getString("px"));
                    Double size = Double.parseDouble(askLevel.getString("sz"));
                    asks.add(new PriceLevel(price, size));
                }
            }

            // Update the order book atomically using the new snapshot method in a separate
            // thread
            orderBookUpdateExecutor.submit(() -> {
                try {
                    orderBook.updateFromSnapshot(bids, asks, timestamp);
                    logger.debug("Updated order book for {} with {} bids and {} asks at {}", coin, bids.size(),
                            asks.size(), timestamp);
                } catch (Exception e) {
                    logger.error("Error updating order book for {}: {}", coin, e.getMessage(), e);
                }
            });

        } catch (Exception e) {
            logger.error("Error processing Hyperliquid L2 book message: {}", message, e);
        }
    }

    /**
     * Shutdown the executor service and cleanup resources. This should be called
     * when the processor is no longer needed to prevent resource leaks.
     */
    public void shutdown() {
        if (orderBookUpdateExecutor != null && !orderBookUpdateExecutor.isShutdown()) {
            orderBookUpdateExecutor.shutdown();
            try {
                // Wait for existing tasks to complete
                if (!orderBookUpdateExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    // Force shutdown if tasks don't complete within timeout
                    orderBookUpdateExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                // Force shutdown if interrupted
                orderBookUpdateExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
