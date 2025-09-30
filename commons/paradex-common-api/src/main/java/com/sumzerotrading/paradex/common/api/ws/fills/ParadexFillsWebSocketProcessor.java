package com.sumzerotrading.paradex.common.api.ws.fills;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.websocket.AbstractWebSocketProcessor;
import com.sumzerotrading.websocket.IWebSocketClosedListener;

public class ParadexFillsWebSocketProcessor extends AbstractWebSocketProcessor<ParadexFill> {

    private static final Logger logger = LoggerFactory.getLogger(ParadexFillsWebSocketProcessor.class);

    public ParadexFillsWebSocketProcessor(IWebSocketClosedListener listener) {
        super(listener);
    }

    @Override
    protected ParadexFill parseMessage(String message) {
        logger.info("Received user fill message: {}", message);
        try {
            JSONObject jsonObject = new JSONObject(message);
            if (!jsonObject.has("method")) {
                return null;
            }
            String method = jsonObject.getString("method");

            if ("subscription".equals(method)) {
                JSONObject params = jsonObject.getJSONObject("params");
                JSONObject data = params.getJSONObject("data");
                return parseFillData(data);
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("Error parsing message: " + message, e);
            return null;
        }
    }

    private ParadexFill parseFillData(JSONObject data) throws SumZeroException {
        ParadexFill fill = new ParadexFill();
        fill.account = data.optString("account", null);
        fill.clientId = data.optString("client_id", null);
        fill.createdAt = data.optLong("created_at", 0);
        fill.fee = data.optString("fee", null);
        fill.feeCurrency = data.optString("fee_currency", null);
        fill.fillType = ParadexFill.FillType.valueOf(data.optString("fill_type", null));
        JSONArray flagsArray = data.optJSONArray("flags");
        List<String> flagsList = new ArrayList<>();
        if (flagsArray != null) {
            for (int i = 0; i < flagsArray.length(); i++) {
                flagsList.add(flagsArray.optString(i));
            }
        }
        fill.flags = flagsList;
        fill.id = data.optString("id", null);
        String liquidityStr = data.optString("liquidity", null);
        fill.liquidity = liquidityStr != null ? ParadexFill.LiquidityType.valueOf(liquidityStr) : null;
        fill.market = data.optString("market", null);
        fill.orderId = data.optString("order_id", null);
        fill.price = data.optString("price", null);
        fill.realizedFunding = data.optString("realized_funding", null);
        fill.realizedPnl = data.optString("realized_pnl", null);
        fill.remainingSize = data.optString("remaining_size", null);
        fill.side = ParadexFill.Side.valueOf(data.optString("side", null));
        fill.size = data.optString("size", null);
        fill.underlyingPrice = data.optString("underlying_price", null);
        return fill;
    }

}
