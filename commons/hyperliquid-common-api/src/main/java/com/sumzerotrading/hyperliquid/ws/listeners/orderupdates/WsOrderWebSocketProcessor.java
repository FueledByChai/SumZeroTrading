package com.sumzerotrading.hyperliquid.ws.listeners.orderupdates;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.websocket.AbstractWebSocketProcessor;
import com.sumzerotrading.websocket.IWebSocketClosedListener;

public class WsOrderWebSocketProcessor extends AbstractWebSocketProcessor<List<WsOrderUpdate>> {

    private static final Logger logger = LoggerFactory.getLogger(WsOrderWebSocketProcessor.class);

    public WsOrderWebSocketProcessor(IWebSocketClosedListener listener) {
        super(listener);
    }

    @Override
    protected List<WsOrderUpdate> parseMessage(String message) {
        logger.info("Received order update message: {}", message);
        try {
            JSONObject jsonObject = new JSONObject(message);

            if (!jsonObject.has("channel")) {
                return null;
            }

            String channel = jsonObject.getString("channel");

            if ("orderUpdates".equals(channel)) {
                List<WsOrderUpdate> orderUpdates = new ArrayList<>();
                JSONArray dataArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject data = dataArray.getJSONObject(i);
                    WsOrderUpdate orderUpdate = parseOrderUpdate(data);
                    orderUpdates.add(orderUpdate);
                }
                return orderUpdates;
            } else {
                logger.warn("Unknown message type: " + channel);
                logger.warn(message);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error parsing message: " + message, e);
            return null;
        }
    }

    protected WsOrderUpdate parseOrderUpdate(JSONObject data) {
        try {
            WsOrderUpdate orderUpdate = new WsOrderUpdate();
            orderUpdate.setStatus(data.getString("status"));
            orderUpdate.setStatusTimestamp(data.getLong("statusTimestamp"));

            JSONObject order = data.getJSONObject("order");
            orderUpdate.setCoin(order.getString("coin"));
            orderUpdate.setSide(order.getString("side"));
            orderUpdate.setLimitPrice(order.getString("limitPx"));
            orderUpdate.setSize(order.getString("sz"));
            orderUpdate.setOrderId(order.getLong("oid"));
            orderUpdate.setOrderTimestamp(order.getLong("timestamp"));
            orderUpdate.setOriginalSize(order.getString("origSz"));
            orderUpdate.setClientOrderId(order.optString("cloid", null));
            return orderUpdate;
        } catch (Exception e) {
            logger.error("Error parsing order update: " + data, e);
            throw new SumZeroException("Error parsing order update", e);
        }
    }

}
