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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.broker.AbstractBasicBroker;
import com.sumzerotrading.broker.BrokerRequestResult;
import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.order.Fill;
import com.sumzerotrading.broker.order.OrderEvent;
import com.sumzerotrading.broker.order.OrderStatus;
import com.sumzerotrading.broker.order.OrderStatus.CancelReason;
import com.sumzerotrading.broker.order.OrderStatus.Status;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.paradex.common.api.IParadexRestApi;
import com.sumzerotrading.paradex.common.api.ParadexApiFactory;
import com.sumzerotrading.paradex.common.api.ParadexConfiguration;
import com.sumzerotrading.paradex.common.api.RestResponse;
import com.sumzerotrading.paradex.common.api.ws.ParadexWSClientBuilder;
import com.sumzerotrading.paradex.common.api.ws.ParadexWebSocketClient;
import com.sumzerotrading.paradex.common.api.ws.accountinfo.AccountWebSocketProcessor;
import com.sumzerotrading.paradex.common.api.ws.accountinfo.IAccountUpdate;
import com.sumzerotrading.paradex.common.api.ws.fills.ParadexFill;
import com.sumzerotrading.paradex.common.api.ws.fills.ParadexFillsWebSocketProcessor;
import com.sumzerotrading.paradex.common.api.ws.orderstatus.IParadexOrderStatusUpdate;
import com.sumzerotrading.paradex.common.api.ws.orderstatus.OrderStatusWebSocketProcessor;
import com.sumzerotrading.util.FillDeduper;

/**
 */
public class ParadexBroker extends AbstractBasicBroker {
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
    protected ParadexFillsWebSocketProcessor fillsWebSocketProcessor;

    protected int nextOrderId = -1;

    protected Set<String> filledOrderSet = new HashSet<>();

    protected ScheduledExecutorService authenticationScheduler;

    protected IParadexTranslator translator;

    protected boolean started = false;

    protected List<Position> positionsList = new ArrayList<>();
    protected Map<String, OrderTicket> tradeOrderMap = new HashMap<>();

    protected Map<String, OrderTicket> completedOrderMap = new HashMap<>();

    protected FillDeduper fillDeduper = new FillDeduper();

    /**
     * Default constructor - uses centralized configuration for API initialization.
     */
    public ParadexBroker() {
        // Initialize using centralized configuration
        this.restApi = ParadexApiFactory.getPrivateApi();

        // Get JWT refresh interval from configuration
        ParadexConfiguration config = ParadexConfiguration.getInstance();
        this.jwtRefreshInSeconds = config.getJwtRefreshSeconds();

        this.translator = ParadexTranslator.getInstance();

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
    public BrokerRequestResult cancelOrder(String id) {
        checkConnected();
        logger.info("Canceling order with ID: {}", id);
        RestResponse cancelOrderResponse = restApi.cancelOrder(jwtToken, id);

        if (!cancelOrderResponse.isSuccessful()) {
            String errormessage = cancelOrderResponse.getBody();
            logger.error("Failed to cancel order {}: {}", id, errormessage);

            if (errormessage != null && errormessage.contains("ORDER_IS_CLOSED")) {
                logger.error("Removing order {} from active list since it is likely already closed.", id);
                String closedOrderId = errormessage.replaceAll(".*?(\\d+).*", "$1");
                OrderTicket order = tradeOrderMap.get(closedOrderId);
                if (order != null) {
                    order.setCurrentStatus(OrderStatus.Status.CANCELED);
                    OrderStatus status = new OrderStatus(OrderStatus.Status.CANCELED, order.getOrderId(),
                            order.getFilledSize(), order.getSize().subtract(order.getFilledSize()),
                            order.getFilledPrice(), order.getTicker(), getCurrentTime());
                    status.setCancelReason(CancelReason.USER_CANCELED);

                    OrderEvent event = new OrderEvent(order, status);

                    tradeOrderMap.remove(closedOrderId);
                    super.fireOrderEvent(event);

                }
                return new BrokerRequestResult(false, errormessage);
            }
        } else {
            logger.info("Cancel order request for {} successful.", id);

        }
        return new BrokerRequestResult();
    }

    @Override
    public BrokerRequestResult cancelOrder(OrderTicket order) {
        checkConnected();
        return cancelOrder(order.getOrderId());
    }

    @Override
    public void placeOrder(OrderTicket order) {
        checkConnected();
        order.setOrderEntryTime(getCurrentTime());
        String orderId = restApi.placeOrder(jwtToken, order);
        logger.info("{} Order for {} placed with ID: {}", order.getDirection(), order.getTicker().getSymbol(), orderId);
        order.setOrderId(orderId);
        tradeOrderMap.put(orderId, order);
    }

    @Override
    public String getNextOrderId() {
        return "";
    }

    @Override
    public void connect() {
        accountWebSocketProcessor = new AccountWebSocketProcessor(() -> {
            logger.info("Account info WebSocket closed, trying to restart...");
            startFillsWSClient();
        });
        accountWebSocketProcessor.addEventListener(accountInfo -> {
            onParadexAccountInfoEvent(accountInfo);
        });

        orderStatusProcessor = new OrderStatusWebSocketProcessor(() -> {
            logger.info("Order status WebSocket closed, trying to restart...");
            startOrderStatusWSClient();
        });
        orderStatusProcessor.addEventListener(orderStatus -> {
            onParadexOrderStatusEvent(orderStatus);
        });

        fillsWebSocketProcessor = new ParadexFillsWebSocketProcessor(() -> {
            logger.info("Fills WebSocket closed, trying to restart...");
            startFillsWSClient();
        });
        fillsWebSocketProcessor.addEventListener(fill -> {
            onParadexFillEvent(fill);
        });

        startAuthenticationScheduler();
        startAccountInfoWSClient();
        startOrderStatusWSClient();
        startFillsWSClient();

        connected = true;
    }

    @Override
    protected void onDisconnect() {
        stopAuthenticationScheduler();
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
    }

    protected void onParadexOrderStatusEvent(IParadexOrderStatusUpdate orderStatus) {
        logger.info("Received order status update: {}", orderStatus);
        OrderStatus status = translator.translateOrderStatus(orderStatus);
        OrderTicket order = tradeOrderMap.get(orderStatus.getOrderId());
        if (order == null) {
            order = completedOrderMap.get(orderStatus.getOrderId());
            if (order == null) {
                logger.warn("Received order status update for unknown order ID: {}", orderStatus.getOrderId());
                return;
            }
        }
        order.setCurrentStatus(status.getStatus());
        order.setFilledPrice(status.getFillPrice());
        order.setFilledSize(status.getFilled());
        // Can't set the order commission here, only on fill events.

        OrderEvent event = new OrderEvent(order, status);
        if (status.getStatus() == OrderStatus.Status.FILLED || status.getStatus() == OrderStatus.Status.CANCELED) {
            tradeOrderMap.remove(orderStatus.getOrderId());
        }

        if (status.getStatus() == OrderStatus.Status.FILLED) {
            order.setOrderFilledTime(status.getTimestamp());
        }

        super.fireOrderEvent(event);
    }

    protected void onParadexFillEvent(ParadexFill paradexFill) {
        logger.info("Received fill event: {}", paradexFill);
        Fill fill = translator.translateFill(paradexFill);
        if (fill.isSnapshot() || fillDeduper.firstTime(fill.getFillId())) {
            fireFillEvent(fill);
        } else {
            logger.warn("Duplicate fill received, ignoring: {}", fill);
        }
    }

    protected void onParadexAccountInfoEvent(IAccountUpdate accountInfo) {
        fireAccountEquityUpdated(accountInfo.getAccountValue());

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

    protected String authenticate() throws Exception {
        jwtToken = restApi.getJwtToken();
        logger.info("Obtained JWT Token");
        return jwtToken;
    }

    public void startAccountInfoWSClient() {
        logger.info("Starting account info WebSocket client");
        String jwtToken = restApi.getJwtToken();
        String wsUrl = ParadexApiFactory.getWebSocketUrl();

        try {
            accountInfoWSClient = ParadexWSClientBuilder.buildAccountInfoClient(wsUrl, accountWebSocketProcessor,
                    jwtToken);
            accountInfoWSClient.connect();
        } catch (Exception e) {
            throw new IllegalStateException(e);

        }

    }

    public void startOrderStatusWSClient() {
        logger.info("Starting order status WebSocket client");
        String jwtToken = restApi.getJwtToken();
        String wsUrl = ParadexApiFactory.getWebSocketUrl();

        try {
            orderStatusWSClient = ParadexWSClientBuilder.buildOrderStatusClient(wsUrl, orderStatusProcessor, jwtToken);
            orderStatusWSClient.connect();
        } catch (Exception e) {
            throw new IllegalStateException(e);

        }

    }

    public void startFillsWSClient() {
        logger.info("Starting fills WebSocket client");
        String jwtToken = restApi.getJwtToken();
        String wsUrl = ParadexApiFactory.getWebSocketUrl();

        try {
            fillsWebSocketProcessor = new ParadexFillsWebSocketProcessor(() -> {
                logger.info("Fills WebSocket closed, trying to restart...");
                startFillsWSClient();
            });
            fillsWebSocketProcessor.addEventListener(fill -> {
                onParadexFillEvent(fill);
            });

            accountInfoWSClient = ParadexWSClientBuilder.buildFillsClient(wsUrl, fillsWebSocketProcessor, jwtToken);
            accountInfoWSClient.connect();
        } catch (Exception e) {
            throw new IllegalStateException(e);

        }

    }

    @Override
    public BrokerRequestResult cancelAllOrders(Ticker ticker) {
        throw new UnsupportedOperationException("Cancel all orders by ticker not implemented yet");
    }

    @Override
    public BrokerRequestResult cancelAllOrders() {
        throw new UnsupportedOperationException("Cancel all orders not implemented yet");

    }

}
