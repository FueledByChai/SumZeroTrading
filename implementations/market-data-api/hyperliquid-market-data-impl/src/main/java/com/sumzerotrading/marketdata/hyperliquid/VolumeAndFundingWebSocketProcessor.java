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

public class VolumeAndFundingWebSocketProcessor implements IWebSocketProcessor {

    protected static final Logger logger = LoggerFactory.getLogger(VolumeAndFundingWebSocketProcessor.class);
    protected IWebSocketClosedListener listener;
    protected final List<IVolumeAndFundingWebsocketListener> listeners = new CopyOnWriteArrayList<>();
    protected Ticker ticker;

    // Thread pool for processing listener notifications asynchronously
    protected final ExecutorService listenerExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "VolumeAndFunding-listener-" + System.nanoTime());
            t.setDaemon(true);
            return t;
        }
    });

    public VolumeAndFundingWebSocketProcessor(Ticker ticker, IWebSocketClosedListener listener) {
        this.ticker = ticker;
        this.listener = listener;
    }

    public void add(IVolumeAndFundingWebsocketListener listener) {
        listeners.add(listener);
    }

    public void remove(IVolumeAndFundingWebsocketListener listener) {
        listeners.remove(listener);
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
            if (!jsonObject.has("channel") || !"activeAssetCtx".equals(jsonObject.getString("channel"))) {
                logger.debug("Ignoring non-activeAssetCtx message: {}", message);
                return;
            }

            JSONObject data = jsonObject.getJSONObject("data");
            String coin = data.getString("coin");
            JSONObject activeAssetCtx = data.getJSONObject("ctx");

            // Convert timestamp to ZonedDateTime
            ZonedDateTime timestamp = getTimestamp();

            BigDecimal volumeNotional = new BigDecimal(activeAssetCtx.getString("dayNtlVlm"));
            BigDecimal fundingRate = new BigDecimal(activeAssetCtx.getString("funding"));
            BigDecimal markPrice = new BigDecimal(activeAssetCtx.getString("markPx"));
            BigDecimal openInterest = new BigDecimal(activeAssetCtx.getString("openInterest"));
            BigDecimal volume = new BigDecimal(activeAssetCtx.getString("dayBaseVlm"));

            // Notify listeners asynchronously
            for (IVolumeAndFundingWebsocketListener volumeListener : listeners) {
                listenerExecutor.submit(() -> {
                    try {
                        volumeListener.volumeAndFundingUpdate(ticker, volume, volumeNotional, fundingRate, markPrice,
                                openInterest, timestamp);
                    } catch (Exception e) {
                        logger.error("Error notifying volume and funding listener: {}", e.getMessage(), e);
                    }
                });
            }

            logger.debug("Processed volume and funding update for {} at {}", coin, timestamp);

        } catch (Exception e) {
            logger.error("Error processing Hyperliquid volume and funding message: {}", message, e);
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

    protected ZonedDateTime getTimestamp() {
        return ZonedDateTime.now(ZoneId.of("UTC"));
    }
}
