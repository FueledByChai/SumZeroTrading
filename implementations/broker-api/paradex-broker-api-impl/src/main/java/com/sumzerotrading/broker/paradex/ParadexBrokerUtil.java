package com.sumzerotrading.broker.paradex;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.sumzerotrading.broker.order.OrderStatus;
import com.sumzerotrading.broker.order.OrderStatus.Status;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.paradex.common.IParadexTickerRegistry;
import com.sumzerotrading.paradex.common.ParadexTickerRegistry;

public class ParadexBrokerUtil {

    protected static IParadexTickerRegistry tickerRegistry = ParadexTickerRegistry.getInstance();

    public static OrderStatus translateOrderStatus(IParadexOrderStatusUpdate paradexStatus) {

        Ticker ticker = tickerRegistry.lookupByBrokerSymbol(paradexStatus.getTickerString());
        Status status = translateStatusCode(paradexStatus.getStatus(), paradexStatus.getCancelReason(),
                paradexStatus.getOriginalSize(), paradexStatus.getRemainingSize());
        OrderStatus orderStatus = null;
        BigDecimal filledSize = paradexStatus.getOriginalSize().subtract(paradexStatus.getRemainingSize());

        ZonedDateTime timestamp = paradexStatus.getTimestamp() == 0 ? ZonedDateTime.now()
                : ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(paradexStatus.getTimestamp()),
                        ZoneId.of("UTC"));

        orderStatus = new OrderStatus(status, paradexStatus.getOrderId(), filledSize, paradexStatus.getRemainingSize(),
                paradexStatus.getAverageFillPrice(), ticker, timestamp);

        if (status == Status.CANCELED) {
            if (paradexStatus.getCancelReason() == CancelReason.POST_ONLY_WOULD_CROSS) {
                orderStatus.setCancelReason(OrderStatus.CancelReason.POST_ONLY_WOULD_CROSS);
            } else if (paradexStatus.getCancelReason() == CancelReason.USER_CANCELED) {
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
