package com.sumzerotrading.hyperliquid.websocket.json;

import com.google.gson.JsonObject;

public abstract class OrderType {
    public abstract JsonObject toJson();
}