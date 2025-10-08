/**
 * Simple documentation generator for testing purposes
 */
package com.sumzerotrading.examples.simple;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SimpleDocGenerator {

    public static void main(String[] args) {
        try {
            System.out.println("🚀 Simple Documentation Generator");

            String outputDir = args.length > 0 ? args[0] : "docs/generated";
            System.out.println("📁 Output directory: " + outputDir);

            // Create output directory
            Files.createDirectories(Paths.get(outputDir));

            // Create a simple README
            String content = generateSimpleReadme();
            Files.write(Paths.get(outputDir, "README.md"), content.getBytes());

            // Create broker capabilities summary
            String brokerSummary = generateBrokerSummary();
            Files.write(Paths.get(outputDir, "hyperliquid-capabilities.md"), brokerSummary.getBytes());

            System.out.println("✅ Documentation generated successfully!");
            System.out.println("📖 Check: " + outputDir + "/README.md");
            System.out.println("📖 Check: " + outputDir + "/hyperliquid-capabilities.md");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String generateSimpleReadme() {
        StringBuilder sb = new StringBuilder();
        sb.append("# SumZero Trading - Broker Capabilities\n\n");
        sb.append("Generated on: ").append(java.time.LocalDateTime.now()).append("\n\n");
        sb.append("## Available Brokers\n\n");
        sb.append("### Hyperliquid\n");
        sb.append("- **Type**: Decentralized Perpetual Futures Exchange\n");
        sb.append("- **Version**: 0.2.0\n");
        sb.append("- **Real-time Data**: ✅ Supported\n");
        sb.append("- **Paper Trading**: ✅ Supported (via testnet)\n");
        sb.append("- **Order Types**: MARKET, LIMIT, STOP, STOP_LIMIT\n");
        sb.append("- **Instruments**: PERPETUAL_FUTURES only\n\n");
        sb.append("## Method Support Matrix\n\n");
        sb.append("| Method | Hyperliquid |\n");
        sb.append("|--------|-------------|\n");
        sb.append("| `cancelOrder(String id)` | ✅ |\n");
        sb.append("| `cancelOrders(List<String> ids)` | ❌ |\n");
        sb.append("| `placeOrder(OrderTicket order)` | ✅ |\n");
        sb.append("| `modifyOrder(OrderTicket order)` | ❌ |\n");
        sb.append("| `getPositions()` | ✅ |\n");
        sb.append("| `getAccountInfo()` | ✅ |\n\n");
        sb.append("## Key Limitations\n\n");
        sb.append("- **Hyperliquid**: No batch operations, cancel/replace for modifications\n");
        sb.append("- **Future brokers**: Will be added as implemented\n\n");
        return sb.toString();
    }

    private static String generateBrokerSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Hyperliquid Broker Capabilities\n\n");
        sb.append("**Version:** 0.2.0  \n");
        sb.append("**Type:** Decentralized Perpetual Futures Exchange  \n");
        sb.append("**Description:** High-performance DEX with low latency trading (~3ms signing)\n\n");

        sb.append("## ✅ Supported Features\n\n");
        sb.append("### Order Types\n");
        sb.append("- **MARKET**: Converted to aggressive limit orders with slippage protection\n");
        sb.append("- **LIMIT**: Standard limit orders with maker/taker functionality\n");
        sb.append("- **STOP**: Stop market orders triggered at specified price\n");
        sb.append("- **STOP_LIMIT**: Stop limit orders for precise execution control\n\n");

        sb.append("### Order Durations\n");
        sb.append("- **GOOD_UNTIL_CANCELED**: Orders remain active until manually canceled\n");
        sb.append("- **IMMEDIATE_OR_CANCEL**: Execute partial fills immediately\n");
        sb.append("- **FILL_OR_KILL**: Execute complete order immediately or cancel\n\n");

        sb.append("### Order Modifiers\n");
        sb.append("- **POST_ONLY**: Maker-only execution (adds liquidity)\n");
        sb.append("- **REDUCE_ONLY**: Can only reduce existing position size\n\n");

        sb.append("### Supported Methods\n");
        sb.append("- ✅ `cancelOrder(String id)` - Cancel single order\n");
        sb.append("- ✅ `placeOrder(OrderTicket order)` - Place single order\n");
        sb.append("- ✅ `getPositions()` - Get all positions\n");
        sb.append("- ✅ `getAccountInfo()` - Get account information\n");
        sb.append("- ✅ `subscribeMarketData(Ticker)` - Real-time market data\n\n");

        sb.append("## ❌ Unsupported Features\n\n");
        sb.append("### Batch Operations\n");
        sb.append("- ❌ `cancelOrders(List<String> ids)` - Use individual calls\n");
        sb.append("- ❌ `placeOrders(List<OrderTicket>)` - Submit individually\n\n");

        sb.append("### Order Modifications\n");
        sb.append("- ❌ `modifyOrder()` - Use cancel/replace pattern\n");
        sb.append("- ❌ `modifyOrderPrice()` - Use cancel/replace pattern\n\n");

        sb.append("### Complex Orders\n");
        sb.append("- ❌ `placeBracketOrder()` - Use separate orders\n");
        sb.append("- ❌ `placeOCOOrder()` - Not supported\n\n");

        sb.append("## ⚠️ Important Limitations\n\n");
        sb.append("1. **5 Significant Digits**: Price precision limited to 5 significant digits\n");
        sb.append("2. **Perpetual Futures Only**: No spot, options, or traditional futures\n");
        sb.append("3. **Cancel/Replace Pattern**: No direct order modification\n");
        sb.append("4. **WebSocket Required**: Real-time connection needed for updates\n");
        sb.append("5. **EIP-712 Signatures**: All orders require cryptographic signing\n\n");

        sb.append("## 🚀 Performance\n\n");
        sb.append("- **Signing Latency**: ~3ms (optimized with template caching)\n");
        sb.append("- **Order Submission**: Near real-time via WebSocket\n");
        sb.append("- **Market Data**: Real-time streaming updates\n\n");

        return sb.toString();
    }
}