package com.sumzerotrading.hyperliquid.websocket.json;

import com.google.gson.JsonObject;

public class LimitType extends OrderType {

    public enum TimeInForce {
        ALO, IOC, GTC
    }

    public TimeInForce tif; // "Alo" | "Ioc" | "Gtc"

    public LimitType(TimeInForce tif) {
        this.tif = tif;
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        JsonObject limit = new JsonObject();
        limit.addProperty("tif", tif.toString().toLowerCase());
        obj.add("limit", limit);
        return obj;
    }
}