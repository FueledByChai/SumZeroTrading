package com.sumzerotrading.hyperliquid.websocket.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "action", "nonce", "vaultAddress", "expiresAfter" })
public class SignableExchangeOrderRequest {
    @JsonProperty("action")
    private OrderAction action;

    // recommend: current epoch millis
    @JsonProperty("nonce")
    private long nonceMs;

    // optional; must be lowercased per HL docs
    @JsonProperty("vaultAddress")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String vaultAddress;

    // optional epoch millis
    @JsonProperty("expiresAfter")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long expiresAfterMs;
}
