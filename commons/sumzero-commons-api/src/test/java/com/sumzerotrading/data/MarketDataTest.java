
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import junit.framework.TestCase;

public class MarketDataTest extends TestCase {

	public void testAppendBar() {
		MarketData data = new MarketData();
		ZonedDateTime cal = ZonedDateTime.now();

		BarData bar1 = new BarData(cal, new BigDecimal(1), new BigDecimal(1), new BigDecimal(1), new BigDecimal(1),
				new BigDecimal(1));
		BarData bar2 = new BarData(cal, new BigDecimal(2), new BigDecimal(2), new BigDecimal(2), new BigDecimal(2),
				new BigDecimal(2));

		data.appendBar(bar1);

		BarData[] bars = data.getBars();
		assertEquals(1, data.getBars().length);
		assertEquals(bar1, data.getBars()[0]);

		data.appendBar(bar2);
		bars = data.getBars();
		assertEquals(2, data.getBars().length);
		assertEquals(bar1, data.getBars()[0]);
		assertEquals(bar2, data.getBars()[1]);

	}

	public void testInsertBar() {
		MarketData data = new MarketData();
		ZonedDateTime cal = ZonedDateTime.now();
		BarData bar1 = new BarData(cal, new BigDecimal(1), new BigDecimal(1), new BigDecimal(1), new BigDecimal(1),
				new BigDecimal(1));
		BarData bar2 = new BarData(cal, new BigDecimal(2), new BigDecimal(2), new BigDecimal(2), new BigDecimal(2),
				new BigDecimal(2));

		data.insertBar(bar1);

		BarData[] bars = data.getBars();
		assertEquals(1, data.getBars().length);
		assertEquals(bar1, data.getBars()[0]);

		data.insertBar(bar2);
		bars = data.getBars();
		assertEquals(2, data.getBars().length);
		assertEquals(bar2, data.getBars()[0]);
		assertEquals(bar1, data.getBars()[1]);

	}

	public void testFoo() {
		MarketData data = new MarketData();
		ZonedDateTime cal = ZonedDateTime.now();
		BarData bar1 = new BarData(cal, new BigDecimal(1), new BigDecimal(1), new BigDecimal(1), new BigDecimal(1),
				new BigDecimal(1));
		BarData bar2 = new BarData(cal, new BigDecimal(2), new BigDecimal(2), new BigDecimal(2), new BigDecimal(2),
				new BigDecimal(2));
		BarData bar3 = new BarData(cal, new BigDecimal(3), new BigDecimal(3), new BigDecimal(3), new BigDecimal(3),
				new BigDecimal(3));
		BarData bar4 = new BarData(cal, new BigDecimal(4), new BigDecimal(4), new BigDecimal(4), new BigDecimal(4),
				new BigDecimal(4));

		data.appendBar(bar1);
		data.appendBar(bar2);
		data.appendBar(bar3);

		BarData[] bars = data.getBars();
		assertEquals(3, data.getBars().length);
		assertEquals(bar1, data.getBars()[0]);
		assertEquals(bar2, data.getBars()[1]);
		assertEquals(bar3, data.getBars()[2]);

		data.replaceBar(bar2, bar4);

		bars = data.getBars();
		assertEquals(3, data.getBars().length);
		assertEquals(bar1, data.getBars()[0]);
		assertEquals(bar4, data.getBars()[1]);
		assertEquals(bar3, data.getBars()[2]);

	}

}
