package com.sumzerotrading.hyperliquid.ws.json.ws;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderDetail {

    @JsonProperty("coin")
    public String coin;

    @JsonProperty("side")
    public String side;

    @JsonProperty("limitPx")
    public String limitPrice;

    @JsonProperty("sz")
    public String size;

    @JsonProperty("oid")
    public long orderId;

    @JsonProperty("timestamp")
    public long timestamp;

    @JsonProperty("origSz")
    public String originalSize;
}
