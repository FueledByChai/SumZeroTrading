package com.sumzerotrading.broker.paradex;

import java.math.BigDecimal;

import com.sumzerotrading.broker.order.OrderStatus;
import com.sumzerotrading.broker.order.OrderStatus.Status;
import com.sumzerotrading.paradex.common.api.ws.orderstatus.CancelReason;
import com.sumzerotrading.paradex.common.api.ws.orderstatus.IParadexOrderStatusUpdate;
import com.sumzerotrading.paradex.common.api.ws.orderstatus.ParadexOrderStatus;

public interface IParadexTranslator {

    OrderStatus translateOrderStatus(IParadexOrderStatusUpdate paradexStatus);

    Status translateStatusCode(ParadexOrderStatus paradexStatus, CancelReason cancelReason, BigDecimal originalSize,
            BigDecimal remainingSize);

}