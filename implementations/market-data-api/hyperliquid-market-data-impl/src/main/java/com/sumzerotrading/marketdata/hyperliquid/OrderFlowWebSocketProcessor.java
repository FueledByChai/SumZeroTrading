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
import com.sumzerotrading.marketdata.OrderFlow;
import com.sumzerotrading.marketdata.OrderFlow.Side;
import com.sumzerotrading.websocket.IWebSocketClosedListener;
import com.sumzerotrading.websocket.IWebSocketProcessor;

public class OrderFlowWebSocketProcessor implements IWebSocketProcessor {

    protected static final Logger logger = LoggerFactory.getLogger(OrderFlowWebSocketProcessor.class);
    protected IWebSocketClosedListener listener;
    protected final List<IOrderflowUpdateListener> orderFlowListeners = new CopyOnWriteArrayList<>();
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

    public OrderFlowWebSocketProcessor(Ticker ticker, IWebSocketClosedListener listener) {
        this.ticker = ticker;
        this.listener = listener;
    }

    public void addOrderFlowListener(IOrderflowUpdateListener orderFlowListener) {
        orderFlowListeners.add(orderFlowListener);
    }

    public void removeOrderFlowListener(IOrderflowUpdateListener orderFlowListener) {
        orderFlowListeners.remove(orderFlowListener);
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
            if (!jsonObject.has("channel") || !"trades".equals(jsonObject.getString("channel"))) {
                logger.debug("Ignoring non-trades message: {}", message);
                return;
            }

            JSONArray data = jsonObject.getJSONArray("data");
            JSONObject tradeData = data.getJSONObject(0);
            String coin = tradeData.getString("coin");
            long timestampMillis = tradeData.getLong("time");
            String side = tradeData.getString("side");
            Side orderSide = "B".equalsIgnoreCase(side) ? Side.BUY : Side.SELL;
            BigDecimal price = ticker.formatPrice(tradeData.getString("px"));
            BigDecimal size = new BigDecimal(tradeData.getString("sz"));

            // Convert timestamp to ZonedDateTime
            ZonedDateTime timestamp = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestampMillis),
                    ZoneId.of("UTC"));

            // Notify listeners asynchronously
            OrderFlow orderFlow = new OrderFlow(ticker, price, size, orderSide, timestamp);
            for (IOrderflowUpdateListener orderFlowListener : orderFlowListeners) {
                listenerExecutor.submit(() -> {
                    try {
                        orderFlowListener.onOrderflowUpdate(orderFlow);
                    } catch (Exception e) {
                        logger.error("Error notifying OrderFlow listener: {}", e.getMessage(), e);
                    }
                });
            }

            logger.debug("Processed OrderFlow update for {} at {}", coin, timestamp);

        } catch (Exception e) {
            logger.error("Error processing Hyperliquid OrderFlow message: {}", message, e);
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
