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

import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.hyperliquid.HyperliquidPositionUpdate;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.websocket.HyperliquidTickerRegistry;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.broker.hyperliquid.json.OrderJson;

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
        assertNull(Translator.translatePositions(null));
    }

    @Test
    public void testTranslatePositions_EmptyList() {
        List<Position> result = Translator.translatePositions(Collections.emptyList());
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
        List<Position> positions = Translator.translatePositions(updates);
        assertEquals(2, positions.size());
        for (Position pos : positions) {
            assertEquals(mockTicker, pos.getTicker());
        }
    }

    @Test
    public void testTranslatePosition_NullInput() {
        assertNull(Translator.translatePosition(null));
    }

    @Test
    public void testTranslatePosition_ValidInput() {
        when(mockUpdate1.getTicker()).thenReturn("BTCUSD");
        when(mockUpdate1.getSize()).thenReturn(java.math.BigDecimal.valueOf(2.0));
        when(mockUpdate1.getEntryPrice()).thenReturn(java.math.BigDecimal.valueOf(50000.0));
        when(mockUpdate1.getLiquidationPrice()).thenReturn(java.math.BigDecimal.valueOf(45000.0));
        when(mockRegistry.lookupByBrokerSymbol("BTCUSD")).thenReturn(mockTicker);

        Position pos = Translator.translatePosition(mockUpdate1);
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
        when(ticker.formatPrice(new java.math.BigDecimal("101.0505000"))).thenReturn(new BigDecimal("101.05"));
        OrderTicket ticket = new OrderTicket();
        ticket.setTicker(ticker);
        ticket.setTradeDirection(TradeDirection.BUY);
        ticket.setType(OrderTicket.Type.MARKET);
        ticket.setSize(new java.math.BigDecimal("1.5"));
        ticket.setReference("cloid-1");
        java.math.BigDecimal currentBid = new java.math.BigDecimal("100.00");
        java.math.BigDecimal currentAsk = new java.math.BigDecimal("101.00");

        OrderJson orderJson = Translator.translateOrderTicketToOrderJson(ticket, currentBid, currentAsk);
        assertEquals(123, orderJson.assetId);
        assertTrue(orderJson.isBuy);
        assertEquals("1.5", orderJson.size);
        assertEquals("cloid-1", orderJson.clientOrderId);
        assertEquals("101.05", orderJson.price); // 101 + 0.05% slippage
        assertNotNull(orderJson.type);
        assertFalse(orderJson.reduceOnly);
        assertTrue(orderJson.type instanceof com.sumzerotrading.broker.hyperliquid.json.LimitType);
    }

    @Test
    public void testTranslateOrderTicketToOrderJson_LimitSell_PostOnly() {
        Ticker ticker = mock(Ticker.class);
        when(ticker.getIdAsInt()).thenReturn(456);
        OrderTicket ticket = new OrderTicket();
        ticket.setTicker(ticker);
        ticket.setTradeDirection(TradeDirection.SELL);
        ticket.setType(OrderTicket.Type.LIMIT);
        ticket.setSize(new java.math.BigDecimal("2.0"));
        ticket.setLimitPrice(new java.math.BigDecimal("99.99"));
        ticket.setReference("cloid-2");
        ticket.getModifiers().add(OrderTicket.Modifier.POST_ONLY);
        java.math.BigDecimal currentBid = new java.math.BigDecimal("99.00");
        java.math.BigDecimal currentAsk = new java.math.BigDecimal("100.00");

        OrderJson orderJson = Translator.translateOrderTicketToOrderJson(ticket, currentBid, currentAsk);
        assertEquals(456, orderJson.assetId);
        assertFalse(orderJson.isBuy);
        assertEquals("2.0", orderJson.size);
        assertEquals("cloid-2", orderJson.clientOrderId);
        assertEquals("99.99", orderJson.price);
        assertNotNull(orderJson.type);
        assertFalse(orderJson.reduceOnly);
        assertTrue(orderJson.type instanceof com.sumzerotrading.broker.hyperliquid.json.LimitType);
        com.sumzerotrading.broker.hyperliquid.json.LimitType limitType = (com.sumzerotrading.broker.hyperliquid.json.LimitType) orderJson.type;
        assertEquals(com.sumzerotrading.broker.hyperliquid.json.LimitType.TimeInForce.ALO, limitType.tif);
    }

    @Test
    public void testTranslateOrderTicket_MultipleTickets() {
        Ticker ticker1 = mock(Ticker.class);
        when(ticker1.getIdAsInt()).thenReturn(1);
        when(ticker1.formatPrice(new java.math.BigDecimal("100.0500000"))).thenReturn(new BigDecimal("100.05"));
        Ticker ticker2 = mock(Ticker.class);
        when(ticker2.getIdAsInt()).thenReturn(2);
        OrderTicket ticket1 = new OrderTicket();
        ticket1.setTicker(ticker1);
        ticket1.setTradeDirection(TradeDirection.BUY);
        ticket1.setType(OrderTicket.Type.MARKET);
        ticket1.setSize(new java.math.BigDecimal("1.0"));
        ticket1.setReference("cloid-1");
        OrderTicket ticket2 = new OrderTicket();
        ticket2.setTicker(ticker2);
        ticket2.setTradeDirection(TradeDirection.SELL);
        ticket2.setType(OrderTicket.Type.LIMIT);
        ticket2.setSize(new java.math.BigDecimal("2.0"));
        ticket2.setLimitPrice(new java.math.BigDecimal("99.99"));
        ticket2.setReference("cloid-2");
        java.math.BigDecimal currentBid = new java.math.BigDecimal("99.00");
        java.math.BigDecimal currentAsk = new java.math.BigDecimal("100.00");
        var tickets = java.util.Arrays.asList(ticket1, ticket2);
        var orderAction = Translator.translateOrderTickets(tickets, currentBid, currentAsk);
        assertNotNull(orderAction);
        assertEquals(2, orderAction.toJson().getAsJsonArray("orders").size());
    }

    @Test
    public void testTranslateOrderTicket_ReduceOnlyModifier() {
        Ticker ticker = mock(Ticker.class);
        when(ticker.getIdAsInt()).thenReturn(789);
        OrderTicket ticket = new OrderTicket();
        ticket.setTicker(ticker);
        ticket.setTradeDirection(TradeDirection.BUY);
        ticket.setType(OrderTicket.Type.LIMIT);
        ticket.setSize(new java.math.BigDecimal("3.0"));
        ticket.setLimitPrice(new java.math.BigDecimal("105.00"));
        ticket.setReference("cloid-3");
        ticket.getModifiers().add(OrderTicket.Modifier.REDUCE_ONLY);
        java.math.BigDecimal currentBid = new java.math.BigDecimal("104.00");
        java.math.BigDecimal currentAsk = new java.math.BigDecimal("105.00");
        OrderJson orderJson = Translator.translateOrderTicketToOrderJson(ticket, currentBid, currentAsk);
        assertTrue(orderJson.reduceOnly);
    }
}
