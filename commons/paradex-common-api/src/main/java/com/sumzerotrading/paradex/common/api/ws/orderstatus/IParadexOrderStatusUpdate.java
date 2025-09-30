package com.sumzerotrading.paradex.common.api.ws.orderstatus;

import java.math.BigDecimal;

import com.sumzerotrading.paradex.common.api.order.OrderType;
import com.sumzerotrading.paradex.common.api.order.Side;

public interface IParadexOrderStatusUpdate {

    String getTickerString();

    String getOrderId();

    void setOrderId(String orderId);

    BigDecimal getRemainingSize();

    void setRemainingSize(BigDecimal remainingSize);

    ParadexOrderStatus getStatus();

    void setStatus(ParadexOrderStatus status);

    String getCancelReasonString();

    void setCancelReasonString(String cancelReason);

    BigDecimal getOriginalSize();

    void setOriginalSize(BigDecimal originalSize);

    CancelReason getCancelReason();

    void setCancelReason(CancelReason cancelReason);

    BigDecimal getAverageFillPrice();

    OrderType getOrderType();

    Side getSide();

    long getTimestamp();

}