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
package com.sumzerotrading.broker.hyperliquid.examples;

import java.util.Arrays;
import java.util.List;
import com.sumzerotrading.broker.capabilities.BrokerMethodCapability;
import com.sumzerotrading.broker.hyperliquid.capabilities.HyperliquidBrokerCapabilities;

/**
 * Example demonstrating method-level capability checking for Hyperliquid
 * broker. This shows how to handle the case where cancelOrder(ID) is supported
 * but cancelOrders(IDs) is not.
 */
public class MethodCapabilityExample {

    public static void main(String[] args) {
        HyperliquidBrokerCapabilities caps = HyperliquidBrokerCapabilities.getInstance();

        // Example: Check if single order cancellation is supported
        System.out.println("=== Single Order Cancellation ===");
        if (caps.supportsMethod(BrokerMethodCapability.CANCEL_ORDER_BY_ID)) {
            System.out.println("✅ cancelOrder(String id) - SUPPORTED");
            System.out.println("   You can cancel individual orders by ID");
        } else {
            System.out.println("❌ cancelOrder(String id) - NOT SUPPORTED");
        }

        // Example: Check if batch order cancellation is supported
        System.out.println("\n=== Batch Order Cancellation ===");
        if (caps.supportsMethod(BrokerMethodCapability.CANCEL_ORDERS_BY_IDS)) {
            System.out.println("✅ cancelOrders(List&lt;String&gt; ids) - SUPPORTED");
        } else {
            System.out.println("❌ cancelOrders(List<String> ids) - NOT SUPPORTED");

            // Get specific error message
            String error = caps.validateMethodCall(BrokerMethodCapability.CANCEL_ORDERS_BY_IDS);
            System.out.println("   Reason: " + error);

            // Show workaround
            System.out.println("   Workaround: Use individual cancelOrder() calls");
        }

        // Example: Demonstrate safe batch cancellation for Hyperliquid
        System.out.println("\n=== Safe Batch Cancellation Implementation ===");
        List<String> orderIds = Arrays.asList("order1", "order2", "order3");

        if (caps.supportsMethod(BrokerMethodCapability.CANCEL_ORDERS_BY_IDS)) {
            // If broker supports batch cancellation, use it
            System.out.println("Using batch cancellation: broker.cancelOrders(orderIds)");
        } else {
            // Fall back to individual cancellations
            System.out.println("Using individual cancellations:");
            for (String orderId : orderIds) {
                if (caps.supportsMethod(BrokerMethodCapability.CANCEL_ORDER_BY_ID)) {
                    System.out.println("  broker.cancelOrder(\"" + orderId + "\")");
                } else {
                    System.out.println(
                            "  ERROR: Cannot cancel order " + orderId + " - no cancellation method supported!");
                }
            }
        }

        // Example: Check order modification capabilities
        System.out.println("\n=== Order Modification Capabilities ===");
        checkMethodSupport(caps, BrokerMethodCapability.MODIFY_ORDER);
        checkMethodSupport(caps, BrokerMethodCapability.MODIFY_ORDER_PRICE);
        checkMethodSupport(caps, BrokerMethodCapability.REPLACE_ORDER);

        // Example: Check order placement capabilities
        System.out.println("\n=== Order Placement Capabilities ===");
        checkMethodSupport(caps, BrokerMethodCapability.PLACE_ORDER);
        checkMethodSupport(caps, BrokerMethodCapability.PLACE_ORDERS_BATCH);
        checkMethodSupport(caps, BrokerMethodCapability.PLACE_BRACKET_ORDER);

        // Example: Get methods by category
        System.out.println("\n=== Methods by Category ===");
        System.out.println(
                "Cancellation methods: " + caps.getSupportedMethodsByCategory("cancellation").size() + " supported");
        System.out
                .println("Placement methods: " + caps.getSupportedMethodsByCategory("placement").size() + " supported");
        System.out.println(
                "Modification methods: " + caps.getSupportedMethodsByCategory("modification").size() + " supported");
    }

    private static void checkMethodSupport(HyperliquidBrokerCapabilities caps, BrokerMethodCapability method) {
        String status = caps.supportsMethod(method) ? "✅ SUPPORTED" : "❌ NOT SUPPORTED";
        System.out.println(method.getMethodSignature() + " - " + status);

        if (!caps.supportsMethod(method)) {
            String error = caps.validateMethodCall(method);
            if (error != null) {
                System.out.println("   Reason: " + error);
            }
        }
    }
}