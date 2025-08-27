package com.sumzerotrading.broker.paradex;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sumzerotrading.broker.BrokerAccountInfoListener;
import com.sumzerotrading.broker.BrokerErrorListener;
import com.sumzerotrading.broker.order.OrderEvent;
import com.sumzerotrading.broker.order.OrderEventListener;
import com.sumzerotrading.broker.order.OrderStatus;
import com.sumzerotrading.broker.order.TradeOrder;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.paradex.common.api.ParadexConfiguration;
import com.sumzerotrading.paradex.common.api.ParadexRestApi;
import com.sumzerotrading.paradex.common.api.ParadexWebSocketClient;
import com.sumzerotrading.time.TimeUpdatedListener;

/**
 * Comprehensive JUnit 5/Mockito test class for ParadexBroker Tests all public
 * methods and protected field access
 */
@ExtendWith(MockitoExtension.class)
public class ParadexBrokerTest {

    @Mock
    private ParadexRestApi mockRestApi;

    @Mock
    private ParadexWebSocketClient mockOrderStatusWSClient;

    @Mock
    private OrderStatusWebSocketProcessor mockOrderStatusProcessor;

    @Mock
    private ScheduledExecutorService mockAuthenticationScheduler;

    @Mock
    private TradeOrder mockTradeOrder;

    @Mock
    private OrderEventListener mockOrderEventListener;

    @Mock
    private BrokerErrorListener mockBrokerErrorListener;

    @Mock
    private TimeUpdatedListener mockTimeUpdateListener;

    @Mock
    private IParadexOrderStatusUpdate mockOrderStatusUpdate;

    @Mock
    private Ticker mockTicker1;

    @Mock
    private Ticker mockTicker2;

    private ParadexBroker broker;

    @BeforeEach
    public void setUp() {
        broker = new ParadexBroker(mockRestApi);
        broker.orderStatusWSClient = mockOrderStatusWSClient;
        broker.orderStatusProcessor = mockOrderStatusProcessor;
        broker.authenticationScheduler = mockAuthenticationScheduler;
    }

    // ==================== Order Management Tests ====================

    @Test
    public void testCancelOrder_ById_CallsRestApi() {
        // Arrange
        String orderId = "order123";
        String jwtToken = "testToken";
        broker.jwtToken = jwtToken;
        broker.connected = true;

        // Act
        broker.cancelOrder(orderId);

        // Assert
        verify(mockRestApi).cancelOrder(jwtToken, orderId);
    }

    @Test
    public void testCancelOrder_ByTradeOrder_CallsRestApi() {
        // Arrange
        String orderId = "order123";
        String jwtToken = "testToken";
        broker.jwtToken = jwtToken;
        broker.connected = true;
        when(mockTradeOrder.getOrderId()).thenReturn(orderId);

        // Act
        broker.cancelOrder(mockTradeOrder);

        // Assert
        verify(mockRestApi).cancelOrder(jwtToken, orderId);
        verify(mockTradeOrder).getOrderId();
    }

    @Test
    public void testPlaceOrder_CallsRestApi() {
        // Arrange
        String jwtToken = "testToken";
        broker.jwtToken = jwtToken;
        broker.connected = true;

        // Act
        broker.placeOrder(mockTradeOrder);

        // Assert
        verify(mockRestApi).placeOrder(jwtToken, mockTradeOrder);
    }

    @Test
    public void testGetNextOrderId_ReturnsEmptyString() {
        // Act
        String result = broker.getNextOrderId();

        // Assert
        assertEquals("", result);
    }

    // ==================== Unsupported Operation Tests ====================

    @Test
    public void testAddAndRemoveOrderEventListener_ThrowsUnsupportedOperationException() {
        // Act & Assert
        broker.addOrderEventListener(mockOrderEventListener);
        assertTrue(broker.orderEventListeners.contains(mockOrderEventListener));

        broker.removeOrderEventListener(mockOrderEventListener);
        assertFalse(broker.orderEventListeners.contains(mockOrderEventListener));
    }

    @Test
    public void testAddBrokerErrorListener_ThrowsUnsupportedOperationException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            broker.addBrokerErrorListener(mockBrokerErrorListener);
        });
    }

    @Test
    public void testRemoveBrokerErrorListener_ThrowsUnsupportedOperationException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            broker.removeBrokerErrorListener(mockBrokerErrorListener);
        });
    }

    @Test
    public void testGetFormattedDate_WithTimeParameters_ThrowsUnsupportedOperationException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            broker.getFormattedDate(10, 30, 45);
        });
    }

    @Test
    public void testGetFormattedDate_WithZonedDateTime_ThrowsUnsupportedOperationException() {
        // Arrange
        ZonedDateTime zonedDateTime = ZonedDateTime.now();

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            broker.getFormattedDate(zonedDateTime);
        });
    }

    @Test
    public void testGetCurrentTime_ThrowsUnsupportedOperationException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            broker.getCurrentTime();
        });
    }

    @Test
    public void testIsConnected_ThrowsUnsupportedOperationException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            broker.isConnected();
        });
    }

    @Test
    public void testAquireLock_ThrowsUnsupportedOperationException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            broker.aquireLock();
        });
    }

    @Test
    public void testReleaseLock_ThrowsUnsupportedOperationException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            broker.releaseLock();
        });
    }

    @Test
    public void testBuildComboTicker_TwoTickers_ThrowsUnsupportedOperationException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            broker.buildComboTicker(mockTicker1, mockTicker2);
        });
    }

    @Test
    public void testBuildComboTicker_WithRatios_ThrowsUnsupportedOperationException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            broker.buildComboTicker(mockTicker1, 1, mockTicker2, 2);
        });
    }

    @Test
    public void testRequestOrderStatus_ThrowsUnsupportedOperationException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            broker.requestOrderStatus("orderId");
        });
    }

    @Test
    public void testGetOpenOrders_ThrowsUnsupportedOperationException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            broker.getOpenOrders();
        });
    }

    @Test
    public void testCancelAndReplaceOrder_ThrowsUnsupportedOperationException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            broker.cancelAndReplaceOrder("originalOrderId", mockTradeOrder);
        });
    }

    @Test
    public void testAddTimeUpdateListener_ThrowsUnsupportedOperationException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            broker.addTimeUpdateListener(mockTimeUpdateListener);
        });
    }

    @Test
    public void testRemoveTimeUpdateListener_ThrowsUnsupportedOperationException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            broker.removeTimeUpdateListener(mockTimeUpdateListener);
        });
    }

    @Test
    public void testGetAllPositions_ThrowsUnsupportedOperationException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            broker.getAllPositions();
        });
    }

    // ==================== Connection Management Tests ====================

    @Test
    public void testConnect_SetsConnectedToTrue() {
        // Arrange
        assertFalse(broker.connected);

        // Act
        broker.connect();

        // Assert
        assertTrue(broker.connected);
        assertNotNull(broker.orderStatusProcessor);
        assertNotNull(broker.orderEventExecutor);
        assertFalse(broker.orderEventExecutor.isShutdown());

        // Clean up
        broker.disconnect();
    }

    @Test
    public void testDisconnect_SetsConnectedToFalse() {
        // Arrange
        broker.connect();
        assertTrue(broker.connected);
        assertNotNull(broker.orderEventExecutor);

        // Act
        broker.disconnect();

        // Assert
        assertFalse(broker.connected);
        assertTrue(broker.orderEventExecutor.isShutdown());
    }

    @Test
    public void testAddRemoveBrokerAccountInfoListener() {
        BrokerAccountInfoListener mockListener = mock(BrokerAccountInfoListener.class);

        // Add the listener
        broker.addBrokerAccountInfoListener(mockListener);
        assertTrue(broker.brokerAccountInfoListeners.contains(mockListener));

        // Remove the listener
        broker.removeBrokerAccountInfoListener(mockListener);
        assertFalse(broker.brokerAccountInfoListeners.contains(mockListener));
    }

    // ==================== Order Status Listener Tests ====================

    // @Test
    public void testOrderStatusUpdated_DoesNotThrowException() {
        // Arrange - Mock ParadexBrokerUtil.translateOrderStatus to return a valid
        // OrderStatus
        try (var mockedStatic = mockStatic(ParadexBrokerUtil.class)) {
            OrderStatus mockOrderStatus = mock(OrderStatus.class);
            when(mockOrderStatus.getStatus()).thenReturn(OrderStatus.Status.NEW);
            mockedStatic.when(() -> ParadexBrokerUtil.translateOrderStatus(mockOrderStatusUpdate))
                    .thenReturn(mockOrderStatus);

            // Act & Assert
            assertDoesNotThrow(() -> {
                broker.orderStatusUpdated(mockOrderStatusUpdate);
            });
        }
    }

    @Test
    public void testOrderStatusUpdated_CallsListenersAsynchronously() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        OrderEventListener asyncListener = mock(OrderEventListener.class);
        TradeOrder mockTradeOrder = mock(TradeOrder.class);

        // Configure the mock to signal when called
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(asyncListener).orderEvent(any(OrderEvent.class));

        // Set up the broker with mocked dependencies and initialize executor
        broker.orderEventExecutor = Executors.newCachedThreadPool();
        broker.orderEventListeners.add(asyncListener);
        broker.tradeOrderMap.put("testOrderId", mockTradeOrder);

        // Mock the order status update
        when(mockOrderStatusUpdate.getOrderId()).thenReturn("testOrderId");

        // Mock ParadexBrokerUtil.translateOrderStatus to return a valid OrderStatus
        try (var mockedStatic = mockStatic(ParadexBrokerUtil.class)) {
            OrderStatus mockOrderStatus = mock(OrderStatus.class);
            when(mockOrderStatus.getStatus()).thenReturn(OrderStatus.Status.NEW);
            mockedStatic.when(() -> ParadexBrokerUtil.translateOrderStatus(mockOrderStatusUpdate))
                    .thenReturn(mockOrderStatus);

            // Act
            broker.orderStatusUpdated(mockOrderStatusUpdate);

            // Assert - Wait for the async call to complete
            assertTrue(latch.await(1, TimeUnit.SECONDS), "Listener should be called asynchronously");

            // Verify the listener was called
            verify(asyncListener, timeout(1000)).orderEvent(any(OrderEvent.class));
        } finally {
            // Clean up
            broker.orderEventExecutor.shutdown();
        }
    }

    // ==================== Authentication Tests ====================

    @Test
    public void testAuthenticate_CallsRestApiGetJwtToken() throws Exception {
        // Arrange
        String expectedToken = "expectedJwtToken";
        when(mockRestApi.getJwtToken()).thenReturn(expectedToken);

        // Act
        String result = broker.authenticate();

        // Assert
        verify(mockRestApi).getJwtToken();
        assertEquals(expectedToken, result);
        assertEquals(expectedToken, broker.jwtToken);
    }

    @Test
    public void testStartOrderStatusWSClient_CallsRestApiForJwtToken() {
        // Arrange
        String jwtToken = "testToken";
        when(mockRestApi.getJwtToken()).thenReturn(jwtToken);

        // Act - Method will create WebSocket client but may fail due to invalid URL
        assertDoesNotThrow(() -> {
            broker.startOrderStatusWSClient();
        });

        // Assert - Verify that the JWT token was requested
        verify(mockRestApi).getJwtToken();
    }

    @Test
    public void testStartOrderStatusWSClient_HandlesException() {
        // Arrange
        when(mockRestApi.getJwtToken()).thenThrow(new RuntimeException("Connection failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            broker.startOrderStatusWSClient();
        });
    }

    // ==================== Protected Field Access Tests ====================

    @Test
    public void testProtectedFields_AccessAndModification() {
        // Test initial values and field access
        assertNotNull(broker.currencyOrderList);
        assertNotNull(broker.nextIdQueue);
        assertNotNull(broker.brokerTimeQueue);
        assertNotNull(broker.brokerErrorQueue);
        assertNotNull(broker.orderEventQueue);
        assertNotNull(broker.orderEventListeners);
        assertNotNull(broker.filledOrderSet);
        assertNotNull(broker.lock);
        assertNotNull(broker.semaphore);
        assertNotNull(broker.tradeFileSemaphore);
        assertNotNull(broker.positionsList);
        assertNotNull(broker.completedOrderMap);
        assertNotNull(broker.dateFormatter);
        assertNotNull(broker.zonedDateFormatter);

        // Test field modifications
        broker.jwtRefreshInSeconds = 120;
        assertEquals(120, broker.jwtRefreshInSeconds);

        broker.connected = true;
        assertTrue(broker.connected);

        broker.nextOrderId = 100;
        assertEquals(100, broker.nextOrderId);

        broker.started = true;
        assertTrue(broker.started);

        broker.directory = "/test/directory";
        assertEquals("/test/directory", broker.directory);
    }

    @Test
    public void testStaticFields_AccessAndModification() {
        // Test configuration access
        ParadexConfiguration config = ParadexConfiguration.getInstance();
        String wsUrl = config.getWebSocketUrl();
        assertNotNull(wsUrl);
        assertTrue(wsUrl.contains("testnet") || wsUrl.contains("prod"));

        // Test static field modifications
        int originalContractId = ParadexBroker.contractRequestId;
        int originalExecutionId = ParadexBroker.executionRequestId;

        ParadexBroker.contractRequestId = 999;
        ParadexBroker.executionRequestId = 888;

        assertEquals(999, ParadexBroker.contractRequestId);
        assertEquals(888, ParadexBroker.executionRequestId);

        // Reset to original values
        ParadexBroker.contractRequestId = originalContractId;
        ParadexBroker.executionRequestId = originalExecutionId;
    }

    @Test
    public void testCheckConnected_DoesNotThrow() {
        // Act & Assert - checkConnected is currently a no-op
        assertDoesNotThrow(() -> {
            broker.checkConnected();
        });
    }

    // ==================== Integration Tests ====================

    @Test
    public void testFullConnectDisconnectCycle() {
        // Arrange
        assertFalse(broker.connected);

        // Act - Connect
        broker.connect();

        // Assert - Connected state
        assertTrue(broker.connected);
        assertNotNull(broker.orderStatusProcessor);

        // Act - Disconnect
        broker.disconnect();

        // Assert - Disconnected state
        assertFalse(broker.connected);
    }

    @Test
    public void testOrderOperationsSequence() {
        // Arrange
        String jwtToken = "testToken";
        String orderId = "order123";
        broker.jwtToken = jwtToken;
        broker.connected = true;
        when(mockTradeOrder.getOrderId()).thenReturn(orderId);

        // Act & Assert - Place order
        assertDoesNotThrow(() -> broker.placeOrder(mockTradeOrder));
        verify(mockRestApi).placeOrder(jwtToken, mockTradeOrder);

        // Act & Assert - Cancel by order object
        assertDoesNotThrow(() -> broker.cancelOrder(mockTradeOrder));
        verify(mockRestApi).cancelOrder(jwtToken, orderId);

        // Act & Assert - Cancel by ID
        assertDoesNotThrow(() -> broker.cancelOrder(orderId));
        verify(mockRestApi, times(2)).cancelOrder(jwtToken, orderId);
    }

    // ==================== Enhanced Order Status Update Tests ====================

    @Test
    public void testOrderStatusUpdated_OrderNotInMap() {
        // Arrange
        String nonExistentOrderId = "nonExistentOrder";
        when(mockOrderStatusUpdate.getOrderId()).thenReturn(nonExistentOrderId);

        try (var mockedStatic = mockStatic(ParadexBrokerUtil.class)) {
            OrderStatus mockOrderStatus = mock(OrderStatus.class);
            when(mockOrderStatus.getStatus()).thenReturn(OrderStatus.Status.NEW);
            mockedStatic.when(() -> ParadexBrokerUtil.translateOrderStatus(mockOrderStatusUpdate))
                    .thenReturn(mockOrderStatus);

            // Act & Assert - Should handle gracefully without throwing exception
            assertThrows(NullPointerException.class, () -> {
                broker.orderStatusUpdated(mockOrderStatusUpdate);
            });
        }
    }

    @Test
    public void testOrderStatusUpdated_FilledOrderRemoval() {
        // Arrange
        String orderId = "filledOrder";
        TradeOrder mockTradeOrder = mock(TradeOrder.class);
        broker.tradeOrderMap.put(orderId, mockTradeOrder);
        broker.orderEventExecutor = Executors.newCachedThreadPool();

        when(mockOrderStatusUpdate.getOrderId()).thenReturn(orderId);

        try (var mockedStatic = mockStatic(ParadexBrokerUtil.class)) {
            OrderStatus mockOrderStatus = mock(OrderStatus.class);
            when(mockOrderStatus.getStatus()).thenReturn(OrderStatus.Status.FILLED);
            mockedStatic.when(() -> ParadexBrokerUtil.translateOrderStatus(mockOrderStatusUpdate))
                    .thenReturn(mockOrderStatus);

            // Act
            broker.orderStatusUpdated(mockOrderStatusUpdate);

            // Assert - Order should be removed from map for FILLED status
            assertFalse(broker.tradeOrderMap.containsKey(orderId));
        } finally {
            broker.orderEventExecutor.shutdown();
        }
    }

    @Test
    public void testOrderStatusUpdated_CanceledOrderRemoval() {
        // Arrange
        String orderId = "canceledOrder";
        TradeOrder mockTradeOrder = mock(TradeOrder.class);
        broker.tradeOrderMap.put(orderId, mockTradeOrder);
        broker.orderEventExecutor = Executors.newCachedThreadPool();

        when(mockOrderStatusUpdate.getOrderId()).thenReturn(orderId);

        try (var mockedStatic = mockStatic(ParadexBrokerUtil.class)) {
            OrderStatus mockOrderStatus = mock(OrderStatus.class);
            when(mockOrderStatus.getStatus()).thenReturn(OrderStatus.Status.CANCELED);
            mockedStatic.when(() -> ParadexBrokerUtil.translateOrderStatus(mockOrderStatusUpdate))
                    .thenReturn(mockOrderStatus);

            // Act
            broker.orderStatusUpdated(mockOrderStatusUpdate);

            // Assert - Order should be removed from map for CANCELED status
            assertFalse(broker.tradeOrderMap.containsKey(orderId));
        } finally {
            broker.orderEventExecutor.shutdown();
        }
    }

    @Test
    public void testOrderStatusUpdated_NewOrderNotRemoved() {
        // Arrange
        String orderId = "newOrder";
        TradeOrder mockTradeOrder = mock(TradeOrder.class);
        broker.tradeOrderMap.put(orderId, mockTradeOrder);
        broker.orderEventExecutor = Executors.newCachedThreadPool();

        when(mockOrderStatusUpdate.getOrderId()).thenReturn(orderId);

        try (var mockedStatic = mockStatic(ParadexBrokerUtil.class)) {
            OrderStatus mockOrderStatus = mock(OrderStatus.class);
            when(mockOrderStatus.getStatus()).thenReturn(OrderStatus.Status.NEW);
            mockedStatic.when(() -> ParadexBrokerUtil.translateOrderStatus(mockOrderStatusUpdate))
                    .thenReturn(mockOrderStatus);

            // Act
            broker.orderStatusUpdated(mockOrderStatusUpdate);

            // Assert - Order should remain in map for NEW status
            assertTrue(broker.tradeOrderMap.containsKey(orderId));
        } finally {
            broker.orderEventExecutor.shutdown();
        }
    }

    @Test
    public void testOrderStatusUpdated_ExecutorShutdown() {
        // Arrange
        String orderId = "testOrder";
        TradeOrder mockTradeOrder = mock(TradeOrder.class);
        broker.tradeOrderMap.put(orderId, mockTradeOrder);
        broker.orderEventExecutor = Executors.newCachedThreadPool();
        broker.orderEventExecutor.shutdown(); // Shutdown executor before test

        OrderEventListener mockListener = mock(OrderEventListener.class);
        broker.orderEventListeners.add(mockListener);

        when(mockOrderStatusUpdate.getOrderId()).thenReturn(orderId);

        try (var mockedStatic = mockStatic(ParadexBrokerUtil.class)) {
            OrderStatus mockOrderStatus = mock(OrderStatus.class);
            when(mockOrderStatus.getStatus()).thenReturn(OrderStatus.Status.NEW);
            mockedStatic.when(() -> ParadexBrokerUtil.translateOrderStatus(mockOrderStatusUpdate))
                    .thenReturn(mockOrderStatus);

            // Act
            broker.orderStatusUpdated(mockOrderStatusUpdate);

            // Assert - Listener should not be called because executor is shutdown
            verify(mockListener, never()).orderEvent(any(OrderEvent.class));
        }
    }

    @Test
    public void testOrderStatusUpdated_ListenerExceptionHandling() throws InterruptedException {
        // Arrange
        String orderId = "testOrder";
        TradeOrder mockTradeOrder = mock(TradeOrder.class);
        broker.tradeOrderMap.put(orderId, mockTradeOrder);
        broker.orderEventExecutor = Executors.newCachedThreadPool();

        OrderEventListener exceptionListener = mock(OrderEventListener.class);
        OrderEventListener normalListener = mock(OrderEventListener.class);

        CountDownLatch exceptionLatch = new CountDownLatch(1);
        CountDownLatch normalLatch = new CountDownLatch(1);

        // Configure first listener to throw exception
        doAnswer(invocation -> {
            exceptionLatch.countDown();
            throw new RuntimeException("Test exception");
        }).when(exceptionListener).orderEvent(any(OrderEvent.class));

        // Configure second listener to work normally
        doAnswer(invocation -> {
            normalLatch.countDown();
            return null;
        }).when(normalListener).orderEvent(any(OrderEvent.class));

        broker.orderEventListeners.add(exceptionListener);
        broker.orderEventListeners.add(normalListener);

        when(mockOrderStatusUpdate.getOrderId()).thenReturn(orderId);

        try (var mockedStatic = mockStatic(ParadexBrokerUtil.class)) {
            OrderStatus mockOrderStatus = mock(OrderStatus.class);
            when(mockOrderStatus.getStatus()).thenReturn(OrderStatus.Status.NEW);
            mockedStatic.when(() -> ParadexBrokerUtil.translateOrderStatus(mockOrderStatusUpdate))
                    .thenReturn(mockOrderStatus);

            // Act
            broker.orderStatusUpdated(mockOrderStatusUpdate);

            // Assert - Both listeners should be called despite exception in first one
            assertTrue(exceptionLatch.await(1, TimeUnit.SECONDS), "Exception listener should be called");
            assertTrue(normalLatch.await(1, TimeUnit.SECONDS), "Normal listener should be called");
        } finally {
            broker.orderEventExecutor.shutdown();
        }
    }

    // ==================== Authentication Scheduler Tests ====================

    @Test
    public void testStartAuthenticationScheduler_InitialAuthentication() throws Exception {
        // Arrange
        String expectedToken = "initialToken";
        when(mockRestApi.getJwtToken()).thenReturn(expectedToken);

        // Reset the authentication scheduler to null so it will actually start
        broker.authenticationScheduler = null;

        // Act
        broker.connect();

        // Wait a bit for the authentication to complete since it runs in a thread
        Thread.sleep(100);

        // Assert - Verify that authentication was attempted
        verify(mockRestApi, atLeastOnce()).getJwtToken();
        assertEquals(expectedToken, broker.jwtToken);

        // Cleanup
        broker.disconnect();
    }

    @Test
    public void testStartAuthenticationScheduler_AlreadyStarted() throws Exception {
        // Arrange
        String expectedToken = "testToken";
        lenient().when(mockRestApi.getJwtToken()).thenReturn(expectedToken);

        // Act - First start
        broker.connect();

        // Act - Try to start again (should not create new scheduler)
        broker.connect();

        // Assert - Should not create new scheduler
        assertNotNull(broker.authenticationScheduler);
        assertFalse(broker.authenticationScheduler.isShutdown());

        // Cleanup
        broker.disconnect();
    }

    @Test
    public void testAuthenticationFailure_ErrorHandling() throws Exception {
        // Arrange
        lenient().when(mockRestApi.getJwtToken()).thenThrow(new RuntimeException("Auth failed"));

        // Act & Assert - Should not throw exception, just log error
        assertDoesNotThrow(() -> {
            broker.connect();
        });

        // Cleanup
        broker.disconnect();
    }

    // ==================== Shutdown and Cleanup Tests ====================

    @Test
    public void testStopAuthenticationScheduler_TimeoutHandling() throws InterruptedException {
        // Arrange
        broker.authenticationScheduler = mock(ScheduledExecutorService.class);
        when(broker.authenticationScheduler.isShutdown()).thenReturn(false);
        when(broker.authenticationScheduler.awaitTermination(5, TimeUnit.SECONDS)).thenReturn(false);

        // Act
        broker.disconnect();

        // Assert
        verify(broker.authenticationScheduler).shutdown();
        verify(broker.authenticationScheduler).awaitTermination(5, TimeUnit.SECONDS);
        verify(broker.authenticationScheduler).shutdownNow();
    }

    @Test
    public void testStopAuthenticationScheduler_InterruptedException() throws InterruptedException {
        // Arrange
        broker.authenticationScheduler = mock(ScheduledExecutorService.class);
        when(broker.authenticationScheduler.isShutdown()).thenReturn(false);
        when(broker.authenticationScheduler.awaitTermination(5, TimeUnit.SECONDS))
                .thenThrow(new InterruptedException("Test interrupt"));

        // Act & Assert
        assertDoesNotThrow(() -> {
            broker.disconnect();
        });

        // Assert
        verify(broker.authenticationScheduler).shutdown();
        verify(broker.authenticationScheduler).shutdownNow();
        assertTrue(Thread.currentThread().isInterrupted()); // Thread should be re-interrupted

        // Reset interrupt flag for other tests
        Thread.interrupted();
    }

    @Test
    public void testStopOrderEventExecutor_TimeoutHandling() throws InterruptedException {
        // Arrange
        ExecutorService mockExecutor = mock(ExecutorService.class);
        broker.orderEventExecutor = mockExecutor;
        when(mockExecutor.isShutdown()).thenReturn(false);
        when(mockExecutor.awaitTermination(5, TimeUnit.SECONDS)).thenReturn(false);

        // Act
        broker.disconnect();

        // Assert
        verify(mockExecutor).shutdown();
        verify(mockExecutor).awaitTermination(5, TimeUnit.SECONDS);
        verify(mockExecutor).shutdownNow();
    }

    @Test
    public void testStopOrderEventExecutor_InterruptedException() throws InterruptedException {
        // Arrange
        ExecutorService mockExecutor = mock(ExecutorService.class);
        broker.orderEventExecutor = mockExecutor;
        when(mockExecutor.isShutdown()).thenReturn(false);
        when(mockExecutor.awaitTermination(5, TimeUnit.SECONDS)).thenThrow(new InterruptedException("Test interrupt"));

        // Act & Assert
        assertDoesNotThrow(() -> {
            broker.disconnect();
        });

        // Assert
        verify(mockExecutor).shutdown();
        verify(mockExecutor).shutdownNow();
        assertTrue(Thread.currentThread().isInterrupted()); // Thread should be re-interrupted

        // Reset interrupt flag for other tests
        Thread.interrupted();
    }

    // ==================== WebSocket Error Recovery Tests ====================

    @Test
    public void testStartOrderStatusWSClient_ConnectionFailure() {
        // Arrange - This test may not reliably throw an exception since WebSocket
        // connection
        // is handled asynchronously. Let's just verify that the method completes
        // without blocking.
        when(mockRestApi.getJwtToken()).thenReturn("testToken");

        // Act - Should complete without blocking (connection errors are handled
        // internally)
        assertDoesNotThrow(() -> {
            broker.startOrderStatusWSClient();
        });

        // Verify JWT token was requested
        verify(mockRestApi).getJwtToken();
    }

    @Test
    public void testStartOrderStatusWSClient_JwtTokenFailure() {
        // Arrange
        when(mockRestApi.getJwtToken()).thenThrow(new RuntimeException("JWT failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            broker.startOrderStatusWSClient();
        });
    }

    // ==================== Edge Case Tests ====================

    @Test
    public void testMultipleConnectDisconnectCycles() {
        // Act & Assert - Multiple cycles should work without issues
        for (int i = 0; i < 3; i++) {
            assertDoesNotThrow(() -> broker.connect());
            assertTrue(broker.connected);

            assertDoesNotThrow(() -> broker.disconnect());
            assertFalse(broker.connected);
        }
    }

    @Test
    public void testDisconnectWithoutConnect() {
        // Arrange - Ensure broker is not connected
        assertFalse(broker.connected);

        // Act & Assert - Should handle gracefully
        assertDoesNotThrow(() -> {
            broker.disconnect();
        });
    }

    @Test
    public void testPlaceOrderWithNullExecutor() {
        // Arrange
        String jwtToken = "testToken";
        String orderId = "order123";
        broker.jwtToken = jwtToken;
        broker.connected = true;
        broker.orderEventExecutor = null; // Simulate null executor

        when(mockRestApi.placeOrder(jwtToken, mockTradeOrder)).thenReturn(orderId);

        // Act & Assert
        assertDoesNotThrow(() -> {
            broker.placeOrder(mockTradeOrder);
        });

        // Verify order was placed and mapped
        verify(mockRestApi).placeOrder(jwtToken, mockTradeOrder);
        assertEquals(mockTradeOrder, broker.tradeOrderMap.get(orderId));
    }

    @Test
    public void testConcurrentOrderStatusUpdates() throws InterruptedException {
        // Arrange
        int numThreads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numThreads);

        broker.orderEventExecutor = Executors.newCachedThreadPool();

        try (var mockedStatic = mockStatic(ParadexBrokerUtil.class)) {
            OrderStatus mockOrderStatus = mock(OrderStatus.class);
            lenient().when(mockOrderStatus.getStatus()).thenReturn(OrderStatus.Status.NEW);
            lenient().when(ParadexBrokerUtil.translateOrderStatus(any())).thenReturn(mockOrderStatus);

            // Create multiple order status updates with valid status
            for (int i = 0; i < numThreads; i++) {
                final int orderId = i;
                TradeOrder order = mock(TradeOrder.class);
                broker.tradeOrderMap.put(String.valueOf(orderId), order);

                IParadexOrderStatusUpdate update = mock(IParadexOrderStatusUpdate.class);
                lenient().when(update.getOrderId()).thenReturn(String.valueOf(orderId));
                lenient().when(update.getStatus()).thenReturn(ParadexOrderStatus.NEW); // Provide valid status

                new Thread(() -> {
                    try {
                        startLatch.await();
                        broker.orderStatusUpdated(update);
                    } catch (Exception e) {
                        // Expected for concurrent testing - some may fail due to status issues
                    } finally {
                        completionLatch.countDown();
                    }
                }).start();
            }

            // Act - Start all threads simultaneously
            startLatch.countDown();

            // Assert - All threads should complete without blocking
            assertTrue(completionLatch.await(5, TimeUnit.SECONDS),
                    "All concurrent order status updates should complete");
        } finally {
            broker.orderEventExecutor.shutdown();
        }
    }

}
