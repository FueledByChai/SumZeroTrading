package com.sumzerotrading.broker.hyperliquid;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sumzerotrading.BestBidOffer;
import com.sumzerotrading.broker.hyperliquid.translators.ITranslator;
// import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.websocket.IHyperliquidRestApi;
import com.sumzerotrading.hyperliquid.websocket.json.PlaceOrderRequest;

@ExtendWith(MockitoExtension.class)
public class HyperliquidBrokerTest {

    @Mock
    IHyperliquidRestApi mockRestApi;
    @Mock
    OrderTicket mockOrderTicket;
    @Mock
    ScheduledExecutorService mockScheduler;
    @Mock
    ExecutorService mockExecutor;

    @Mock
    ITranslator mockTranslator;

    HyperliquidBroker broker;

    @BeforeEach
    public void setup() {
        broker = Mockito.spy(new HyperliquidBroker(mockRestApi));
        broker.translator = mockTranslator;
    }

    @Test
    public void testCancelOrderById() {
        broker.connected = true;
        assertDoesNotThrow(() -> broker.cancelOrder("orderId"));
    }

    @Test
    public void testCancelOrderByOrderTicket() {
        broker.connected = true;
        when(mockOrderTicket.getOrderId()).thenReturn("orderId");
        assertDoesNotThrow(() -> broker.cancelOrder(mockOrderTicket));
        verify(mockOrderTicket).getOrderId();
    }

    @Test
    public void testPlaceOrder() {

        broker.connected = true;
        // Mock BestBidOffer and bestBidOfferMap
        BestBidOffer mockBBO = mock(com.sumzerotrading.BestBidOffer.class);
        Ticker mockTicker = mock(com.sumzerotrading.data.Ticker.class);
        PlaceOrderRequest mockPlaceOrderRequest = mock(PlaceOrderRequest.class);
        when(mockOrderTicket.getTicker()).thenReturn(mockTicker);
        when(mockOrderTicket.getTicker().getSymbol()).thenReturn("BTCUSD");
        HyperliquidOrderTicket hyperliquidOrderTicket = new HyperliquidOrderTicket(mockBBO, mockOrderTicket);
        when(mockTranslator.translateOrderTickets(hyperliquidOrderTicket)).thenReturn(mockPlaceOrderRequest);
        broker.bestBidOfferMap.put("BTCUSD", mockBBO);
        doNothing().when(broker).checkConnected();

        broker.placeOrder(mockOrderTicket);

        verify(broker).checkConnected();
        verify(mockRestApi).placeOrder(mockPlaceOrderRequest);

    }

    @Test
    public void testGetNextOrderId() {
        assertEquals("", broker.getNextOrderId());
    }

    @Test
    public void testConnectSetsConnectedTrue() {
        broker.connect();
        assertTrue(broker.connected);
        assertNotNull(broker.orderEventExecutor);
    }

    @Test
    public void testDisconnectSetsConnectedFalse() {
        broker.connect();
        broker.disconnect();
        assertFalse(broker.connected);
    }

    @Test
    public void testUnsupportedMethodsThrow() {
        assertThrows(UnsupportedOperationException.class, () -> broker.isConnected());
        assertThrows(UnsupportedOperationException.class, () -> broker.requestOrderStatus("id"));
        assertThrows(UnsupportedOperationException.class, () -> broker.getOpenOrders());
        assertThrows(UnsupportedOperationException.class, () -> broker.cancelAndReplaceOrder("id", mockOrderTicket));
        assertThrows(UnsupportedOperationException.class, () -> broker.getAllPositions());
    }

    @Test
    public void testCheckConnectedNoop() {
        assertDoesNotThrow(() -> broker.checkConnected());
    }
}
