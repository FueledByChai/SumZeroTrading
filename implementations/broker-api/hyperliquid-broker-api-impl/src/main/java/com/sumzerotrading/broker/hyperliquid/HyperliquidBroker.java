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
package com.sumzerotrading.broker.hyperliquid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.broker.AbstractBasicBroker;
import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.hyperliquid.translators.Translator;
import com.sumzerotrading.broker.order.OrderEvent;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.websocket.HyperliquidApiFactory;
import com.sumzerotrading.hyperliquid.websocket.HyperliquidConfiguration;
import com.sumzerotrading.hyperliquid.websocket.HyperliquidWebSocketClient;
import com.sumzerotrading.hyperliquid.websocket.IHyperliquidRestApi;
import com.sumzerotrading.websocket.IWebSocketEventListener;

/**
 * Supported Order types are: Market, Stop and Limit Supported order parameters
 * are parent/child, OCA, Good-after-time, good-till-date. Supported
 * Time-in-force: DAY, Good-till-canceled, Good-till-time, Immediate-or-cancel
 *
 * @author Rob Terpilowski
 */
public class HyperliquidBroker extends AbstractBasicBroker implements IWebSocketEventListener<IAccountUpdate> {
    protected static Logger logger = LoggerFactory.getLogger(HyperliquidBroker.class);

    protected IHyperliquidRestApi restApi;
    protected String jwtToken;
    protected int jwtRefreshInSeconds = 60;
    protected boolean connected = false;

    protected List<Position> currentPositions = new ArrayList<>();

    protected HyperliquidWebSocketClient accountInfoWSClient;
    protected HyperliquidWebSocketClient orderStatusWSClient;
    protected AccountWebSocketProcessor accountWebSocketProcessor;

    protected Set<OrderTicket> currencyOrderList = new HashSet<>();
    protected BlockingQueue<Integer> nextIdQueue = new LinkedBlockingQueue<>();

    protected int nextOrderId = -1;

    protected ScheduledExecutorService authenticationScheduler;
    protected ExecutorService orderEventExecutor;

    protected boolean started = false;
    protected String directory;
    protected Map<String, OrderEvent> orderEventMap;
    protected List<Position> positionsList = new ArrayList<>();
    protected Map<String, OrderTicket> tradeOrderMap = new HashMap<>();

    protected Map<String, OrderTicket> completedOrderMap = new HashMap<>();

    /**
     * Default constructor - uses centralized configuration for API initialization.
     */
    public HyperliquidBroker() {
        // Initialize using centralized configuration
        this.restApi = HyperliquidApiFactory.getPrivateApi();

        // Get JWT refresh interval from configuration
        HyperliquidConfiguration config = HyperliquidConfiguration.getInstance();
        this.jwtRefreshInSeconds = config.getJwtRefreshSeconds();

        logger.info("HyperliquidBroker initialized with configuration: {}",
                HyperliquidApiFactory.getConfigurationInfo());
    }

    /**
     * Constructor for testing or custom configuration.
     * 
     * @param restApi custom HyperliquidRestApi instance
     */
    public HyperliquidBroker(IHyperliquidRestApi restApi) {
        this.restApi = restApi;
        this.jwtRefreshInSeconds = 60; // default
    }

    @Override
    public void cancelOrder(String id) {
        checkConnected();
        // restApi.cancelOrder(jwtToken, id);
    }

    @Override
    public void cancelOrder(OrderTicket order) {
        checkConnected();
        cancelOrder(order.getOrderId());
    }

    @Override
    public void placeOrder(OrderTicket order) {
        checkConnected();
        // String orderId = restApi.placeOrder(jwtToken, order);
        // order.setOrderId(orderId);
        // tradeOrderMap.put(orderId, order);
    }

    @Override
    public String getNextOrderId() {
        return "";
    }

    @Override
    public void connect() {
        // startAuthenticationScheduler();
        orderEventExecutor = Executors.newCachedThreadPool();
        startAccountInfoWSClient();

        connected = true;
    }

    @Override
    protected void onDisconnect() {
        stopOrderEventExecutor();
        connected = false;
    }

    @Override
    public boolean isConnected() {
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
    public List<Position> getAllPositions() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    protected void checkConnected() {
        // if (bitmexClient == null) {
        // throw new SumZeroException("Not connected to broker, call connect() first");
        // }
    }

    // @Override
    // public void orderStatusUpdated(IParadexOrderStatusUpdate orderStatus) {
    // OrderStatus status = ParadexBrokerUtil.translateOrderStatus(orderStatus);
    // OrderTicket order = tradeOrderMap.get(orderStatus.getOrderId());
    // order.setCurrentStatus(status.getStatus());
    // order.setFilledPrice(status.getFillPrice());
    // order.setFilledSize(status.getFilled());
    // // Can't set the order commission here, only on fill events.

    // OrderEvent event = new OrderEvent(order, status);
    // if (status.getStatus() == OrderStatus.Status.FILLED || status.getStatus() ==
    // OrderStatus.Status.CANCELED) {
    // tradeOrderMap.remove(orderStatus.getOrderId());
    // }
    // for (OrderEventListener listener : orderEventListeners) {
    // if (orderEventExecutor != null && !orderEventExecutor.isShutdown()) {
    // orderEventExecutor.submit(() -> {
    // try {
    // listener.orderEvent(event);
    // } catch (Exception e) {
    // logger.error("Error notifying order event listener", e);
    // }
    // });
    // }
    // }
    // }

    // private void startAuthenticationScheduler() {
    // if (authenticationScheduler == null || authenticationScheduler.isShutdown())
    // {
    // authenticationScheduler = Executors.newSingleThreadScheduledExecutor();

    // // Authenticate immediately when starting
    // try {
    // logger.info("Initial JWT token authentication");
    // // jwtToken = authenticate();
    // } catch (Exception e) {
    // logger.error("Failed to obtain initial JWT token", e);
    // }

    // // Schedule authentication every minute
    // authenticationScheduler.scheduleAtFixedRate(() -> {
    // try {
    // logger.info("Refreshing JWT token");
    // // jwtToken = authenticate();
    // } catch (Exception e) {
    // logger.error("Failed to refresh JWT token", e);
    // }
    // }, jwtRefreshInSeconds, jwtRefreshInSeconds, TimeUnit.SECONDS);

    // logger.info("Authentication scheduler started - will refresh JWT token every
    // minute");
    // }
    // }

    // private void stopAuthenticationScheduler() {
    // if (authenticationScheduler != null && !authenticationScheduler.isShutdown())
    // {
    // authenticationScheduler.shutdown();
    // try {
    // if (!authenticationScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
    // authenticationScheduler.shutdownNow();
    // }
    // logger.info("Authentication scheduler stopped");
    // } catch (InterruptedException e) {
    // authenticationScheduler.shutdownNow();
    // Thread.currentThread().interrupt();
    // }
    // }
    // }

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

    // protected String authenticate() throws Exception {
    // jwtToken = restApi.getJwtToken();
    // logger.info("Obtained JWT Token");
    // return jwtToken;
    // }

    // public void startOrderStatusWSClient() {
    // logger.info("Starting order status WebSocket client");
    // String jwtToken = restApi.getJwtToken();
    // String wsUrl = ParadexApiFactory.getWebSocketUrl();

    // try {
    // orderStatusWSClient = new ParadexWebSocketClient(wsUrl, "orders.ALL",
    // orderStatusProcessor, jwtToken);
    // orderStatusWSClient.connect();
    // } catch (Exception e) {
    // throw new IllegalStateException(e);
    // }
    // }

    protected void startAccountInfoWSClient() {
        logger.info("Starting account info WebSocket client");
        String wsUrl = HyperliquidConfiguration.getInstance().getWebSocketUrl();

        try {
            accountWebSocketProcessor = new AccountWebSocketProcessor(() -> {
                logger.info("Account info WebSocket closed, trying to restart...");
                startAccountInfoWSClient();
            });
            accountWebSocketProcessor.addEventListener(this);
            accountInfoWSClient = new HyperliquidWebSocketClient(wsUrl, "account", accountWebSocketProcessor);

            accountInfoWSClient.connect();
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

    @Override
    public void onWebSocketEvent(IAccountUpdate event) {
        double accountValue = event.getAccountValue();
        fireAccountEquityUpdated(accountValue);

        currentPositions = Translator.translatePositions(event.getPositions());

        List<HyperliquidPositionUpdate> positions = event.getPositions();
        for (HyperliquidPositionUpdate pos : positions) {
            logger.info("Position: {} - Size: {} - Avg Price: {}", pos.getTicker(), pos.getSize(), pos.getEntryPrice());
        }
    }

}
