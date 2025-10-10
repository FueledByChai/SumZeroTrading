# SumZero Trading - Broker Capabilities Documentation

This directory contains comprehensive documentation of broker capabilities, supported features, and API method support for all broker implementations in the SumZero Trading library.

## 📋 Available Documentation

### Overview Documents
- **[Broker Comparison](broker-comparison.md)** - Side-by-side feature comparison
- **[Method Capability Matrix](method-capability-matrix.md)** - Which methods each broker supports

### Individual Broker Documentation
- **[Hyperliquid](hyperliquid-capabilities.md)** - High-performance perpetual futures DEX with low latency trading

## 🚀 Quick Start

```java
// Check if a broker supports a specific method
HyperliquidBrokerCapabilities caps = HyperliquidBrokerCapabilities.getInstance();
if (caps.supportsMethod(BrokerMethodCapability.CANCEL_ORDERS_BY_IDS)) {
    broker.cancelOrders(orderIds);
} else {
    // Use individual cancellation fallback
    for (String id : orderIds) {
        broker.cancelOrder(id);
    }
}
```

## 📊 Summary Statistics

- **Total Brokers**: 1
- **Total Supported Methods**: 23
- **Average Methods per Broker**: 23.0

## 🔄 Regenerating Documentation

To regenerate this documentation:

```bash
# Using Maven
mvn exec:java -pl examples/CryptoExamples \
  -Dexec.mainClass="com.sumzerotrading.documentation.GenerateBrokerDocumentation" \
  -Dexec.args="docs/generated"

# Or compile and run directly
mvn compile
java -cp "target/classes:target/dependency/*" \
  com.sumzerotrading.documentation.GenerateBrokerDocumentation docs/generated
```

---
*Generated on: 2025-10-10T06:39:29.569469*
