package com.sumzerotrading.broker.hyperliquid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.websocket.IWebSocketClosedListener;
import com.sumzerotrading.websocket.IWebSocketProcessor;

public class OrderStatusWebSocketProcessor implements IWebSocketProcessor {

    protected static final Logger logger = LoggerFactory.getLogger(OrderStatusWebSocketProcessor.class);
    protected List<ParadexOrderStatusListener> listeners = new ArrayList<>();
    protected IWebSocketClosedListener closedListener;

    public OrderStatusWebSocketProcessor(IWebSocketClosedListener closedListener) {
        this.closedListener = closedListener;
    }

    public OrderStatusWebSocketProcessor(ParadexOrderStatusListener listener, IWebSocketClosedListener closedListener) {
        listeners.add(listener);
        this.closedListener = closedListener;
    }

    public void addListener(ParadexOrderStatusListener listener) {
        listeners.add(listener);
    }

    @Override
    public void connectionClosed(int code, String reason, boolean remote) {
        logger.error(
                "Disconnected from Paradex WebSocket: code: " + code + " reason: " + reason + " remote: " + remote);
        closedListener.connectionClosed();

    }

    @Override
    public void connectionError(Exception error) {
        logger.error(error.getMessage(), error);
        closedListener.connectionClosed();

    }

    @Override
    public void connectionEstablished() {
        logger.info("Connection Established to Paradex Order Status WebSocket");

    }

    @Override
    public void connectionOpened() {
        logger.info("Connection opened to Paradex Order Status WebSocket");

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

                for (ParadexOrderStatusListener listener : listeners) {
                    Thread thread = new Thread(() -> {
                        try {
                            listener.orderStatusUpdated(orderStatus);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }

                    }, "OrderStatusWebSocketThread");
                    thread.start();
                }

            } else {
                logger.warn("Unknown message type: " + method);
            }
        } catch (Exception e) {
            logger.error("Error processing message: " + message, e);
        }

    }

}
