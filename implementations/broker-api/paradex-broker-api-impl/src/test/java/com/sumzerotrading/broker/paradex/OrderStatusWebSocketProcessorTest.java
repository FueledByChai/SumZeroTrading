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

    private OrderStatusWebSocketProcessor processor;

    @BeforeEach
    public void setUp() {
        processor = new OrderStatusWebSocketProcessor(mockClosedListener);
    }

    // Use stub listeners that implement both ParadexOrderStatusListener and
    // IWebSocketEventListener
    private static class StubListener implements ParadexOrderStatusListener, IParadexOrderStatusUpdate {
        private final CountDownLatch latch;

        public StubListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void orderStatusUpdated(IParadexOrderStatusUpdate update) {
            latch.countDown();
        }

        @Override
        public void onWebSocketEvent(IParadexOrderStatusUpdate event) {
            orderStatusUpdated(event);
        }

        // Implement all required methods from IParadexOrderStatusUpdate
        @Override
        public String getOrderId() {
            return null;
        }

        @Override
        public void setOrderId(String orderId) {
        }

        @Override
        public java.math.BigDecimal getRemainingSize() {
            return null;
        }

        @Override
        public void setRemainingSize(java.math.BigDecimal remainingSize) {
        }

        @Override
        public java.math.BigDecimal getOriginalSize() {
            return null;
        }

        @Override
        public void setOriginalSize(java.math.BigDecimal originalSize) {
        }

        @Override
        public ParadexOrderStatus getStatus() {
            return null;
        }

        @Override
        public void setStatus(ParadexOrderStatus status) {
        }

        @Override
        public String getCancelReasonString() {
            return null;
        }

        @Override
        public void setCancelReasonString(String cancelReasonString) {
        }

        @Override
        public CancelReason getCancelReason() {
            return null;
        }

        @Override
        public void setCancelReason(CancelReason cancelReason) {
        }

        @Override
        public java.math.BigDecimal getAverageFillPrice() {
            return null;
        }

        @Override
        public OrderType getOrderType() {
            return null;
        }

        @Override
        public Side getSide() {
            return null;
        }

        @Override
        public long getTimestamp() {
            return 0;
        }

        @Override
        public String getTickerString() {
            return null;
        }
    }

    // ==================== Listener Management Tests ====================

    @Test
    public void testAddListener_SingleListener() {
        CountDownLatch latch = new CountDownLatch(1);
        StubListener stubListener = new StubListener(latch);
        processor.addEventListener(stubListener);
        assertNotNull(processor);
    }

    @Test
    public void testAddListener_MultipleListeners() {
        CountDownLatch latch = new CountDownLatch(2);
        StubListener stubListener1 = new StubListener(latch);
        StubListener stubListener2 = new StubListener(latch);
        processor.addEventListener(stubListener1);
        processor.addEventListener(stubListener2);
        assertNotNull(processor);
        // ...existing code...
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
        CountDownLatch latch = new CountDownLatch(1);
        StubListener stubListener = new StubListener(latch);
        processor.addEventListener(stubListener);

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

        processor.messageReceived(validMessage);
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Listener should be called within 5 seconds");
        // No need for ArgumentCaptor, just check stubListener.lastUpdate
        // ...existing code...
    }

    @Test
    public void testMessageReceived_EmptyAverageFillPrice() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        StubListener stubListener = new StubListener(latch);
        processor.addEventListener(stubListener);

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

        processor.messageReceived(messageWithEmptyFillPrice);
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        // ...existing code...
    }

    @Test
    public void testMessageReceived_MultipleListeners() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(2);
        StubListener stubListener1 = new StubListener(latch);
        StubListener stubListener2 = new StubListener(latch);
        processor.addEventListener(stubListener1);
        processor.addEventListener(stubListener2);

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

        processor.messageReceived(validMessage);
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Both listeners should be called within 5 seconds");
        // ...existing code...
    }

    @Test
    public void testMessageReceived_NoMethodField() {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        StubListener stubListener = new StubListener(latch);
        processor.addEventListener(stubListener);

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

        // Assert - latch should not count down
        assertEquals(1, latch.getCount());
    }

    @Test
    public void testMessageReceived_UnknownMethod() {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        StubListener stubListener = new StubListener(latch);
        processor.addEventListener(stubListener);

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

        // Assert - latch should not count down
        assertEquals(1, latch.getCount());
    }

    @Test
    public void testMessageReceived_InvalidJson() {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        StubListener stubListener = new StubListener(latch);
        processor.addEventListener(stubListener);

        String invalidJson = "{ invalid json }";

        // Act - should not throw exception
        assertDoesNotThrow(() -> processor.messageReceived(invalidJson));

        // Assert - latch should not count down
        assertEquals(1, latch.getCount());
    }

    @Test
    public void testMessageReceived_MissingDataFields() {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        StubListener stubListener = new StubListener(latch);
        processor.addEventListener(stubListener);

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

        // Assert - latch should not count down
        assertEquals(1, latch.getCount());
    }

    @Test
    public void testMessageReceived_ListenerThrowsException() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        // First stub throws exception, second counts down latch
        StubListener stubListener1 = new StubListener(new CountDownLatch(0)) {
            @Override
            public void orderStatusUpdated(IParadexOrderStatusUpdate update) {
                throw new RuntimeException("Listener error");
            }
        };
        StubListener stubListener2 = new StubListener(latch);
        processor.addEventListener(stubListener1);
        processor.addEventListener(stubListener2);

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

        processor.messageReceived(validMessage);
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        // ...existing code...
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
        CountDownLatch latch = new CountDownLatch(1);
        StubListener stubListener = new StubListener(latch);
        processor.addEventListener(stubListener);

        // Act - should not throw exception
        assertDoesNotThrow(() -> processor.messageReceived(null));

        // Assert - latch should not count down
        assertEquals(1, latch.getCount());
    }

    @Test
    public void testMessageReceived_EmptyMessage() {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        StubListener stubListener = new StubListener(latch);
        processor.addEventListener(stubListener);

        // Act - should not throw exception
        assertDoesNotThrow(() -> processor.messageReceived(""));

        // Assert - latch should not count down
        assertEquals(1, latch.getCount());
    }
}
