package com.sumzerotrading.hyperliquid.ws.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "type", "payload" })
public class WebServicePostRequest {
    @JsonProperty("type")
    public String type = "action";

    @JsonProperty("payload")
    public Object payload;
}
