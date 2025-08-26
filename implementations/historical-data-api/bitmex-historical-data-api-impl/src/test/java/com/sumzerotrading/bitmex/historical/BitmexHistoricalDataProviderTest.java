/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.historical;

import com.sumzerotrading.bitmex.client.BitmexRestClient;
import com.sumzerotrading.bitmex.client.IBitmexClient;
import com.sumzerotrading.bitmex.entity.BitmexChartData;
import com.sumzerotrading.data.BarData;
import com.sumzerotrading.data.GenericTicker;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.historicaldata.IHistoricalDataProvider;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author RobTerpilowski
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore("Ignore until API is fixed")
public class BitmexHistoricalDataProviderTest {

    @Spy
    protected BitmexHistoricalDataProvider testProvider;

    @Mock
    protected IBitmexClient mockClient;

    public BitmexHistoricalDataProviderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
    }

    @Test
    public void testIsConnected() {
        testProvider.connected = true;
        assertTrue(testProvider.isConnected());
    }

    @Test
    public void testConnect() {
        testProvider.connect();
        assertNotNull(testProvider.client);
        assertTrue(testProvider.isConnected());
    }

    @Test
    public void testInit() {
        try {
            testProvider.init(new Properties());
            fail();
        } catch (SumZeroException ex) {

        }
    }

    @Test
    public void testRequestHistoricalData_NotTrades() {
        try {
            testProvider.requestHistoricalData(new GenericTicker("ABC"), 1, BarData.LengthUnit.DAY, 1,
                    BarData.LengthUnit.MINUTE, IHistoricalDataProvider.ShowProperty.ASK, true);
            fail();
        } catch (SumZeroException ex) {
            // this should happen
        }

    }

    @Test
    public void testRequestHistoricalData() {
        Ticker ticker = new GenericTicker("ABC");
        int duration = 100;
        int barSize = 1;
        BarData.LengthUnit lengthUnit = BarData.LengthUnit.MINUTE;
        testProvider.client = mockClient;
        List<BitmexChartData> dataList = new ArrayList<>();

        BitmexChartData data = new BitmexChartData();
        data.setOpen(0);
        dataList.add(data);

        List<BarData> barDataList = new ArrayList<>();
        BarData barData = new BarData(ZonedDateTime.now(), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE);
        barDataList.add(barData);

        when(mockClient.getChartData(ticker, duration, BitmexRestClient.ChartDataBinSize.ONE_MINUTE, "", true))
                .thenReturn(dataList);
        doReturn(barDataList).when(testProvider).convertToBarData(ticker, barSize, lengthUnit, dataList);

        assertEquals(barDataList, testProvider.requestHistoricalData(ticker, duration, lengthUnit, barSize, lengthUnit,
                IHistoricalDataProvider.ShowProperty.TRADES, true));
    }

    @Test
    public void testRequestHistoricalData_OverloadedMethod() {
        try {
            testProvider.requestHistoricalData(new GenericTicker("ABC"), new Date(), 2, BarData.LengthUnit.MONTH, 10,
                    BarData.LengthUnit.MINUTE, IHistoricalDataProvider.ShowProperty.TRADES, true);
            fail();
        } catch (SumZeroException ex) {

        }
    }

    @Test
    public void testConvertToBarData() {
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

        List<BitmexChartData> chartDataList = new ArrayList<>();
        chartDataList.add(chartData);

        List<BarData> expectedDataList = new ArrayList<>();
        expectedDataList.add(expectedData);

        List<BarData> actualDataList = testProvider.convertToBarData(ticker, barLength, lengthUnit, chartDataList);

        assertEquals(1, actualDataList.size());
        assertEquals(expectedData, actualDataList.get(0));
    }

}
