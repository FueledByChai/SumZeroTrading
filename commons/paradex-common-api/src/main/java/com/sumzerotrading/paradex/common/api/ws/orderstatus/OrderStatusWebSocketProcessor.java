package com.sumzerotrading.paradex.common.api.ws.orderstatus;

import java.math.BigDecimal;

import org.json.JSONObject;

import com.sumzerotrading.websocket.AbstractWebSocketProcessor;
import com.sumzerotrading.websocket.IWebSocketClosedListener;

public class OrderStatusWebSocketProcessor extends AbstractWebSocketProcessor<IParadexOrderStatusUpdate> {

    public OrderStatusWebSocketProcessor(IWebSocketClosedListener closedListener) {
        super(closedListener);
    }

    @Override
    protected IParadexOrderStatusUpdate parseMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            if (!jsonObject.has("method")) {
                return null;
            }
            String method = jsonObject.getString("method");

            if ("subscription".equals(method)) {
                JSONObject params = jsonObject.getJSONObject("params");
                JSONObject data = params.getJSONObject("data");
                String orderId = data.getString("id");
                String remainingSizeStr = data.getString("remaining_size");
                String status = data.getString("status");
                String originalSizeStr = data.getString("size");
                String cancelReason = data.getString("cancel_reason");
                String orderType = data.getString("type");
                String averageFillPriceStr = data.getString("avg_fill_price");
                long timestamp = data.getLong("timestamp");
                String side = data.getString("side");
                String tickerString = data.getString("market");
                if (averageFillPriceStr.equals("")) {
                    averageFillPriceStr = "0";
                }

                ParadoxOrderStatusUpdate orderStatus = new ParadoxOrderStatusUpdate(tickerString, orderId,
                        new BigDecimal(remainingSizeStr), new BigDecimal(originalSizeStr), status, cancelReason,
                        new BigDecimal(averageFillPriceStr), orderType, side, timestamp);
                return orderStatus;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("Error parsing message: " + message, e);
            return null;
        }
    }

}
