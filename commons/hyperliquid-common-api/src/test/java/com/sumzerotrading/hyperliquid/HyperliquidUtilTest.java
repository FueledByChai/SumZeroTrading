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
        // Test shows how both 5 sig figs and decimal places constraints apply
        Ticker t0 = makeTickerWithSzDecimals(0); // szDecimals=6, allowedDecimalPlaces=0
        assertEquals("1", HyperliquidUtil.formatPriceAsString(t0, new BigDecimal("1.23456789")));
        Ticker t2 = makeTickerWithSzDecimals(2); // szDecimals=4, allowedDecimalPlaces=2
        assertEquals("1.23", HyperliquidUtil.formatPriceAsString(t2, new BigDecimal("1.23456789")));
        Ticker t4 = makeTickerWithSzDecimals(4); // szDecimals=2, allowedDecimalPlaces=4
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
        Ticker t2 = makeTickerWithSzDecimals(2); // szDecimals=4, allowedDecimalPlaces=2
        assertEquals("0.12", HyperliquidUtil.formatPriceAsString(t2, new BigDecimal("0.123456789")));
        assertEquals("12.34", HyperliquidUtil.formatPriceAsString(t2, new BigDecimal("12.3456789")));
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
        assertEquals("-1.234", HyperliquidUtil.formatPriceAsString(t4, new BigDecimal("-1.234")));
    }

    @Test
    public void testAsterTicker() {
        Ticker t6 = makeTickerWithSzDecimals(6);
        assertEquals("1.9876", HyperliquidUtil.formatPriceAsString(t6, new BigDecimal("1.987654")));
        // Ticker [id=207, instrumentType=PERPETUAL_FUTURES, symbol=ASTER,
        // exchange=Exchange{exchangeName=HYPERLIQUID},
        // primaryExchange=Exchange{exchangeName=HYPERLIQUID}, currency=USD,
        // minimumTickSize=0.000001, contractMultiplier=1, orderSizeIncrement=1,
        // minimumOrderSize=null, expiryMonth=0, expiryYear=0, expiryDay=0, strike=null,
        // right=NONE, fundingRateInterval=1, minimumOrderSizeNotional=null],
        // price=1.8599, size=296.0, side=SELL, time=2025-09-21T04:47:17.208Z[GMT],
        // orderId=168725
    }
}
