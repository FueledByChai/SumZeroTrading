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

import java.time.ZonedDateTime;
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
import com.sumzerotrading.broker.BrokerRequestResult;
import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.hyperliquid.translators.ITranslator;
import com.sumzerotrading.broker.hyperliquid.translators.Translator;
import com.sumzerotrading.broker.order.Fill;
import com.sumzerotrading.broker.order.OrderEvent;
import com.sumzerotrading.broker.order.OrderStatus;
import com.sumzerotrading.broker.order.OrderStatus.CancelReason;
import com.sumzerotrading.broker.order.OrderStatus.Status;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.HyperliquidUtil;
import com.sumzerotrading.hyperliquid.ws.HyperliquidApiFactory;
import com.sumzerotrading.hyperliquid.ws.HyperliquidConfiguration;
import com.sumzerotrading.hyperliquid.ws.HyperliquidWebSocketClient;
import com.sumzerotrading.hyperliquid.ws.HyperliquidWebSocketClientBuilder;
import com.sumzerotrading.hyperliquid.ws.IHyperliquidRestApi;
import com.sumzerotrading.hyperliquid.ws.IHyperliquidWebsocketApi;
import com.sumzerotrading.hyperliquid.ws.json.ws.SubmitPostResponse;
import com.sumzerotrading.hyperliquid.ws.listeners.accountinfo.AccountWebSocketProcessor;
import com.sumzerotrading.hyperliquid.ws.listeners.accountinfo.HyperliquidPositionUpdate;
import com.sumzerotrading.hyperliquid.ws.listeners.accountinfo.IAccountUpdate;
import com.sumzerotrading.hyperliquid.ws.listeners.orderupdates.WsOrderUpdate;
import com.sumzerotrading.hyperliquid.ws.listeners.orderupdates.WsOrderWebSocketProcessor;
import com.sumzerotrading.hyperliquid.ws.listeners.userfills.WsUserFill;
import com.sumzerotrading.hyperliquid.ws.listeners.userfills.WsUserFillsWebSocketProcessor;
import com.sumzerotrading.marketdata.ILevel1Quote;
import com.sumzerotrading.marketdata.Level1QuoteListener;
import com.sumzerotrading.marketdata.QuoteEngine;
import com.sumzerotrading.marketdata.QuoteType;
import com.sumzerotrading.marketdata.hyperliquid.HyperliquidQuoteEngine;
import com.sumzerotrading.util.FillDeduper;

/**
 * Supported Order types are: Market, Stop and Limit Supported order parameters
 * are parent/child, OCA, Good-after-time, good-till-date. Supported
 * Time-in-force: DAY, Good-till-canceled, Good-till-time, Immediate-or-cancel
 *
 * @author Rob Terpilowski
 */
public class HyperliquidBroker extends AbstractBasicBroker implements Level1QuoteListener {
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
    protected WsUserFillsWebSocketProcessor fillWebSocketProcessor;

    protected Set<OrderTicket> currencyOrderList = new HashSet<>();
    protected BlockingQueue<Integer> nextIdQueue = new LinkedBlockingQueue<>();

    protected int nextOrderId = 1;
    protected String accountAddress;
    protected String wsUrl;

    protected ScheduledExecutorService authenticationScheduler;
    protected ExecutorService orderEventExecutor;

    protected boolean started = false;
    protected String directory;
    protected Map<String, OrderEvent> orderEventMap;
    protected List<Position> positionsList = new ArrayList<>();
    protected Map<String, OrderTicket> tradeOrderMap = new HashMap<>();

    protected Map<String, OrderTicket> completedOrderMap = new HashMap<>();
    protected QuoteEngine quoteEngine;

    protected Map<String, OrderTicket> pendingOrderMapByCloid = new HashMap<>();
    protected Map<String, String> exchangeIdToCloidMap = new HashMap<>();

    protected FillDeduper fillDeduper = new FillDeduper();

    /**
     * Default constructor - uses centralized configuration for API initialization.
     */
    public HyperliquidBroker() {
        // Initialize using centralized configuration
        this.restApi = HyperliquidApiFactory.getRestApi();
        this.websocketApi = HyperliquidApiFactory.getWebsocketApi();
        this.quoteEngine = QuoteEngine.getInstance(HyperliquidQuoteEngine.class);
        this.quoteEngine.startEngine();
        this.quoteEngine.subscribeGlobalLevel1(this);
        this.accountAddress = HyperliquidConfiguration.getInstance().getTradingAccount();
        this.wsUrl = HyperliquidConfiguration.getInstance().getWebSocketUrl();

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
    public BrokerRequestResult cancelOrder(String id) {
        checkConnected();
        throw new UnsupportedOperationException("Cancel order by ID not implemented yet");
    }

    @Override
    public BrokerRequestResult cancelOrder(OrderTicket order) {
        checkConnected();
        throw new UnsupportedOperationException("Cancel order by OrderTicket not implemented yet");
    }

    @Override
    public void placeOrder(OrderTicket order) {
        checkConnected();
        order.setClientOrderId(getNextOrderId());
        BestBidOffer bbo = bestBidOfferMap.get(order.getTicker().getSymbol());
        int tries = 0;
        int maxTries = 30; // Wait up to 30 seconds for market data
        while (bbo == null) {
            try {
                logger.info("Waiting for market data for " + order.getTicker().getSymbol() + " to place order");
                Thread.sleep(1000);
                bbo = bestBidOfferMap.get(order.getTicker().getSymbol());
                tries++;
                if (tries > maxTries) {
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SumZeroException(
                        "Interrupted while waiting for market data for " + order.getTicker().getSymbol());
            }
        }
        if (bbo == null) {
            throw new SumZeroException("No market data available for " + order.getTicker() + ", cannot place order");
        }
        order.setOrderEntryTime(getCurrentTime());
        HyperliquidOrderTicket hyperliquidOrderTicket = new HyperliquidOrderTicket(bbo, order);
        pendingOrderMapByCloid.put(order.getClientOrderId(), order);
        logger.info("Created order ticket: ");
        SubmitPostResponse submittedOrders = websocketApi
                .submitOrders(translator.translateOrderTickets(hyperliquidOrderTicket));
        updateOrderIds(order, submittedOrders);

    }

    @Override
    public String getNextOrderId() {
        return HyperliquidUtil.encode128BitHex(nextOrderId++ + "");
    }

    @Override
    public void connect() {
        // startAuthenticationScheduler();
        orderEventExecutor = Executors.newCachedThreadPool();
        startAccountInfoWSClient();
        startOrderStatusWSClient();
        startFillWSClient();

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

        try {
            accountWebSocketProcessor = new AccountWebSocketProcessor(() -> {
                logger.info("Account info WebSocket closed, trying to restart...");
                startAccountInfoWSClient();
            });
            accountWebSocketProcessor.addEventListener((IAccountUpdate event) -> {
                accountUpdateWsEventReceived(event);
            });
            accountInfoWSClient = HyperliquidWebSocketClientBuilder.buildAccountInfoClient(wsUrl, accountAddress,
                    accountWebSocketProcessor);

            accountInfoWSClient.connect();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    protected void startOrderStatusWSClient() {
        logger.info("Starting order status WebSocket client");

        try {
            orderStatusWebSocketProcessor = new WsOrderWebSocketProcessor(() -> {
                logger.info("Order status WebSocket closed, trying to restart...");
                startOrderStatusWSClient();
            });
            orderStatusWebSocketProcessor.addEventListener((List<WsOrderUpdate> event) -> {
                ordersUpdateWsEventReceived(event);
            });
            orderStatusWSClient = HyperliquidWebSocketClientBuilder.buildOrderUpdateClient(wsUrl, accountAddress,
                    orderStatusWebSocketProcessor);

            orderStatusWSClient.connect();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    public void startFillWSClient() {
        logger.info("Starting fill WebSocket client");

        try {
            fillWebSocketProcessor = new WsUserFillsWebSocketProcessor(() -> {
                logger.info("Fill WebSocket closed, trying to restart...");
                startFillWSClient();
            });
            fillWebSocketProcessor.addEventListener((WsUserFill event) -> {
                fillEventWsReceived(event);
            });
            HyperliquidWebSocketClient fillWSClient = HyperliquidWebSocketClientBuilder.buildUserFillsClient(wsUrl,
                    accountAddress, fillWebSocketProcessor);

            fillWSClient.connect();
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

    public void ordersUpdateWsEventReceived(List<WsOrderUpdate> event) {

        for (WsOrderUpdate orderUpdate : event) {
            logger.info("Order Update: {}", orderUpdate);
            OrderTicket orderTicket = pendingOrderMapByCloid.get(orderUpdate.getClientOrderId());
            if (orderTicket == null) {
                logger.warn("NO ORDER TICKET FOUND for client order ID {}, ignoring order update",
                        orderUpdate.getClientOrderId());
                continue;
            }
            Status status;
            CancelReason cancelReason = CancelReason.NONE;
            switch (orderUpdate.getStatus()) {
            case FILLED:
                status = Status.FILLED;
                break;
            case CANCELED:
            case MARGIN_CANCELED:
            case VAULT_WITHDRAWAL_CANCELED:
            case OPEN_INTEREST_CAP_CANCELED:
            case SELF_TRADE_CANCELED:
            case REDUCE_ONLY_CANCELED:
            case SIBLING_FILLED_CANCELED:
            case DELISTED_CANCELED:
            case LIQUIDATED_CANCELED:
            case SCHEDULED_CANCEL:
            case TICK_REJECTED:
            case MIN_TRADE_NTL_REJECTED:
            case PERP_MARGIN_REJECTED:
            case REDUCE_ONLY_REJECTED:
            case BAD_TRIGGER_PX_REJECTED:
            case MARKET_ORDER_NO_LIQUIDITY_REJECTED:
            case POSITION_INCREASE_AT_OPEN_INTEREST_CAP_REJECTED:
            case POSITION_FLIP_AT_OPEN_INTEREST_CAP_REJECTED:
            case TOO_AGGRESSIVE_AT_OPEN_INTEREST_CAP_REJECTED:
            case OPEN_INTEREST_INCREASE_REJECTED:
            case INSUFFICIENT_SPOT_BALANCE_REJECTED:
            case ORACLE_REJECTED:
            case PERP_MAX_POSITION_REJECTED:
                status = Status.CANCELED;
                cancelReason = CancelReason.USER_CANCELED;
                break;
            case BAD_ALO_PX_REJECTED:
                status = Status.CANCELED;
                cancelReason = CancelReason.POST_ONLY_WOULD_CROSS;
                break;
            case REJECTED:
                status = Status.REJECTED;
                break;
            default:
                status = Status.UNKNOWN;
            }

            ZonedDateTime timestamp = ZonedDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(orderUpdate.getStatusTimestamp()), java.time.ZoneId.of("UTC"));
            if (status == Status.FILLED) {
                orderTicket.setOrderFilledTime(timestamp);
                orderTicket.setCurrentStatus(Status.FILLED);
            }

            OrderStatus orderStatus = new OrderStatus(status, orderTicket.getOrderId(), orderTicket.getFilledSize(),
                    orderTicket.getRemainingSize(), orderTicket.getFilledPrice(), orderTicket.getTicker(), timestamp);
            orderStatus.setCancelReason(cancelReason);

            OrderEvent orderEvent = new OrderEvent(orderTicket, orderStatus);

            fireOrderEvent(orderEvent);
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

    public void fillEventWsReceived(WsUserFill wsUserFills) {
        logger.info("Fill event received: {}", wsUserFills);
        List<Fill> fills = translator.translateFill(wsUserFills);
        for (Fill fill : fills) {
            if (fill.isSnapshot() || fillDeduper.firstTime(fill.getFillId())) {

                logger.info("Fill received: {}", fill);
                String clientOrderId = exchangeIdToCloidMap.get(fill.getOrderId());
                logger.info("Client order ID for exchange order ID {} is {}", fill.getOrderId(), clientOrderId);
                fill.setClientOrderId(clientOrderId != null ? clientOrderId : "");
                OrderTicket orderTicket = pendingOrderMapByCloid.get(clientOrderId);
                if (orderTicket != null) {
                    orderTicket.setFilledSize(orderTicket.getFilledSize().add(fill.getSize()));
                    orderTicket.setCommission(fill.getCommission().add(orderTicket.getCommission()));
                    orderTicket.addFill(fill);
                    if (orderTicket.getFilledSize().compareTo(orderTicket.getSize()) >= 0) {
                        orderTicket.setCurrentStatus(OrderStatus.Status.FILLED);
                    } else {
                        orderTicket.setCurrentStatus(OrderStatus.Status.PARTIAL_FILL);
                    }
                }
                fireFillEvent(fill);
            } else {
                logger.warn("Duplicate fill received, ignoring: {}", fill);
            }
        }
    }

    @Override
    public void quoteRecieved(ILevel1Quote quote) {
        // logger.info("Quote received: {}", quote);
        if (quote.containsType(QuoteType.BID) && quote.containsType(QuoteType.ASK)) {
            bestBidOfferMap.put(quote.getTicker().getSymbol(),
                    new BestBidOffer(quote.getValue(QuoteType.BID), quote.getValue(QuoteType.ASK)));
        }

    }

    protected void updateOrderIds(OrderTicket order, SubmitPostResponse response) {

        if (response.orders.size() != 1) {
            throw new IllegalStateException("Expected exactly one order in response");
        }
        int hyperliquidOrderId = response.orders.get(0).orderId;
        if (hyperliquidOrderId <= 0) {
            return;
        }

        order.setOrderId(String.valueOf(hyperliquidOrderId));
        exchangeIdToCloidMap.put(String.valueOf(hyperliquidOrderId), order.getClientOrderId());

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
