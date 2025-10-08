# Method-Level Capability Documentation Solution

## Problem Statement

Your question: _"What do I do about supporting `cancelOrder(ID)` but not `cancelOrders(IDs)` - where one cancel method is supported but not the other?"_

## Solution Overview

The enhanced capability system now supports **method-level granularity**, allowing you to document exactly which method variants are supported by each broker implementation.

## 🔧 Implementation

### 1. Method Capability Enum

```java
public enum BrokerMethodCapability {
    // Single vs Batch Cancellation
    CANCEL_ORDER_BY_ID("cancelOrder(String id)", "Cancel single order by ID"),
    CANCEL_ORDER_BY_TICKET("cancelOrder(OrderTicket order)", "Cancel order by ticket reference"),
    CANCEL_ORDERS_BY_IDS("cancelOrders(List<String> ids)", "Cancel multiple orders by IDs"),  // ← This is the key difference
    CANCEL_ORDERS_BY_TICKETS("cancelOrders(List<OrderTicket> orders)", "Cancel multiple orders by tickets"),

    // Other method variants...
}
```

### 2. Broker-Specific Implementation

```java
// Hyperliquid Broker Capabilities
public class HyperliquidBrokerCapabilities extends AbstractBrokerCapabilities {

    private HyperliquidBrokerCapabilities() {
        super(new Builder("Hyperliquid")
            .supportedMethods(
                // ✅ SUPPORTED: Single order cancellation
                BrokerMethodCapability.CANCEL_ORDER_BY_ID,
                BrokerMethodCapability.CANCEL_ORDER_BY_TICKET,
                BrokerMethodCapability.CANCEL_ALL_ORDERS,

                // ❌ NOT SUPPORTED: Batch cancellation methods
                // BrokerMethodCapability.CANCEL_ORDERS_BY_IDS,      ← Intentionally omitted
                // BrokerMethodCapability.CANCEL_ORDERS_BY_TICKETS  ← Intentionally omitted

                // ... other supported methods
            )
        );
    }

    @Override
    public String validateMethodCall(BrokerMethodCapability method) {
        String baseValidation = super.validateMethodCall(method);
        if (baseValidation != null) return baseValidation;

        // Specific error messages for unsupported batch operations
        switch (method) {
            case CANCEL_ORDERS_BY_IDS:
            case CANCEL_ORDERS_BY_TICKETS:
                return "Hyperliquid does not support batch order cancellation. Use individual cancelOrder() calls instead.";
            default:
                return null; // Method is supported
        }
    }
}
```

### 3. Runtime Validation & Usage

```java
public class SafeBrokerOperations {

    public static void safeCancelOrders(IBroker broker, List<String> orderIds) {
        HyperliquidBrokerCapabilities caps = HyperliquidBrokerCapabilities.getInstance();

        // Check if batch cancellation is supported
        if (caps.supportsMethod(BrokerMethodCapability.CANCEL_ORDERS_BY_IDS)) {
            // Use efficient batch method if available
            broker.cancelOrders(orderIds);
        } else {
            // Fall back to individual cancellations
            for (String orderId : orderIds) {
                if (caps.supportsMethod(BrokerMethodCapability.CANCEL_ORDER_BY_ID)) {
                    broker.cancelOrder(orderId);
                } else {
                    throw new UnsupportedOperationException(
                        "No order cancellation methods supported by " + caps.getBrokerName()
                    );
                }
            }
        }
    }

    public static void validateBeforeCall(BrokerMethodCapability method) {
        HyperliquidBrokerCapabilities caps = HyperliquidBrokerCapabilities.getInstance();

        String validation = caps.validateMethodCall(method);
        if (validation != null) {
            throw new IllegalArgumentException(validation);
        }
    }
}
```

## 📊 Real-World Example Output

```
=== Single Order Cancellation ===
✅ cancelOrder(String id) - SUPPORTED
   You can cancel individual orders by ID

=== Batch Order Cancellation ===
❌ cancelOrders(List<String> ids) - NOT SUPPORTED
   Reason: Hyperliquid does not support batch order cancellation. Use individual cancelOrder() calls instead.
   Workaround: Use individual cancelOrder() calls

=== Safe Batch Cancellation Implementation ===
Using individual cancellations:
  broker.cancelOrder("order1")
  broker.cancelOrder("order2")
  broker.cancelOrder("order3")
```

## 🎯 Benefits of This Approach

### 1. **Precise Documentation**

- Documents exactly which method signatures are supported
- No ambiguity about which variant of `cancelOrder()` works

### 2. **Runtime Safety**

```java
// Before calling a method, validate it's supported
String error = caps.validateMethodCall(BrokerMethodCapability.CANCEL_ORDERS_BY_IDS);
if (error != null) {
    // Handle gracefully or show specific error message
    logger.warn("Method not supported: " + error);
}
```

### 3. **Automatic Fallback Logic**

```java
// Smart wrapper that automatically chooses best available method
if (caps.supportsMethod(CANCEL_ORDERS_BY_IDS)) {
    // Use batch method - most efficient
} else if (caps.supportsMethod(CANCEL_ORDER_BY_ID)) {
    // Use individual method - still works
} else {
    // No cancellation support at all
}
```

### 4. **Clear Error Messages**

Instead of generic "method not supported", users get specific guidance:

- "Hyperliquid does not support batch order cancellation. Use individual cancelOrder() calls instead."
- "Interactive Brokers supports batch cancellation via cancelOrders(List<String> ids)"

### 5. **Future-Proof Design**

Easy to add new method variants:

```java
// Future: Support for conditional cancellation
CANCEL_ORDERS_CONDITIONALLY("cancelOrders(Predicate<OrderTicket> condition)")
```

## 🚀 Implementation Strategy

### For Each Broker:

1. **Audit existing methods** - determine which variants are actually implemented
2. **Update capability definitions** - list supported methods explicitly
3. **Add validation logic** - provide specific error messages for unsupported variants
4. **Create wrapper methods** - implement smart fallbacks in client code

### For Documentation:

1. **Generate method matrices** showing which brokers support which methods
2. **Provide usage examples** for each method category
3. **Document workarounds** for unsupported operations

This solution gives you **precise control** over documenting method-level support while providing **actionable guidance** to users about what works and what doesn't with each broker implementation.
