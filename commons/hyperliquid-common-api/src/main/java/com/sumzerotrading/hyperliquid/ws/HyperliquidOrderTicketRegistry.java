package com.sumzerotrading.hyperliquid.ws;

import java.util.HashMap;
import java.util.Map;

import com.sumzerotrading.broker.order.OrderTicket;

public class HyperliquidOrderTicketRegistry {

    protected Map<String, OrderTicket> orderTicketsByClientOrderId = new HashMap<>();
    protected Map<String, OrderTicket> orderTicketsByOrderId = new HashMap<>();

    public OrderTicket getOrderTicketByClientOrderId(String clientOrderId) {
        return orderTicketsByClientOrderId.get(clientOrderId);
    }

    public OrderTicket getOrderTicketByOrderId(String orderId) {
        return orderTicketsByOrderId.get(orderId);
    }

    public void setOrderTicketByClientId(String clientOrderId, String orderId) {
        OrderTicket ticket = orderTicketsByClientOrderId.get(clientOrderId);
        if (ticket != null) {
            ticket.setOrderId(orderId);
            orderTicketsByOrderId.put(orderId, ticket);
        }
    }

    public void registerOrderTicketByClientOrderId(String clientOrderId, OrderTicket ticket) {
        orderTicketsByClientOrderId.put(clientOrderId, ticket);
        if (ticket.getOrderId() != null && !ticket.getOrderId().isEmpty()) {
            orderTicketsByOrderId.put(ticket.getOrderId(), ticket);
        }
    }

}
