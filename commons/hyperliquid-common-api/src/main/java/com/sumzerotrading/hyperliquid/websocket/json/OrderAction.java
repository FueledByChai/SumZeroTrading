package com.sumzerotrading.hyperliquid.websocket.json;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@JsonPropertyOrder({ "type", "orders", "grouping", "builder" })
public class OrderAction {

    @JsonProperty("type")
    public String type = "order";

    @JsonProperty("orders")
    public List<OrderJson> orders;

    @JsonProperty("grouping")
    public String grouping = "na"; // "na" | "normalTpsl" | "positionTpsl"

    @JsonProperty("builder")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Map<String, Object> builder; // optional: {"b":"0x..","f":10}

}
