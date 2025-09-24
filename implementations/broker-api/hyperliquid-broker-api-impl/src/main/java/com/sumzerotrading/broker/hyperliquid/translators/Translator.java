package com.sumzerotrading.broker.hyperliquid.translators;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.hyperliquid.HyperliquidPositionUpdate;
import com.sumzerotrading.broker.hyperliquid.json.LimitType;
import com.sumzerotrading.broker.hyperliquid.json.OrderAction;
import com.sumzerotrading.broker.hyperliquid.json.OrderJson;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.websocket.HyperliquidTickerRegistry;

public class Translator {

    protected static final double SLIPPAGE_PERCENTAGE = 0.05; // 0.25%

    public static OrderAction translateOrderTicket(OrderTicket ticket, BigDecimal currentBid, BigDecimal currentAsk) {
        List<OrderJson> orders = new ArrayList<>();

        orders.add(translateOrderTicketToOrderJson(ticket, currentBid, currentAsk));

        return new OrderAction(orders);
    }

    public static OrderAction translateOrderTickets(List<OrderTicket> tickets, BigDecimal currentBid,
            BigDecimal currentAsk) {
        List<OrderJson> orders = new ArrayList<>();
        for (OrderTicket ticket : tickets) {
            orders.add(translateOrderTicketToOrderJson(ticket, currentBid, currentAsk));
        }
        return new OrderAction(orders);
    }

    public static OrderJson translateOrderTicketToOrderJson(OrderTicket ticket, BigDecimal currentBid,
            BigDecimal currentAsk) {
        OrderJson order = new OrderJson();
        order.assetId = ticket.getTicker().getIdAsInt();
        order.isBuy = ticket.isBuyOrder();
        order.size = ticket.getSize().toPlainString();
        order.clientOrderId = ticket.getReference();
        order.reduceOnly = false;

        if (ticket.getType() == OrderTicket.Type.MARKET) {
            if (ticket.isBuyOrder()) {
                order.price = getBuySlippage(ticket.getTicker(), currentAsk);
            } else {
                order.price = getSellSlippage(ticket.getTicker(), currentBid);
            }
            order.type = new LimitType(LimitType.TimeInForce.IOC);
        } else if (ticket.getType() == OrderTicket.Type.LIMIT) {
            order.price = ticket.getLimitPrice() != null ? ticket.getLimitPrice().toPlainString() : null;
            if (ticket.getModifiers().contains(OrderTicket.Modifier.POST_ONLY)) {
                order.type = new LimitType(LimitType.TimeInForce.ALO);
            } else if (ticket.getDuration() == OrderTicket.Duration.IMMEDIATE_OR_CANCEL) {
                order.type = new LimitType(LimitType.TimeInForce.IOC);
            } else if (ticket.getDuration() == OrderTicket.Duration.GOOD_UNTIL_CANCELED) {
                order.type = new LimitType(LimitType.TimeInForce.GTC);
            } else {
                order.type = new LimitType(LimitType.TimeInForce.GTC);
            }
        } else {
            throw new IllegalArgumentException("Unsupported order type: " + ticket.getType());
        }

        if (ticket.getModifiers().contains(OrderTicket.Modifier.REDUCE_ONLY)) {
            order.reduceOnly = true;
        }
        return order;
    }

    public static List<Position> translatePositions(List<HyperliquidPositionUpdate> positionUpdates) {
        if (positionUpdates == null)
            return null;
        return positionUpdates.stream().map(Translator::translatePosition).collect(Collectors.toList());
    }

    public static Position translatePosition(HyperliquidPositionUpdate positionUpdate) {
        if (positionUpdate == null)
            return null;
        Ticker ticker = HyperliquidTickerRegistry.getInstance().lookupByBrokerSymbol(positionUpdate.getTicker());

        return new Position(ticker).setSize(positionUpdate.getSize()).setAverageCost(positionUpdate.getEntryPrice())
                .setLiquidationPrice(positionUpdate.getLiquidationPrice());
    }

    public static String getBuySlippage(Ticker ticker, BigDecimal currentAsk) {
        BigDecimal slippage = currentAsk.multiply(BigDecimal.valueOf(SLIPPAGE_PERCENTAGE / 100));
        return ticker.formatPrice(currentAsk.add(slippage)).toPlainString();
    }

    public static String getSellSlippage(Ticker ticker, BigDecimal currentBid) {
        BigDecimal slippage = currentBid.multiply(BigDecimal.valueOf(SLIPPAGE_PERCENTAGE / 100));
        return ticker.formatPrice(currentBid.subtract(slippage)).toPlainString();
    }
}
