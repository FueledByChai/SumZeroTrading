package com.sumzerotrading.broker.hyperliquid.translators;

import java.math.BigDecimal;
import java.util.List;

import com.sumzerotrading.BestBidOffer;
import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.hyperliquid.HyperliquidOrderTicket;
import com.sumzerotrading.broker.hyperliquid.HyperliquidPositionUpdate;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.websocket.json.OrderAction;
import com.sumzerotrading.hyperliquid.websocket.json.OrderJson;
import com.sumzerotrading.hyperliquid.websocket.json.PlaceOrderRequest;

public interface ITranslator {

    OrderAction translateOrderTicket(OrderTicket ticket, BestBidOffer bestBidOffer);

    PlaceOrderRequest translateOrderTickets(HyperliquidOrderTicket ticket);

    PlaceOrderRequest translateOrderTickets(List<HyperliquidOrderTicket> hyperliquidOrderTickets);

    OrderJson translateOrderTicketToOrderJson(OrderTicket ticket, BestBidOffer bestBidOffer);

    List<Position> translatePositions(List<HyperliquidPositionUpdate> positionUpdates);

    Position translatePosition(HyperliquidPositionUpdate positionUpdate);

    String getBuySlippage(Ticker ticker, BigDecimal currentAsk);

    String getSellSlippage(Ticker ticker, BigDecimal currentBid);

}