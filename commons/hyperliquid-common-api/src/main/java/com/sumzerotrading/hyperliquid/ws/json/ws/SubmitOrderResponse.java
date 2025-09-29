package com.sumzerotrading.hyperliquid.ws.json.ws;

public class SubmitOrderResponse {

    public int orderId;
    public String clientOrderId;
    public String status;

    @Override
    public String toString() {
        return "SubmitOrderResponse [orderId=" + orderId + ", clientOrderId=" + clientOrderId + ", status=" + status
                + "]";
    }

}
