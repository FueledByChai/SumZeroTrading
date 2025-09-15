package com.sumzerotrading.marketdata.hyperliquid;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sumzerotrading.marketdata.IOrderBook;
import com.sumzerotrading.websocket.IWebSocketClosedListener;
import com.sumzerotrading.websocket.IWebSocketProcessor;

public class MarketBookWebSocketProcessor implements IWebSocketProcessor {

    protected static final Logger logger = LoggerFactory.getLogger(MarketBookWebSocketProcessor.class);
    protected final IOrderBook orderBook;
    protected IWebSocketClosedListener listener;

    public MarketBookWebSocketProcessor(IOrderBook orderBook, IWebSocketClosedListener listener) {
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
                long timestampMillis = data.getLong("last_updated_at");
                ZonedDateTime timestamp = ZonedDateTime.now(java.time.ZoneId.of("GMT")); // Default to now if not
                                                                                         // provided

                if (timestampMillis > 0) {
                    timestamp = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestampMillis),
                            java.time.ZoneId.of("GMT"));
                }

            } else {
                logger.warn("Unknown message type: " + method);
            }
        } catch (Exception e) {
            logger.error("Error processing message: " + message, e);
            logger.error(e.getMessage(), e);
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class OrderBookResponse {
        private List<List<Map<String, String>>> levels;

        public List<List<Map<String, String>>> getLevels() {
            return levels;
        }

        public void setLevels(List<List<Map<String, String>>> levels) {
            this.levels = levels;
        }
    }

}
