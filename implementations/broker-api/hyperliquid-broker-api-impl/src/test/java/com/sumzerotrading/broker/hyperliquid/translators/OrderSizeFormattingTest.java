/**
 * Test to demonstrate order size formatting behavior
 */
package com.sumzerotrading.broker.hyperliquid.translators;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OrderSizeFormattingTest {

    public static void main(String[] args) {
        System.out.println("=== Order Size Formatting Test ===");

        // Test cases for ZORA token (whole numbers only)
        BigDecimal zoraIncrement1 = new BigDecimal("1"); // scale = 0
        BigDecimal zoraIncrement2 = new BigDecimal("1.0"); // scale = 1 (problematic!)

        // Test cases for other tokens
        BigDecimal btcIncrement = new BigDecimal("0.00001"); // scale = 5
        BigDecimal ethIncrement = new BigDecimal("0.0001"); // scale = 4

        System.out.println("\n1. ZORA Token Tests (should be whole numbers only):");
        testOrderSizeFormatting("ZORA with increment=1", new BigDecimal("500.0"), zoraIncrement1);
        testOrderSizeFormatting("ZORA with increment=1.0", new BigDecimal("500.0"), zoraIncrement2);
        testOrderSizeFormatting("ZORA fractional", new BigDecimal("500.123"), zoraIncrement1);

        System.out.println("\n2. BTC Token Tests (5 decimal places):");
        testOrderSizeFormatting("BTC normal", new BigDecimal("0.123456789"), btcIncrement);
        testOrderSizeFormatting("BTC whole", new BigDecimal("1.0"), btcIncrement);

        System.out.println("\n3. ETH Token Tests (4 decimal places):");
        testOrderSizeFormatting("ETH normal", new BigDecimal("1.23456789"), ethIncrement);
        testOrderSizeFormatting("ETH whole", new BigDecimal("5.0"), ethIncrement);
    }

    private static void testOrderSizeFormatting(String testName, BigDecimal orderSize, BigDecimal increment) {
        System.out.printf("%-25s | Input: %-12s | Increment: %-8s | ", testName, orderSize.toPlainString(),
                increment.toPlainString());

        // Old method (problematic)
        String oldResult = orderSize.setScale(increment.scale(), RoundingMode.DOWN).toPlainString();

        // New method (correct)
        String newResult = formatOrderSizeCorrectly(orderSize, increment);

        System.out.printf("Old: %-8s | New: %-8s", oldResult, newResult);

        if (!oldResult.equals(newResult)) {
            System.out.print(" ⚠️  DIFFERENT!");
        }
        System.out.println();
    }

    /**
     * Improved formatting method that handles trailing zeros correctly
     */
    private static String formatOrderSizeCorrectly(BigDecimal orderSize, BigDecimal orderSizeIncrement) {
        if (orderSizeIncrement == null) {
            return orderSize.toPlainString();
        }

        // Determine the correct scale from the increment
        // Use stripTrailingZeros to get the actual precision needed
        BigDecimal strippedIncrement = orderSizeIncrement.stripTrailingZeros();
        int targetScale = Math.max(0, strippedIncrement.scale());

        // Round the order size to the correct scale
        BigDecimal roundedSize = orderSize.setScale(targetScale, RoundingMode.DOWN);

        // Strip trailing zeros to avoid "500.0" -> return "500" for integer values
        return roundedSize.stripTrailingZeros().toPlainString();
    }
}