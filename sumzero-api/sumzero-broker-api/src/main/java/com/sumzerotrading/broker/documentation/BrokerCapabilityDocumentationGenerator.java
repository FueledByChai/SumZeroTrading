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
package com.sumzerotrading.broker.documentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.sumzerotrading.broker.capabilities.BrokerCapabilities;
import com.sumzerotrading.broker.order.OrderTicket.Duration;
import com.sumzerotrading.broker.order.OrderTicket.Modifier;
import com.sumzerotrading.broker.order.OrderTicket.Type;
import com.sumzerotrading.data.InstrumentType;

/**
 * Utility class for generating comprehensive documentation of broker
 * capabilities. Generates markdown files, comparison tables, and capability
 * matrices.
 */
public class BrokerCapabilityDocumentationGenerator {

    /**
     * Generate comprehensive documentation for a single broker
     */
    public static String generateBrokerDocumentation(BrokerCapabilities capabilities) {
        StringBuilder doc = new StringBuilder();

        doc.append("# ").append(capabilities.getBrokerName()).append(" Broker\n\n");
        doc.append("**Version:** ").append(capabilities.getVersion()).append("\n\n");
        doc.append("**Description:** ").append(capabilities.getDescription()).append("\n\n");

        // Capabilities Overview
        doc.append("## 📋 Capabilities Overview\n\n");
        doc.append("| Feature | Supported |\n");
        doc.append("|---------|----------|\n");
        doc.append("| Real-time Market Data | ").append(checkmark(capabilities.supportsRealTimeData())).append(" |\n");
        doc.append("| Historical Data | ").append(checkmark(capabilities.supportsHistoricalData())).append(" |\n");
        doc.append("| Paper Trading | ").append(checkmark(capabilities.supportsPaperTrading())).append(" |\n");
        doc.append("| Portfolio Management | ").append(checkmark(capabilities.supportsPortfolioManagement()))
                .append(" |\n");
        doc.append("| Order Cancellation | ").append(checkmark(capabilities.supportsOrderCancellation()))
                .append(" |\n");
        doc.append("| Order Modification | ").append(checkmark(capabilities.supportsOrderModification()))
                .append(" |\n\n");

        // Order Types
        doc.append("## 📊 Supported Order Types\n\n");
        Set<Type> orderTypes = capabilities.getSupportedOrderTypes();
        if (!orderTypes.isEmpty()) {
            for (Type type : orderTypes) {
                doc.append("- **").append(type.name()).append("**: ").append(getOrderTypeDescription(type))
                        .append("\n");
            }
        } else {
            doc.append("*No order types documented*\n");
        }
        doc.append("\n");

        // Order Durations
        doc.append("## ⏱️ Supported Order Durations\n\n");
        Set<Duration> durations = capabilities.getSupportedDurations();
        if (!durations.isEmpty()) {
            for (Duration duration : durations) {
                doc.append("- **").append(duration.name()).append("**: ").append(getDurationDescription(duration))
                        .append("\n");
            }
        } else {
            doc.append("*No order durations documented*\n");
        }
        doc.append("\n");

        // Order Modifiers
        doc.append("## 🔧 Supported Order Modifiers\n\n");
        Set<Modifier> modifiers = capabilities.getSupportedModifiers();
        if (!modifiers.isEmpty()) {
            for (Modifier modifier : modifiers) {
                doc.append("- **").append(modifier.name()).append("**: ").append(getModifierDescription(modifier))
                        .append("\n");
            }
        } else {
            doc.append("*No order modifiers documented*\n");
        }
        doc.append("\n");

        // Instrument Types
        doc.append("## 🎯 Supported Instrument Types\n\n");
        Set<InstrumentType> instruments = capabilities.getSupportedInstrumentTypes();
        if (!instruments.isEmpty()) {
            for (InstrumentType instrument : instruments) {
                doc.append("- **").append(instrument.name()).append("**: ").append(getInstrumentDescription(instrument))
                        .append("\n");
            }
        } else {
            doc.append("*No instrument types documented*\n");
        }
        doc.append("\n");

        // Order Size Limits
        doc.append("## 💰 Order Size Limits\n\n");
        if (capabilities.getMinOrderSize() > 0) {
            doc.append("**Minimum Order Size:** ").append(capabilities.getMinOrderSize()).append("\n\n");
        }
        if (capabilities.getMaxOrderSize() > 0) {
            doc.append("**Maximum Order Size:** ").append(capabilities.getMaxOrderSize()).append("\n\n");
        }
        if (capabilities.getMinOrderSize() == 0 && capabilities.getMaxOrderSize() == 0) {
            doc.append("*No order size limits documented*\n\n");
        }

        // Limitations
        doc.append("## ⚠️ Limitations & Restrictions\n\n");
        Set<String> limitations = capabilities.getLimitations();
        if (!limitations.isEmpty()) {
            for (String limitation : limitations) {
                doc.append("- ").append(limitation).append("\n");
            }
        } else {
            doc.append("*No limitations documented*\n");
        }
        doc.append("\n");

        return doc.toString();
    }

    /**
     * Generate comparison table for multiple brokers
     */
    public static String generateBrokerComparison(List<BrokerCapabilities> brokers) {
        if (brokers.isEmpty()) {
            return "No brokers to compare.\n";
        }

        StringBuilder doc = new StringBuilder();
        doc.append("# Broker Capability Comparison\n\n");

        // Header
        doc.append("| Feature |");
        for (BrokerCapabilities broker : brokers) {
            doc.append(" ").append(broker.getBrokerName()).append(" |");
        }
        doc.append("\n|---------|");
        for (int i = 0; i < brokers.size(); i++) {
            doc.append("----------|");
        }
        doc.append("\n");

        // Capabilities rows
        addComparisonRow(doc, "Real-time Data", brokers, b -> b.supportsRealTimeData());
        addComparisonRow(doc, "Historical Data", brokers, b -> b.supportsHistoricalData());
        addComparisonRow(doc, "Paper Trading", brokers, b -> b.supportsPaperTrading());
        addComparisonRow(doc, "Portfolio Mgmt", brokers, b -> b.supportsPortfolioManagement());
        addComparisonRow(doc, "Order Cancel", brokers, b -> b.supportsOrderCancellation());
        addComparisonRow(doc, "Order Modify", brokers, b -> b.supportsOrderModification());

        // Order types comparison
        doc.append("\n## Order Types Comparison\n\n");
        doc.append("| Order Type |");
        for (BrokerCapabilities broker : brokers) {
            doc.append(" ").append(broker.getBrokerName()).append(" |");
        }
        doc.append("\n|------------|");
        for (int i = 0; i < brokers.size(); i++) {
            doc.append("----------|");
        }
        doc.append("\n");

        // Get all order types across all brokers
        Set<Type> allOrderTypes = java.util.EnumSet.noneOf(Type.class);
        for (BrokerCapabilities broker : brokers) {
            allOrderTypes.addAll(broker.getSupportedOrderTypes());
        }

        for (Type orderType : allOrderTypes) {
            doc.append("| ").append(orderType.name()).append(" |");
            for (BrokerCapabilities broker : brokers) {
                doc.append(" ").append(checkmark(broker.supportsOrderType(orderType))).append(" |");
            }
            doc.append("\n");
        }

        return doc.toString();
    }

    /**
     * Save documentation to file
     */
    public static void saveBrokerDocumentation(BrokerCapabilities capabilities, String outputDir) throws IOException {
        String content = generateBrokerDocumentation(capabilities);
        Path outputPath = Paths.get(outputDir, capabilities.getBrokerName().toLowerCase() + "-capabilities.md");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, content.getBytes());
    }

    // Helper methods
    private static String checkmark(boolean supported) {
        return supported ? "✅" : "❌";
    }

    private static void addComparisonRow(StringBuilder doc, String feature, List<BrokerCapabilities> brokers,
            java.util.function.Function<BrokerCapabilities, Boolean> checker) {
        doc.append("| ").append(feature).append(" |");
        for (BrokerCapabilities broker : brokers) {
            doc.append(" ").append(checkmark(checker.apply(broker))).append(" |");
        }
        doc.append("\n");
    }

    private static String getOrderTypeDescription(Type type) {
        switch (type) {
        case MARKET:
            return "Execute immediately at best available price";
        case LIMIT:
            return "Execute only at specified price or better";
        case STOP:
            return "Market order triggered when stop price is reached";
        case STOP_LIMIT:
            return "Limit order triggered when stop price is reached";
        case MARKET_ON_OPEN:
            return "Market order executed at market open";
        case MARKET_ON_CLOSE:
            return "Market order executed at market close";
        case TRAILING_STOP:
            return "Stop order that trails the market price";
        case TRAILING_STOP_LIMIT:
            return "Stop limit order that trails the market price";
        default:
            return "Standard order type";
        }
    }

    private static String getDurationDescription(Duration duration) {
        switch (duration) {
        case DAY:
            return "Order valid until end of trading day";
        case GOOD_UNTIL_CANCELED:
            return "Order remains active until canceled";
        case GOOD_UNTIL_TIME:
            return "Order valid until specified time";
        case FILL_OR_KILL:
            return "Execute completely immediately or cancel";
        case IMMEDIATE_OR_CANCEL:
            return "Execute as much as possible immediately, cancel remainder";
        default:
            return "Standard duration type";
        }
    }

    private static String getModifierDescription(Modifier modifier) {
        switch (modifier) {
        case ALL_OR_NONE:
            return "Execute entire order or none at all";
        case POST_ONLY:
            return "Order will only add liquidity (maker-only)";
        case REDUCE_ONLY:
            return "Order can only reduce existing position";
        default:
            return "Order modifier";
        }
    }

    private static String getInstrumentDescription(InstrumentType type) {
        switch (type) {
        case STOCK:
            return "Equity securities";
        case OPTION:
            return "Options contracts";
        case FUTURES:
            return "Futures contracts";
        case PERPETUAL_FUTURES:
            return "Perpetual futures contracts";
        case PERPETUAL_OPTIONS:
            return "Perpetual options contracts";
        case CURRENCY:
            return "Foreign exchange pairs";
        case CRYPTO_SPOT:
            return "Cryptocurrency spot trading";
        case CFD:
            return "Contracts for difference";
        case INDEX:
            return "Index instruments";
        case COMBO:
            return "Combination instruments";
        case SPOT_METALS:
            return "Spot metals trading";
        case NONE:
            return "No specific instrument type";
        default:
            return "Financial instrument";
        }
    }
}