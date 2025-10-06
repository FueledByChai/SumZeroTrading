package com.sumzerotrading.broker.hyperliquid.translators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sumzerotrading.BestBidOffer;
import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.hyperliquid.HyperliquidOrderTicket;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.ws.HyperliquidTickerRegistry;
import com.sumzerotrading.hyperliquid.ws.json.OrderJson;
import com.sumzerotrading.hyperliquid.ws.json.LimitType;
import com.sumzerotrading.hyperliquid.ws.listeners.accountinfo.HyperliquidPositionUpdate;

@ExtendWith(MockitoExtension.class)
public class TranslatorTest {

    @Mock
    HyperliquidTickerRegistry mockRegistry;
    @Mock
    Ticker mockTicker;
    @Mock
    HyperliquidPositionUpdate mockUpdate1;
    @Mock
    HyperliquidPositionUpdate mockUpdate2;

    ITranslator translator = Translator.getInstance();

    private static org.mockito.MockedStatic<HyperliquidTickerRegistry> staticMock;

    @org.junit.jupiter.api.BeforeAll
    public static void setupAll() {
        staticMock = Mockito.mockStatic(HyperliquidTickerRegistry.class);
    }

    @BeforeEach
    public void setup() {
        staticMock.when(HyperliquidTickerRegistry::getInstance).thenReturn(mockRegistry);
    }

    @org.junit.jupiter.api.AfterAll
    public static void tearDownAll() {
        if (staticMock != null) {
            staticMock.close();
            staticMock = null;
        }
    }

    @Test
    public void testTranslatePositions_NullInput() {
        assertNull(translator.translatePositions(null));
    }

    @Test
    public void testTranslatePositions_EmptyList() {
        List<Position> result = translator.translatePositions(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testTranslatePositions_ValidList() {
        when(mockUpdate1.getTicker()).thenReturn("BTCUSD");
        when(mockUpdate1.getSize()).thenReturn(java.math.BigDecimal.valueOf(2.0));
        when(mockUpdate1.getEntryPrice()).thenReturn(java.math.BigDecimal.valueOf(50000.0));
        when(mockUpdate1.getLiquidationPrice()).thenReturn(java.math.BigDecimal.valueOf(45000.0));
        when(mockRegistry.lookupByBrokerSymbol("BTCUSD")).thenReturn(mockTicker);

        when(mockUpdate2.getTicker()).thenReturn("ETHUSD");
        when(mockUpdate2.getSize()).thenReturn(java.math.BigDecimal.valueOf(1.0));
        when(mockUpdate2.getEntryPrice()).thenReturn(java.math.BigDecimal.valueOf(3000.0));
        when(mockUpdate2.getLiquidationPrice()).thenReturn(java.math.BigDecimal.valueOf(2500.0));
        when(mockRegistry.lookupByBrokerSymbol("ETHUSD")).thenReturn(mockTicker);

        List<HyperliquidPositionUpdate> updates = Arrays.asList(mockUpdate1, mockUpdate2);
        List<Position> positions = translator.translatePositions(updates);
        assertEquals(2, positions.size());
        for (Position pos : positions) {
            assertEquals(mockTicker, pos.getTicker());
        }
    }

    @Test
    public void testTranslatePosition_NullInput() {
        assertNull(translator.translatePosition(null));
    }

    @Test
    public void testTranslatePosition_ValidInput() {
        when(mockUpdate1.getTicker()).thenReturn("BTCUSD");
        when(mockUpdate1.getSize()).thenReturn(java.math.BigDecimal.valueOf(2.0));
        when(mockUpdate1.getEntryPrice()).thenReturn(java.math.BigDecimal.valueOf(50000.0));
        when(mockUpdate1.getLiquidationPrice()).thenReturn(java.math.BigDecimal.valueOf(45000.0));
        when(mockRegistry.lookupByBrokerSymbol("BTCUSD")).thenReturn(mockTicker);

        Position pos = translator.translatePosition(mockUpdate1);
        assertNotNull(pos);
        assertEquals(mockTicker, pos.getTicker());
        assertEquals(2.0, pos.getSize().doubleValue());
        assertEquals(50000.0, pos.getAverageCost().doubleValue());
        assertEquals(45000.0, pos.getLiquidationPrice().doubleValue());
    }

    @Test
    public void testTranslateOrderTicketToOrderJson_MarketBuy() {
        Ticker ticker = mock(Ticker.class);
        when(ticker.getIdAsInt()).thenReturn(123);
        when(ticker.getMinimumTickSize()).thenReturn(new BigDecimal("0.01"));
        OrderTicket ticket = new OrderTicket();
        ticket.setTicker(ticker);
        ticket.setTradeDirection(TradeDirection.BUY);
        ticket.setType(OrderTicket.Type.MARKET);
        ticket.setSize(new java.math.BigDecimal("1.5"));
        ticket.setClientOrderId("cloid-1");
        BestBidOffer bestBidOffer = new BestBidOffer(new java.math.BigDecimal("100.00"),
                new java.math.BigDecimal("101.00"));

        OrderJson orderJson = translator.translateOrderTicketToOrderJson(ticket, bestBidOffer);
        assertEquals(123, orderJson.assetId);
        assertTrue(orderJson.isBuy);
        assertEquals("1.5", orderJson.size);
        assertEquals("cloid-1", orderJson.clientOrderId);
        assertEquals("101.05", orderJson.price); // 101 + 0.05% slippage
        assertNotNull(orderJson.type);
        assertFalse(orderJson.reduceOnly);
        assertTrue(orderJson.type instanceof com.sumzerotrading.hyperliquid.ws.json.LimitType);
    }

    @Test
    public void testTranslateOrderTicketToOrderJson_LimitSell_PostOnly() {
        Ticker ticker = mock(Ticker.class);
        when(ticker.getIdAsInt()).thenReturn(456);
        when(ticker.getMinimumTickSize()).thenReturn(new BigDecimal("0.01"));
        OrderTicket ticket = new OrderTicket();
        ticket.setTicker(ticker);
        ticket.setTradeDirection(TradeDirection.SELL);
        ticket.setType(OrderTicket.Type.LIMIT);
        ticket.setSize(new java.math.BigDecimal("2.0"));
        ticket.setLimitPrice(new java.math.BigDecimal("99.99"));
        ticket.setClientOrderId("cloid-2");
        ticket.getModifiers().add(OrderTicket.Modifier.POST_ONLY);
        BestBidOffer bestBidOffer = new BestBidOffer(new java.math.BigDecimal("99.00"),
                new java.math.BigDecimal("100.00"));

        OrderJson orderJson = translator.translateOrderTicketToOrderJson(ticket, bestBidOffer);
        assertEquals(456, orderJson.assetId);
        assertFalse(orderJson.isBuy);
        assertEquals("2.0", orderJson.size);
        assertEquals("cloid-2", orderJson.clientOrderId);
        assertEquals("99.99", orderJson.price);
        assertNotNull(orderJson.type);
        assertFalse(orderJson.reduceOnly);
        assertTrue(orderJson.type instanceof LimitType);
        LimitType limitType = (LimitType) orderJson.type;
        assertEquals(LimitType.TimeInForce.ALO, limitType.tif);
    }

    @Test
    public void testTranslateOrderTicket_MultipleTickets() {
        Ticker ticker1 = mock(Ticker.class);
        when(ticker1.getIdAsInt()).thenReturn(1);
        when(ticker1.getMinimumTickSize()).thenReturn(new BigDecimal("0.01"));
        Ticker ticker2 = mock(Ticker.class);
        when(ticker2.getIdAsInt()).thenReturn(2);
        when(ticker2.getMinimumTickSize()).thenReturn(new BigDecimal("0.01"));
        OrderTicket ticket1 = new OrderTicket();
        ticket1.setTicker(ticker1);
        ticket1.setTradeDirection(TradeDirection.BUY);
        ticket1.setType(OrderTicket.Type.MARKET);
        ticket1.setSize(new java.math.BigDecimal("1.0"));
        ticket1.setClientOrderId("cloid-1");
        OrderTicket ticket2 = new OrderTicket();
        ticket2.setTicker(ticker2);
        ticket2.setTradeDirection(TradeDirection.SELL);
        ticket2.setType(OrderTicket.Type.LIMIT);
        ticket2.setSize(new java.math.BigDecimal("2.0"));
        ticket2.setLimitPrice(new java.math.BigDecimal("99.99"));
        ticket2.setClientOrderId("cloid-2");
        BestBidOffer bestBidOffer1 = new BestBidOffer(new java.math.BigDecimal("99.00"),
                new java.math.BigDecimal("100.00"));

        BestBidOffer bestBidOffer = new BestBidOffer(new java.math.BigDecimal("999.00"),
                new java.math.BigDecimal("1000.00"));
        HyperliquidOrderTicket hyperliquidOrderTicket1 = new HyperliquidOrderTicket(bestBidOffer1, ticket1);
        HyperliquidOrderTicket hyperliquidOrderTicket2 = new HyperliquidOrderTicket(bestBidOffer, ticket2);
        var tickets = java.util.Arrays.asList(hyperliquidOrderTicket1, hyperliquidOrderTicket2);
        var orderAction = translator.translateOrderTickets(tickets);
        assertNotNull(orderAction);

    }

    @Test
    public void testTranslateOrderTicket_ReduceOnlyModifier() {
        Ticker ticker = mock(Ticker.class);
        when(ticker.getIdAsInt()).thenReturn(789);
        when(ticker.getMinimumTickSize()).thenReturn(new BigDecimal("0.01"));
        OrderTicket ticket = new OrderTicket();
        ticket.setTicker(ticker);
        ticket.setTradeDirection(TradeDirection.BUY);
        ticket.setType(OrderTicket.Type.LIMIT);
        ticket.setSize(new java.math.BigDecimal("3.0"));
        ticket.setLimitPrice(new java.math.BigDecimal("105.00"));
        ticket.setReference("cloid-3");
        ticket.getModifiers().add(OrderTicket.Modifier.REDUCE_ONLY);
        BestBidOffer bestBidOffer = new BestBidOffer(new java.math.BigDecimal("104.00"),
                new java.math.BigDecimal("105.00"));
        OrderJson orderJson = translator.translateOrderTicketToOrderJson(ticket, bestBidOffer);
        assertTrue(orderJson.reduceOnly);
    }

    @Test
    public void testGetBuySlippage() {
        // id=207, instrumentType=PERPETUAL_FUTURES, symbol=ASTER,
        // exchange=Exchange{exchangeName=HYPERLIQUID},
        // primaryExchange=Exchange{exchangeName=HYPERLIQUID}, currency=USD,
        // minimumTickSize=0.000001, contractMultiplier=1, orderSizeIncrement=1,
        // minimumOrderSize=null, expiryMonth=0, expiryYear=0, expiryDay=0, strike=null,
        // right=NONE, fundingRateInterval=1, minimumOrderSizeNotional=null],
        // price=1.8599, size=296.0, side=SELL, time=2025-09-21T04:47:17.208Z[GMT],
        // orderId=168725

        Ticker ticker = mock(Ticker.class);
        when(ticker.getMinimumTickSize()).thenReturn(new BigDecimal("0.00001"));
        Translator.SLIPPAGE_PERCENTAGE = 0;
        String price = Translator.getInstance().getBuySlippage(ticker, new BigDecimal("1.23456789"));
        assertEquals("1.2345", price);
    }
}
