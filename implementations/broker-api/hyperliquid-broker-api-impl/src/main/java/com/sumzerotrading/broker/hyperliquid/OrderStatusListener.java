package com.sumzerotrading.broker.hyperliquid;

public interface OrderStatusListener {

    public void orderStatusUpdated(IOrderStatusUpdate orderStatus);
}
