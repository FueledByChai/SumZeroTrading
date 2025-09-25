package com.sumzerotrading.hyperliquid.websocket.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "a", "b", "p", "s", "r", "t", "c" })
public class OrderJson {

    @JsonProperty("a")
    public int assetId; // asset

    @JsonProperty("b")
    public Boolean isBuy; // isBuy

    @JsonProperty("p")
    public String price; // price

    @JsonProperty("s")
    public String size; // size

    @JsonProperty("r")
    public Boolean reduceOnly; // reduceOnly

    @JsonProperty("t")
    public OrderType type; // type (limit or trigger)

    @JsonProperty("c")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String clientOrderId = null; // clientId/cloid (optional)

}