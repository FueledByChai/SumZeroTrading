/**
 * MIT License
 *
 * Copyright (c) 2015  Rob Terpilowski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.sumzerotrading.broker.bitmex;

import com.sumzerotrading.bitmex.client.IBitmexClient;
import com.sumzerotrading.bitmex.common.api.BitmexClientRegistry;
import com.sumzerotrading.bitmex.entity.BitmexOrder;
import com.sumzerotrading.broker.BrokerAccountInfoListener;
import com.sumzerotrading.broker.BrokerError;
import com.sumzerotrading.broker.BrokerErrorListener;
import com.sumzerotrading.broker.IBroker;
import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.order.OrderEvent;
import com.sumzerotrading.broker.order.OrderEventListener;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.data.ComboTicker;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;

import com.sumzerotrading.time.TimeUpdatedListener;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

/**
 * Supported Order types are: Market, Stop and Limit Supported order parameters
 * are parent/child, OCA, Good-after-time, good-till-date. Supported
 * Time-in-force: DAY, Good-till-canceled, Good-till-time, Immediate-or-cancel
 *
 * @author Rob Terpilowski
 */
public class BitmexBroker implements IBroker {

    protected static int contractRequestId = 1;
    protected static int executionRequestId = 1;
    protected static Logger logger = Logger.getLogger(BitmexBroker.class);
    // protected EClientSocket ibConnection;
    // protected IBSocket ibSocket;
    // protected IBConnectionInterface callbackInterface;
    protected Set<OrderTicket> currencyOrderList = new HashSet<>();
    protected BlockingQueue<Integer> nextIdQueue = new LinkedBlockingQueue<>();
    protected BlockingQueue<ZonedDateTime> brokerTimeQueue = new LinkedBlockingQueue<>();
    protected BlockingQueue<BrokerError> brokerErrorQueue = new LinkedBlockingQueue<>();
    protected BlockingQueue<OrderEvent> orderEventQueue = new LinkedBlockingQueue<>();
    // protected BlockingQueue<ContractDetails> contractDetailsQueue = new
    // LinkedBlockingDeque<>();
    protected int nextOrderId = -1;
    protected SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    protected DateTimeFormatter zonedDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

    protected List<OrderEventListener> orderEventListeners = new ArrayList<>();
    protected BitmexOrderEventProcessor orderProcessor;
    protected Set<String> filledOrderSet = new HashSet<>();
    protected Timer currencyOrderTimer;
    protected Object lock = new Object();
    protected Semaphore semaphore = new Semaphore(1);
    protected Semaphore tradeFileSemaphore = new Semaphore(1);
    protected boolean started = false;
    protected String directory;
    protected Map<String, OrderEvent> orderEventMap;
    protected CountDownLatch getPositionsCountdownLatch = null;
    protected List<Position> positionsList = new ArrayList<>();

    protected IBitmexClient bitmexClient;
    protected Map<String, BitmexOrder> openOrderMap = new HashMap<>();
    protected Map<String, OrderTicket> completedOrderMap = new HashMap<>();

    @Override
    public void cancelOrder(String id) {
        checkConnected();
        bitmexClient.cancelOrder(openOrderMap.get(id));
    }

    @Override
    public void cancelOrder(OrderTicket order) {
        checkConnected();
        cancelOrder(order.getOrderId());
    }

    @Override
    public void placeOrder(OrderTicket order) {
        BitmexOrder bitmexOrder = OrderManagmentUtil.createBitmexOrder(order);
        BitmexOrder submittedOrder = bitmexClient.submitOrder(bitmexOrder);
        openOrderMap.put(submittedOrder.getOrderID(), submittedOrder);
    }

    @Override
    public String getNextOrderId() {
        return "";
    }

    @Override
    public void addBrokerAccountInfoListener(BrokerAccountInfoListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    @Override
    public void removeBrokerAccountInfoListener(BrokerAccountInfoListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    @Override
    public void addOrderEventListener(OrderEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public void removeOrderEventListener(OrderEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public void addBrokerErrorListener(BrokerErrorListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public void removeBrokerErrorListener(BrokerErrorListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public String getFormattedDate(int hour, int minute, int second) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public String getFormattedDate(ZonedDateTime date) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public ZonedDateTime getCurrentTime() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public void connect() {
        bitmexClient = BitmexClientRegistry.getInstance().getBitmexClient();
    }

    @Override
    public void disconnect() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public boolean isConnected() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public void aquireLock() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public void releaseLock() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public ComboTicker buildComboTicker(Ticker ticker1, Ticker ticker2) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public ComboTicker buildComboTicker(Ticker ticker1, int ratio1, Ticker ticker2, int ratio2) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public OrderTicket requestOrderStatus(String orderId) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public List<OrderTicket> getOpenOrders() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public void cancelAndReplaceOrder(String originalOrderId, OrderTicket newOrder) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public void addTimeUpdateListener(TimeUpdatedListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public void removeTimeUpdateListener(TimeUpdatedListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public List<Position> getAllPositions() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    protected void checkConnected() {
        if (bitmexClient == null) {
            throw new SumZeroException("Not connected to broker, call connect() first");
        }
    }

    @Override
    public void cancelAllOrders(Ticker ticker) {
        throw new UnsupportedOperationException("Cancel all orders by ticker not implemented yet");
    }

    @Override
    public void cancelAllOrders() {
        throw new UnsupportedOperationException("Cancel all orders not implemented yet");

    }

}
