package com.sumzerotrading.broker.paradex;

import com.sumzerotrading.broker.order.OrderStatus;

public class ParadexBrokerUtil {

    public static OrderStatus translateOrderStatus(ParadoxOrderStatusUpdate paradexStatus) {
        paradexStatus.getTickerString();
        OrderStatus status = new OrderStatus();

        return status;
    }
}
