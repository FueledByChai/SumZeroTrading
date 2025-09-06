/**
 * MIT License

Copyright (c) 2015  Rob Terpilowski

Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
and associated documentation files (the "Software"), to deal in the Software without restriction, 
including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.sumzerotrading.data;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive test class for the Ticker class.
 * 
 * @author Rob Terpilowski
 */
public class TickerTest {

    private Ticker ticker;

    @Before
    public void setUp() {
        ticker = new Ticker();
    }

    @Test
    public void testDefaultConstructor() {
        Ticker newTicker = new Ticker();
        assertNotNull(newTicker);
        assertNull(newTicker.getSymbol());
        assertNull(newTicker.getExchange());
        assertEquals("USD", newTicker.getCurrency()); // Default value is "USD"
        assertEquals(new BigDecimal("0.01"), newTicker.getMinimumTickSize()); // Default value is 0.01
        assertEquals(BigDecimal.ONE, newTicker.getContractMultiplier()); // Default value is 1
        assertNull(newTicker.getInstrumentType());
        assertEquals(0, newTicker.getExpiryMonth());
        assertEquals(0, newTicker.getExpiryYear());
        assertEquals(0, newTicker.getExpiryDay());
        assertEquals(0, newTicker.getFundingRateInterval());
    }

    @Test
    public void testStringConstructor() {
        String symbol = "AAPL";
        Ticker newTicker = new Ticker(symbol);
        assertNotNull(newTicker);
        assertEquals(symbol, newTicker.getSymbol());
    }

    @Test
    public void testSymbolGetterSetter() {
        String symbol = "MSFT";
        ticker.setSymbol(symbol);
        assertEquals(symbol, ticker.getSymbol());
    }

    @Test
    public void testExchangeGetterSetter() {
        Exchange exchange = Exchange.NASDAQ;
        ticker.setExchange(exchange);
        assertEquals(exchange, ticker.getExchange());
    }

    @Test
    public void testCurrencyGetterSetter() {
        String currency = "EUR";
        ticker.setCurrency(currency);
        assertEquals(currency, ticker.getCurrency());
    }

    @Test
    public void testMinimumTickSizeGetterSetter() {
        BigDecimal tickSize = new BigDecimal("0.01");
        ticker.setMinimumTickSize(tickSize);
        assertEquals(tickSize, ticker.getMinimumTickSize());
    }

    @Test
    public void testContractMultiplierGetterSetter() {
        BigDecimal multiplier = new BigDecimal("100");
        ticker.setContractMultiplier(multiplier);
        assertEquals(multiplier, ticker.getContractMultiplier());
    }

    @Test
    public void testDecimalFormatGetterSetter() {
        DecimalFormat format = new DecimalFormat("#.##");
        ticker.setDecimalFormat(format);
        assertEquals(format, ticker.getDecimalFormat());
    }

    @Test
    public void testPrimaryExchangeGetterSetter() {
        Exchange primaryExchange = Exchange.NASDAQ;
        ticker.setPrimaryExchange(primaryExchange);
        assertEquals(primaryExchange, ticker.getPrimaryExchange());
    }

    @Test
    public void testOrderSizeIncrementGetterSetter() {
        BigDecimal increment = new BigDecimal("0.1");
        ticker.setOrderSizeIncrement(increment);
        assertEquals(increment, ticker.getOrderSizeIncrement());
    }

    @Test
    public void testRightGetterSetter() {
        Ticker.Right right = Ticker.Right.CALL;
        ticker.setRight(right);
        assertEquals(right, ticker.getRight());
    }

    @Test
    public void testInstrumentTypeGetterSetter() {
        InstrumentType type = InstrumentType.STOCK;
        ticker.setInstrumentType(type);
        assertEquals(type, ticker.getInstrumentType());
    }

    @Test
    public void testExpiryMonthGetterSetter() {
        int month = 12;
        ticker.setExpiryMonth(month);
        assertEquals(month, ticker.getExpiryMonth());
    }

    @Test
    public void testExpiryYearGetterSetter() {
        int year = 2024;
        ticker.setExpiryYear(year);
        assertEquals(year, ticker.getExpiryYear());
    }

    @Test
    public void testExpiryDayGetterSetter() {
        int day = 15;
        ticker.setExpiryDay(day);
        assertEquals(day, ticker.getExpiryDay());
    }

    @Test
    public void testStrikeGetterSetter() {
        BigDecimal strike = new BigDecimal("100.50");
        ticker.setStrike(strike);
        assertEquals(strike, ticker.getStrike());
    }

    @Test
    public void testFundingRateIntervalGetterSetter() {
        int interval = 8;
        ticker.setFundingRateInterval(interval);
        assertEquals(interval, ticker.getFundingRateInterval());
    }

    @Test
    public void testPadMonth() {
        assertEquals("01", ticker.padMonth(1));
        assertEquals("09", ticker.padMonth(9));
        assertEquals("10", ticker.padMonth(10));
        assertEquals("12", ticker.padMonth(12));
    }

    @Test
    public void testSupportsHalfTick() {
        // supportsHalfTick always returns false in the current implementation
        assertFalse(ticker.supportsHalfTick());

        // Test with minimum tick size set - still returns false
        ticker.setMinimumTickSize(new BigDecimal("0.01"));
        assertFalse(ticker.supportsHalfTick());

        // Test with different minimum tick size - still returns false
        ticker.setMinimumTickSize(new BigDecimal("0.25"));
        assertFalse(ticker.supportsHalfTick());
    }

    @Test
    public void testFormatPriceBigDecimal() {
        BigDecimal price = new BigDecimal("1.25");

        // Test with default minimum tick size (0.01) - should format to 2 decimal
        // places
        assertEquals(new BigDecimal("1.25"), ticker.formatPrice(price));

        // Test with 0.001 tick size - should format to 3 decimal places
        ticker.setMinimumTickSize(new BigDecimal("0.001"));
        assertEquals(new BigDecimal("1.250"), ticker.formatPrice(price));

        // Test with 0.01 tick size - should format to 2 decimal places
        ticker.setMinimumTickSize(new BigDecimal("0.01"));
        assertEquals(new BigDecimal("1.25"), ticker.formatPrice(price));

        // Test with 0.25 tick size - should format to 2 decimal places
        ticker.setMinimumTickSize(new BigDecimal("0.25"));
        assertEquals(new BigDecimal("1.25"), ticker.formatPrice(price));

        // Test with 1.0 tick size - should format to 1 decimal place (since 1.0 has
        // scale 1)
        ticker.setMinimumTickSize(new BigDecimal("1.0"));
        assertEquals(new BigDecimal("1.3"), ticker.formatPrice(price));

        // Test with 1 tick size (integer) - should format to 0 decimal places
        ticker.setMinimumTickSize(new BigDecimal("1"));
        assertEquals(new BigDecimal("1"), ticker.formatPrice(price));

        // Test with null minimum tick size - should return original price
        ticker.setMinimumTickSize(null);
        assertEquals(price, ticker.formatPrice(price));
    }

    @Test
    public void testFormatPriceBigDecimalWithNullInput() {
        ticker.setMinimumTickSize(new BigDecimal("0.01"));
        assertNull(ticker.formatPrice((BigDecimal) null));
    }

    @Test
    public void testFormatPriceString() {
        String price = "1.25";

        // Test with default minimum tick size (0.01) - should format to 2 decimal
        // places
        assertEquals(new BigDecimal("1.25"), ticker.formatPrice(price));

        // Test with 0.001 tick size - should format to 3 decimal places
        ticker.setMinimumTickSize(new BigDecimal("0.001"));
        assertEquals(new BigDecimal("1.250"), ticker.formatPrice(price));

        // Test with 0.01 tick size - should format to 2 decimal places
        ticker.setMinimumTickSize(new BigDecimal("0.01"));
        assertEquals(new BigDecimal("1.25"), ticker.formatPrice(price));
    }

    @Test
    public void testFormatPriceStringWithNullInput() {
        ticker.setMinimumTickSize(new BigDecimal("0.01"));
        assertNull(ticker.formatPrice((String) null));
    }

    @Test(expected = NumberFormatException.class)
    public void testFormatPriceStringWithInvalidInput() {
        ticker.setMinimumTickSize(new BigDecimal("0.01"));
        ticker.formatPrice("invalid");
    }

    @Test
    public void testFormatPriceWithLargerNumbers() {
        ticker.setMinimumTickSize(new BigDecimal("0.01"));
        BigDecimal largePrice = new BigDecimal("1234.5678");
        assertEquals(new BigDecimal("1234.57"), ticker.formatPrice(largePrice));
    }

    @Test
    public void testEquals() {
        Ticker ticker1 = new Ticker("AAPL");
        ticker1.setExchange(Exchange.NASDAQ);
        ticker1.setCurrency("USD");
        ticker1.setMinimumTickSize(new BigDecimal("0.01"));
        ticker1.setInstrumentType(InstrumentType.STOCK);

        Ticker ticker2 = new Ticker("AAPL");
        ticker2.setExchange(Exchange.NASDAQ);
        ticker2.setCurrency("USD");
        ticker2.setMinimumTickSize(new BigDecimal("0.01"));
        ticker2.setInstrumentType(InstrumentType.STOCK);

        // Test equality
        assertTrue(ticker1.equals(ticker2));
        assertTrue(ticker2.equals(ticker1));

        // Test self equality
        assertTrue(ticker1.equals(ticker1));

        // Test null
        assertFalse(ticker1.equals(null));

        // Test different class
        assertFalse(ticker1.equals("not a ticker"));

        // Test different symbol
        ticker2.setSymbol("MSFT");
        assertFalse(ticker1.equals(ticker2));
    }

    @Test
    public void testEqualsWithNullFields() {
        Ticker ticker1 = new Ticker();
        Ticker ticker2 = new Ticker();

        // Both have all null fields (except currency which defaults to "USD")
        assertTrue(ticker1.equals(ticker2));

        // One has symbol, other doesn't
        ticker1.setSymbol("AAPL");
        assertFalse(ticker1.equals(ticker2));

        // Both have same symbol
        ticker2.setSymbol("AAPL");
        assertTrue(ticker1.equals(ticker2));
    }

    @Test
    public void testHashCode() {
        Ticker ticker1 = new Ticker("AAPL");
        ticker1.setExchange(Exchange.NASDAQ);
        ticker1.setCurrency("USD");

        Ticker ticker2 = new Ticker("AAPL");
        ticker2.setExchange(Exchange.NASDAQ);
        ticker2.setCurrency("USD");

        // Equal objects should have equal hash codes
        assertEquals(ticker1.hashCode(), ticker2.hashCode());

        // Change one property and hash codes should be different
        ticker2.setSymbol("MSFT");
        assertNotEquals(ticker1.hashCode(), ticker2.hashCode());
    }

    @Test
    public void testHashCodeWithNullFields() {
        Ticker ticker1 = new Ticker();
        Ticker ticker2 = new Ticker();

        // Both have all null fields - should have same hash code
        assertEquals(ticker1.hashCode(), ticker2.hashCode());
    }

    @Test
    public void testToString() {
        ticker.setSymbol("AAPL");
        ticker.setExchange(Exchange.NASDAQ);
        ticker.setCurrency("USD");
        ticker.setInstrumentType(InstrumentType.STOCK);
        ticker.setMinimumTickSize(new BigDecimal("0.01"));

        String result = ticker.toString();

        // Verify the string contains key information
        assertTrue(result.contains("AAPL"));
        assertTrue(result.contains("NASDAQ"));
        assertTrue(result.contains("USD"));
        assertTrue(result.contains("STOCK"));
        assertTrue(result.contains("0.01"));
        assertTrue(result.startsWith("Ticker ["));
        assertTrue(result.endsWith("]"));
    }

    @Test
    public void testToStringWithNullFields() {
        String result = ticker.toString();

        // Should handle null fields gracefully
        assertNotNull(result);
        assertTrue(result.startsWith("Ticker ["));
        assertTrue(result.endsWith("]"));
        assertTrue(result.contains("null"));
    }

    @Test
    public void testFluentInterface() {
        // Test that setter methods return the ticker instance for method chaining
        Ticker result = ticker.setSymbol("AAPL").setExchange(Exchange.NASDAQ).setCurrency("USD")
                .setMinimumTickSize(new BigDecimal("0.01")).setInstrumentType(InstrumentType.STOCK);

        assertSame(ticker, result);
        assertEquals("AAPL", ticker.getSymbol());
        assertEquals(Exchange.NASDAQ, ticker.getExchange());
        assertEquals("USD", ticker.getCurrency());
        assertEquals(new BigDecimal("0.01"), ticker.getMinimumTickSize());
        assertEquals(InstrumentType.STOCK, ticker.getInstrumentType());
    }

    @Test
    public void testComplexScenarioWithOptions() {
        ticker.setSymbol("AAPL240315C00180000").setInstrumentType(InstrumentType.OPTION).setExchange(Exchange.CBOE)
                .setCurrency("USD").setStrike(new BigDecimal("180.00")).setRight(Ticker.Right.CALL).setExpiryYear(2024)
                .setExpiryMonth(3).setExpiryDay(15).setMinimumTickSize(new BigDecimal("0.01"))
                .setContractMultiplier(new BigDecimal("100"));

        assertEquals("AAPL240315C00180000", ticker.getSymbol());
        assertEquals(InstrumentType.OPTION, ticker.getInstrumentType());
        assertEquals(Exchange.CBOE, ticker.getExchange());
        assertEquals("USD", ticker.getCurrency());
        assertEquals(new BigDecimal("180.00"), ticker.getStrike());
        assertEquals(Ticker.Right.CALL, ticker.getRight());
        assertEquals(2024, ticker.getExpiryYear());
        assertEquals(3, ticker.getExpiryMonth());
        assertEquals(15, ticker.getExpiryDay());
        assertEquals(new BigDecimal("0.01"), ticker.getMinimumTickSize());
        assertEquals(new BigDecimal("100"), ticker.getContractMultiplier());

        // Test price formatting for options
        assertEquals(new BigDecimal("4.50"), ticker.formatPrice(new BigDecimal("4.50")));
    }

    @Test
    public void testComplexScenarioWithCrypto() {
        ticker.setSymbol("BTC-USD").setInstrumentType(InstrumentType.CRYPTO_SPOT).setExchange(Exchange.PARADEX)
                .setCurrency("USD").setMinimumTickSize(new BigDecimal("0.01"))
                .setOrderSizeIncrement(new BigDecimal("0.00000001")).setFundingRateInterval(8);

        assertEquals("BTC-USD", ticker.getSymbol());
        assertEquals(InstrumentType.CRYPTO_SPOT, ticker.getInstrumentType());
        assertEquals(Exchange.PARADEX, ticker.getExchange());
        assertEquals("USD", ticker.getCurrency());
        assertEquals(new BigDecimal("0.01"), ticker.getMinimumTickSize());
        assertEquals(new BigDecimal("0.00000001"), ticker.getOrderSizeIncrement());
        assertEquals(8, ticker.getFundingRateInterval());

        // Test price formatting for crypto
        assertEquals(new BigDecimal("50000.00"), ticker.formatPrice(new BigDecimal("50000")));
    }
}
