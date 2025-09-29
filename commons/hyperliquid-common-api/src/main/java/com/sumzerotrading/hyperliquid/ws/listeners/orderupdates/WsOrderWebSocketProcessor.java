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
        try {
            JSONObject jsonObject = new JSONObject(message);

            // Legacy message format handling
            if (!jsonObject.has("channel")) {
                return null;
            }
            String channel = jsonObject.getString("channel");

            if ("orderUpdates".equals(channel)) {
                List<WsOrderUpdate> orderUpdates = new ArrayList<>();
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray ordersArray = data.getJSONArray("orders");
                if (ordersArray.length() == 0) {
                    return null;
                }
                JSONObject orderJson = ordersArray.getJSONObject(0);

                WsOrderUpdate orderUpdate = parseOrderUpdate(orderJson);
                if (orderUpdate != null) {

                    orderUpdates.add(orderUpdate);
                    return orderUpdates;
                } else {
                    throw new SumZeroException("Error parsing order update");
                }
            } else {
                logger.warn("Unknown message type: " + channel);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error parsing message: " + message, e);
            return null;
        }
    }

    protected WsOrderUpdate parseOrderUpdate(JSONObject orderJson) {
        try {
            WsOrderUpdate orderUpdate = new WsOrderUpdate();
            orderUpdate.setStatus(orderJson.getString("status"));
            orderUpdate.setStatusTimestamp(orderJson.getLong("timestamp"));

            JSONObject order = orderJson.getJSONObject("order");

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
            logger.error("Error parsing order update: " + orderJson, e);
            throw new SumZeroException("Error parsing order update", e);
        }
    }

}
