# Hyperliquid Broker

**Version:** 0.2.0

**Description:** High-performance perpetual futures DEX with low latency trading

## 📋 Capabilities Overview

| Feature | Supported |
|---------|----------|
| Real-time Market Data | ✅ |
| Historical Data | ❌ |
| Paper Trading | ✅ |
| Portfolio Management | ✅ |
| Order Cancellation | ✅ |
| Order Modification | ❌ |

## 📊 Supported Order Types

- **MARKET**: Execute immediately at best available price
- **STOP**: Market order triggered when stop price is reached
- **LIMIT**: Execute only at specified price or better
- **STOP_LIMIT**: Limit order triggered when stop price is reached

## ⏱️ Supported Order Durations

- **GOOD_UNTIL_CANCELED**: Order remains active until canceled
- **FILL_OR_KILL**: Execute completely immediately or cancel
- **IMMEDIATE_OR_CANCEL**: Execute as much as possible immediately, cancel remainder

## 🔧 Supported Order Modifiers

- **POST_ONLY**: Order will only add liquidity (maker-only)
- **REDUCE_ONLY**: Order can only reduce existing position

## 🎯 Supported Instrument Types

- **PERPETUAL_FUTURES**: Perpetual futures contracts

## 💰 Order Size Limits

**Minimum Order Size:** 1.0E-5

## ⚠️ Limitations & Restrictions

- Market orders are converted to aggressive limit orders with 5% slippage protection
- Only perpetual futures contracts are supported
- Order modification requires cancel and replace
- EIP-712 signatures required for all order operations
- WebSocket connection required for real-time updates
- Testnet and mainnet require different configurations
- Maximum 5 significant digits for price formatting

