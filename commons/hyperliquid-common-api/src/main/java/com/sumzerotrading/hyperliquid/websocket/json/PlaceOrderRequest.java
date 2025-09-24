package com.sumzerotrading.hyperliquid.websocket.json;

import com.google.gson.JsonObject;

public class PlaceOrderRequest {

    protected OrderAction action;
    protected long nonce;
    protected Signature signature;

    public PlaceOrderRequest(OrderAction action, long nonce, Signature signature) {
        this.action = action;
        this.nonce = nonce;
        this.signature = signature;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.add("action", action.toJson());
        obj.addProperty("nonce", nonce);
        obj.addProperty("signature", signature.getSignature());
        return obj;
    }

}
