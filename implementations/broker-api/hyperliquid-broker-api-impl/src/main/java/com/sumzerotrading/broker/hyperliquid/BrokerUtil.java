package com.sumzerotrading.broker.hyperliquid;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.sumzerotrading.broker.order.OrderStatus;
import com.sumzerotrading.broker.order.OrderStatus.Status;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.websocket.HyperliquidTickerRegistry;
import com.sumzerotrading.util.ITickerRegistry;

public class BrokerUtil {

    protected static ITickerRegistry tickerRegistry = HyperliquidTickerRegistry.getInstance();

    public static OrderStatus translateOrderStatus(IOrderStatusUpdate orderStatusUpdate) {

        Ticker ticker = tickerRegistry.lookupByBrokerSymbol(orderStatusUpdate.getTickerString());
        Status status = translateStatusCode(orderStatusUpdate.getStatus(), orderStatusUpdate.getCancelReason(),
                orderStatusUpdate.getOriginalSize(), orderStatusUpdate.getRemainingSize());
        OrderStatus orderStatus = null;
        BigDecimal filledSize = orderStatusUpdate.getOriginalSize().subtract(orderStatusUpdate.getRemainingSize());

        ZonedDateTime timestamp = orderStatusUpdate.getTimestamp() == 0 ? ZonedDateTime.now()
                : ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(orderStatusUpdate.getTimestamp()),
                        ZoneId.of("UTC"));

        orderStatus = new OrderStatus(status, orderStatusUpdate.getOrderId(), filledSize,
                orderStatusUpdate.getRemainingSize(), orderStatusUpdate.getAverageFillPrice(), ticker, timestamp);

        if (status == Status.CANCELED) {
            if (orderStatusUpdate.getCancelReason() == CancelReason.POST_ONLY_WOULD_CROSS) {
                orderStatus.setCancelReason(OrderStatus.CancelReason.POST_ONLY_WOULD_CROSS);
            } else if (orderStatusUpdate.getCancelReason() == CancelReason.USER_CANCELED) {
                orderStatus.setCancelReason(OrderStatus.CancelReason.USER_CANCELED);
            } else {
                orderStatus.setCancelReason(OrderStatus.CancelReason.NONE);
            }
        }

        return orderStatus;
    }

    public static Status translateStatusCode(ParadexOrderStatus paradexStatus, CancelReason cancelReason,
            BigDecimal originalSize, BigDecimal remainingSize) {

        if (paradexStatus == ParadexOrderStatus.NEW || paradexStatus == ParadexOrderStatus.UNTRIGGERED) {
            return Status.NEW;
        }

        if (paradexStatus == ParadexOrderStatus.CLOSED) {
            if (cancelReason != CancelReason.NONE) {
                return Status.CANCELED;
            }

            if (remainingSize != null && remainingSize.compareTo(BigDecimal.ZERO) == 0) {
                return Status.FILLED;
            }

        }

        if (paradexStatus == ParadexOrderStatus.OPEN) {
            if (remainingSize != null && originalSize != null && remainingSize.compareTo(originalSize) < 0) {
                return Status.PARTIAL_FILL;
            }

            return Status.NEW;
        }

        throw new SumZeroException("Unknown Paradex order status: " + paradexStatus);

    }
}
