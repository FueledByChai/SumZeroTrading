package com.sumzerotrading.broker.hyperliquid.json;

import com.google.gson.JsonObject;

public abstract class OrderType {
    public abstract JsonObject toJson();
}