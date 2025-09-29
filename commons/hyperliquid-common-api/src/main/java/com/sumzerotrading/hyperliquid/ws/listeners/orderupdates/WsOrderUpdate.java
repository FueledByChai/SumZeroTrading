package com.sumzerotrading.hyperliquid.ws.listeners.orderupdates;

import com.sumzerotrading.hyperliquid.ws.json.OrderStatusType;

public class WsOrderUpdate {

    protected String coin;
    protected String side;
    protected String limitPrice;
    protected String size;
    protected long orderId;
    protected String originalSize;
    protected String clientOrderId;
    protected OrderStatusType status;
    protected long orderTimestamp;
    protected long statusTimestamp;

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getLimitPrice() {
        return limitPrice;
    }

    public void setLimitPrice(String limitPrice) {
        this.limitPrice = limitPrice;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getOriginalSize() {
        return originalSize;
    }

    public void setOriginalSize(String originalSize) {
        this.originalSize = originalSize;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public OrderStatusType getStatus() {
        return status;
    }

    public void setStatus(OrderStatusType status) {
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = OrderStatusType.fromString(status);
    }

    public long getOrderTimestamp() {
        return orderTimestamp;
    }

    public void setOrderTimestamp(long orderTimestamp) {
        this.orderTimestamp = orderTimestamp;
    }

    public long getStatusTimestamp() {
        return statusTimestamp;
    }

    public void setStatusTimestamp(long statusTimestamp) {
        this.statusTimestamp = statusTimestamp;
    }

    @Override
    public String toString() {
        return "WsOrderUpdate [coin=" + coin + ", side=" + side + ", limitPrice=" + limitPrice + ", size=" + size
                + ", orderId=" + orderId + ", originalSize=" + originalSize + ", clientOrderId=" + clientOrderId
                + ", status=" + status + ", orderTimestamp=" + orderTimestamp + ", statusTimestamp=" + statusTimestamp
                + "]";
    }

}