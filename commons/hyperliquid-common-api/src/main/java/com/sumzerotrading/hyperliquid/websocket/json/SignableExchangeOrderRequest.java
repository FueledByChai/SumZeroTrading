package com.sumzerotrading.hyperliquid.websocket.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "action", "nonce", "vaultAddress", "expiresAfter" })
public class SignableExchangeOrderRequest {
    @JsonProperty("action")
    public OrderAction action;

    // recommend: current epoch millis
    @JsonProperty("nonce")
    public long nonceMs;

    // optional; must be lowercased per HL docs
    @JsonProperty("vaultAddress")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String vaultAddress;

    // optional epoch millis
    @JsonProperty("expiresAfter")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Long expiresAfterMs;

    public void validate() {
        if (action == null)
            throw new IllegalStateException("action required");
        if (action.orders == null || action.orders.isEmpty())
            throw new IllegalStateException("at least one order required");
        for (OrderJson o : action.orders) {
            if (o.type == null)
                throw new IllegalStateException("order.kind (t) required");
            // if your OrderKind implementations expose self-checks, call them here.
        }
    }
}
