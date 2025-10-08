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
package com.sumzerotrading.broker.hyperliquid.capabilities;

import com.sumzerotrading.broker.capabilities.AbstractBrokerCapabilities;
import com.sumzerotrading.broker.capabilities.BrokerMethodCapability;
import com.sumzerotrading.broker.order.OrderTicket.Duration;
import com.sumzerotrading.broker.order.OrderTicket.Modifier;
import com.sumzerotrading.broker.order.OrderTicket.Type;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;

/**
 * Capability definition for Hyperliquid broker implementation. Documents all
 * supported features, limitations, and restrictions.
 */
public class HyperliquidBrokerCapabilities extends AbstractBrokerCapabilities {

    private static final HyperliquidBrokerCapabilities INSTANCE = new HyperliquidBrokerCapabilities();

    private HyperliquidBrokerCapabilities() {
        super(new Builder("Hyperliquid").description("High-performance perpetual futures DEX with low latency trading")
                .version("0.2.0")

                // Supported order types
                .supportedOrderTypes(Type.MARKET, // Market orders (converted to aggressive limit)
                        Type.LIMIT, // Standard limit orders
                        Type.STOP, // Stop market orders
                        Type.STOP_LIMIT // Stop limit orders
                )

                // Supported durations
                .supportedDurations(Duration.GOOD_UNTIL_CANCELED, // GTC orders
                        Duration.IMMEDIATE_OR_CANCEL, // IOC orders
                        Duration.FILL_OR_KILL // FOK orders (via IOC + minimum fill)
                )

                // Supported modifiers
                .supportedModifiers(Modifier.POST_ONLY, // Post-only orders (maker-only)
                        Modifier.REDUCE_ONLY // Reduce-only orders
                )

                // Supported instruments
                .supportedInstruments(InstrumentType.PERPETUAL_FUTURES // Only perp futures
                )

                // Capabilities
                .supportsRealTimeData(true).supportsHistoricalData(false) // Not implemented yet
                .supportsPaperTrading(true) // Via testnet
                .supportsPortfolioManagement(true).supportsOrderCancellation(true).supportsOrderModification(false) // Must
                                                                                                                    // cancel/replace

                // Method-level capabilities - what specific methods are supported
                .supportedMethods(
                        // Order cancellation - single order only, no batch cancellation
                        BrokerMethodCapability.CANCEL_ORDER_BY_ID, BrokerMethodCapability.CANCEL_ORDER_BY_TICKET,
                        BrokerMethodCapability.CANCEL_ALL_ORDERS, BrokerMethodCapability.CANCEL_ALL_ORDERS_FOR_TICKER,
                        // Note: CANCEL_ORDERS_BY_IDS and CANCEL_ORDERS_BY_TICKETS not supported

                        // Order placement - single orders only
                        BrokerMethodCapability.PLACE_ORDER,
                        // Note: PLACE_ORDERS_BATCH not supported - must place individually

                        // Order modification via cancel/replace pattern
                        BrokerMethodCapability.REPLACE_ORDER,
                        // Note: MODIFY_ORDER, MODIFY_ORDER_PRICE, MODIFY_ORDER_SIZE not supported

                        // Order queries
                        BrokerMethodCapability.GET_OPEN_ORDERS, BrokerMethodCapability.GET_OPEN_ORDERS_FOR_TICKER,
                        BrokerMethodCapability.GET_ORDER_STATUS, BrokerMethodCapability.GET_FILLS,
                        BrokerMethodCapability.GET_FILLS_FOR_TICKER,

                        // Position management
                        BrokerMethodCapability.GET_POSITIONS, BrokerMethodCapability.GET_POSITION_FOR_TICKER,
                        BrokerMethodCapability.CLOSE_POSITION, BrokerMethodCapability.CLOSE_ALL_POSITIONS,

                        // Account information
                        BrokerMethodCapability.GET_ACCOUNT_INFO, BrokerMethodCapability.GET_ACCOUNT_BALANCE,

                        // Market data
                        BrokerMethodCapability.SUBSCRIBE_MARKET_DATA, BrokerMethodCapability.UNSUBSCRIBE_MARKET_DATA,
                        BrokerMethodCapability.GET_MARKET_DATA_SNAPSHOT,

                        // Connection management
                        BrokerMethodCapability.CONNECT, BrokerMethodCapability.DISCONNECT,
                        BrokerMethodCapability.IS_CONNECTED)

                // Size limits
                .minOrderSize(0.00001) // 0.00001 BTC minimum
                .maxOrderSize(0.0) // No hard limit (subject to available liquidity)

                // Limitations
                .limitations("Market orders are converted to aggressive limit orders with 5% slippage protection",
                        "Only perpetual futures contracts are supported",
                        "Order modification requires cancel and replace",
                        "Maximum 5 significant digits for price formatting",
                        "Testnet and mainnet require different configurations",
                        "WebSocket connection required for real-time updates",
                        "EIP-712 signatures required for all order operations"));
    }

    public static HyperliquidBrokerCapabilities getInstance() {
        return INSTANCE;
    }

    @Override
    public double getMinOrderSize(Ticker ticker) {
        // Hyperliquid has different minimum order sizes per asset
        if (ticker.getSymbol().equals("BTC")) {
            return 0.00001;
        } else if (ticker.getSymbol().equals("ETH")) {
            return 0.0001;
        } else if (ticker.getSymbol().equals("SOL")) {
            return 0.001;
        }
        // Default for other assets
        return 0.001;
    }

    @Override
    public String validateOrderTicket(com.sumzerotrading.broker.order.OrderTicket order) {
        // First run standard validation
        String baseValidation = super.validateOrderTicket(order);
        if (baseValidation != null) {
            return baseValidation;
        }

        // Hyperliquid-specific validations
        if (order.getType() == Type.MARKET_ON_OPEN || order.getType() == Type.MARKET_ON_CLOSE) {
            return "Market on open/close orders are not supported by Hyperliquid";
        }

        if (order.getType() == Type.TRAILING_STOP || order.getType() == Type.TRAILING_STOP_LIMIT) {
            return "Trailing stop orders are not currently supported by Hyperliquid";
        }

        // Check price precision (max 5 significant digits)
        if (order.getLimitPrice() != null) {
            String priceStr = order.getLimitPrice().toPlainString();
            if (getSignificantDigitCount(priceStr) > 5) {
                return "Price precision limited to 5 significant digits on Hyperliquid";
            }
        }

        if (order.getStopPrice() != null) {
            String priceStr = order.getStopPrice().toPlainString();
            if (getSignificantDigitCount(priceStr) > 5) {
                return "Stop price precision limited to 5 significant digits on Hyperliquid";
            }
        }

        return null; // Valid
    }

    @Override
    public String validateMethodCall(BrokerMethodCapability method) {
        // Check base validation first
        String baseValidation = super.validateMethodCall(method);
        if (baseValidation != null) {
            return baseValidation;
        }

        // Hyperliquid-specific method limitations
        switch (method) {
        case CANCEL_ORDERS_BY_IDS:
        case CANCEL_ORDERS_BY_TICKETS:
            return "Hyperliquid does not support batch order cancellation. Use individual cancelOrder() calls instead.";

        case PLACE_ORDERS_BATCH:
            return "Hyperliquid does not support batch order placement. Submit orders individually for better error handling.";

        case MODIFY_ORDER:
        case MODIFY_ORDER_PRICE:
        case MODIFY_ORDER_SIZE:
            return "Hyperliquid does not support direct order modification. Use cancel and replace pattern instead.";

        case GET_ORDER_HISTORY:
            return "Order history is not currently available via Hyperliquid API. Use fills data for execution history.";

        case GET_HISTORICAL_BARS:
        case GET_HISTORICAL_TICKS:
            return "Historical data is not yet implemented for Hyperliquid. Use real-time data only.";

        case PLACE_BRACKET_ORDER:
        case PLACE_OCO_ORDER:
        case PLACE_CONDITIONAL_ORDER:
            return "Complex order types not supported. Use individual orders with POST_ONLY or REDUCE_ONLY modifiers.";

        default:
            return null; // Method is supported
        }
    }

    private int getSignificantDigitCount(String numberStr) {
        // Remove decimal point and leading zeros
        String cleaned = numberStr.replaceAll("\\.", "").replaceAll("^0+", "");
        if (cleaned.isEmpty()) {
            return 1; // For "0" or "0.000"
        }
        // Remove trailing zeros after decimal point
        if (numberStr.contains(".")) {
            cleaned = cleaned.replaceAll("0+$", "");
        }
        return cleaned.length();
    }
}