package com.sumzerotrading.hyperliquid.websocket.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "action", "nonce", "vaultAddress", "expiresAfter", "signature" })
public class SubmitExchangeRequest extends SignableExchangeOrderRequest {

    @JsonProperty("signature")
    private SignatureFields signature;

    public SignatureFields getSignature() {
        return signature;
    }

    public void setSignature(SignatureFields signature) {
        this.signature = signature;
    }

}
