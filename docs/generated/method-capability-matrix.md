# Method Capability Matrix

This table shows which specific methods are supported by each broker implementation.

| Method | Description | Hyperliquid |
|--------|-------------|----------|
| `cancelOrder()` | Cancel single order by ID | ✅ |
| `cancelOrder()` | Cancel order by ticket reference | ✅ |
| `cancelAllOrders()` | Cancel all open orders | ✅ |
| `cancelAllOrders()` | Cancel all orders for specific ticker | ✅ |
| `placeOrder()` | Place single order | ✅ |
| `replaceOrder()` | Replace order (cancel + new) | ✅ |
| `getOpenOrders()` | Get all open orders | ✅ |
| `getOpenOrders()` | Get open orders for ticker | ✅ |
| `getOrderStatus()` | Get status of specific order | ✅ |
| `getFills()` | Get order fills | ✅ |
| `getFills()` | Get fills for specific ticker | ✅ |
| `getPositions()` | Get all positions | ✅ |
| `getPosition()` | Get position for specific ticker | ✅ |
| `closePosition()` | Close position for ticker | ✅ |
| `closeAllPositions()` | Close all positions | ✅ |
| `getAccountInfo()` | Get account information | ✅ |
| `getAccountBalance()` | Get account balance | ✅ |
| `subscribeMarketData()` | Subscribe to real-time market data | ✅ |
| `unsubscribeMarketData()` | Unsubscribe from market data | ✅ |
| `getMarketDataSnapshot()` | Get current market data snapshot | ✅ |
| `connect()` | Establish broker connection | ✅ |
| `disconnect()` | Close broker connection | ✅ |
| `isConnected()` | Check connection status | ✅ |
