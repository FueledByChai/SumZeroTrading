package com.sumzerotrading.hyperliquid.websocket.json;

import com.google.gson.JsonObject;

public class TriggerType extends OrderType {
    public Boolean isMarket;
    public String triggerPx;
    public String tpsl; // "tp" | "sl"

    public TriggerType(Boolean isMarket, String triggerPx, String tpsl) {
        this.isMarket = isMarket;
        this.triggerPx = triggerPx;
        this.tpsl = tpsl;
    }

    @Override
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        JsonObject trigger = new JsonObject();
        trigger.addProperty("isMarket", isMarket);
        trigger.addProperty("triggerPx", triggerPx);
        trigger.addProperty("tpsl", tpsl);
        obj.add("trigger", trigger);
        return obj;
    }
}