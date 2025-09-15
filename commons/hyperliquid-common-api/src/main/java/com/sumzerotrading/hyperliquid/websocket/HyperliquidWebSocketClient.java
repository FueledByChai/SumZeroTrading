package com.sumzerotrading.hyperliquid.websocket;

import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.JsonObject;
import com.sumzerotrading.websocket.AbstractWebSocketClient;
import com.sumzerotrading.websocket.IWebSocketProcessor;

public class HyperliquidWebSocketClient extends AbstractWebSocketClient {

    protected String jwtToken = null;
    protected String coin = null;

    public HyperliquidWebSocketClient(String serverUri, String channel, String coin, IWebSocketProcessor processor)
            throws Exception {
        super(serverUri, channel, processor);
        this.coin = coin;
    }

    public HyperliquidWebSocketClient(String serverUri, String channel, String coin, IWebSocketProcessor processor,
            String jwtToken) throws Exception {
        this(serverUri, channel, coin, processor);
        this.jwtToken = jwtToken;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.info("Connected to Paradex WebSocket");

        if (jwtToken != null) {
            JsonObject authJson = new JsonObject();
            authJson.addProperty("jsonrpc", "2.0");
            authJson.addProperty("method", "auth");
            JsonObject params = new JsonObject();
            params.addProperty("bearer", jwtToken);
            authJson.add("params", params);
            authJson.addProperty("id", 0);

            logger.info("Authenticating with JWT token");
            send(authJson.toString());

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        JsonObject subscribeJson = new JsonObject();
        subscribeJson.addProperty("jsonrpc", "2.0");
        subscribeJson.addProperty("method", "subscribe");
        JsonObject subscription = new JsonObject();
        subscription.addProperty("type", channel);
        subscription.addProperty("coin", coin);
        subscribeJson.add("subscription", subscription);

        logger.info("Subscribing to channel: " + subscribeJson.toString());

        send(subscribeJson.toString());

    }

}