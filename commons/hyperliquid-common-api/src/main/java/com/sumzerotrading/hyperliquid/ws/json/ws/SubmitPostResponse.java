package com.sumzerotrading.hyperliquid.ws.json.ws;

import java.util.ArrayList;
import java.util.List;

public class SubmitPostResponse {

    public int requestId;
    public String requestStatus;
    public List<SubmitOrderResponse> orders = new ArrayList<>();

    public void addSubmitOrderResponse(SubmitOrderResponse order) {
        orders.add(order);
    }

    public void addSubmitOrderResponse(int orderId, String clientOrderId, String status) {
        SubmitOrderResponse newOrder = new SubmitOrderResponse();
        newOrder.orderId = orderId;
        newOrder.clientOrderId = clientOrderId;
        newOrder.status = status;
        orders.add(newOrder);
    }

    public void addSubmitOrderResponse(String status) {
        addSubmitOrderResponse(-1, "", status);
    }

    @Override
    public String toString() {
        return "SubmitPostResponse [requestId=" + requestId + ", requestStatus=" + requestStatus + ", orders=" + orders
                + "]";
    }

}
