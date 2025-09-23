package com.sumzerotrading.broker.hyperliquid;

import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.hyperliquid.websocket.IHyperliquidRestApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    HyperliquidBroker broker;

    @BeforeEach
    public void setup() {
        broker = new HyperliquidBroker(mockRestApi);
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
        assertDoesNotThrow(() -> broker.placeOrder(mockOrderTicket));
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
