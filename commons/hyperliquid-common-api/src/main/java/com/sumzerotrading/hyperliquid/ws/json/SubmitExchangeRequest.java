package com.sumzerotrading.hyperliquid.ws.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "action", "nonce", "signature", "vaultAddress", "expiresAfter", })
public class SubmitExchangeRequest extends SignableExchangeOrderRequest {

    @JsonProperty("signature")
    public SignatureFields signature;

}
