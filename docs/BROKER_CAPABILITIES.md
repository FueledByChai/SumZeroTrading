# SumZero Trading Library - Broker Capabilities Documentation

This document provides a comprehensive overview of supported features, order types, and limitations for each broker implementation in the SumZero Trading Library.

## 📊 Broker Comparison Matrix

| Broker              | Real-time Data | Historical Data | Paper Trading | Order Types                     | Instruments       | Version |
| ------------------- | -------------- | --------------- | ------------- | ------------------------------- | ----------------- | ------- |
| Hyperliquid         | ✅             | ❌              | ✅            | MARKET, LIMIT, STOP, STOP_LIMIT | PERPETUAL_FUTURES | 0.2.0   |
| Interactive Brokers | ✅             | ✅              | ✅            | ALL                             | ALL               | 1.0.0   |
| Paper Broker        | ✅             | ✅              | ✅            | ALL                             | ALL               | 1.0.0   |
| Paradex             | ✅             | ❌              | ✅            | LIMIT, MARKET                   | PERPETUAL_FUTURES | 0.1.0   |
| dYdX                | ✅             | ❌              | ✅            | LIMIT, MARKET, STOP             | PERPETUAL_FUTURES | 0.1.0   |

---

## 🏦 Detailed Broker Capabilities

### Hyperliquid Broker

**Version:** 0.2.0  
**Type:** Decentralized Perpetual Futures Exchange  
**Description:** High-performance perpetual futures DEX with low latency trading

#### 📋 Capabilities Overview

- ✅ Real-time Market Data via WebSocket
- ❌ Historical Data (not yet implemented)
- ✅ Paper Trading (via testnet)
- ✅ Portfolio Management
- ✅ Order Cancellation
- ❌ Order Modification (requires cancel/replace)

#### 📊 Supported Order Types

- **MARKET**: Converted to aggressive limit orders with 5% slippage protection
- **LIMIT**: Standard limit orders with maker/taker functionality
- **STOP**: Stop market orders triggered at specified price
- **STOP_LIMIT**: Stop limit orders for precise execution control

#### ⏱️ Supported Order Durations

- **GOOD_UNTIL_CANCELED**: Orders remain active until manually canceled
- **IMMEDIATE_OR_CANCEL**: Execute partial fills immediately, cancel remainder
- **FILL_OR_KILL**: Execute complete order immediately or cancel entirely

#### 🔧 Supported Order Modifiers

- **POST_ONLY**: Orders will only add liquidity (maker-only execution)
- **REDUCE_ONLY**: Orders can only reduce existing position size

#### 🎯 Supported Instrument Types

- **PERPETUAL_FUTURES**: Perpetual futures contracts (BTC, ETH, SOL, etc.)

#### 💰 Order Size Limits

- **Minimum Order Size**:
  - BTC: 0.00001
  - ETH: 0.0001
  - SOL: 0.001
  - Other assets: 0.001
- **Maximum Order Size**: No hard limit (subject to available liquidity)

#### ⚠️ Limitations & Restrictions

- Market orders are converted to aggressive limit orders with 5% slippage protection
- Only perpetual futures contracts are supported
- Order modification requires cancel and replace operations
- Maximum 5 significant digits for price formatting
- Testnet and mainnet require different configurations
- WebSocket connection required for real-time updates
- EIP-712 signatures required for all order operations
- ~3ms signing latency for order submission

---

### Interactive Brokers

**Version:** 1.0.0  
**Type:** Traditional Brokerage  
**Description:** Full-service broker with comprehensive market access

#### 📋 Capabilities Overview

- ✅ Real-time Market Data
- ✅ Historical Data
- ✅ Paper Trading
- ✅ Portfolio Management
- ✅ Order Cancellation
- ✅ Order Modification

#### 📊 Supported Order Types

- **MARKET**: Immediate execution at best available price
- **LIMIT**: Execute at specified price or better
- **STOP**: Stop-loss orders
- **STOP_LIMIT**: Stop orders with price limits
- **MARKET_ON_OPEN**: Execute at market open
- **MARKET_ON_CLOSE**: Execute at market close
- **TRAILING_STOP**: Dynamic stop orders

#### 🎯 Supported Instrument Types

- **STOCK**: Equity securities
- **OPTION**: Options contracts
- **FUTURES**: Futures contracts
- **CURRENCY**: Forex pairs
- **INDEX**: Index products
- **CFD**: Contracts for difference

#### ⚠️ Limitations & Restrictions

- Requires IB account and connection
- Market data subscriptions required for real-time data
- Complex commission structure
- Geographic restrictions may apply

---

### Paper Broker

**Version:** 1.0.0  
**Type:** Simulation Broker  
**Description:** Paper trading implementation for testing strategies

#### 📋 Capabilities Overview

- ✅ Real-time Market Data (simulated)
- ✅ Historical Data (simulated)
- ✅ Paper Trading (primary purpose)
- ✅ Portfolio Management
- ✅ Order Cancellation
- ✅ Order Modification

#### 📊 Supported Order Types

- All order types supported for simulation

#### 🎯 Supported Instrument Types

- All instrument types supported for simulation

#### ⚠️ Limitations & Restrictions

- No actual trading - simulation only
- Fill simulation may not reflect real market conditions
- No real money profit/loss

---

## 🔧 Usage Examples

### Checking Broker Capabilities Programmatically

```java
// Get broker capabilities
HyperliquidBrokerCapabilities caps = HyperliquidBrokerCapabilities.getInstance();

// Check if order type is supported
if (caps.supportsOrderType(OrderTicket.Type.LIMIT)) {
    System.out.println("Limit orders are supported");
}

// Validate an order before submission
OrderTicket order = new OrderTicket();
order.setType(OrderTicket.Type.MARKET);
order.setTicker(btcTicker);
order.setSize(new BigDecimal("0.01"));

String validation = caps.validateOrderTicket(order);
if (validation == null) {
    broker.placeOrder(order);
} else {
    System.err.println("Order validation failed: " + validation);
}
```

### Generating Documentation

```java
// Generate documentation for a single broker
HyperliquidBrokerCapabilities caps = HyperliquidBrokerCapabilities.getInstance();
String documentation = BrokerCapabilityDocumentationGenerator.generateBrokerDocumentation(caps);

// Save to file
BrokerCapabilityDocumentationGenerator.saveBrokerDocumentation(caps, "docs/brokers/");

// Generate comparison across multiple brokers
List<BrokerCapabilities> brokers = Arrays.asList(
    HyperliquidBrokerCapabilities.getInstance(),
    InteractiveBrokersBrokerCapabilities.getInstance(),
    PaperBrokerCapabilities.getInstance()
);
String comparison = BrokerCapabilityDocumentationGenerator.generateBrokerComparison(brokers);
```

---

## 📝 Implementation Guidelines

### Adding New Broker Capabilities

1. **Extend AbstractBrokerCapabilities**:

   ```java
   public class MyBrokerCapabilities extends AbstractBrokerCapabilities {
       private static final MyBrokerCapabilities INSTANCE = new MyBrokerCapabilities();

       private MyBrokerCapabilities() {
           super(new Builder("MyBroker")
               .description("My broker description")
               .supportedOrderTypes(Type.MARKET, Type.LIMIT)
               .supportedInstruments(InstrumentType.STOCK)
               // ... configure capabilities
           );
       }
   }
   ```

2. **Implement Custom Validation**:

   ```java
   @Override
   public String validateOrderTicket(OrderTicket order) {
       String baseValidation = super.validateOrderTicket(order);
       if (baseValidation != null) return baseValidation;

       // Custom broker-specific validations
       if (/* custom condition */) {
           return "Custom validation error message";
       }

       return null; // Valid
   }
   ```

3. **Update Documentation**: Use the documentation generator to create comprehensive capability documentation

### Best Practices

- **Singleton Pattern**: Use singleton instances for capability classes
- **Immutable Configuration**: Make capability configurations immutable
- **Validation First**: Always validate orders before submission
- **Clear Error Messages**: Provide specific, actionable error messages
- **Version Tracking**: Include version information for capability changes
- **Comprehensive Testing**: Test all capability validation logic

---

## 🚀 Future Enhancements

- [ ] Dynamic capability discovery from broker APIs
- [ ] Capability change notifications
- [ ] Performance metrics integration
- [ ] Multi-language documentation generation
- [ ] REST API for capability queries
- [ ] Integration with strategy validation systems
