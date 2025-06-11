/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.broker.ib;

import com.ib.client.EClientSocket;
import com.sumzerotrading.broker.order.OrderEvent;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.broker.order.TradeOrder;
import com.sumzerotrading.data.StockTicker;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.ib.IBConnectionInterface;
import com.sumzerotrading.ib.IBSocket;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import org.apache.log4j.Logger;
import static org.jmock.Expectations.any;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author RobTerpilowski
 */
@Ignore("Ignore until IB Implemenation is fixed")
public class InteractiveBrokersBrokerTest {

    protected InteractiveBrokersBroker broker;
    protected IBSocket mockIbSocket;
    protected IBConnectionInterface mockConnectionInterface;
    protected EClientSocket mockClientSocketInterface;
    protected BlockingQueue mockOrderEventQueue;
    protected Logger mockLogger = mock(Logger.class);

    public InteractiveBrokersBrokerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        mockIbSocket = mock(IBSocket.class);
        mockConnectionInterface = mock(IBConnectionInterface.class);
        mockClientSocketInterface = mock(EClientSocket.class);
        mockOrderEventQueue = mock(BlockingQueue.class);

        when(mockIbSocket.getClientSocket()).thenReturn(mockClientSocketInterface);
        when(mockIbSocket.getConnection()).thenReturn(mockConnectionInterface);

        InteractiveBrokersBroker.logger = mockLogger;
        broker = new InteractiveBrokersBroker(mockIbSocket);
        broker.orderEventQueue = mockOrderEventQueue;

    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConstructor() {
        try {
            InteractiveBrokersBroker broker = new InteractiveBrokersBroker(mockIbSocket);
        } catch (SumZeroException ex) {
            ex.getCause().printStackTrace();
            fail();

        }

    }

    @Test
    public void testGetFormattedDate() {
        ZonedDateTime date = ZonedDateTime.of(2015, 10, 5, 13, 30, 30, 0, ZoneId.of("America/New_York"));
        String expectedString = "20151005 13:30:30";

        String formattedDate = broker.getFormattedDate(date);
        assertEquals(expectedString, formattedDate);
    }

    @Test
    public void testAddTimeUpdateListener() {
        try {
            broker.addTimeUpdateListener((LocalDateTime localDateTime) -> {
            });
            fail();
        } catch (UnsupportedOperationException ex) {
            // this should happen
        }
    }

    @Test
    public void testOrderStatus_OrderNotFound() throws Exception {

        broker.orderStatus(50, "status", com.ib.client.Decimal.get(0), com.ib.client.Decimal.get(0), 0, 0, 0, 0, 0, "",
                0.0);

        verify(mockLogger).error("Open Order with ID: 50 not found");
        verify(mockOrderEventQueue, never()).put(any(OrderEvent.class));
    }

    @Test
    public void testOrderStatus_NonCompletedOrder() throws Exception {
        InteractiveBrokersBroker b = spy(InteractiveBrokersBroker.class);
        b.orderEventMap = new HashMap<>();
        b.orderEventQueue = mockOrderEventQueue;
        ZonedDateTime now = ZonedDateTime.now();

        int orderId = 50;
        String orderIdString = "50";
        int size = 40;
        int filled = 10;
        int remaining = 10;
        double averageFilPrice = 2.5;
        String status = "submitted";
        int permId = 0;
        int parentId = 0;
        double lastFillPrice = 2.5;
        int clientId = 0;
        String whyHeld = "";

        Ticker ticker = new StockTicker("ABC");
        TradeOrder order = new TradeOrder(orderIdString, ticker, size, TradeDirection.BUY);
        b.orderMap.put(orderIdString, order);
        doNothing().when(b).saveOrderMaps();
        doReturn(now).when(b).getZoneDateTime();

        OrderEvent expectedEvent = OrderManagmentUtil.createOrderEvent(order, status, filled, remaining,
                averageFilPrice, permId, parentId, lastFillPrice, clientId, whyHeld, now);

        b.orderStatus(orderId, status, com.ib.client.Decimal.get(filled), com.ib.client.Decimal.get(remaining),
                averageFilPrice, permId, parentId, lastFillPrice, clientId, whyHeld, 0.0);

        verify(mockOrderEventQueue).put(expectedEvent);

    }

    @Test
    public void testGetDirName() {
        when(mockIbSocket.getClientId()).thenReturn(99);
        String userDir = System.getProperty("user.dir");
        String expectedString = userDir + "/ib-order-management/client-99/";
        assertEquals(expectedString, broker.getDirName());

    }

}
