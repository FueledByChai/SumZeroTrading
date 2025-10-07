package com.sumzerotrading.hyperliquid;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import com.sumzerotrading.data.Ticker;

class HyperliquidUtilTest {
    private Ticker makeTickerWithSzDecimals(int szDecimals) {
        int maxDecimals = 6; // Hyperliquid supports up to 6 decimal places
        Ticker t = new Ticker("TEST");
        // tick size = 1 / 10^szDecimals
        t.setMinimumTickSize(BigDecimal.ONE.divide(BigDecimal.TEN.pow(maxDecimals - szDecimals)));
        return t;
    }

    @Test
    void testFormatPriceAsString_variousSzDecimals() {
        // Test shows 5 sig figs rule is primary - all should give same result
        Ticker t0 = makeTickerWithSzDecimals(0);
        assertEquals("1.2345", HyperliquidUtil.formatPriceAsString(t0, new BigDecimal("1.23456789")));
        Ticker t2 = makeTickerWithSzDecimals(2);
        assertEquals("1.2345", HyperliquidUtil.formatPriceAsString(t2, new BigDecimal("1.23456789")));
        Ticker t4 = makeTickerWithSzDecimals(4);
        assertEquals("1.2345", HyperliquidUtil.formatPriceAsString(t4, new BigDecimal("1.23456789")));
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
        // 0.123456789: 0 integer digits, so 5 sig figs allows 5 decimals = 0.12345
        assertEquals("0.12345", HyperliquidUtil.formatPriceAsString(t2, new BigDecimal("0.123456789")));
        // 12.3456789: 2 integer digits, so 5 sig figs allows 3 decimals = 12.345
        assertEquals("12.345", HyperliquidUtil.formatPriceAsString(t2, new BigDecimal("12.3456789")));
    }

    @Test
    void testFormatPriceAsString_trailingZeros() {
        Ticker t2 = makeTickerWithSzDecimals(2);
        Ticker t4 = makeTickerWithSzDecimals(4);
        // Trailing zeros are stripped, so 1.200000 becomes 1.2
        assertEquals("1.2", HyperliquidUtil.formatPriceAsString(t2, new BigDecimal("1.200000")));
        // 0.010000 becomes 0.01
        assertEquals("0.01", HyperliquidUtil.formatPriceAsString(t4, new BigDecimal("0.010000")));
    }

    @Test
    void testFormatPriceAsString_edgeCases() {
        Ticker t4 = makeTickerWithSzDecimals(4);
        // -1.234: 1 integer digit, 5 sig figs allows 4 decimals, but input only has 3 =
        // -1.234
        assertEquals("-1.234", HyperliquidUtil.formatPriceAsString(t4, new BigDecimal("-1.234")));
    }

    @Test
    public void testAsterTicker() {
        Ticker t6 = makeTickerWithSzDecimals(6);
        // 1.987654: 1 integer digit, 5 sig figs allows 4 decimals = 1.9876
        assertEquals("1.9876", HyperliquidUtil.formatPriceAsString(t6, new BigDecimal("1.987654")));
        // Ticker [id=207, instrumentType=PERPETUAL_FUTURES, symbol=ASTER,
        // exchange=Exchange{exchangeName=HYPERLIQUID},
        // primaryExchange=Exchange{exchangeName=HYPERLIQUID}, currency=USD,
        // minimumTickSize=0.000001, contractMultiplier=1, orderSizeIncrement=1,
        // minimumOrderSize=null, expiryMonth=0, expiryYear=0, expiryDay=0, strike=null,
        // right=NONE, fundingRateInterval=1, minimumOrderSizeNotional=null],
        // price=1.8599, size=296.0, side=SELL, time=2025-09-21T04:47:17.208Z[UTC],
        // orderId=168725
    }
}
