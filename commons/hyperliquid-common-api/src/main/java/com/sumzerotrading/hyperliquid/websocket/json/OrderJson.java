package com.sumzerotrading.hyperliquid.websocket.json;

import com.google.gson.JsonObject;

public class OrderJson {
    public Number assetId; // asset
    public Boolean isBuy; // isBuy
    public String price; // price
    public String size; // size
    public Boolean reduceOnly; // reduceOnly
    public OrderType type; // type (limit or trigger)

    // Client Order ID (cloid) is an optional 128 bit hex string, e.g.
    // 0x1234567890abcdef1234567890abcdef
    public String clientOrderId = null; // clientId/cloid (optional)

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("a", assetId);
        obj.addProperty("b", isBuy);
        obj.addProperty("p", price);
        obj.addProperty("s", size);
        obj.addProperty("r", reduceOnly);
        obj.add("t", type.toJson());
        if (clientOrderId != null)
            obj.addProperty("c", clientOrderId);
        return obj;
    }
}