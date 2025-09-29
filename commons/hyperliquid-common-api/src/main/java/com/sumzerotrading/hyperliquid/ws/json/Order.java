package com.sumzerotrading.hyperliquid.ws.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "a", "b", "p", "s", "r", "t", "c" })
public class Order {
    @JsonProperty("a")
    private int assetId;

    @JsonProperty("b")
    private boolean isBuy;

    @JsonProperty("p")
    private String price; // keep as String!

    @JsonProperty("s")
    private String size; // keep as String!

    @JsonProperty("r")
    private boolean reduceOnly;

    @JsonProperty("t")
    private OrderType orderType;

    @JsonProperty("c")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String clientOrderId;
}
