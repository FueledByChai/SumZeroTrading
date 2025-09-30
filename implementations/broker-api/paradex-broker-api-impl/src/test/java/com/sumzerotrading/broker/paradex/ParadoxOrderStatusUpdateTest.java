package com.sumzerotrading.broker.paradex;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sumzerotrading.paradex.common.api.order.OrderType;
import com.sumzerotrading.paradex.common.api.order.Side;
import com.sumzerotrading.paradex.common.api.ws.orderstatus.CancelReason;
import com.sumzerotrading.paradex.common.api.ws.orderstatus.ParadexOrderStatus;
import com.sumzerotrading.paradex.common.api.ws.orderstatus.ParadoxOrderStatusUpdate;

/**
 * Comprehensive JUnit 5 test class for ParadoxOrderStatusUpdate
 * 
 * @author GitHub Copilot
 */
public class ParadoxOrderStatusUpdateTest {

        private ParadoxOrderStatusUpdate orderStatusUpdate;
        private String tickerString;
        private String orderId;
        private BigDecimal remainingSize;
        private BigDecimal originalSize;
        private String status;
        private String cancelReasonString;
        private BigDecimal averageFillPrice;
        private String orderType;
        private String side;
        private long timestamp;

        @BeforeEach
        public void setUp() {
                tickerString = "BTC-USD";
                orderId = "order123";
                remainingSize = new BigDecimal("5.0");
                originalSize = new BigDecimal("10.0");
                status = "OPEN";
                cancelReasonString = "NONE";
                averageFillPrice = new BigDecimal("50000.00");
                orderType = "LIMIT";
                side = "BUY";
                timestamp = System.currentTimeMillis();
        }

        @Test
        public void testConstructor_ValidParameters_CreatesObjectSuccessfully() {
                // Act
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, cancelReasonString, averageFillPrice, orderType, side, timestamp);

                // Assert
                assertNotNull(orderStatusUpdate);
                assertEquals(tickerString, orderStatusUpdate.getTickerString());
                assertEquals(orderId, orderStatusUpdate.getOrderId());
                assertEquals(remainingSize, orderStatusUpdate.getRemainingSize());
                assertEquals(originalSize, orderStatusUpdate.getOriginalSize());
                assertEquals(ParadexOrderStatus.OPEN, orderStatusUpdate.getStatus());
                assertEquals(cancelReasonString, orderStatusUpdate.getCancelReasonString());
                assertEquals(CancelReason.NONE, orderStatusUpdate.getCancelReason());
                assertEquals(averageFillPrice, orderStatusUpdate.getAverageFillPrice());
                assertEquals(OrderType.LIMIT, orderStatusUpdate.getOrderType());
                assertEquals(Side.BUY, orderStatusUpdate.getSide());
                assertEquals(timestamp, orderStatusUpdate.getTimestamp());
        }

        @Test
        public void testConstructor_AllParadexOrderStatusValues() {
                // Test NEW status
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                "NEW", cancelReasonString, averageFillPrice, orderType, side, timestamp);
                assertEquals(ParadexOrderStatus.NEW, orderStatusUpdate.getStatus());

                // Test OPEN status
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                "OPEN", cancelReasonString, averageFillPrice, orderType, side, timestamp);
                assertEquals(ParadexOrderStatus.OPEN, orderStatusUpdate.getStatus());

                // Test CLOSED status
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                "CLOSED", cancelReasonString, averageFillPrice, orderType, side, timestamp);
                assertEquals(ParadexOrderStatus.CLOSED, orderStatusUpdate.getStatus());

                // Test UNTRIGGERED status
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                "UNTRIGGERED", cancelReasonString, averageFillPrice, orderType, side, timestamp);
                assertEquals(ParadexOrderStatus.UNTRIGGERED, orderStatusUpdate.getStatus());
        }

        @Test
        public void testConstructor_AllCancelReasonValues() {
                // Test NONE cancel reason
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, "NONE", averageFillPrice, orderType, side, timestamp);
                assertEquals(CancelReason.NONE, orderStatusUpdate.getCancelReason());

                // Test POST_ONLY_WOULD_CROSS cancel reason
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, "POST_ONLY_WOULD_CROSS", averageFillPrice, orderType, side, timestamp);
                assertEquals(CancelReason.POST_ONLY_WOULD_CROSS, orderStatusUpdate.getCancelReason());

                // Test USER_CANCELED cancel reason
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, "USER_CANCELED", averageFillPrice, orderType, side, timestamp);
                assertEquals(CancelReason.USER_CANCELED, orderStatusUpdate.getCancelReason());
        }

        @Test
        public void testConstructor_InvalidCancelReason_DefaultsToNone() {
                // Act
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, "INVALID_REASON", averageFillPrice, orderType, side, timestamp);

                // Assert
                assertEquals(CancelReason.NONE, orderStatusUpdate.getCancelReason());
                assertEquals("INVALID_REASON", orderStatusUpdate.getCancelReasonString());
        }

        @Test
        public void testConstructor_NullCancelReason_DefaultsToNone() {
                // Act
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, null, averageFillPrice, orderType, side, timestamp);

                // Assert
                assertEquals(CancelReason.NONE, orderStatusUpdate.getCancelReason());
                assertNull(orderStatusUpdate.getCancelReasonString());
        }

        @Test
        public void testConstructor_EmptyCancelReason_DefaultsToNone() {
                // Act
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, "", averageFillPrice, orderType, side, timestamp);

                // Assert
                assertEquals(CancelReason.NONE, orderStatusUpdate.getCancelReason());
                assertEquals("", orderStatusUpdate.getCancelReasonString());
        }

        @Test
        public void testConstructor_AllOrderTypes() {
                // Test MARKET order type
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, cancelReasonString, averageFillPrice, "MARKET", side, timestamp);
                assertEquals(OrderType.MARKET, orderStatusUpdate.getOrderType());

                // Test LIMIT order type
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, cancelReasonString, averageFillPrice, "LIMIT", side, timestamp);
                assertEquals(OrderType.LIMIT, orderStatusUpdate.getOrderType());

                // Test STOP order type
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, cancelReasonString, averageFillPrice, "STOP", side, timestamp);
                assertEquals(OrderType.STOP, orderStatusUpdate.getOrderType());
        }

        @Test
        public void testConstructor_AllSides() {
                // Test BUY side
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, cancelReasonString, averageFillPrice, orderType, "BUY", timestamp);
                assertEquals(Side.BUY, orderStatusUpdate.getSide());

                // Test SELL side
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, cancelReasonString, averageFillPrice, orderType, "SELL", timestamp);
                assertEquals(Side.SELL, orderStatusUpdate.getSide());
        }

        @Test
        public void testConstructor_InvalidOrderStatus_ThrowsException() {
                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> {
                        new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                        "INVALID_STATUS", cancelReasonString, averageFillPrice, orderType, side,
                                        timestamp);
                });
        }

        @Test
        public void testConstructor_InvalidOrderType_ThrowsException() {
                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> {
                        new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize, status,
                                        cancelReasonString, averageFillPrice, "INVALID_TYPE", side, timestamp);
                });
        }

        @Test
        public void testConstructor_InvalidSide_ThrowsException() {
                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> {
                        new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize, status,
                                        cancelReasonString, averageFillPrice, orderType, "INVALID_SIDE", timestamp);
                });
        }

        @Test
        public void testSetOrderId() {
                // Arrange
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, cancelReasonString, averageFillPrice, orderType, side, timestamp);
                String newOrderId = "newOrder456";

                // Act
                orderStatusUpdate.setOrderId(newOrderId);

                // Assert
                assertEquals(newOrderId, orderStatusUpdate.getOrderId());
        }

        @Test
        public void testSetRemainingSize() {
                // Arrange
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, cancelReasonString, averageFillPrice, orderType, side, timestamp);
                BigDecimal newRemainingSize = new BigDecimal("3.5");

                // Act
                orderStatusUpdate.setRemainingSize(newRemainingSize);

                // Assert
                assertEquals(newRemainingSize, orderStatusUpdate.getRemainingSize());
        }

        @Test
        public void testSetStatus() {
                // Arrange
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, cancelReasonString, averageFillPrice, orderType, side, timestamp);

                // Act
                orderStatusUpdate.setStatus(ParadexOrderStatus.CLOSED);

                // Assert
                assertEquals(ParadexOrderStatus.CLOSED, orderStatusUpdate.getStatus());
        }

        @Test
        public void testSetCancelReasonString() {
                // Arrange
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, cancelReasonString, averageFillPrice, orderType, side, timestamp);
                String newCancelReason = "MARKET_CLOSED";

                // Act
                orderStatusUpdate.setCancelReasonString(newCancelReason);

                // Assert
                assertEquals(newCancelReason, orderStatusUpdate.getCancelReasonString());
        }

        @Test
        public void testSetOriginalSize() {
                // Arrange
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, cancelReasonString, averageFillPrice, orderType, side, timestamp);
                BigDecimal newOriginalSize = new BigDecimal("15.0");

                // Act
                orderStatusUpdate.setOriginalSize(newOriginalSize);

                // Assert
                assertEquals(newOriginalSize, orderStatusUpdate.getOriginalSize());
        }

        @Test
        public void testSetCancelReason() {
                // Arrange
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, cancelReasonString, averageFillPrice, orderType, side, timestamp);

                // Act
                orderStatusUpdate.setCancelReason(CancelReason.USER_CANCELED);

                // Assert
                assertEquals(CancelReason.USER_CANCELED, orderStatusUpdate.getCancelReason());
        }

        @Test
        public void testToString_ContainsAllFields() {
                // Arrange
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, cancelReasonString, averageFillPrice, orderType, side, timestamp);

                // Act
                String result = orderStatusUpdate.toString();

                // Assert
                assertNotNull(result);
                assertTrue(result.contains(tickerString));
                assertTrue(result.contains(orderId));
                assertTrue(result.contains(originalSize.toString()));
                assertTrue(result.contains(remainingSize.toString()));
                assertTrue(result.contains(status));
                assertTrue(result.contains(cancelReasonString));
                assertTrue(result.contains(averageFillPrice.toString()));
                assertTrue(result.contains(orderType));
                assertTrue(result.contains(side));
        }

        @Test
        public void testBigDecimalFields_HandlesZeroValues() {
                // Arrange
                BigDecimal zeroSize = BigDecimal.ZERO;
                BigDecimal zeroPrice = BigDecimal.ZERO;

                // Act
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, zeroSize, zeroSize, status,
                                cancelReasonString, zeroPrice, orderType, side, timestamp);

                // Assert
                assertEquals(zeroSize, orderStatusUpdate.getRemainingSize());
                assertEquals(zeroSize, orderStatusUpdate.getOriginalSize());
                assertEquals(zeroPrice, orderStatusUpdate.getAverageFillPrice());
        }

        @Test
        public void testBigDecimalFields_HandlesNullValues() {
                // Act
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, null, null, status,
                                cancelReasonString, null, orderType, side, timestamp);

                // Assert
                assertNull(orderStatusUpdate.getRemainingSize());
                assertNull(orderStatusUpdate.getOriginalSize());
                assertNull(orderStatusUpdate.getAverageFillPrice());
        }

        @Test
        public void testTimestamp_HandlesZero() {
                // Act
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, cancelReasonString, averageFillPrice, orderType, side, 0);

                // Assert
                assertEquals(0, orderStatusUpdate.getTimestamp());
        }

        @Test
        public void testTimestamp_HandlesNegativeValue() {
                // Arrange
                long negativeTimestamp = -1L;

                // Act
                orderStatusUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize, originalSize,
                                status, cancelReasonString, averageFillPrice, orderType, side, negativeTimestamp);

                // Assert
                assertEquals(negativeTimestamp, orderStatusUpdate.getTimestamp());
        }

        @Test
        public void testTickerString_VariousFormats() {
                // Test different ticker formats
                String[] tickers = { "BTC-USD", "ETH/USDT", "SOL_USDC", "AVAX", "DOT-BTC" };

                for (String ticker : tickers) {
                        orderStatusUpdate = new ParadoxOrderStatusUpdate(ticker, orderId, remainingSize, originalSize,
                                        status, cancelReasonString, averageFillPrice, orderType, side, timestamp);

                        assertEquals(ticker, orderStatusUpdate.getTickerString());
                }
        }

        @Test
        public void testPartiallyFilledOrder_Scenario() {
                // Arrange - Simulate a partially filled order
                BigDecimal fullSize = new BigDecimal("10.0");
                BigDecimal partialRemaining = new BigDecimal("3.0");
                BigDecimal fillPrice = new BigDecimal("45000.50");

                // Act
                orderStatusUpdate = new ParadoxOrderStatusUpdate("BTC-USD", "partial123", partialRemaining, fullSize,
                                "OPEN", "NONE", fillPrice, "LIMIT", "BUY", timestamp);

                // Assert
                assertEquals(fullSize, orderStatusUpdate.getOriginalSize());
                assertEquals(partialRemaining, orderStatusUpdate.getRemainingSize());
                assertEquals(fillPrice, orderStatusUpdate.getAverageFillPrice());
                assertEquals(ParadexOrderStatus.OPEN, orderStatusUpdate.getStatus());
                assertEquals(CancelReason.NONE, orderStatusUpdate.getCancelReason());
        }

        @Test
        public void testCanceledOrder_Scenario() {
                // Arrange - Simulate a canceled order
                // Act
                orderStatusUpdate = new ParadoxOrderStatusUpdate("ETH-USD", "cancel456", remainingSize, originalSize,
                                "CLOSED", "USER_CANCELED", averageFillPrice, "MARKET", "SELL", timestamp);

                // Assert
                assertEquals(ParadexOrderStatus.CLOSED, orderStatusUpdate.getStatus());
                assertEquals(CancelReason.USER_CANCELED, orderStatusUpdate.getCancelReason());
                assertEquals("USER_CANCELED", orderStatusUpdate.getCancelReasonString());
                assertEquals(OrderType.MARKET, orderStatusUpdate.getOrderType());
                assertEquals(Side.SELL, orderStatusUpdate.getSide());
        }

        @Test
        public void testPostOnlyWouldCrossOrder_Scenario() {
                // Arrange - Simulate a post-only order that would cross
                // Act
                orderStatusUpdate = new ParadoxOrderStatusUpdate("SOL-USD", "postonly789", originalSize, originalSize,
                                "CLOSED", "POST_ONLY_WOULD_CROSS", BigDecimal.ZERO, "LIMIT", "BUY", timestamp);

                // Assert
                assertEquals(ParadexOrderStatus.CLOSED, orderStatusUpdate.getStatus());
                assertEquals(CancelReason.POST_ONLY_WOULD_CROSS, orderStatusUpdate.getCancelReason());
                assertEquals("POST_ONLY_WOULD_CROSS", orderStatusUpdate.getCancelReasonString());
                assertEquals(BigDecimal.ZERO, orderStatusUpdate.getAverageFillPrice());
        }
}
