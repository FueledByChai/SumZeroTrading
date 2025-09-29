package com.sumzerotrading.hyperliquid.ws.json.ws;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderUpdateChannelData {

    @JsonProperty("channel")
    public String channel = "orderUpdates";

    @JsonProperty("data")
    public OrderStatus[] data;
}
