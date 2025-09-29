package com.sumzerotrading.hyperliquid;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.sumzerotrading.data.Ticker;

public class HyperliquidUtil {

    public static String encode128BitHex(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            sb.append("0x");
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 encoding failed", e);
        }
    }

    /**
     * Formats a price for Hyperliquid: up to 5 significant figures (unless
     * integer), no more than (maxDecimals - szDecimals) decimal places,
     * integer-only for prices >= 100,000.
     */
    public static String formatPriceAsString(Ticker ticker, BigDecimal price) {
        int maxDecimals = 6; // Hyperliquid max decimals is 6 for perps, 8 for spot
        BigDecimal priceTickSize = ticker.getMinimumTickSize();
        // int priceDecimals = priceTickSize.stripTrailingZeros().scale(); // unused
        int szDecimals = priceTickSize.scale(); // szDecimals is the scale of tick size
        int allowedDecimals = maxDecimals - szDecimals; // Update allowedDecimals calculation
        // BigDecimal absPrice = price.abs(); // unused

        // Always allow integer prices
        if (price.stripTrailingZeros().scale() <= 0) {
            return price.setScale(0, RoundingMode.DOWN).toPlainString();
        }

        // Truncate to allowed decimals first
        BigDecimal rounded = price.setScale(allowedDecimals, RoundingMode.DOWN);
        String plain = rounded.stripTrailingZeros().toPlainString();

        // Count significant figures
        int sigFigs = countSignificantFigures(plain);
        if (sigFigs > 5) {
            int intDigits = plain.contains(".") ? plain.indexOf(".") : plain.length();
            int decimalsToKeep = Math.max(0, 5 - intDigits);
            // Only further truncate if decimalsToKeep < allowedDecimals
            if (decimalsToKeep < allowedDecimals) {
                rounded = price.setScale(decimalsToKeep, RoundingMode.DOWN);
                plain = rounded.stripTrailingZeros().toPlainString();
            }
        }
        return plain;
    }

    private static int countSignificantFigures(String s) {
        // Remove leading zeros, decimal point, and trailing zeros after decimal
        String digits = s.replaceFirst("^-?0*", "");
        if (digits.contains(".")) {
            digits = digits.replaceFirst("\\.", "");
            digits = digits.replaceFirst("0+$", "");
        }
        // Remove negative sign if present
        digits = digits.replace("-", "");
        return digits.length();
    }
}
