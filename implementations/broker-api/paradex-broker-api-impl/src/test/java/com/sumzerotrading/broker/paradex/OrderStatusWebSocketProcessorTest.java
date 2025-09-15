package com.sumzerotrading.broker.paradex;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sumzerotrading.paradex.common.api.order.OrderType;
import com.sumzerotrading.paradex.common.api.order.Side;
import com.sumzerotrading.websocket.IWebSocketClosedListener;

/**
 * JUnit 5/Mockito test class for OrderStatusWebSocketProcessor Tests WebSocket
 * message processing, listener management, and error handling
 */
@ExtendWith(MockitoExtension.class)
public class OrderStatusWebSocketProcessorTest {

    @Mock
    private IWebSocketClosedListener mockClosedListener;

    @Mock
    private ParadexOrderStatusListener mockListener1;

    @Mock
    private ParadexOrderStatusListener mockListener2;

    private OrderStatusWebSocketProcessor processor;

    @BeforeEach
    public void setUp() {
        processor = new OrderStatusWebSocketProcessor(mockClosedListener);
    }

    // ==================== Constructor Tests ====================

    @Test
    public void testConstructor_WithClosedListenerOnly() {
        // Act
        OrderStatusWebSocketProcessor proc = new OrderStatusWebSocketProcessor(mockClosedListener);

        // Assert
        assertEquals(mockClosedListener, proc.closedListener);
        assertTrue(proc.listeners.isEmpty());
    }

    @Test
    public void testConstructor_WithListenerAndClosedListener() {
        // Act
        OrderStatusWebSocketProcessor proc = new OrderStatusWebSocketProcessor(mockListener1, mockClosedListener);

        // Assert
        assertEquals(mockClosedListener, proc.closedListener);
        assertEquals(1, proc.listeners.size());
        assertEquals(mockListener1, proc.listeners.get(0));
    }

    // ==================== Listener Management Tests ====================

    @Test
    public void testAddListener_SingleListener() {
        // Act
        processor.addListener(mockListener1);

        // Assert
        assertEquals(1, processor.listeners.size());
        assertEquals(mockListener1, processor.listeners.get(0));
    }

    @Test
    public void testAddListener_MultipleListeners() {
        // Act
        processor.addListener(mockListener1);
        processor.addListener(mockListener2);

        // Assert
        assertEquals(2, processor.listeners.size());
        assertEquals(mockListener1, processor.listeners.get(0));
        assertEquals(mockListener2, processor.listeners.get(1));
    }

    // ==================== Connection Event Tests ====================

    @Test
    public void testConnectionClosed() {
        // Act
        processor.connectionClosed(1000, "Normal closure", false);

        // Assert
        verify(mockClosedListener).connectionClosed();
    }

    @Test
    public void testConnectionError() {
        // Arrange
        Exception testException = new RuntimeException("Test error");

        // Act
        processor.connectionError(testException);

        // Assert
        verify(mockClosedListener).connectionClosed();
    }

    @Test
    public void testConnectionEstablished() {
        // Act - should not throw exception
        assertDoesNotThrow(() -> processor.connectionEstablished());
    }

    @Test
    public void testConnectionOpened() {
        // Act - should not throw exception
        assertDoesNotThrow(() -> processor.connectionOpened());
    }

    // ==================== Message Processing Tests ====================

    @Test
    public void testMessageReceived_ValidSubscriptionMessage() throws InterruptedException {
        // Arrange
        processor.addListener(mockListener1);

        String validMessage = """
                {
                    "method": "subscription",
                    "params": {
                        "data": {
                            "id": "order123",
                            "remaining_size": "5.0",
                            "status": "OPEN",
                            "size": "10.0",
                            "cancel_reason": "NONE",
                            "type": "LIMIT",
                            "avg_fill_price": "50.25",
                            "timestamp": 1693036800000,
                            "side": "BUY",
                            "market": "BTC-USD"
                        }
                    }
                }
                """;

        // Use CountDownLatch to wait for async processing
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(mockListener1).orderStatusUpdated(any(IParadexOrderStatusUpdate.class));

        // Act
        processor.messageReceived(validMessage);

        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Listener should be called within 1 second");

        ArgumentCaptor<IParadexOrderStatusUpdate> captor = ArgumentCaptor.forClass(IParadexOrderStatusUpdate.class);
        verify(mockListener1).orderStatusUpdated(captor.capture());

        IParadexOrderStatusUpdate captured = captor.getValue();
        assertEquals("order123", captured.getOrderId());
        assertEquals("BTC-USD", captured.getTickerString());
        assertEquals(0, new BigDecimal("5.0").compareTo(captured.getRemainingSize()));
        assertEquals(0, new BigDecimal("10.0").compareTo(captured.getOriginalSize()));
        assertEquals("OPEN", captured.getStatus().toString());
        assertEquals("NONE", captured.getCancelReason().toString());
        assertEquals(OrderType.LIMIT, captured.getOrderType());
        assertEquals(0, new BigDecimal("50.25").compareTo(captured.getAverageFillPrice()));
        assertEquals(1693036800000L, captured.getTimestamp());
        assertEquals(Side.BUY, captured.getSide());
    }

    @Test
    public void testMessageReceived_EmptyAverageFillPrice() throws InterruptedException {
        // Arrange
        processor.addListener(mockListener1);

        String messageWithEmptyFillPrice = """
                {
                    "method": "subscription",
                    "params": {
                        "data": {
                            "id": "order456",
                            "remaining_size": "10.0",
                            "status": "NEW",
                            "size": "10.0",
                            "cancel_reason": "NONE",
                            "type": "MARKET",
                            "avg_fill_price": "",
                            "timestamp": 1693036900000,
                            "side": "SELL",
                            "market": "ETH-USD"
                        }
                    }
                }
                """;

        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(mockListener1).orderStatusUpdated(any(IParadexOrderStatusUpdate.class));

        // Act
        processor.messageReceived(messageWithEmptyFillPrice);

        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS));

        ArgumentCaptor<IParadexOrderStatusUpdate> captor = ArgumentCaptor.forClass(IParadexOrderStatusUpdate.class);
        verify(mockListener1).orderStatusUpdated(captor.capture());

        IParadexOrderStatusUpdate captured = captor.getValue();
        assertEquals(0, BigDecimal.ZERO.compareTo(captured.getAverageFillPrice()));
    }

    @Test
    public void testMessageReceived_MultipleListeners() throws InterruptedException {
        // Arrange
        processor.addListener(mockListener1);
        processor.addListener(mockListener2);

        String validMessage = """
                {
                    "method": "subscription",
                    "params": {
                        "data": {
                            "id": "order789",
                            "remaining_size": "0.0",
                            "status": "CLOSED",
                            "size": "5.0",
                            "cancel_reason": "NONE",
                            "type": "LIMIT",
                            "avg_fill_price": "100.50",
                            "timestamp": 1693037000000,
                            "side": "BUY",
                            "market": "SOL-USD"
                        }
                    }
                }
                """;

        CountDownLatch latch = new CountDownLatch(2);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(mockListener1).orderStatusUpdated(any(IParadexOrderStatusUpdate.class));

        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(mockListener2).orderStatusUpdated(any(IParadexOrderStatusUpdate.class));

        // Act
        processor.messageReceived(validMessage);

        // Assert
        assertTrue(latch.await(2, TimeUnit.SECONDS), "Both listeners should be called within 2 seconds");
        verify(mockListener1).orderStatusUpdated(any(IParadexOrderStatusUpdate.class));
        verify(mockListener2).orderStatusUpdated(any(IParadexOrderStatusUpdate.class));
    }

    @Test
    public void testMessageReceived_NoMethodField() {
        // Arrange
        processor.addListener(mockListener1);

        String messageWithoutMethod = """
                {
                    "params": {
                        "data": {
                            "id": "order123"
                        }
                    }
                }
                """;

        // Act
        processor.messageReceived(messageWithoutMethod);

        // Assert - should not call listener
        verifyNoInteractions(mockListener1);
    }

    @Test
    public void testMessageReceived_UnknownMethod() {
        // Arrange
        processor.addListener(mockListener1);

        String messageWithUnknownMethod = """
                {
                    "method": "unknown_method",
                    "params": {
                        "data": {
                            "id": "order123"
                        }
                    }
                }
                """;

        // Act
        processor.messageReceived(messageWithUnknownMethod);

        // Assert - should not call listener
        verifyNoInteractions(mockListener1);
    }

    @Test
    public void testMessageReceived_InvalidJson() {
        // Arrange
        processor.addListener(mockListener1);

        String invalidJson = "{ invalid json }";

        // Act - should not throw exception
        assertDoesNotThrow(() -> processor.messageReceived(invalidJson));

        // Assert - should not call listener
        verifyNoInteractions(mockListener1);
    }

    @Test
    public void testMessageReceived_MissingDataFields() {
        // Arrange
        processor.addListener(mockListener1);

        String messageWithMissingFields = """
                {
                    "method": "subscription",
                    "params": {
                        "data": {
                            "id": "order123"
                        }
                    }
                }
                """;

        // Act - should not throw exception
        assertDoesNotThrow(() -> processor.messageReceived(messageWithMissingFields));

        // Assert - should not call listener
        verifyNoInteractions(mockListener1);
    }

    @Test
    public void testMessageReceived_ListenerThrowsException() throws InterruptedException {
        // Arrange
        processor.addListener(mockListener1);
        processor.addListener(mockListener2); // This one should still be called

        String validMessage = """
                {
                    "method": "subscription",
                    "params": {
                        "data": {
                            "id": "order999",
                            "remaining_size": "2.5",
                            "status": "OPEN",
                            "size": "5.0",
                            "cancel_reason": "NONE",
                            "type": "STOP",
                            "avg_fill_price": "75.00",
                            "timestamp": 1693037100000,
                            "side": "SELL",
                            "market": "AVAX-USD"
                        }
                    }
                }
                """;

        CountDownLatch latch = new CountDownLatch(1);

        // First listener throws exception
        doThrow(new RuntimeException("Listener error")).when(mockListener1).orderStatusUpdated(any());

        // Second listener should still be called
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(mockListener2).orderStatusUpdated(any(IParadexOrderStatusUpdate.class));

        // Act
        processor.messageReceived(validMessage);

        // Assert - both listeners should be called despite first one throwing exception
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(mockListener1).orderStatusUpdated(any(IParadexOrderStatusUpdate.class));
        verify(mockListener2).orderStatusUpdated(any(IParadexOrderStatusUpdate.class));
    }

    // ==================== Edge Case Tests ====================

    @Test
    public void testMessageReceived_NoListeners() {
        // Arrange - no listeners added
        String validMessage = """
                {
                    "method": "subscription",
                    "params": {
                        "data": {
                            "id": "order123",
                            "remaining_size": "5.0",
                            "status": "OPEN",
                            "size": "10.0",
                            "cancel_reason": "NONE",
                            "type": "LIMIT",
                            "avg_fill_price": "50.25",
                            "timestamp": 1693036800000,
                            "side": "BUY",
                            "market": "BTC-USD"
                        }
                    }
                }
                """;

        // Act - should not throw exception
        assertDoesNotThrow(() -> processor.messageReceived(validMessage));
    }

    @Test
    public void testMessageReceived_NullMessage() {
        // Arrange
        processor.addListener(mockListener1);

        // Act - should not throw exception
        assertDoesNotThrow(() -> processor.messageReceived(null));

        // Assert
        verifyNoInteractions(mockListener1);
    }

    @Test
    public void testMessageReceived_EmptyMessage() {
        // Arrange
        processor.addListener(mockListener1);

        // Act - should not throw exception
        assertDoesNotThrow(() -> processor.messageReceived(""));

        // Assert
        verifyNoInteractions(mockListener1);
    }
}
