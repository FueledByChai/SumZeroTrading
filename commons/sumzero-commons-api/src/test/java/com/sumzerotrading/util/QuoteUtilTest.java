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

package com.sumzerotrading.util;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Test;

import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;

public class QuoteUtilTest {

	@Test
	public void testRoundPrice() {
		BigDecimal stopLoss = getPrice(1323.37, 2);
		BigDecimal tickSize = getPrice(0.25, 2);
		BigDecimal rounded = QuoteUtil.roundPrice(stopLoss, tickSize, 2);

		System.out.println("Rounded: " + rounded);
		assertEquals(rounded, getPrice(1323.25, 2));

		stopLoss = getPrice(1323.38, 2);
		rounded = QuoteUtil.roundPrice(stopLoss, tickSize, 2);
		System.out.println("Rounded: " + rounded);
		assertEquals(rounded, getPrice(1323.50, 2));

		tickSize = getPrice(1, 0);
		stopLoss = getPrice(11100.54, 0);
		rounded = QuoteUtil.roundPrice(stopLoss, tickSize, 0);
		System.out.println("Rounded: " + rounded);
		assertEquals(rounded, getPrice(11101, 0));

		stopLoss = getPrice(11100.44, 0);
		rounded = QuoteUtil.roundPrice(stopLoss, tickSize, 0);
		System.out.println("Rounded: " + rounded);
		assertEquals(rounded, getPrice(11100, 0));

		rounded = QuoteUtil.roundPrice(new BigDecimal(1323.3737373), new BigDecimal(0.25), 2);
		System.out.println("Rounded: " + rounded);
		assertEquals(rounded, getPrice(1323.25, 2));

	}

	@Test
	public void testRoundPriceUseTicksizeScale() {
		double value = 16.778;
		BigDecimal price = new BigDecimal(value).setScale(3, RoundingMode.HALF_UP);
		BigDecimal scale = new BigDecimal("0.01").setScale(2, RoundingMode.HALF_UP);
		BigDecimal actual = QuoteUtil.roundPrice(price, scale);

		BigDecimal expected = new BigDecimal(16.78).setScale(2, RoundingMode.HALF_UP);
		assertEquals(expected, actual);
	}

	@Test
	public void testGetBigDecimalValue() {
		Ticker ticker = new Ticker("ABC");
		ticker.setInstrumentType(InstrumentType.STOCK);
		double value = 1.25;
		BigDecimal bdValue = QuoteUtil.getBigDecimalValue(ticker, value);

		BigDecimal expected = new BigDecimal("1.25").setScale(2, RoundingMode.HALF_UP);
		assertEquals(expected, bdValue);
	}

	@Test
	public void testGetBigDecimalIntValue() {
		int value = 5;
		BigDecimal actual = QuoteUtil.getBigDecimalValue(value);
		BigDecimal expected = new BigDecimal(5).setScale(0, RoundingMode.HALF_UP);
		assertEquals(expected, actual);
	}

	private BigDecimal getPrice(double price, int scale) {
		return new BigDecimal(price).setScale(scale, BigDecimal.ROUND_HALF_UP);
	}
}
