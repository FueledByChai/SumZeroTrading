package com.sumzerotrading.hyperliquid.websocket.json;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.JsonObject;

@JsonPropertyOrder({ "limit" })
public class LimitType extends OrderType {

    public enum TimeInForce {
        ALO, IOC, GTC
    }

    public TimeInForce tif; // "Alo" | "Ioc" | "Gtc"

    public LimitType(TimeInForce tif) {
        this.tif = tif;
    }

}