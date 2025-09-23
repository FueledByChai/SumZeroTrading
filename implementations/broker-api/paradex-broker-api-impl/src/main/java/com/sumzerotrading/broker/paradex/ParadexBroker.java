/**
 * MIT License
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
package com.sumzerotrading.broker.paradex;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.broker.BrokerAccountInfoListener;
import com.sumzerotrading.broker.BrokerError;
import com.sumzerotrading.broker.BrokerErrorListener;
import com.sumzerotrading.broker.IBroker;
import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.order.OrderEvent;
import com.sumzerotrading.broker.order.OrderEventListener;
import com.sumzerotrading.broker.order.OrderStatus;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.data.ComboTicker;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.paradex.common.api.IParadexRestApi;
import com.sumzerotrading.paradex.common.api.ParadexApiFactory;
import com.sumzerotrading.paradex.common.api.ParadexConfiguration;
import com.sumzerotrading.paradex.common.api.ParadexWebSocketClient;
import com.sumzerotrading.time.TimeUpdatedListener;
import com.sumzerotrading.websocket.IWebSocketEventListener;

/**
 * Supported Order types are: Market, Stop and Limit Supported order parameters
 * are parent/child, OCA, Good-after-time, good-till-date. Supported
 * Time-in-force: DAY, Good-till-canceled, Good-till-time, Immediate-or-cancel
 *
 * @author Rob Terpilowski
 */
public class ParadexBroker implements IBroker, IWebSocketEventListener<IParadexOrderStatusUpdate> {
    protected static Logger logger = LoggerFactory.getLogger(ParadexBroker.class);

    protected static int contractRequestId = 1;
    protected static int executionRequestId = 1;

    protected IParadexRestApi restApi;
    protected String jwtToken;
    protected int jwtRefreshInSeconds = 60;
    protected boolean connected = false;

    protected ParadexWebSocketClient accountInfoWSClient;
    protected ParadexWebSocketClient orderStatusWSClient;
    protected OrderStatusWebSocketProcessor orderStatusProcessor;
    protected AccountWebSocketProcessor accountWebSocketProcessor;

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
    protected List<BrokerAccountInfoListener> brokerAccountInfoListeners = new ArrayList<>();
    protected Set<String> filledOrderSet = new HashSet<>();
    protected Timer currencyOrderTimer;
    protected ScheduledExecutorService authenticationScheduler;
    protected ExecutorService orderEventExecutor;
    protected Object lock = new Object();
    protected Semaphore semaphore = new Semaphore(1);
    protected Semaphore tradeFileSemaphore = new Semaphore(1);
    protected boolean started = false;
    protected String directory;
    protected Map<String, OrderEvent> orderEventMap;
    protected CountDownLatch getPositionsCountdownLatch = null;
    protected List<Position> positionsList = new ArrayList<>();
    protected Map<String, OrderTicket> tradeOrderMap = new HashMap<>();

    protected Map<String, OrderTicket> completedOrderMap = new HashMap<>();

    /**
     * Default constructor - uses centralized configuration for API initialization.
     */
    public ParadexBroker() {
        // Initialize using centralized configuration
        this.restApi = ParadexApiFactory.getPrivateApi();

        // Get JWT refresh interval from configuration
        ParadexConfiguration config = ParadexConfiguration.getInstance();
        this.jwtRefreshInSeconds = config.getJwtRefreshSeconds();

        logger.info("ParadexBroker initialized with configuration: {}", ParadexApiFactory.getConfigurationInfo());
    }

    /**
     * Constructor for testing or custom configuration.
     * 
     * @param restApi custom ParadexRestApi instance
     */
    public ParadexBroker(IParadexRestApi restApi) {
        this.restApi = restApi;
        this.jwtRefreshInSeconds = 60; // default
    }

    @Override
    public void cancelOrder(String id) {
        checkConnected();
        restApi.cancelOrder(jwtToken, id);
    }

    @Override
    public void cancelOrder(OrderTicket order) {
        checkConnected();
        cancelOrder(order.getOrderId());
    }

    @Override
    public void placeOrder(OrderTicket order) {
        checkConnected();
        String orderId = restApi.placeOrder(jwtToken, order);
        order.setOrderId(orderId);
        tradeOrderMap.put(orderId, order);
    }

    @Override
    public String getNextOrderId() {
        return "";
    }

    @Override
    public void addOrderEventListener(OrderEventListener listener) {
        orderEventListeners.add(listener);
    }

    @Override
    public void removeOrderEventListener(OrderEventListener listener) {
        orderEventListeners.remove(listener);
    }

    @Override
    public void addBrokerAccountInfoListener(BrokerAccountInfoListener listener) {
        brokerAccountInfoListeners.add(listener);
    }

    @Override
    public void removeBrokerAccountInfoListener(BrokerAccountInfoListener listener) {
        brokerAccountInfoListeners.remove(listener);
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
        startAuthenticationScheduler();
        orderEventExecutor = Executors.newCachedThreadPool();
        orderStatusProcessor = new OrderStatusWebSocketProcessor(() -> {
            logger.info("Order status WebSocket closed, trying to restart...");
            startOrderStatusWSClient();
        });
        orderStatusProcessor.addEventListener(this);
        connected = true;
    }

    @Override
    public void disconnect() {
        stopAuthenticationScheduler();
        stopOrderEventExecutor();
        connected = false;
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
        throw new UnsupportedOperationException("Not supported for paradex."); // To change body of generated methods,
                                                                               // choose
        // Tools | Templates.
    }

    @Override
    public ComboTicker buildComboTicker(Ticker ticker1, int ratio1, Ticker ticker2, int ratio2) {
        throw new UnsupportedOperationException("Not supported for paradex."); // To change body of generated methods,
                                                                               // choose
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
        // if (bitmexClient == null) {
        // throw new SumZeroException("Not connected to broker, call connect() first");
        // }
    }

    @Override
    public void onWebSocketEvent(IParadexOrderStatusUpdate orderStatus) {
        OrderStatus status = ParadexBrokerUtil.translateOrderStatus(orderStatus);
        OrderTicket order = tradeOrderMap.get(orderStatus.getOrderId());
        order.setCurrentStatus(status.getStatus());
        order.setFilledPrice(status.getFillPrice());
        order.setFilledSize(status.getFilled());
        // Can't set the order commission here, only on fill events.

        OrderEvent event = new OrderEvent(order, status);
        if (status.getStatus() == OrderStatus.Status.FILLED || status.getStatus() == OrderStatus.Status.CANCELED) {
            tradeOrderMap.remove(orderStatus.getOrderId());
        }
        for (OrderEventListener listener : orderEventListeners) {
            if (orderEventExecutor != null && !orderEventExecutor.isShutdown()) {
                orderEventExecutor.submit(() -> {
                    try {
                        listener.orderEvent(event);
                    } catch (Exception e) {
                        logger.error("Error notifying order event listener", e);
                    }
                });
            }
        }
    }

    private void startAuthenticationScheduler() {
        if (authenticationScheduler == null || authenticationScheduler.isShutdown()) {
            authenticationScheduler = Executors.newSingleThreadScheduledExecutor();

            // Authenticate immediately when starting
            try {
                logger.info("Initial JWT token authentication");
                jwtToken = authenticate();
            } catch (Exception e) {
                logger.error("Failed to obtain initial JWT token", e);
            }

            // Schedule authentication every minute
            authenticationScheduler.scheduleAtFixedRate(() -> {
                try {
                    logger.info("Refreshing JWT token");
                    jwtToken = authenticate();
                } catch (Exception e) {
                    logger.error("Failed to refresh JWT token", e);
                }
            }, jwtRefreshInSeconds, jwtRefreshInSeconds, TimeUnit.SECONDS);

            logger.info("Authentication scheduler started - will refresh JWT token every minute");
        }
    }

    private void stopAuthenticationScheduler() {
        if (authenticationScheduler != null && !authenticationScheduler.isShutdown()) {
            authenticationScheduler.shutdown();
            try {
                if (!authenticationScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    authenticationScheduler.shutdownNow();
                }
                logger.info("Authentication scheduler stopped");
            } catch (InterruptedException e) {
                authenticationScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void stopOrderEventExecutor() {
        if (orderEventExecutor != null && !orderEventExecutor.isShutdown()) {
            orderEventExecutor.shutdown();
            try {
                if (!orderEventExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    orderEventExecutor.shutdownNow();
                }
                logger.info("Order event executor stopped");
            } catch (InterruptedException e) {
                orderEventExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    protected String authenticate() throws Exception {
        jwtToken = restApi.getJwtToken();
        logger.info("Obtained JWT Token");
        return jwtToken;
    }

    public void startOrderStatusWSClient() {
        logger.info("Starting order status WebSocket client");
        String jwtToken = restApi.getJwtToken();
        String wsUrl = ParadexApiFactory.getWebSocketUrl();

        try {
            orderStatusWSClient = new ParadexWebSocketClient(wsUrl, "orders.ALL", orderStatusProcessor, jwtToken);
            orderStatusWSClient.connect();
        } catch (Exception e) {
            throw new IllegalStateException(e);
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
