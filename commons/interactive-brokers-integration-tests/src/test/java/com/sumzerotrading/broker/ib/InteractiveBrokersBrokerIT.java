/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.broker.ib;

import com.sumzerotrading.broker.order.OrderEvent;
import com.sumzerotrading.broker.order.OrderEventListener;
import com.sumzerotrading.broker.order.OrderStatus;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.broker.order.TradeOrder;
import com.sumzerotrading.data.StockTicker;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.ib.IBConnectionUtil;
import com.sumzerotrading.ib.IBSocket;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import net.jcip.annotations.NotThreadSafe;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author RobTerpilowski
 */

@NotThreadSafe
public class InteractiveBrokersBrokerIT implements OrderEventListener {

    IBConnectionUtil util = new IBConnectionUtil("localhost", 7999, 2);
    IBSocket socket;
    InteractiveBrokersBroker broker;
    List<OrderEvent> eventList = new ArrayList<>();
    Ticker qqqTicker = new StockTicker("QQQ");

    public InteractiveBrokersBrokerIT() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        System.out.println("running setup");
        socket = util.getIBSocket();
        broker = new InteractiveBrokersBroker(socket);
        broker.addOrderEventListener(this);
        eventList.clear();
        socket.connect();
        broker.connect();
        System.out.println("done running setup");
        // try { Thread.sleep(2000); } catch( Exception ex ) {}
    }

    @After
    public void tearDown() {
        broker.disconnect();
        socket.disconnect();

    }

    @Test

    public void testConnect() {
        System.out.println("Running test connect");
        if (socket.isConnected()) {
            broker.disconnect();
            socket.disconnect();

        }

        assertFalse(broker.isConnected());
        assertFalse(broker.started);
        assertFalse(broker.orderProcessor.shouldRun);

        broker.connect();

        assertTrue(broker.started);
        assertTrue(broker.isConnected());
        assertTrue(broker.orderProcessor.shouldRun);
        System.out.println("done with test connect");
    }

    @Test
    public void testGetNextOrderId() {
        System.out.println("running next order id");
        broker.connect();
        String id = broker.getNextOrderId();
        assertNotNull(id);

        String nextId = Integer.toString(Integer.parseInt(id) + 1);
        assertEquals(nextId, broker.getNextOrderId());
    }

    @Test
    public void testSubmitMarketOrder() throws Exception {
        System.out.println("running test submit");
        broker.connect();

        String orderId = broker.getNextOrderId();
        System.out.println("Order id: " + orderId);
        TradeOrder order = new TradeOrder(orderId, qqqTicker, 100, TradeDirection.BUY);
        broker.placeOrder(order);

        // If the order is executed immediately we problably won't get a 'presubmitted',
        // or 'submitted'
        // status from IB, it will go straight to filled;

        Thread.sleep(10000);

        boolean filledSizeCorrect = false;
        boolean filledOrderStatusCorrect = false;
        boolean remainingCorrect = false;

        for (OrderEvent event : eventList) {
            System.out.println("Event for orderId: " + event.getOrderStatus().getOrderId());

            System.out.println("Status: " + event.getOrder().getCurrentStatus());
            if ((event.getOrder().getCurrentStatus() == OrderStatus.Status.FILLED)
                    && event.getOrderStatus().getStatus() == OrderStatus.Status.FILLED) {
                filledOrderStatusCorrect = true;
            }

            System.out.println("Filled size: " + event.getOrder().getFilledSize());
            if (event.getOrder().getFilledSize() == 100) {
                filledSizeCorrect = true;
            }

            System.out.println("Remaining: " + event.getOrderStatus().getRemaining());
            if (event.getOrderStatus().getRemaining().compareTo(java.math.BigDecimal.ZERO) == 0) {
                remainingCorrect = true;
            }
        }

        assertTrue(filledSizeCorrect);
        assertTrue(remainingCorrect);
        assertTrue(filledOrderStatusCorrect);
        System.out.println("done with test order");

    }

    @Test
    public void testSubmitMarketOrder_GoodAfterTime() throws Exception {
        System.out.println("running test submit good after time");
        broker.connect();
        int orderSize = 99;

        ZonedDateTime date = ZonedDateTime.now(ZoneId.systemDefault());
        date = date.plusMinutes(1);

        String orderId = broker.getNextOrderId();
        System.out.println("Order id: " + orderId);
        TradeOrder order = new TradeOrder(orderId, qqqTicker, orderSize, TradeDirection.BUY);
        order.setGoodAfterTime(date);
        broker.placeOrder(order);

        // If the order is executed immediately we problably won't get a 'presubmitted',
        // or 'submitted'
        // status from IB, it will go straight to filled;

        Thread.sleep(70000);

        boolean filledSizeCorrect = false;
        boolean filledOrderStatusCorrect = false;
        boolean remainingCorrect = false;

        for (OrderEvent event : eventList) {
            System.out.println("Event for orderId: " + event.getOrderStatus().getOrderId());

            System.out.println("Status: " + event.getOrder().getCurrentStatus());
            if ((event.getOrder().getCurrentStatus() == OrderStatus.Status.FILLED)
                    && event.getOrderStatus().getStatus() == OrderStatus.Status.FILLED) {
                filledOrderStatusCorrect = true;
            }

            System.out.println("Filled size: " + event.getOrder().getFilledSize());
            if (event.getOrder().getFilledSize() == orderSize) {
                filledSizeCorrect = true;
            }

            System.out.println("Remaining: " + event.getOrderStatus().getRemaining());
            if (event.getOrderStatus().getRemaining().compareTo(java.math.BigDecimal.ZERO) == 0) {
                remainingCorrect = true;
            }
        }

        assertTrue(filledSizeCorrect);
        assertTrue(remainingCorrect);
        assertTrue(filledOrderStatusCorrect);
        System.out.println("done with test order");

    }

    @Test
    public void testDisconnect() {
        System.out.println("running test disconnect");
        broker.connect();
        assertTrue(broker.isConnected());
        broker.disconnect();
        assertFalse(broker.orderProcessor.shouldRun);
        System.out.println("done with test disconnect");
    }

    @Override
    public void orderEvent(OrderEvent event) {
        System.out.println("Received order event: " + event);
        eventList.add(event);
    }

}
