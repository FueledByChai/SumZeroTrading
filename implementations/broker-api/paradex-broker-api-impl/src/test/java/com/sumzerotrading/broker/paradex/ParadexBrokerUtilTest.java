package com.sumzerotrading.broker.paradex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sumzerotrading.broker.order.OrderStatus;
import com.sumzerotrading.broker.order.OrderStatus.Status;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.paradex.common.ParadexTickerRegistry;
import com.sumzerotrading.paradex.common.api.ws.orderstatus.CancelReason;
import com.sumzerotrading.paradex.common.api.ws.orderstatus.ParadexOrderStatus;
import com.sumzerotrading.paradex.common.api.ws.orderstatus.ParadoxOrderStatusUpdate;
import com.sumzerotrading.util.ITickerRegistry;

/**
 * JUnit 5/Mockito test class for ParadexBrokerUtil Tests the two main static
 * methods: translateOrderStatus and translateStatusCode
 */
@ExtendWith(MockitoExtension.class)
public class ParadexBrokerUtilTest {

    @Mock
    private ParadexTickerRegistry mockTickerRegistry;

    @Mock
    private Ticker mockTicker;

    private ITickerRegistry originalTickerRegistry;

    @BeforeEach
    public void setUp() {
        // Save original tickerRegistry and inject mock
        originalTickerRegistry = ParadexTranslator.tickerRegistry;
        ParadexTranslator.tickerRegistry = mockTickerRegistry;
    }

    @AfterEach
    public void tearDown() {
        // Restore original tickerRegistry
        ParadexTranslator.tickerRegistry = originalTickerRegistry;
    }

    @Test
    public void testTranslateOrderStatus_NewOrder_Success() {
        // Arrange
        String tickerString = "BTC-USD";
        String orderId = "order123";
        BigDecimal originalSize = new BigDecimal("10.0");
        BigDecimal remainingSize = new BigDecimal("10.0"); // No fill yet
        BigDecimal averageFillPrice = BigDecimal.ZERO;
        long timestamp = 1693036800000L;

        when(mockTickerRegistry.lookupByBrokerSymbol(tickerString)).thenReturn(mockTicker);

        ParadoxOrderStatusUpdate orderUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize,
                originalSize, "NEW", "NONE", averageFillPrice, "LIMIT", "BUY", timestamp);

        // Act
        OrderStatus result = ParadexTranslator.translateOrderStatus(orderUpdate);

        // Assert
        assertEquals(Status.NEW, result.getStatus());
        assertEquals(orderId, result.getOrderId());
        assertEquals(0, result.getFilled().compareTo(BigDecimal.ZERO)); // originalSize - remainingSize
        assertEquals(remainingSize, result.getRemaining());
        assertEquals(averageFillPrice, result.getFillPrice());
        assertEquals(mockTicker, result.getTicker());
        assertEquals(timestamp, result.getTimestamp().toInstant().toEpochMilli());

        verify(mockTickerRegistry).lookupByBrokerSymbol(tickerString);
    }

    @Test
    public void testTranslateOrderStatus_PartiallyFilled_Success() {
        // Arrange
        String tickerString = "ETH-USD";
        String orderId = "order456";
        BigDecimal originalSize = new BigDecimal("5.0");
        BigDecimal remainingSize = new BigDecimal("2.0"); // Partially filled
        BigDecimal averageFillPrice = new BigDecimal("2000.50");
        long timestamp = 1693036900000L;

        when(mockTickerRegistry.lookupByBrokerSymbol(tickerString)).thenReturn(mockTicker);

        ParadoxOrderStatusUpdate orderUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize,
                originalSize, "OPEN", "NONE", averageFillPrice, "MARKET", "SELL", timestamp);

        // Act
        OrderStatus result = ParadexTranslator.translateOrderStatus(orderUpdate);

        // Assert
        assertEquals(Status.PARTIAL_FILL, result.getStatus());
        assertEquals(0, originalSize.subtract(remainingSize).compareTo(result.getFilled()));
        assertEquals(remainingSize, result.getRemaining());
    }

    @Test
    public void testTranslateOrderStatus_FullyFilled_Success() {
        // Arrange
        String tickerString = "SOL-USD";
        String orderId = "order789";
        BigDecimal originalSize = new BigDecimal("100.0");
        BigDecimal remainingSize = BigDecimal.ZERO; // Fully filled
        BigDecimal averageFillPrice = new BigDecimal("45.25");
        long timestamp = 1693037000000L;

        when(mockTickerRegistry.lookupByBrokerSymbol(tickerString)).thenReturn(mockTicker);

        ParadoxOrderStatusUpdate orderUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize,
                originalSize, "CLOSED", "NONE", averageFillPrice, "LIMIT", "BUY", timestamp);

        // Act
        OrderStatus result = ParadexTranslator.translateOrderStatus(orderUpdate);

        // Assert
        assertEquals(Status.FILLED, result.getStatus());
        assertEquals(0, originalSize.compareTo(result.getFilled())); // 100.0 - 0.0
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getRemaining()));
    }

    @Test
    public void testTranslateOrderStatus_Canceled_Success() {
        // Arrange
        String tickerString = "AVAX-USD";
        String orderId = "order999";
        BigDecimal originalSize = new BigDecimal("50.0");
        BigDecimal remainingSize = new BigDecimal("30.0"); // Partially filled before cancel
        BigDecimal averageFillPrice = new BigDecimal("12.50");
        long timestamp = 1693037100000L;

        when(mockTickerRegistry.lookupByBrokerSymbol(tickerString)).thenReturn(mockTicker);

        ParadoxOrderStatusUpdate orderUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize,
                originalSize, "CLOSED", "USER_CANCELED", averageFillPrice, "STOP", "SELL", timestamp);

        // Act
        OrderStatus result = ParadexTranslator.translateOrderStatus(orderUpdate);

        // Assert
        assertEquals(Status.CANCELED, result.getStatus());
        assertEquals(0, new BigDecimal("20.0").compareTo(result.getFilled())); // 50.0 - 30.0
    }

    @Test
    public void testTranslateOrderStatus_ZeroTimestamp_UsesCurrentTime() {
        // Arrange
        String tickerString = "DOT-USD";
        String orderId = "order000";
        BigDecimal originalSize = new BigDecimal("25.0");
        BigDecimal remainingSize = new BigDecimal("25.0");
        BigDecimal averageFillPrice = BigDecimal.ZERO;

        when(mockTickerRegistry.lookupByBrokerSymbol(tickerString)).thenReturn(mockTicker);

        ParadoxOrderStatusUpdate orderUpdate = new ParadoxOrderStatusUpdate(tickerString, orderId, remainingSize,
                originalSize, "NEW", "NONE", averageFillPrice, "LIMIT", "BUY", 0L // Zero timestamp
        );

        ZonedDateTime beforeCall = ZonedDateTime.now();

        // Act
        OrderStatus result = ParadexTranslator.translateOrderStatus(orderUpdate);

        ZonedDateTime afterCall = ZonedDateTime.now();

        // Assert
        assertTrue(result.getTimestamp().isAfter(beforeCall.minusSeconds(1)));
        assertTrue(result.getTimestamp().isBefore(afterCall.plusSeconds(1)));
    }

    @Test
    public void testTranslateStatusCode_NewStatus_ReturnsNew() {
        // Act
        Status result = ParadexTranslator.translateStatusCode(ParadexOrderStatus.NEW, CancelReason.NONE,
                new BigDecimal("10.0"), new BigDecimal("10.0"));

        // Assert
        assertEquals(Status.NEW, result);
    }

    @Test
    public void testTranslateStatusCode_UntriggeredStatus_ReturnsNew() {
        // Act
        Status result = ParadexTranslator.translateStatusCode(ParadexOrderStatus.UNTRIGGERED, CancelReason.NONE,
                new BigDecimal("10.0"), new BigDecimal("10.0"));

        // Assert
        assertEquals(Status.NEW, result);
    }

    @Test
    public void testTranslateStatusCode_ClosedWithUserCanceled_ReturnsCanceled() {
        // Act
        Status result = ParadexTranslator.translateStatusCode(ParadexOrderStatus.CLOSED, CancelReason.USER_CANCELED,
                new BigDecimal("10.0"), new BigDecimal("5.0"));

        // Assert
        assertEquals(Status.CANCELED, result);
    }

    @Test
    public void testTranslateStatusCode_ClosedWithPostOnlyWouldCross_ReturnsCanceled() {
        // Act
        Status result = ParadexTranslator.translateStatusCode(ParadexOrderStatus.CLOSED,
                CancelReason.POST_ONLY_WOULD_CROSS, new BigDecimal("10.0"), new BigDecimal("10.0"));

        // Assert
        assertEquals(Status.CANCELED, result);
    }

    @Test
    public void testTranslateStatusCode_ClosedWithZeroRemaining_ReturnsFilled() {
        // Act
        Status result = ParadexTranslator.translateStatusCode(ParadexOrderStatus.CLOSED, CancelReason.NONE,
                new BigDecimal("10.0"), BigDecimal.ZERO);

        // Assert
        assertEquals(Status.FILLED, result);
    }

    @Test
    public void testTranslateStatusCode_OpenWithPartialFill_ReturnsPartialFill() {
        // Act
        Status result = ParadexTranslator.translateStatusCode(ParadexOrderStatus.OPEN, CancelReason.NONE,
                new BigDecimal("10.0"), new BigDecimal("3.0"));

        // Assert
        assertEquals(Status.PARTIAL_FILL, result);
    }

    @Test
    public void testTranslateStatusCode_OpenWithFullRemaining_ReturnsNew() {
        // Act
        Status result = ParadexTranslator.translateStatusCode(ParadexOrderStatus.OPEN, CancelReason.NONE,
                new BigDecimal("10.0"), new BigDecimal("10.0"));

        // Assert
        assertEquals(Status.NEW, result);
    }

    @Test
    public void testTranslateStatusCode_OpenWithNullRemaining_ReturnsNew() {
        // Act
        Status result = ParadexTranslator.translateStatusCode(ParadexOrderStatus.OPEN, CancelReason.NONE,
                new BigDecimal("10.0"), null);

        // Assert
        assertEquals(Status.NEW, result);
    }

    @Test
    public void testTranslateStatusCode_OpenWithNullOriginal_ReturnsNew() {
        // Act
        Status result = ParadexTranslator.translateStatusCode(ParadexOrderStatus.OPEN, CancelReason.NONE, null,
                new BigDecimal("5.0"));

        // Assert
        assertEquals(Status.NEW, result);
    }

    @Test
    public void testTranslateStatusCode_ClosedWithoutValidConditions_ThrowsException() {
        // This tests the fallthrough case where CLOSED doesn't meet FILLED or CANCELED
        // conditions
        assertThrows(SumZeroException.class, () -> {
            ParadexTranslator.translateStatusCode(ParadexOrderStatus.CLOSED, CancelReason.NONE, new BigDecimal("10.0"),
                    new BigDecimal("5.0") // Not zero remaining, no cancel reason
            );
        });
    }
}
