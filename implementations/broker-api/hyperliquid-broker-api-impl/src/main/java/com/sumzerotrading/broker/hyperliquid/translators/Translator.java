package com.sumzerotrading.broker.hyperliquid.translators;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sumzerotrading.BestBidOffer;
import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.hyperliquid.HyperliquidOrderTicket;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.ws.HyperliquidTickerRegistry;
import com.sumzerotrading.hyperliquid.ws.json.LimitType;
import com.sumzerotrading.hyperliquid.ws.json.OrderAction;
import com.sumzerotrading.hyperliquid.ws.json.OrderJson;
import com.sumzerotrading.hyperliquid.ws.listeners.accountinfo.HyperliquidPositionUpdate;

public class Translator implements ITranslator {

    protected static final double SLIPPAGE_PERCENTAGE = 0.05; // 0.25%
    protected static ITranslator instance;

    public static ITranslator getInstance() {
        if (instance == null) {
            instance = new Translator();
        }
        return instance;
    }

    @Override
    public OrderAction translateOrderTicket(OrderTicket ticket, BestBidOffer bestBidOffer) {
        List<OrderJson> orders = new ArrayList<>();

        orders.add(translateOrderTicketToOrderJson(ticket, bestBidOffer));

        return new OrderAction(orders);
    }

    @Override
    public OrderAction translateOrderTickets(HyperliquidOrderTicket ticket) {
        List<OrderJson> orders = new ArrayList<>();
        orders.add(translateOrderTicketToOrderJson(ticket.getOrderTicket(), ticket.getBestBidOffer()));
        return new OrderAction(orders);

    }

    @Override
    public OrderAction translateOrderTickets(List<HyperliquidOrderTicket> hyperliquidOrderTickets) {
        List<OrderJson> orders = new ArrayList<>();
        for (HyperliquidOrderTicket ticket : hyperliquidOrderTickets) {
            orders.add(translateOrderTicketToOrderJson(ticket.getOrderTicket(), ticket.getBestBidOffer()));
        }
        OrderAction action = new OrderAction(orders);
        return action;
    }

    @Override
    public OrderJson translateOrderTicketToOrderJson(OrderTicket ticket, BestBidOffer bestBidOffer) {
        OrderJson order = new OrderJson();
        order.assetId = ticket.getTicker().getIdAsInt();
        order.isBuy = ticket.isBuyOrder();
        order.size = ticket.getSize().toPlainString();
        order.clientOrderId = ticket.getReference();
        order.reduceOnly = false;

        if (ticket.getType() == OrderTicket.Type.MARKET) {
            if (bestBidOffer == null || bestBidOffer.getBid() == null || bestBidOffer.getAsk() == null) {
                throw new IllegalArgumentException("BestBid/BestAsk is required for market orders.");
            }
            if (ticket.isBuyOrder()) {
                order.price = getBuySlippage(ticket.getTicker(), bestBidOffer.getAsk());
            } else {
                order.price = getSellSlippage(ticket.getTicker(), bestBidOffer.getBid());
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

    @Override
    public List<Position> translatePositions(List<HyperliquidPositionUpdate> positionUpdates) {
        if (positionUpdates == null)
            return null;
        return positionUpdates.stream().map(this::translatePosition).collect(Collectors.toList());
    }

    @Override
    public Position translatePosition(HyperliquidPositionUpdate positionUpdate) {
        if (positionUpdate == null)
            return null;
        Ticker ticker = HyperliquidTickerRegistry.getInstance().lookupByBrokerSymbol(positionUpdate.getTicker());

        return new Position(ticker).setSize(positionUpdate.getSize()).setAverageCost(positionUpdate.getEntryPrice())
                .setLiquidationPrice(positionUpdate.getLiquidationPrice());
    }

    @Override
    public String getBuySlippage(Ticker ticker, BigDecimal currentAsk) {
        BigDecimal slippage = currentAsk.multiply(BigDecimal.valueOf(SLIPPAGE_PERCENTAGE / 100));
        return ticker.formatPrice(currentAsk.add(slippage)).toPlainString();
    }

    @Override
    public String getSellSlippage(Ticker ticker, BigDecimal currentBid) {
        BigDecimal slippage = currentBid.multiply(BigDecimal.valueOf(SLIPPAGE_PERCENTAGE / 100));
        return ticker.formatPrice(currentBid.subtract(slippage)).toPlainString();
    }
}
