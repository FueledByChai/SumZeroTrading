package com.sumzerotrading.hyperliquid.ws.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "method", "id", "request" })
public class WebServicePostMessage {
    @JsonProperty("method")
    public String method = "post";

    @JsonProperty("id")
    public int id;

    @JsonProperty("request")
    public WebServicePostRequest request;

    public void setPayload(Object payload) {
        if (request == null) {
            request = new WebServicePostRequest();
        }
        request.payload = payload;
    }

}
