/**
 * MIT License
 *
 * Copyright (c) 2015  Rob Terpilowski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.sumzerotrading.bitmex.historical;

import com.sumzerotrading.bitmex.client.BitmexRestClient;
import com.sumzerotrading.bitmex.entity.BitmexChartData;
import com.sumzerotrading.data.BarData;
import com.sumzerotrading.data.GenericTicker;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.bitmex.historical.HistoricalDataUtils;
import com.sumzerotrading.bitmex.historical.InvalidBarSizeException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
    public void testGetBinSize_HappyPaths() {
        assertEquals(BitmexRestClient.ChartDataBinSize.ONE_DAY,
                HistoricalDataUtils.getBinSize(1, BarData.LengthUnit.DAY));
        assertEquals(BitmexRestClient.ChartDataBinSize.ONE_HOUR,
                HistoricalDataUtils.getBinSize(1, BarData.LengthUnit.HOUR));
        assertEquals(BitmexRestClient.ChartDataBinSize.FIVE_MINUTES,
                HistoricalDataUtils.getBinSize(5, BarData.LengthUnit.MINUTE));
        assertEquals(BitmexRestClient.ChartDataBinSize.ONE_MINUTE,
                HistoricalDataUtils.getBinSize(1, BarData.LengthUnit.MINUTE));
    }

    @Test
    public void testGetBinSize_UnhappyPaths() {
        try {
            HistoricalDataUtils.getBinSize(1, BarData.LengthUnit.MONTH);
            fail();
        } catch (InvalidBarSizeException ex) {
            // this should happen
        }
        try {
            HistoricalDataUtils.getBinSize(2, BarData.LengthUnit.DAY);
            fail();
        } catch (InvalidBarSizeException ex) {
            // this should happen
        }

        try {
            HistoricalDataUtils.getBinSize(2, BarData.LengthUnit.HOUR);
            fail();
        } catch (InvalidBarSizeException ex) {
            // this should happen
        }

        try {
            HistoricalDataUtils.getBinSize(2, BarData.LengthUnit.MINUTE);
            fail();
        } catch (InvalidBarSizeException ex) {
            // this should happen
        }
    }

    @Test
    public void testBuildBarData() {
        int barLength = 1;
        BarData.LengthUnit lengthUnit = BarData.LengthUnit.MINUTE;
        Ticker ticker = new GenericTicker("ABC");
        BigDecimal open = new BigDecimal(123.23);
        BigDecimal high = new BigDecimal(234.45);
        BigDecimal low = new BigDecimal(100.23);
        BigDecimal close = new BigDecimal(145.54);
        BigDecimal volume = new BigDecimal(2231);
        ZonedDateTime timestamp = ZonedDateTime.of(2018, 6, 13, 14, 50, 0, 0, ZoneId.of("UTC"));

        BitmexChartData chartData = new BitmexChartData();
        chartData.setTimestamp(timestamp);
        chartData.setOpen(open.doubleValue());
        chartData.setHigh(high.doubleValue());
        chartData.setLow(low.doubleValue());
        chartData.setClose(close.doubleValue());
        chartData.setVolume(volume.doubleValue());

        BarData expectedData = new BarData(ticker, ZonedDateTime.of(2018, 6, 13, 7, 50, 0, 0, ZoneId.of("UTC")), open,
                high, low, close, volume, barLength, lengthUnit);

        assertEquals(expectedData, HistoricalDataUtils.buildBarData(ticker, barLength, lengthUnit, chartData));
    }

}
