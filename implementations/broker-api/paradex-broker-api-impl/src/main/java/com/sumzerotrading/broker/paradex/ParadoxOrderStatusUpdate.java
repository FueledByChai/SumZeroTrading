package com.sumzerotrading.broker.paradex;

import java.math.BigDecimal;

import com.sumzerotrading.paradex.common.api.order.OrderType;
import com.sumzerotrading.paradex.common.api.order.Side;

public class ParadoxOrderStatusUpdate implements IParadexOrderStatusUpdate {

    protected String tickerString;
    protected String orderId;
    protected BigDecimal originalSize;
    protected BigDecimal remainingSize;
    protected ParadexOrderStatus status;
    protected String cancelReasonString;
    protected CancelReason cancelReason;
    protected BigDecimal averageFillPrice;
    protected OrderType orderType;
    protected Side side;
    protected long timestamp;

    public ParadoxOrderStatusUpdate(String tickerString, String orderId, BigDecimal remainingSize,
            BigDecimal originalSize, String status, String cancelReasonAsString, BigDecimal averageFillPrice,
            String orderType, String side, long timestamp) {
        this.tickerString = tickerString;
        this.orderId = orderId;
        this.remainingSize = remainingSize;
        this.status = ParadexOrderStatus.valueOf(status);
        this.originalSize = originalSize;
        this.cancelReasonString = cancelReasonAsString;
        if (cancelReasonAsString != null && !cancelReasonAsString.isEmpty()) {
            try {
                this.cancelReason = CancelReason.valueOf(cancelReasonAsString);
            } catch (IllegalArgumentException e) {
                // Handle the case when cancelReasonAsString is not a valid enum value
                this.cancelReason = CancelReason.NONE;
            }
        } else {
            this.cancelReason = CancelReason.NONE;
        }
        this.averageFillPrice = averageFillPrice;
        this.orderType = OrderType.valueOf(orderType);
        this.side = Side.valueOf(side);
        this.timestamp = timestamp;
    }

    @Override
    public String getOrderId() {
        return orderId;
    }

    @Override
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public BigDecimal getRemainingSize() {
        return remainingSize;
    }

    @Override
    public void setRemainingSize(BigDecimal remainingSize) {
        this.remainingSize = remainingSize;
    }

    @Override
    public ParadexOrderStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(ParadexOrderStatus status) {
        this.status = status;
    }

    @Override
    public String getCancelReasonString() {
        return cancelReasonString;
    }

    @Override
    public void setCancelReasonString(String cancelReason) {
        this.cancelReasonString = cancelReason;
    }

    @Override
    public BigDecimal getOriginalSize() {
        return originalSize;
    }

    @Override
    public void setOriginalSize(BigDecimal originalSize) {
        this.originalSize = originalSize;
    }

    @Override
    public CancelReason getCancelReason() {
        return cancelReason;
    }

    @Override
    public void setCancelReason(CancelReason cancelReason) {
        this.cancelReason = cancelReason;
    }

    @Override
    public BigDecimal getAverageFillPrice() {
        return averageFillPrice;
    }

    @Override
    public OrderType getOrderType() {
        return orderType;
    }

    @Override
    public Side getSide() {
        return side;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getTickerString() {
        return tickerString;
    }

    @Override
    public String toString() {
        return "ParadoxOrderStatus [tickerString=" + tickerString + ", orderId=" + orderId + ", originalSize="
                + originalSize + ", remainingSize=" + remainingSize + ", status=" + status + ", cancelReasonString="
                + cancelReasonString + ", cancelReason=" + cancelReason + ", averageFillPrice=" + averageFillPrice
                + ", orderType=" + orderType + ", side=" + side + "]";
    }

}
