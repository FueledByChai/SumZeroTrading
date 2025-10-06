package com.sumzerotrading.broker.hyperliquid.translators;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sumzerotrading.BestBidOffer;
import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.hyperliquid.HyperliquidOrderTicket;
import com.sumzerotrading.broker.order.Fill;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.HyperliquidUtil;
import com.sumzerotrading.hyperliquid.ws.HyperliquidTickerRegistry;
import com.sumzerotrading.hyperliquid.ws.json.LimitType;
import com.sumzerotrading.hyperliquid.ws.json.OrderAction;
import com.sumzerotrading.hyperliquid.ws.json.OrderJson;
import com.sumzerotrading.hyperliquid.ws.listeners.accountinfo.HyperliquidPositionUpdate;
import com.sumzerotrading.hyperliquid.ws.listeners.userfills.WsFill;
import com.sumzerotrading.hyperliquid.ws.listeners.userfills.WsUserFill;
import com.sumzerotrading.util.Util;

public class Translator implements ITranslator {

    protected static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Translator.class);

    protected static double SLIPPAGE_PERCENTAGE = 0.05; // 0.25%
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
        order.clientOrderId = ticket.getClientOrderId();
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
            order.type = new LimitType(LimitType.TimeInForce.GTC);
        } else if (ticket.getType() == OrderTicket.Type.LIMIT) {
            order.price = ticket.getLimitPrice() != null
                    ? HyperliquidUtil.formatPriceAsString(ticket.getTicker(), ticket.getLimitPrice())
                    : null;
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
        logger.info("Translating order ticket: " + ticket);
        logger.info("Best bid/offer: " + bestBidOffer);
        logger.info("Translated order: " + order);

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
        double slippage = currentAsk.doubleValue() * (SLIPPAGE_PERCENTAGE / 100);
        return HyperliquidUtil.formatPriceAsString(ticker, currentAsk.add(BigDecimal.valueOf(slippage)));
    }

    @Override
    public String getSellSlippage(Ticker ticker, BigDecimal currentBid) {
        double slippage = currentBid.doubleValue() * (SLIPPAGE_PERCENTAGE / 100);
        return HyperliquidUtil.formatPriceAsString(ticker, currentBid.subtract(BigDecimal.valueOf(slippage)));
    }

    @Override
    public List<Fill> translateFill(WsUserFill wsUserFill) {
        if (wsUserFill == null) {
            return new ArrayList<>();
        }

        List<Fill> fills = new ArrayList<>();
        for (WsFill wsFill : wsUserFill.getFills()) {
            Ticker ticker = HyperliquidTickerRegistry.getInstance().lookupByBrokerSymbol(wsFill.getCoin());
            Fill fill = new Fill();
            fill.setTicker(ticker);
            fill.setSnapshot(wsUserFill.isSnapshot());
            fill.setPrice(new BigDecimal(wsFill.getPrice()));
            fill.setSize(new BigDecimal(wsFill.getSize()));
            fill.setFillId(wsFill.getTradeId() + "");
            fill.setCommission(wsFill.getFee() != null ? new BigDecimal(wsFill.getFee()) : BigDecimal.ZERO);
            fill.setOrderId(wsFill.getOrderId() + "");
            fill.setSide("buy".equalsIgnoreCase(wsFill.getSide()) ? TradeDirection.BUY : TradeDirection.SELL);
            fill.setTime(Util.convertEpochToZonedDateTime(wsFill.getTime()));
            fill.setTaker(wsFill.isTaker());
            fill.setClientOrderId("");
            fills.add(fill);
        }
        return fills;
    }
}
