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

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.broker.order.OrderTicket.Duration;
import com.sumzerotrading.broker.order.OrderTicket.Modifier;
import com.sumzerotrading.broker.order.OrderTicket.Type;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;

/**
 * Abstract base class for broker capabilities that provides default
 * implementations and utility methods for capability checking.
 */
public abstract class AbstractBrokerCapabilities implements BrokerCapabilities {

    protected final String brokerName;
    protected final String description;
    protected final Set<Type> supportedOrderTypes;
    protected final Set<Duration> supportedDurations;
    protected final Set<Modifier> supportedModifiers;
    protected final Set<InstrumentType> supportedInstruments;
    protected final Set<String> limitations;
    protected final String version;
    protected final Set<BrokerMethodCapability> supportedMethods;

    protected final boolean supportsRealTimeData;
    protected final boolean supportsHistoricalData;
    protected final boolean supportsPaperTrading;
    protected final boolean supportsPortfolioManagement;
    protected final boolean supportsOrderCancellation;
    protected final boolean supportsOrderModification;

    protected final double maxOrderSize;
    protected final double minOrderSize;

    protected AbstractBrokerCapabilities(Builder builder) {
        this.brokerName = builder.brokerName;
        this.description = builder.description;
        this.supportedOrderTypes = EnumSet.copyOf(builder.supportedOrderTypes);
        this.supportedDurations = EnumSet.copyOf(builder.supportedDurations);
        this.supportedModifiers = EnumSet.copyOf(builder.supportedModifiers);
        this.supportedInstruments = EnumSet.copyOf(builder.supportedInstruments);
        this.limitations = new HashSet<>(builder.limitations);
        this.version = builder.version;
        this.supportedMethods = EnumSet.copyOf(builder.supportedMethods);

        this.supportsRealTimeData = builder.supportsRealTimeData;
        this.supportsHistoricalData = builder.supportsHistoricalData;
        this.supportsPaperTrading = builder.supportsPaperTrading;
        this.supportsPortfolioManagement = builder.supportsPortfolioManagement;
        this.supportsOrderCancellation = builder.supportsOrderCancellation;
        this.supportsOrderModification = builder.supportsOrderModification;

        this.maxOrderSize = builder.maxOrderSize;
        this.minOrderSize = builder.minOrderSize;
    }

    @Override
    public String getBrokerName() {
        return brokerName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean supportsOrderType(Type orderType) {
        return supportedOrderTypes.contains(orderType);
    }

    @Override
    public Set<Type> getSupportedOrderTypes() {
        return EnumSet.copyOf(supportedOrderTypes);
    }

    @Override
    public boolean supportsDuration(Duration duration) {
        return supportedDurations.contains(duration);
    }

    @Override
    public Set<Duration> getSupportedDurations() {
        return EnumSet.copyOf(supportedDurations);
    }

    @Override
    public boolean supportsModifier(Modifier modifier) {
        return supportedModifiers.contains(modifier);
    }

    @Override
    public Set<Modifier> getSupportedModifiers() {
        return EnumSet.copyOf(supportedModifiers);
    }

    @Override
    public boolean supportsInstrumentType(InstrumentType instrumentType) {
        return supportedInstruments.contains(instrumentType);
    }

    @Override
    public Set<InstrumentType> getSupportedInstrumentTypes() {
        return EnumSet.copyOf(supportedInstruments);
    }

    @Override
    public boolean supportsTicker(Ticker ticker) {
        return supportsInstrumentType(ticker.getInstrumentType());
    }

    @Override
    public boolean supportsRealTimeData() {
        return supportsRealTimeData;
    }

    @Override
    public boolean supportsHistoricalData() {
        return supportsHistoricalData;
    }

    @Override
    public boolean supportsPaperTrading() {
        return supportsPaperTrading;
    }

    @Override
    public boolean supportsPortfolioManagement() {
        return supportsPortfolioManagement;
    }

    @Override
    public boolean supportsOrderCancellation() {
        return supportsOrderCancellation;
    }

    @Override
    public boolean supportsOrderModification() {
        return supportsOrderModification;
    }

    @Override
    public double getMaxOrderSize() {
        return maxOrderSize;
    }

    @Override
    public double getMinOrderSize() {
        return minOrderSize;
    }

    @Override
    public double getMinOrderSize(Ticker ticker) {
        // Default implementation - subclasses can override for ticker-specific logic
        return getMinOrderSize();
    }

    @Override
    public Set<String> getLimitations() {
        return new HashSet<>(limitations);
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String validateOrderTicket(OrderTicket order) {
        // Basic validation - subclasses can extend this
        if (!supportsOrderType(order.getType())) {
            return "Order type " + order.getType() + " is not supported by " + brokerName;
        }

        if (order.getDuration() != null && !supportsDuration(order.getDuration())) {
            return "Order duration " + order.getDuration() + " is not supported by " + brokerName;
        }

        if (order.getModifiers() != null) {
            for (Modifier modifier : order.getModifiers()) {
                if (!supportsModifier(modifier)) {
                    return "Order modifier " + modifier + " is not supported by " + brokerName;
                }
            }
        }

        if (!supportsInstrumentType(order.getTicker().getInstrumentType())) {
            return "Instrument type " + order.getTicker().getInstrumentType() + " is not supported by " + brokerName;
        }

        double orderSize = order.getSize().doubleValue();
        if (maxOrderSize > 0 && orderSize > maxOrderSize) {
            return "Order size " + orderSize + " exceeds maximum allowed size of " + maxOrderSize;
        }

        if (minOrderSize > 0 && orderSize < minOrderSize) {
            return "Order size " + orderSize + " is below minimum required size of " + minOrderSize;
        }

        return null; // Valid
    }

    @Override
    public boolean supportsMethod(BrokerMethodCapability method) {
        return supportedMethods.contains(method);
    }

    @Override
    public Set<BrokerMethodCapability> getSupportedMethods() {
        return EnumSet.copyOf(supportedMethods);
    }

    @Override
    public Set<BrokerMethodCapability> getSupportedMethodsByCategory(String category) {
        return supportedMethods.stream().filter(method -> {
            switch (category.toLowerCase()) {
            case "cancellation":
                return method.isOrderCancellation();
            case "placement":
                return method.isOrderPlacement();
            case "modification":
                return method.isOrderModification();
            case "marketdata":
                return method.isMarketData();
            default:
                return false;
            }
        }).collect(java.util.stream.Collectors.toSet());
    }

    @Override
    public String validateMethodCall(BrokerMethodCapability method) {
        if (!supportsMethod(method)) {
            return "Method " + method.getMethodSignature() + " is not supported by " + brokerName;
        }
        return null; // Valid
    }

    /**
     * Builder pattern for creating broker capabilities
     */
    public static class Builder {
        private String brokerName;
        private String description = "";
        private Set<Type> supportedOrderTypes = EnumSet.noneOf(Type.class);
        private Set<Duration> supportedDurations = EnumSet.noneOf(Duration.class);
        private Set<Modifier> supportedModifiers = EnumSet.noneOf(Modifier.class);
        private Set<InstrumentType> supportedInstruments = EnumSet.noneOf(InstrumentType.class);
        private Set<String> limitations = new HashSet<>();
        private String version = "1.0";
        private Set<BrokerMethodCapability> supportedMethods = EnumSet.noneOf(BrokerMethodCapability.class);

        private boolean supportsRealTimeData = false;
        private boolean supportsHistoricalData = false;
        private boolean supportsPaperTrading = false;
        private boolean supportsPortfolioManagement = false;
        private boolean supportsOrderCancellation = true;
        private boolean supportsOrderModification = false;

        private double maxOrderSize = 0.0;
        private double minOrderSize = 0.0;

        public Builder(String brokerName) {
            this.brokerName = brokerName;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder supportedOrderTypes(Type... types) {
            for (Type type : types) {
                this.supportedOrderTypes.add(type);
            }
            return this;
        }

        public Builder supportedDurations(Duration... durations) {
            for (Duration duration : durations) {
                this.supportedDurations.add(duration);
            }
            return this;
        }

        public Builder supportedModifiers(Modifier... modifiers) {
            for (Modifier modifier : modifiers) {
                this.supportedModifiers.add(modifier);
            }
            return this;
        }

        public Builder supportedInstruments(InstrumentType... instruments) {
            for (InstrumentType instrument : instruments) {
                this.supportedInstruments.add(instrument);
            }
            return this;
        }

        public Builder limitations(String... limitations) {
            for (String limitation : limitations) {
                this.limitations.add(limitation);
            }
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder supportsRealTimeData(boolean supports) {
            this.supportsRealTimeData = supports;
            return this;
        }

        public Builder supportsHistoricalData(boolean supports) {
            this.supportsHistoricalData = supports;
            return this;
        }

        public Builder supportsPaperTrading(boolean supports) {
            this.supportsPaperTrading = supports;
            return this;
        }

        public Builder supportsPortfolioManagement(boolean supports) {
            this.supportsPortfolioManagement = supports;
            return this;
        }

        public Builder supportsOrderCancellation(boolean supports) {
            this.supportsOrderCancellation = supports;
            return this;
        }

        public Builder supportsOrderModification(boolean supports) {
            this.supportsOrderModification = supports;
            return this;
        }

        public Builder maxOrderSize(double maxSize) {
            this.maxOrderSize = maxSize;
            return this;
        }

        public Builder minOrderSize(double minSize) {
            this.minOrderSize = minSize;
            return this;
        }

        public Builder supportedMethods(BrokerMethodCapability... methods) {
            for (BrokerMethodCapability method : methods) {
                this.supportedMethods.add(method);
            }
            return this;
        }

        public Builder addMethod(BrokerMethodCapability method) {
            this.supportedMethods.add(method);
            return this;
        }

        public Builder removeMethod(BrokerMethodCapability method) {
            this.supportedMethods.remove(method);
            return this;
        }
    }
}