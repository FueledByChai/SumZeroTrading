package com.sumzerotrading.hyperliquid.ws.listeners.userfills;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.websocket.AbstractWebSocketProcessor;
import com.sumzerotrading.websocket.IWebSocketClosedListener;

public class WsUserFillsWebSocketProcessor extends AbstractWebSocketProcessor<WsUserFill> {

    private static final Logger logger = LoggerFactory.getLogger(WsUserFillsWebSocketProcessor.class);

    public WsUserFillsWebSocketProcessor(IWebSocketClosedListener listener) {
        super(listener);
    }

    @Override
    protected WsUserFill parseMessage(String message) {
        logger.info("Received user fill message: {}", message);
        try {
            JSONObject jsonObject = new JSONObject(message);

            if (!jsonObject.has("channel")) {
                return null;
            }

            String channel = jsonObject.getString("channel");

            if (!"userFills".equals(channel)) {
                logger.warn("Unknown message type: " + channel);
                logger.warn(message);
                return null;
            }

            WsUserFill userFill = new WsUserFill();
            if (jsonObject.has("isSnapshot")) {
                userFill.setSnapshot(jsonObject.getBoolean("isSnapshot"));
            }
            userFill.setUser(jsonObject.optString("user", null));

            JSONArray fillsArray = jsonObject.optJSONArray("fills");
            if (fillsArray != null) {
                for (int i = 0; i < fillsArray.length(); i++) {
                    JSONObject fillObj = fillsArray.getJSONObject(i);
                    WsFill fill = new WsFill();
                    fill.setCoin(fillObj.optString("coin", null));
                    fill.setPrice(fillObj.optString("px", null));
                    fill.setSize(fillObj.optString("sz", null));
                    fill.setSide(fillObj.optString("side", null));
                    fill.setTime(fillObj.optLong("time", 0));
                    fill.setStartPosition(fillObj.optString("startPosition", null));
                    fill.setDir(fillObj.optString("dir", null));
                    fill.setClosedPnl(fillObj.optString("closedPnl", null));
                    fill.setHash(fillObj.optString("hash", null));
                    fill.setOrderId(fillObj.optLong("oid", 0));
                    fill.setTaker(fillObj.optBoolean("crossed", false));
                    fill.setFee(fillObj.optString("fee", null));
                    fill.setTradeId(fillObj.optLong("tid", 0));
                    fill.setFeeToken(fillObj.optString("feeToken", null));
                    fill.setBuilderFee(fillObj.optString("builderFee", null));
                    // Ignore liquidation field
                    userFill.addFill(fill);
                }
            }
            return userFill;
        } catch (Exception e) {
            logger.error("Error parsing message: " + message, e);
            return null;
        }
    }

}
