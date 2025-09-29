package com.sumzerotrading.hyperliquid;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import com.sumzerotrading.data.Ticker;

class HyperliquidUtilTest {
    private Ticker makeTickerWithSzDecimals(int szDecimals) {
        Ticker t = new Ticker("TEST");
        // tick size = 1 / 10^szDecimals
        t.setMinimumTickSize(BigDecimal.ONE.divide(BigDecimal.TEN.pow(szDecimals)));
        return t;
    }

    @Test
    void testFormatPriceAsString_variousSzDecimals() {
        // szDecimals = 0 (priceDecimals = 6)
        Ticker t0 = makeTickerWithSzDecimals(0);
        assertEquals("1.2345", HyperliquidUtil.formatPriceAsString(t0, new BigDecimal("1.23456789")));
        Ticker t2 = makeTickerWithSzDecimals(2);
        assertEquals("1.2345", HyperliquidUtil.formatPriceAsString(t2, new BigDecimal("1.23456789")));
        Ticker t4 = makeTickerWithSzDecimals(4);
        assertEquals("1.23", HyperliquidUtil.formatPriceAsString(t4, new BigDecimal("1.23456789")));
    }

    @Test
    void testFormatPriceAsString_largePrice_integerOnly() {
        Ticker t2 = makeTickerWithSzDecimals(2);
        Ticker t0 = makeTickerWithSzDecimals(0);
        assertEquals("100000", HyperliquidUtil.formatPriceAsString(t2, new BigDecimal("100000.999")));
        assertEquals("1234567", HyperliquidUtil.formatPriceAsString(t0, new BigDecimal("1234567.89")));
    }

    @Test
    void testFormatPriceAsString_significantFigures() {
        Ticker t2 = makeTickerWithSzDecimals(2);
        assertEquals("0.1234", HyperliquidUtil.formatPriceAsString(t2, new BigDecimal("0.123456789")));
        assertEquals("12.345", HyperliquidUtil.formatPriceAsString(t2, new BigDecimal("12.3456789")));
    }

    @Test
    void testFormatPriceAsString_trailingZeros() {
        Ticker t2 = makeTickerWithSzDecimals(2);
        Ticker t4 = makeTickerWithSzDecimals(4);
        assertEquals("1.2", HyperliquidUtil.formatPriceAsString(t2, new BigDecimal("1.200000")));
        assertEquals("0.01", HyperliquidUtil.formatPriceAsString(t4, new BigDecimal("0.010000")));
    }

    @Test
    void testFormatPriceAsString_edgeCases() {
        Ticker t2 = makeTickerWithSzDecimals(2);
        Ticker t4 = makeTickerWithSzDecimals(4);
        assertEquals("0", HyperliquidUtil.formatPriceAsString(t2, BigDecimal.ZERO));
        assertEquals("-1.23", HyperliquidUtil.formatPriceAsString(t4, new BigDecimal("-1.234")));
    }
}
