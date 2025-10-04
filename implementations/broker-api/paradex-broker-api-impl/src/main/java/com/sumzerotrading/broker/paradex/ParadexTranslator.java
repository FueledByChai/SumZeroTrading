package com.sumzerotrading.broker.paradex;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.sumzerotrading.broker.order.Fill;
import com.sumzerotrading.broker.order.OrderStatus;
import com.sumzerotrading.broker.order.OrderStatus.Status;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.paradex.common.ParadexTickerRegistry;
import com.sumzerotrading.paradex.common.api.ws.fills.ParadexFill;
import com.sumzerotrading.paradex.common.api.ws.orderstatus.CancelReason;
import com.sumzerotrading.paradex.common.api.ws.orderstatus.IParadexOrderStatusUpdate;
import com.sumzerotrading.paradex.common.api.ws.orderstatus.ParadexOrderStatus;
import com.sumzerotrading.util.ITickerRegistry;
import com.sumzerotrading.util.Util;

public class ParadexTranslator implements IParadexTranslator {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ParadexTranslator.class);
    protected static ITickerRegistry tickerRegistry = ParadexTickerRegistry.getInstance();
    protected static IParadexTranslator instance;

    public static IParadexTranslator getInstance() {
        if (instance == null) {
            instance = new ParadexTranslator();
        }
        return instance;
    }

    @Override
    public OrderStatus translateOrderStatus(IParadexOrderStatusUpdate paradexStatus) {

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
            logger.warn("Order {} was canceled. Cancel reason: {}", paradexStatus.getOrderId(),
                    paradexStatus.getCancelReasonString());
            if (paradexStatus.getCancelReason() == CancelReason.POST_ONLY_WOULD_CROSS) {
                orderStatus.setCancelReason(OrderStatus.CancelReason.POST_ONLY_WOULD_CROSS);
            } else if (paradexStatus.getCancelReason() == CancelReason.USER_CANCELED) {
                orderStatus.setCancelReason(OrderStatus.CancelReason.USER_CANCELED);
            } else {
                orderStatus.setCancelReason(OrderStatus.CancelReason.UNKNOWN);
            }
        }

        return orderStatus;
    }

    @Override
    public Status translateStatusCode(ParadexOrderStatus paradexStatus, CancelReason cancelReason,
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

    @Override
    public Fill translateFill(ParadexFill paradexFill) {
        Fill fill = new Fill();
        fill.setTicker(tickerRegistry.lookupByBrokerSymbol(paradexFill.getMarket()));
        fill.setPrice(new BigDecimal(paradexFill.getPrice()));
        fill.setFillId(paradexFill.getId());
        fill.setSize(new BigDecimal(paradexFill.getSize()));
        fill.setSide(
                paradexFill.getSide().equals(ParadexFill.Side.BUY) ? com.sumzerotrading.broker.order.TradeDirection.BUY
                        : com.sumzerotrading.broker.order.TradeDirection.SELL);
        fill.setTime(Util.convertEpochToZonedDateTime(paradexFill.getCreatedAt()));
        fill.setOrderId(String.valueOf(paradexFill.getOrderId()));
        fill.setTaker(paradexFill.getLiquidity() == ParadexFill.LiquidityType.TAKER);
        fill.setCommission(new BigDecimal(paradexFill.getFee()));
        return fill;
    }
}
