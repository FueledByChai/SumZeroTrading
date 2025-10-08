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

import java.util.Set;
import com.sumzerotrading.broker.order.OrderTicket.Duration;
import com.sumzerotrading.broker.order.OrderTicket.Modifier;
import com.sumzerotrading.broker.order.OrderTicket.Type;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;

/**
 * Interface for querying broker capabilities at runtime. This allows for
 * dynamic capability checking and validation.
 */
public interface BrokerCapabilities {

    /**
     * Get the broker name
     */
    String getBrokerName();

    /**
     * Get broker description
     */
    String getDescription();

    /**
     * Check if an order type is supported
     */
    boolean supportsOrderType(Type orderType);

    /**
     * Get all supported order types
     */
    Set<Type> getSupportedOrderTypes();

    /**
     * Check if an order duration is supported
     */
    boolean supportsDuration(Duration duration);

    /**
     * Get all supported order durations
     */
    Set<Duration> getSupportedDurations();

    /**
     * Check if an order modifier is supported
     */
    boolean supportsModifier(Modifier modifier);

    /**
     * Get all supported order modifiers
     */
    Set<Modifier> getSupportedModifiers();

    /**
     * Check if an instrument type is supported
     */
    boolean supportsInstrumentType(InstrumentType instrumentType);

    /**
     * Get all supported instrument types
     */
    Set<InstrumentType> getSupportedInstrumentTypes();

    /**
     * Check if a specific ticker is supported
     */
    boolean supportsTicker(Ticker ticker);

    /**
     * Check if real-time market data is supported
     */
    boolean supportsRealTimeData();

    /**
     * Check if historical data is supported
     */
    boolean supportsHistoricalData();

    /**
     * Check if paper trading is supported
     */
    boolean supportsPaperTrading();

    /**
     * Check if portfolio management is supported
     */
    boolean supportsPortfolioManagement();

    /**
     * Check if order cancellation is supported
     */
    boolean supportsOrderCancellation();

    /**
     * Check if order modification is supported
     */
    boolean supportsOrderModification();

    /**
     * Get maximum order size (0 means no limit)
     */
    double getMaxOrderSize();

    /**
     * Get minimum order size (0 means no minimum)
     */
    double getMinOrderSize();

    /**
     * Get minimum order size for a specific ticker
     */
    double getMinOrderSize(Ticker ticker);

    /**
     * Get any limitations or restrictions
     */
    Set<String> getLimitations();

    /**
     * Get API version
     */
    String getVersion();

    /**
     * Validate if an order ticket is supported by this broker
     * 
     * @return null if valid, or error message if invalid
     */
    String validateOrderTicket(com.sumzerotrading.broker.order.OrderTicket order);

    /**
     * Check if a specific broker method is supported
     */
    boolean supportsMethod(BrokerMethodCapability method);

    /**
     * Get all supported broker methods
     */
    Set<BrokerMethodCapability> getSupportedMethods();

    /**
     * Get supported methods by category
     */
    Set<BrokerMethodCapability> getSupportedMethodsByCategory(String category);

    /**
     * Validate if a specific method call is supported
     * 
     * @return null if valid, or error message if not supported
     */
    String validateMethodCall(BrokerMethodCapability method);
}