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

import com.sumzerotrading.BestBidOffer;
import com.sumzerotrading.broker.AbstractBasicBroker;
import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.hyperliquid.translators.ITranslator;
import com.sumzerotrading.broker.hyperliquid.translators.Translator;
import com.sumzerotrading.broker.order.OrderEvent;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.ws.HyperliquidApiFactory;
import com.sumzerotrading.hyperliquid.ws.HyperliquidConfiguration;
import com.sumzerotrading.hyperliquid.ws.HyperliquidWebSocketClient;
import com.sumzerotrading.hyperliquid.ws.IHyperliquidRestApi;
import com.sumzerotrading.hyperliquid.ws.IHyperliquidWebsocketApi;
import com.sumzerotrading.hyperliquid.ws.json.EncodeUtil;
import com.sumzerotrading.hyperliquid.ws.json.ws.SubmitPostResponse;
import com.sumzerotrading.hyperliquid.ws.listeners.accountinfo.AccountWebSocketProcessor;
import com.sumzerotrading.hyperliquid.ws.listeners.accountinfo.HyperliquidPositionUpdate;
import com.sumzerotrading.hyperliquid.ws.listeners.accountinfo.IAccountUpdate;
import com.sumzerotrading.hyperliquid.ws.listeners.orderupdates.WsOrderUpdate;
import com.sumzerotrading.hyperliquid.ws.listeners.orderupdates.WsOrderWebSocketProcessor;
import com.sumzerotrading.websocket.IWebSocketEventListener;

/**
 * Supported Order types are: Market, Stop and Limit Supported order parameters
 * are parent/child, OCA, Good-after-time, good-till-date. Supported
 * Time-in-force: DAY, Good-till-canceled, Good-till-time, Immediate-or-cancel
 *
 * @author Rob Terpilowski
 */
public class HyperliquidBroker extends AbstractBasicBroker {
    protected static Logger logger = LoggerFactory.getLogger(HyperliquidBroker.class);

    protected IHyperliquidRestApi restApi;
    protected IHyperliquidWebsocketApi websocketApi;

    protected boolean connected = false;
    protected Map<String, BestBidOffer> bestBidOfferMap = new HashMap<>();
    protected ITranslator translator = Translator.getInstance();

    protected List<Position> currentPositions = new ArrayList<>();

    protected HyperliquidWebSocketClient accountInfoWSClient;
    protected HyperliquidWebSocketClient orderStatusWSClient;
    protected AccountWebSocketProcessor accountWebSocketProcessor;
    protected WsOrderWebSocketProcessor orderStatusWebSocketProcessor;

    protected Set<OrderTicket> currencyOrderList = new HashSet<>();
    protected BlockingQueue<Integer> nextIdQueue = new LinkedBlockingQueue<>();

    protected int nextOrderId = 1;

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
        this.restApi = HyperliquidApiFactory.getRestApi();
        this.websocketApi = HyperliquidApiFactory.getWebsocketApi();

        // Get JWT refresh interval from configuration
        HyperliquidConfiguration config = HyperliquidConfiguration.getInstance();

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
    }

    @Override
    public void cancelOrder(String id) {
        checkConnected();
        throw new UnsupportedOperationException("Cancel order by ID not implemented yet");
    }

    @Override
    public void cancelOrder(OrderTicket order) {
        checkConnected();
        throw new UnsupportedOperationException("Cancel order by OrderTicket not implemented yet");
    }

    @Override
    public void placeOrder(OrderTicket order) {
        checkConnected();
        order.setClientOrderId(getNextOrderId());
        BestBidOffer bbo = bestBidOfferMap.get(order.getTicker().getSymbol());
        HyperliquidOrderTicket hyperliquidOrderTicket = new HyperliquidOrderTicket(bbo, order);
        SubmitPostResponse submittedOrders = websocketApi
                .submitOrders(translator.translateOrderTickets(hyperliquidOrderTicket));
        updateOrderIds(order, submittedOrders);

    }

    @Override
    public String getNextOrderId() {
        return EncodeUtil.encode128BitHex(nextOrderId++ + "");
    }

    @Override
    public void connect() {
        // startAuthenticationScheduler();
        orderEventExecutor = Executors.newCachedThreadPool();
        startAccountInfoWSClient();
        startOrderStatusWSClient();

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

    protected void startAccountInfoWSClient() {
        logger.info("Starting account info WebSocket client");
        String wsUrl = HyperliquidConfiguration.getInstance().getWebSocketUrl();

        try {
            accountWebSocketProcessor = new AccountWebSocketProcessor(() -> {
                logger.info("Account info WebSocket closed, trying to restart...");
                startAccountInfoWSClient();
            });
            accountWebSocketProcessor.addEventListener((IAccountUpdate event) -> {
                accountUpdateWsEventReceived(event);
            });
            accountInfoWSClient = new HyperliquidWebSocketClient(wsUrl, "account", accountWebSocketProcessor);

            accountInfoWSClient.connect();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    protected void startOrderStatusWSClient() {
        logger.info("Starting order status WebSocket client");
        String wsUrl = HyperliquidConfiguration.getInstance().getWebSocketUrl();

        try {
            orderStatusWebSocketProcessor = new WsOrderWebSocketProcessor(() -> {
                logger.info("Order status WebSocket closed, trying to restart...");
                startOrderStatusWSClient();
            });
            orderStatusWebSocketProcessor.addEventListener((List<WsOrderUpdate> event) -> {
                ordersUpdateWsEventReceived(event);
            });
            orderStatusWSClient = new HyperliquidWebSocketClient(wsUrl, "order", orderStatusWebSocketProcessor);

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

    public void ordersUpdateWsEventReceived(List<WsOrderUpdate> event) {
        for (WsOrderUpdate orderUpdate : event) {
            logger.info("WS Order Update {}", orderUpdate);
        }

    }

    public void accountUpdateWsEventReceived(IAccountUpdate event) {
        double accountValue = event.getAccountValue();
        fireAccountEquityUpdated(accountValue);

        currentPositions = translator.translatePositions(event.getPositions());

        List<HyperliquidPositionUpdate> positions = event.getPositions();
        for (HyperliquidPositionUpdate pos : positions) {
            logger.info("Position: {} - Size: {} - Avg Price: {}", pos.getTicker(), pos.getSize(), pos.getEntryPrice());
        }
    }

    protected void updateOrderIds(OrderTicket order, SubmitPostResponse response) {

        if (response.orders.size() != 1) {
            throw new IllegalStateException("Expected exactly one order in response");
        }
        int hyperliquidOrderId = response.orders.get(0).orderId;
        order.setOrderId(String.valueOf(hyperliquidOrderId));

    }

    protected void updateOrderIds(List<OrderTicket> orders, SubmitPostResponse response) {
        if (response.orders.size() != orders.size()) {
            throw new IllegalStateException(
                    "Expected " + orders.size() + " orders in response but got " + response.orders.size());
        }

        for (int i = 0; i < orders.size(); i++) {
            OrderTicket order = orders.get(i);
            int hyperliquidOrderId = response.orders.get(i).orderId;
            order.setOrderId(String.valueOf(hyperliquidOrderId));
        }
    }

}
