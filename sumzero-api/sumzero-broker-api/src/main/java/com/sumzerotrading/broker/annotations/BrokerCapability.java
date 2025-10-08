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
package com.sumzerotrading.broker.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sumzerotrading.broker.order.OrderTicket.Duration;
import com.sumzerotrading.broker.order.OrderTicket.Modifier;
import com.sumzerotrading.broker.order.OrderTicket.Type;
import com.sumzerotrading.data.InstrumentType;

/**
 * Annotation to document broker capabilities including supported order types,
 * durations, modifiers, and instrument types.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BrokerCapability {

    /**
     * Broker name for display purposes
     */
    String name();

    /**
     * Brief description of the broker
     */
    String description() default "";

    /**
     * Supported order types
     */
    Type[] supportedOrderTypes() default {};

    /**
     * Supported order durations
     */
    Duration[] supportedDurations() default {};

    /**
     * Supported order modifiers
     */
    Modifier[] supportedModifiers() default {};

    /**
     * Supported instrument types
     */
    InstrumentType[] supportedInstruments() default {};

    /**
     * Whether the broker supports real-time market data
     */
    boolean supportsRealTimeData() default false;

    /**
     * Whether the broker supports historical data
     */
    boolean supportsHistoricalData() default false;

    /**
     * Whether the broker supports paper trading
     */
    boolean supportsPaperTrading() default false;

    /**
     * Whether the broker supports portfolio management
     */
    boolean supportsPortfolioManagement() default false;

    /**
     * Whether the broker supports order cancellation
     */
    boolean supportsOrderCancellation() default true;

    /**
     * Whether the broker supports order modification
     */
    boolean supportsOrderModification() default false;

    /**
     * Maximum order size (0 means no limit)
     */
    double maxOrderSize() default 0.0;

    /**
     * Minimum order size (0 means no minimum)
     */
    double minOrderSize() default 0.0;

    /**
     * Any specific limitations or notes
     */
    String[] limitations() default {};

    /**
     * Version of the broker API implementation
     */
    String version() default "1.0";
}