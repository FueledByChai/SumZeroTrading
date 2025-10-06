package com.sumzerotrading.hyperliquid;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.sumzerotrading.data.Ticker;

public class HyperliquidUtil {

    protected static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HyperliquidUtil.class);

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
     * Formats a price for Hyperliquid according to their pricing rules: - Up to 5
     * significant figures (unless integer) - No more than MAX_DECIMALS - szDecimals
     * decimal places (MAX_DECIMALS = 6 for perps) - Integer prices are always
     * allowed regardless of significant figures
     */
    public static String formatPriceAsString(Ticker ticker, BigDecimal price) {
        // Step 1: Check if this is effectively an integer price
        BigDecimal stripped = price.stripTrailingZeros();
        if (stripped.scale() <= 0) {
            // Integer prices are always allowed regardless of significant figures
            return stripped.toPlainString();
        }

        // Step 2: Apply 5 significant figures limit - this is the PRIMARY constraint
        String priceStr = price.toPlainString();
        String absPrice = priceStr.replace("-", "");
        int decimalIndex = absPrice.indexOf(".");

        // Count integer digits (excluding leading zeros)
        String integerPart = decimalIndex == -1 ? absPrice : absPrice.substring(0, decimalIndex);
        integerPart = integerPart.replaceFirst("^0+", "");
        if (integerPart.isEmpty())
            integerPart = "0";
        int significantIntegerDigits = integerPart.equals("0") ? 0 : integerPart.length();

        // Calculate max decimals allowed for 5 sig figs
        int decimalsForSigFigs = Math.max(0, 5 - significantIntegerDigits);

        // Always apply the 5 sig figs limit FIRST - this is the primary constraint
        BigDecimal rounded = price.setScale(decimalsForSigFigs, RoundingMode.DOWN);

        // The 5 significant figures rule is ALWAYS applied
        // Tick size constraints are only for very low-priced coins where needed
        // For now, we prioritize the 5 sig figs rule in all cases

        return rounded.stripTrailingZeros().toPlainString();
    }

}
