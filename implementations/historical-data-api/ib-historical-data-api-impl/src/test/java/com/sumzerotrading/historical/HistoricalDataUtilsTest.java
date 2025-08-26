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

package com.sumzerotrading.historical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sumzerotrading.data.BarData;
import com.sumzerotrading.data.BarData.LengthUnit;
import com.sumzerotrading.historicaldata.IHistoricalDataProvider.ShowProperty;
import com.sumzerotrading.ib.historical.HistoricalData;
import com.sumzerotrading.ib.historical.HistoricalDataUtils;
import com.sumzerotrading.ib.historical.InvalidBarSizeException;

/**
 *
 * @author Rob Terpilowski
 */
public class HistoricalDataUtilsTest {

    public HistoricalDataUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testbuildBarDataSizeString_Seconds() {
        assertEquals("1 sec", HistoricalDataUtils.buildBarDataSizeString(1, LengthUnit.SECOND));
        assertEquals("5 secs", HistoricalDataUtils.buildBarDataSizeString(5, LengthUnit.SECOND));
        assertEquals("15 secs", HistoricalDataUtils.buildBarDataSizeString(15, LengthUnit.SECOND));
        assertEquals("30 secs", HistoricalDataUtils.buildBarDataSizeString(30, LengthUnit.SECOND));
        try {
            HistoricalDataUtils.buildBarDataSizeString(31, LengthUnit.SECOND);
            fail();
        } catch (InvalidBarSizeException ex) {
            // this should happen
        }

    }

    @Test
    public void testbuildBarDataSizeString_Minutes() {
        assertEquals("1 min", HistoricalDataUtils.buildBarDataSizeString(1, LengthUnit.MINUTE));
        assertEquals("2 mins", HistoricalDataUtils.buildBarDataSizeString(2, LengthUnit.MINUTE));
        assertEquals("5 mins", HistoricalDataUtils.buildBarDataSizeString(5, LengthUnit.MINUTE));
        assertEquals("15 mins", HistoricalDataUtils.buildBarDataSizeString(15, LengthUnit.MINUTE));
        assertEquals("30 mins", HistoricalDataUtils.buildBarDataSizeString(30, LengthUnit.MINUTE));
        try {
            HistoricalDataUtils.buildBarDataSizeString(31, LengthUnit.MINUTE);
            fail();
        } catch (InvalidBarSizeException ex) {
            // this should happen
        }
    }

    @Test
    public void testbuildBarDataSizeString_Hours() {
        assertEquals("1 hour", HistoricalDataUtils.buildBarDataSizeString(1, LengthUnit.HOUR));
        try {
            HistoricalDataUtils.buildBarDataSizeString(2, LengthUnit.HOUR);
            fail();
        } catch (InvalidBarSizeException ex) {
            // this should happen
        }
    }

    @Test
    public void testbuildBarDataSizeString_Days() {
        assertEquals("1 day", HistoricalDataUtils.buildBarDataSizeString(1, LengthUnit.DAY));
        try {
            HistoricalDataUtils.buildBarDataSizeString(2, LengthUnit.DAY);
            fail();
        } catch (InvalidBarSizeException ex) {
            // this should happen
        }
    }

    @Test
    public void testBuildBarDataSizeString_InvalidSize() {
        try {
            HistoricalDataUtils.buildBarDataSizeString(1, LengthUnit.WEEK);
            fail();
        } catch (InvalidBarSizeException ex) {
            // this should happen
        }
    }

    @Test
    public void testBuildDurationString() {
        try {
            HistoricalDataUtils.buildDurationString(1, LengthUnit.TICK);
            fail();
        } catch (IllegalStateException ex) {
            // this should happen
        }

        assertEquals("1 S", HistoricalDataUtils.buildDurationString(1, LengthUnit.SECOND));
        try {
            HistoricalDataUtils.buildDurationString(1, LengthUnit.MINUTE);
            fail();
        } catch (IllegalStateException ex) {
            // this should happen.
        }

        try {
            HistoricalDataUtils.buildDurationString(1, LengthUnit.HOUR);
            fail();
        } catch (IllegalStateException ex) {
            // this should happen
        }

        assertEquals("1 D", HistoricalDataUtils.buildDurationString(1, LengthUnit.DAY));
        assertEquals("1 W", HistoricalDataUtils.buildDurationString(1, LengthUnit.WEEK));
        assertEquals("1 M", HistoricalDataUtils.buildDurationString(1, LengthUnit.MONTH));
        assertEquals("1 Y", HistoricalDataUtils.buildDurationString(1, LengthUnit.YEAR));

    }

    @Test
    public void testBuildBarData() {
        int requestId = 1;
        GregorianCalendar cal = new GregorianCalendar();
        ZonedDateTime date = ZonedDateTime.ofInstant(cal.getTime().toInstant(), ZoneId.of("UTC"));
        double open = 1;
        double high = 2;
        double low = 0.5;
        double close = 1.5;
        int volume = 100;
        int count = 1;
        double wap = 1.0;
        boolean hasGaps = true;

        HistoricalData data = new HistoricalData(requestId, cal, open, high, low, close, volume, count, wap, hasGaps);
        BarData bar = HistoricalDataUtils.buildBarData(data);

        assertEquals(new BigDecimal(close), bar.getClose());
        assertEquals(new BigDecimal(open), bar.getOpen());
        assertEquals(new BigDecimal(high), bar.getHigh());
        assertEquals(new BigDecimal(low), bar.getLow());
        assertEquals(new BigDecimal(volume), bar.getVolume());
        assertEquals(date, bar.getDateTime());
    }

    @Test
    public void testShowPropertyToString() {
        assertEquals("ASK", HistoricalDataUtils.showPropertyToString(ShowProperty.ASK));
        assertEquals("BID", HistoricalDataUtils.showPropertyToString(ShowProperty.BID));
        assertEquals("MIDPOINT", HistoricalDataUtils.showPropertyToString(ShowProperty.MIDPOINT));
        assertEquals("TRADES", HistoricalDataUtils.showPropertyToString(ShowProperty.TRADES));

        try {
            HistoricalDataUtils.showPropertyToString(null);
            fail();
        } catch (IllegalStateException ex) {
            // this should happen
        }
    }
}
