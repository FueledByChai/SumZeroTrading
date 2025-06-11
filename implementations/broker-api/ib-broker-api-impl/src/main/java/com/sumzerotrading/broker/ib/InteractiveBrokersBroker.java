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
package com.sumzerotrading.broker.ib;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.Decimal;
import com.ib.client.EClientSocket;
import com.ib.client.Execution;
import com.ib.client.ExecutionFilter;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.sumzerotrading.broker.BrokerError;
import com.sumzerotrading.broker.BrokerErrorListener;
import com.sumzerotrading.broker.IBroker;
import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.order.OrderEvent;
import com.sumzerotrading.broker.order.OrderEventListener;
import com.sumzerotrading.broker.order.OrderStatus;
import com.sumzerotrading.broker.order.TradeOrder;
import com.sumzerotrading.data.ComboTicker;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.ib.BaseIBConnectionDelegate;
import com.sumzerotrading.ib.ContractBuilderFactory;
import com.sumzerotrading.ib.ContractWrapper;
import com.sumzerotrading.ib.IBConnectionInterface;
import com.sumzerotrading.ib.IBSocket;
import com.sumzerotrading.ib.IbUtils;
import com.sumzerotrading.time.TimeUpdatedListener;
import com.sumzerotrading.util.QuoteUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.log4j.Logger;

/**
 * Supported Order types are: Market, Stop and Limit Supported order parameters
 * are parent/child, OCA, Good-after-time, good-till-date. Supported
 * Time-in-force: DAY, Good-till-canceled, Good-till-time, Immediate-or-cancel
 *
 * @author Rob Terpilowski
 */
public class InteractiveBrokersBroker extends BaseIBConnectionDelegate implements IBroker {

    protected static int contractRequestId = 1;
    protected static int executionRequestId = 1;
    protected static Logger logger = Logger.getLogger(InteractiveBrokersBroker.class);
    protected EClientSocket ibConnection;
    protected IBSocket ibSocket;
    protected IBConnectionInterface callbackInterface;
    protected Set<TradeOrder> currencyOrderList = new HashSet<>();
    protected BlockingQueue<Integer> nextIdQueue = new LinkedBlockingQueue<>();
    protected BlockingQueue<ZonedDateTime> brokerTimeQueue = new LinkedBlockingQueue<>();
    protected BlockingQueue<BrokerError> brokerErrorQueue = new LinkedBlockingQueue<>();
    protected BlockingQueue<OrderEvent> orderEventQueue = new LinkedBlockingQueue<>();
    protected BlockingQueue<ContractDetails> contractDetailsQueue = new LinkedBlockingDeque<>();
    protected int nextOrderId = -1;
    protected SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    protected DateTimeFormatter zonedDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
    protected Map<String, TradeOrder> orderMap = new HashMap<>();
    protected Map<String, TradeOrder> completedOrderMap = new HashMap<>();
    protected List<OrderEventListener> orderEventListeners = new ArrayList<>();
    protected IBOrderEventProcessor orderProcessor;
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

    /**
     * Used by Unit tests
     */
    protected InteractiveBrokersBroker() {

    }

    public InteractiveBrokersBroker(IBSocket ibSocket) {
        this.ibSocket = ibSocket;

        try {
            loadOrderMaps();
        } catch (Exception ex) {
            throw new SumZeroException(ex);
        }

        orderEventMap = new PassiveExpiringMap<>(30, TimeUnit.SECONDS);
        callbackInterface = ibSocket.getConnection();
        callbackInterface.addIbConnectionDelegate(this);

        ibConnection = ibSocket.getClientSocket();
        orderProcessor = new IBOrderEventProcessor(orderEventQueue, this);
        currencyOrderTimer = new Timer(true);
        currencyOrderTimer.schedule(getCurrencyOrderMonitor(), 0, 1000 * 60);
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
    public boolean isConnected() {
        System.out.println("Checking if connected to IB: " + ibSocket.isConnected());
        return ibSocket.isConnected();

    }

    @Override
    public void connect() {
        logger.info("Connecting to Interactive Brokers");
        if (!isConnected()) {
            logger.info("Not connected, connecting now");
            ibSocket.connect();
        } else {
            logger.info("Already connected to Interactive Brokers");
        }
        if (!started) {
            orderProcessor.startProcessor();
            started = true;
        }
    }

    @Override
    public void disconnect() {
        if (started) {
            orderProcessor.stopProcessor();
            started = false;
        }
    }

    @Override
    public void execDetails(int orderId, Contract contract, Execution execution) {
        logger.debug("Execution details: orderId: " + orderId + " contract: " + contract + " Execution: " + execution);
    }

    @Override
    public void nextValidId(int orderId) {
        try {
            nextIdQueue.put(orderId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
        logger.debug(
                "OpenOrder: " + orderId + " Contract: " + contract + " Order: " + order + " OrderState: " + orderState);

    }

    public void timeReceived(ZonedDateTime time) {
        try {
            brokerTimeQueue.put(time);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void orderStatus(int orderId, String status, Decimal filled, Decimal remaining, double avgFillPrice,
            int permId, int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
        logger.debug("OrderStatus(): orderId: " + orderId + " Status: " + status + " filled: " + filled + " remaining: "
                + remaining + " avgFillPrice: " + avgFillPrice + " permId: " + permId + " parentId: " + parentId
                + " lastFillePrice: " + lastFillPrice + " clientId: " + clientId + " whyHeld: " + whyHeld);
        TradeOrder order = orderMap.get(Integer.toString(orderId));

        if (order == null) {
            logger.error("Open Order with ID: " + orderId + " not found");
            return;
        }

        order.setFilledSize(filled.longValue());
        order.setFilledPrice(avgFillPrice);

        try {
            OrderEvent event = OrderManagmentUtil.createOrderEvent(order, status, (int) filled.longValue(),
                    (int) remaining.longValue(), avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld,
                    getZoneDateTime());

            // Check if this order status has been seen in the last minute
            OrderEvent cachedEvent = orderEventMap.get(order.getOrderId());
            if (cachedEvent != null && event.equals(cachedEvent)) {
                orderEventMap.put(order.getOrderId(), event);
                logger.debug("Duplicate order status received....skipping");
                return;
            }
            if (event.getOrderStatus().getStatus() == OrderStatus.Status.FILLED
                    || event.getOrderStatus().getStatus() == OrderStatus.Status.CANCELED) {
                order.setOrderFilledTime(getZoneDateTime());
                completedOrderMap.put(order.getOrderId(), order);
                orderMap.remove(order.getOrderId());
            }
            order.setCurrentStatus(event.getOrderStatus().getStatus());
            orderEventQueue.put(event);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        try {
            saveOrderMaps();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void addOrderEventListener(OrderEventListener listener) {
        synchronized (orderEventListeners) {
            orderEventListeners.add(listener);
        }
    }

    public void removeOrderEventListener(OrderEventListener listener) {
        synchronized (orderEventListeners) {
            orderEventListeners.remove(listener);
        }
    }

    public void error(Exception e) {
        logger.error("BrokerError: " + e.getMessage(), e);
        putOnErrorQueue(new BrokerError(e));
    }

    public void error(String str) {
        logger.error("BrokerError: " + str);
        putOnErrorQueue(new BrokerError(str));
    }

    public void error(int id, int errorCode, String errorMsg) {
        logger.error("BrokerError: ID:" + id + " errorCode:" + errorCode + " errorMessage: " + errorMsg);
        putOnErrorQueue(new BrokerError(id, errorCode, errorMsg));
    }

    protected void putOnErrorQueue(BrokerError error) {
        try {
            brokerErrorQueue.put(error);
        } catch (Exception ex) {
            logger.error(ex, ex);
        }
    }

    public void cancelOrder(String idString) {
        int id = Integer.parseInt(idString);
        // ibConnection.cancelOrder(id);
        IbUtils.throwUnsupportedException();
    }

    public void cancelOrder(TradeOrder order) {
        int id = Integer.parseInt(order.getOrderId());
        // ibConnection.cancelOrder(id);
        IbUtils.throwUnsupportedException();
    }

    public ZonedDateTime getCurrentTime() {
        ibConnection.reqCurrentTime();
        try {
            return brokerTimeQueue.poll(2, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            logger.error(directory);
            System.out.println("Time Out waiting to get current time from broker, returning local current time");
            return ZonedDateTime.now();
        }
    }

    public String getFormattedDate(int hour, int minute, int second) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getFormattedDate(ZonedDateTime date) {
        return date.format(zonedDateFormatter);
    }

    @Override
    public synchronized String getNextOrderId() {
        if (nextOrderId == -1) {
            try {
                ibConnection.reqIds(1);
                nextOrderId = nextIdQueue.take();
                return nextOrderId + "";
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage(), ex);
                return -1 + "";
            }
        }
        return ++nextOrderId + "";
    }

    public void placeOrder(TradeOrder order) {
        logger.info("Order received: " + order);
        order.setOrderEntryTime(getZoneDateTime());

        List<IbOrderAndContract> orders = buildOrderAndContract(order);
        logger.debug("Order converted to " + orders.size() + " IB Order(s)");
        orders.get(orders.size() - 1).getOrder().transmit(true);
        for (IbOrderAndContract ibOrder : orders) {
            ibConnection.placeOrder(ibOrder.getOrder().orderId(), ibOrder.getContract(), ibOrder.getOrder());
        }
        logger.debug("Orders placed at IB");
    }

    public void aquireLock() {
        try {
            semaphore.acquire();
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void releaseLock() {
        semaphore.release();
    }

    public ComboTicker buildComboTicker(Ticker ticker1, Ticker ticker2) {
        return buildComboTicker(ticker1, 1, ticker2, 1);
    }

    @Override
    public ComboTicker buildComboTicker(Ticker ticker1, int ratio1, Ticker ticker2, int ratio2) {
        ContractDetails details1 = getContractDetails(ticker1);
        ContractDetails details2 = getContractDetails(ticker2);

        ComboTicker combo = new ComboTicker(ticker1, ticker2);
        IbUtils.throwUnsupportedException();
        // combo.setContractId1(details1.m_summary.m_conId);
        // combo.setFirstTickerRatio(ratio1);
        // combo.setContractId2(details2.m_summary.m_conId);
        // combo.setSecondTickerRatio(ratio2);

        return combo;
    }

    public void contractDetailsReceived(ContractDetails details) {
        try {
            contractDetailsQueue.put(details);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ContractDetails getContractDetails(Ticker ticker) {
        final Contract contract = ContractBuilderFactory.getContractBuilder(ticker).buildContract(ticker);
        ibConnection.reqContractDetails(contractRequestId++, contract);
        try {
            return contractDetailsQueue.poll(2, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public synchronized TradeOrder requestOrderStatus(String orderId) {
        ExecutionFilter filter = new ExecutionFilter();
        ibConnection.reqExecutions(executionRequestId++, filter);
        throw new IllegalStateException("Not yet implemented");
    }

    @Override
    public List<TradeOrder> getOpenOrders() {
        return new ArrayList<>(orderMap.values());
    }

    @Override
    public List<Position> getAllPositions() {
        getPositionsCountdownLatch = new CountDownLatch(1);
        ibConnection.reqPositions();
        try {
            getPositionsCountdownLatch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            throw new SumZeroException(ex);
        }
        return positionsList;
    }

    // @Override
    public void position(String account, Contract contract, int pos, double avgCost) {
        // IbUtils.
        // positionsList.add(new Position(ticker, pos, avgCost));
        IbUtils.throwUnsupportedException();
        logger.debug("Position - Account: " + account + " Contract: " + new ContractWrapper(contract) + " size: " + pos
                + " avgCost: " + avgCost);
    }

    @Override
    public void positionEnd() {
        getPositionsCountdownLatch.countDown();
        logger.debug("Position END()");
    }

    protected void saveOrderMaps() throws Exception {
        tradeFileSemaphore.acquire();
        try {
            File file = new File(getDirName() + "orders.ser");
            ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file));
            output.writeObject(completedOrderMap);
            output.writeObject(orderMap);
            output.flush();
            output.close();
        } finally {
            tradeFileSemaphore.release();
        }
    }

    protected void loadOrderMaps() throws Exception {
        tradeFileSemaphore.acquire();
        try {
            createDir();
            File file = new File(getDirName() + "orders.ser");
            if (file.exists()) {

                ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
                completedOrderMap = (HashMap) input.readObject();
                orderMap = (HashMap) input.readObject();
                input.close();
            }
        } finally {
            tradeFileSemaphore.release();
        }
    }

    protected void createDir() {
        try {
            Files.createDirectories(Paths.get(getDirName()));

        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    protected String getDirName() {
        return System.getProperty("user.dir") + "/" + "ib-order-management/client-" + ibSocket.getClientId() + "/";
    }

    protected void queueOrder(TradeOrder order) {
        currencyOrderList.add(order);
    }

    protected void processCurrencyOrders() {
        if (currencyOrderList.isEmpty()) {
            return;
        }

        if (!isIdealProClosed()) {
            synchronized (currencyOrderList) {
                for (TradeOrder order : currencyOrderList) {
                    placeOrder(order);
                }
                currencyOrderList.clear();
            }
        }
    }

    protected ZonedDateTime getZoneDateTime() {
        return ZonedDateTime.now();
    }

    protected boolean isIdealProClosed() {
        return false;
    }

    protected TimerTask getCurrencyOrderMonitor() {
        return new TimerTask() {
            @Override
            public void run() {
                processCurrencyOrders();
            }
        };
    }

    protected void fireOrderEvent(OrderEvent event) {
        synchronized (orderEventListeners) {
            for (OrderEventListener listener : orderEventListeners) {
                listener.orderEvent(event);
            }
        }
    }

    protected List<IbOrderAndContract> buildOrderAndContract(TradeOrder order) {
        List<IbOrderAndContract> orderList = new ArrayList<>();

        orderMap.put(order.getOrderId(), order);

        Contract contract = ContractBuilderFactory.getContractBuilder(order.getTicker())
                .buildContract(order.getTicker());

        Order ibOrder = new Order();
        if (order.getType() == TradeOrder.Type.MARKET_ON_OPEN) {
            order.setDuration(TradeOrder.Duration.MARKET_ON_OPEN);
        }

        ibOrder.action(IbUtils.getAction(order.getTradeDirection()));
        ibOrder.orderType(IbUtils.getOrderType(order.getType()));

        ibOrder.tif(IbUtils.getTif(order.getDuration()));
        ibOrder.orderId(Integer.parseInt(order.getOrderId()));
        ibOrder.totalQuantity(com.ib.client.Decimal.get(order.getSize()));

        if (order.getGoodAfterTime() != null) {
            ibOrder.goodAfterTime(getFormattedDate(order.getGoodAfterTime()));
        }

        if (order.getParentOrderId() != null && order.getParentOrderId().length() > 0) {
            ibOrder.parentId(Integer.parseInt(order.getParentOrderId()));
        }

        if (order.getDuration() == TradeOrder.Duration.GOOD_UTNIL_TIME) {
            if (order.getGoodUntilTime() != null) {
                ibOrder.goodTillDate(getFormattedDate(order.getGoodUntilTime()));
            } else {
                throw new IllegalStateException("Duration is good-until-time, but no time has been set in the Order");
            }
        }

        if (order.getType() == TradeOrder.Type.LIMIT) {
            if (order.getLimitPrice() == null) {
                throw new IllegalStateException("Limit price not set for LMT order: " + order.getOrderId());
            }
            double limitPrice = QuoteUtil.getBigDecimalValue(order.getTicker(), order.getLimitPrice()).doubleValue();
            ibOrder.lmtPrice(limitPrice);
        } else if (order.getType() == TradeOrder.Type.STOP) {
            if (order.getStopPrice() == null) {
                throw new IllegalStateException("Stop price not set for StopLoss order: " + order.getOrderId());
            }
            double stopPrice = QuoteUtil.getBigDecimalValue(order.getTicker(), order.getStopPrice()).doubleValue();
            ibOrder.auxPrice(stopPrice);
        }

        if (order.getOcaGroup() != null) {
            ibOrder.ocaGroup(order.getOcaGroup());
        }

        if (order.getReference() != null) {
            ibOrder.orderRef(order.getReference());
        }

        // If the order has already been submitted, have TWS transmit immediately,
        // otherwise this order may be
        // attched to another order which needs to be submitted first.
        ibOrder.transmit(order.isSubmitted());

        // the order has now been submitted.
        order.setSubmitted(true);

        orderList.add(new IbOrderAndContract(ibOrder, contract));
        for (TradeOrder tradeOrder : order.getChildOrders()) {
            orderList.addAll(buildOrderAndContract(tradeOrder));
        }

        if (order.isSubmitChildOrdersFirst()) {
            Collections.reverse(orderList);
        }

        return orderList;
    }

    public void addBrokerErrorListener(BrokerErrorListener listener) {
    }

    public void removeBrokerErrorListener(BrokerErrorListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    public void cancelAndReplaceOrder(String originalOrderId, TradeOrder newOrder) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    protected static class IbOrderAndContract {

        protected Order order;
        protected Contract contract;

        protected IbOrderAndContract(Order order, Contract contract) {
            this.order = order;
            this.contract = contract;
        }

        public Contract getContract() {
            return contract;
        }

        public Order getOrder() {
            return order;
        }
    }
}
