package com.sumzerotrading.hyperliquid.ws;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.JsonObject;
import com.sumzerotrading.websocket.AbstractWebSocketClient;
import com.sumzerotrading.websocket.IWebSocketProcessor;

public class HyperliquidWebSocketClient extends AbstractWebSocketClient {

    protected Map<String, String> params = null;

    private ScheduledExecutorService pingScheduler;
    private ScheduledFuture<?> pingTask;
    private static final long PING_INTERVAL_SECONDS = 30;

    public HyperliquidWebSocketClient(String serverUri, IWebSocketProcessor processor) throws Exception {
        this(serverUri, "", processor);
    }

    public HyperliquidWebSocketClient(String serverUri, String channel, IWebSocketProcessor processor)
            throws Exception {
        super(serverUri, channel, processor);
    }

    public HyperliquidWebSocketClient(String serverUri, String channel, Map<String, String> params,
            IWebSocketProcessor processor) throws Exception {
        super(serverUri, channel, processor);
        this.params = params;
    }

    public HyperliquidWebSocketClient(String serverUri, String channel, Map<String, String> params,
            IWebSocketProcessor processor, String jwtToken) throws Exception {
        this(serverUri, channel, params, processor);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.info("Connected to Hyperliquid WebSocket");
        startPingTask();

        if (channel != null && !channel.isEmpty()) {
            subscribeToChannel();
        }

    }

    protected void subscribeToChannel() {
        JsonObject subscribeJson = new JsonObject();
        subscribeJson.addProperty("jsonrpc", "2.0");
        subscribeJson.addProperty("method", "subscribe");
        JsonObject subscription = new JsonObject();
        subscription.addProperty("type", channel);
        if (params != null) {
            for (String key : params.keySet()) {
                subscription.addProperty(key, params.get(key));
            }
        }

        subscribeJson.add("subscription", subscription);

        logger.info("Subscribing to channel: " + subscribeJson.toString());

        send(subscribeJson.toString());
    }

    public void postMessage(String message) {
        logger.info("Sending POST message: " + message);
        send(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        super.onClose(code, reason, remote);
        stopPingScheduler();
    }

    @Override
    public void onError(Exception ex) {
        super.onError(ex);
        stopPingScheduler();
    }

    // Start a scheduled task to send a lightweight ping message every 30 seconds.
    // We use a text heartbeat here (JSON) to be compatible with servers that
    // don't rely on WebSocket-level ping frames. If you'd prefer a ping frame
    // instead, we can switch to using the library's sendPing API.

    protected void startPingTask() {
        pingScheduler = Executors.newSingleThreadScheduledExecutor(r ->

        {
            Thread t = new Thread(r, "hl-ping-thread");
            t.setDaemon(true);
            return t;
        });
        pingTask = pingScheduler.scheduleAtFixedRate(() -> {

            try {
                if (isOpen()) {
                    // Send a small JSON heartbeat. Adjust payload if the server expects a different
                    // format.
                    logger.info(channel + ": Sending ping");
                    send("{\"method\":\"ping\"}");
                } else {
                    // Connection closed, cancel the scheduled task to avoid unnecessary work.
                    if (pingTask != null && !pingTask.isCancelled()) {
                        pingTask.cancel(false);
                    }
                }
            } catch (Exception e) {
                logger.error("Error sending ping", e);
            }
        }, PING_INTERVAL_SECONDS, PING_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void stopPingScheduler() {
        try {
            if (pingTask != null && !pingTask.isCancelled()) {
                pingTask.cancel(false);
            }
            if (pingScheduler != null && !pingScheduler.isShutdown()) {
                pingScheduler.shutdownNow();
            }
        } catch (Exception e) {
            logger.warn("Error shutting down ping scheduler", e);
        } finally {
            pingTask = null;
            pingScheduler = null;
        }
    }

}