package com.sumzerotrading.marketdata.paradex;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.paradex.common.api.IWebSocketClosedListener;
import com.sumzerotrading.paradex.common.api.IWebSocketProcessor;

public class MarketBookWebSocketProcessor implements IWebSocketProcessor {

    protected static final Logger logger = LoggerFactory.getLogger(MarketBookWebSocketProcessor.class);
    protected final IParadexOrderBook orderBook;
    protected IWebSocketClosedListener listener;

    public MarketBookWebSocketProcessor(IParadexOrderBook orderBook, IWebSocketClosedListener listener) {
        this.orderBook = orderBook;
        this.listener = listener;
    }

    @Override
    public void connectionClosed(int code, String reason, boolean remote) {
        logger.info("Disconnected from Paradex WebSocket: " + reason);
        listener.connectionClosed();

    }

    @Override
    public void connectionError(Exception error) {
        logger.error(error.getMessage(), error);
        listener.connectionClosed();
    }

    @Override
    public void connectionEstablished() {
        // TODO Auto-generated method stub

    }

    @Override
    public void connectionOpened() {
        // TODO Auto-generated method stub

    }

    @Override
    public void messageReceived(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            if (!jsonObject.has("method")) {
                return;
            }
            String method = jsonObject.getString("method");

            if ("subscription".equals(method)) {
                JSONObject params = jsonObject.getJSONObject("params");
                JSONObject data = params.getJSONObject("data");
                String updateType = data.getString("update_type");

                if ("s".equals(updateType)) {
                    orderBook.handleSnapshot(data.toMap());
                } else {
                    orderBook.applyDelta(data.toMap());
                }
            } else {
                logger.warn("Unknown message type: " + method);
            }
        } catch (Exception e) {
            logger.error("Error processing message: " + message, e);
            logger.error(e.getMessage(), e);
        }

    }

}
