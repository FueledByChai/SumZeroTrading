package com.sumzerotrading.paradex.common.api;

import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ParadexWebSocketClient extends AbstractWebSocketClient {

    protected String jwtToken = null;

    public ParadexWebSocketClient(String serverUri, String channel, IWebSocketProcessor processor) throws Exception {
        super(serverUri, channel, processor);
    }

    public ParadexWebSocketClient(String serverUri, String channel, IWebSocketProcessor processor, String jwtToken)
            throws Exception {
        this(serverUri, channel, processor);
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
        JsonElement params = new JsonObject();
        params.getAsJsonObject().addProperty("channel", channel);
        subscribeJson.add("params", params);
        subscribeJson.addProperty("id", 1);

        logger.info("Subscribing to channel: " + subscribeJson.toString());

        send(subscribeJson.toString());

    }

}