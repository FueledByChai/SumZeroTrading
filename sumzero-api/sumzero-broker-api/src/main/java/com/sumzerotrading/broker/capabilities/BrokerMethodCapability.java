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
package com.sumzerotrading.broker.capabilities;

/**
 * Enumeration of specific broker method capabilities. This provides granular
 * control over which specific methods are supported.
 */
public enum BrokerMethodCapability {

    // Order Management Methods
    CANCEL_ORDER_BY_ID("cancelOrder(String id)", "Cancel single order by ID"),
    CANCEL_ORDER_BY_TICKET("cancelOrder(OrderTicket order)", "Cancel order by ticket reference"),
    CANCEL_ORDERS_BY_IDS("cancelOrders(List<String> ids)", "Cancel multiple orders by IDs"),
    CANCEL_ORDERS_BY_TICKETS("cancelOrders(List<OrderTicket> orders)", "Cancel multiple orders by tickets"),
    CANCEL_ALL_ORDERS("cancelAllOrders()", "Cancel all open orders"),
    CANCEL_ALL_ORDERS_FOR_TICKER("cancelAllOrders(Ticker ticker)", "Cancel all orders for specific ticker"),

    // Order Placement Methods
    PLACE_ORDER("placeOrder(OrderTicket order)", "Place single order"),
    PLACE_ORDERS_BATCH("placeOrders(List<OrderTicket> orders)", "Place multiple orders in batch"),
    PLACE_ORDER_ASYNC("placeOrderAsync(OrderTicket order)", "Place order asynchronously"),

    // Order Modification Methods
    MODIFY_ORDER("modifyOrder(OrderTicket order)", "Modify existing order"),
    MODIFY_ORDER_PRICE("modifyOrderPrice(String id, BigDecimal price)", "Modify order price only"),
    MODIFY_ORDER_SIZE("modifyOrderSize(String id, BigDecimal size)", "Modify order size only"),
    REPLACE_ORDER("replaceOrder(String oldId, OrderTicket newOrder)", "Replace order (cancel + new)"),

    // Order Query Methods
    GET_OPEN_ORDERS("getOpenOrders()", "Get all open orders"),
    GET_OPEN_ORDERS_FOR_TICKER("getOpenOrders(Ticker ticker)", "Get open orders for ticker"),
    GET_ORDER_STATUS("getOrderStatus(String id)", "Get status of specific order"),
    GET_ORDER_HISTORY("getOrderHistory()", "Get historical orders"), GET_FILLS("getFills()", "Get order fills"),
    GET_FILLS_FOR_TICKER("getFills(Ticker ticker)", "Get fills for specific ticker"),

    // Position Management Methods
    GET_POSITIONS("getPositions()", "Get all positions"),
    GET_POSITION_FOR_TICKER("getPosition(Ticker ticker)", "Get position for specific ticker"),
    CLOSE_POSITION("closePosition(Ticker ticker)", "Close position for ticker"),
    CLOSE_ALL_POSITIONS("closeAllPositions()", "Close all positions"),

    // Account Information Methods
    GET_ACCOUNT_INFO("getAccountInfo()", "Get account information"),
    GET_ACCOUNT_BALANCE("getAccountBalance()", "Get account balance"),
    GET_BUYING_POWER("getBuyingPower()", "Get available buying power"),
    GET_PORTFOLIO_VALUE("getPortfolioValue()", "Get total portfolio value"),

    // Market Data Methods
    SUBSCRIBE_MARKET_DATA("subscribeMarketData(Ticker ticker)", "Subscribe to real-time market data"),
    UNSUBSCRIBE_MARKET_DATA("unsubscribeMarketData(Ticker ticker)", "Unsubscribe from market data"),
    GET_MARKET_DATA_SNAPSHOT("getMarketDataSnapshot(Ticker ticker)", "Get current market data snapshot"),

    // Historical Data Methods
    GET_HISTORICAL_BARS("getHistoricalBars(Ticker, period)", "Get historical price bars"),
    GET_HISTORICAL_TICKS("getHistoricalTicks(Ticker, period)", "Get historical tick data"),

    // Connection Management Methods
    CONNECT("connect()", "Establish broker connection"), DISCONNECT("disconnect()", "Close broker connection"),
    IS_CONNECTED("isConnected()", "Check connection status"), RECONNECT("reconnect()", "Reconnect to broker"),

    // Risk Management Methods
    SET_RISK_LIMITS("setRiskLimits(RiskLimits limits)", "Set risk management limits"),
    GET_RISK_LIMITS("getRiskLimits()", "Get current risk limits"),

    // Combo/Complex Orders
    PLACE_BRACKET_ORDER("placeBracketOrder(BracketOrder order)", "Place bracket order (entry + stops)"),
    PLACE_OCO_ORDER("placeOCOOrder(OCOOrder order)", "Place one-cancels-other order"),
    PLACE_CONDITIONAL_ORDER("placeConditionalOrder(ConditionalOrder order)", "Place conditional order");

    private final String methodSignature;
    private final String description;

    BrokerMethodCapability(String methodSignature, String description) {
        this.methodSignature = methodSignature;
        this.description = description;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get method name without parameters
     */
    public String getMethodName() {
        int parenIndex = methodSignature.indexOf('(');
        return parenIndex > 0 ? methodSignature.substring(0, parenIndex) : methodSignature;
    }

    /**
     * Check if this capability is related to order cancellation
     */
    public boolean isOrderCancellation() {
        return this.name().startsWith("CANCEL_");
    }

    /**
     * Check if this capability is related to order placement
     */
    public boolean isOrderPlacement() {
        return this.name().startsWith("PLACE_");
    }

    /**
     * Check if this capability is related to order modification
     */
    public boolean isOrderModification() {
        return this.name().startsWith("MODIFY_") || this == REPLACE_ORDER;
    }

    /**
     * Check if this capability is related to market data
     */
    public boolean isMarketData() {
        return this.name().contains("MARKET_DATA") || this.name().contains("HISTORICAL");
    }
}