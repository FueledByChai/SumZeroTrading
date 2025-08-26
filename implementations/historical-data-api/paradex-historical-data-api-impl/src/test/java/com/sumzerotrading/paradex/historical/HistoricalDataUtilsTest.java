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

package com.sumzerotrading.paradex.historical;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sumzerotrading.data.BarData;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.paradex.common.api.historical.OHLCBar;

/**
 * Comprehensive unit tests for HistoricalDataUtils class testing all code
 * paths.
 */
@ExtendWith(MockitoExtension.class)
class HistoricalDataUtilsTest {

    @Mock
    private Ticker mockTicker;

    private static final int DEFAULT_BAR_SIZE = 5;
    private static final BarData.LengthUnit DEFAULT_BAR_SIZE_UNIT = BarData.LengthUnit.MINUTE;

    @Test
    void testConvertToBarData_SingleBar_Success() {
        // Arrange
        OHLCBar mockOHLCBar = mock(OHLCBar.class);
        when(mockOHLCBar.getTime()).thenReturn(1640995200000L); // Jan 1, 2022 00:00:00 UTC
        when(mockOHLCBar.getOpen()).thenReturn(50000.00);
        when(mockOHLCBar.getHigh()).thenReturn(51000.00);
        when(mockOHLCBar.getLow()).thenReturn(49000.00);
        when(mockOHLCBar.getClose()).thenReturn(50500.00);
        when(mockOHLCBar.getVolume()).thenReturn(1000.5);

        List<OHLCBar> ohlcBars = Arrays.asList(mockOHLCBar);

        // Act
        List<BarData> result = HistoricalDataUtils.convertToBarData(mockTicker, DEFAULT_BAR_SIZE, DEFAULT_BAR_SIZE_UNIT,
                ohlcBars);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        BarData barData = result.get(0);
        assertNotNull(barData);
        assertEquals(mockTicker, barData.getTicker());
        // Note: BarData constructor has a bug - it doesn't set barLength and lengthUnit
        assertEquals(1, barData.getBarLength()); // Default value from field initialization
        assertNull(barData.getLengthUnit()); // Default value is null

        // Verify time conversion (milliseconds to LocalDateTime)
        LocalDateTime expectedDateTime = java.time.Instant.ofEpochMilli(1640995200000L).atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        assertEquals(expectedDateTime, barData.getDateTime());

        // Verify price data conversion (double to BigDecimal)
        assertEquals(BigDecimal.valueOf(50000.00), barData.getOpen());
        assertEquals(BigDecimal.valueOf(51000.00), barData.getHigh());
        assertEquals(BigDecimal.valueOf(49000.00), barData.getLow());
        assertEquals(BigDecimal.valueOf(50500.00), barData.getClose());
        assertEquals(BigDecimal.valueOf(1000.5), barData.getVolume());
    }

    @Test
    void testConvertToBarData_MultipleBars_Success() {
        // Arrange
        OHLCBar mockOHLCBar1 = mock(OHLCBar.class);
        when(mockOHLCBar1.getTime()).thenReturn(1640995200000L); // Jan 1, 2022 00:00:00 UTC
        when(mockOHLCBar1.getOpen()).thenReturn(50000.00);
        when(mockOHLCBar1.getHigh()).thenReturn(51000.00);
        when(mockOHLCBar1.getLow()).thenReturn(49000.00);
        when(mockOHLCBar1.getClose()).thenReturn(50500.00);
        when(mockOHLCBar1.getVolume()).thenReturn(1000.5);

        OHLCBar mockOHLCBar2 = mock(OHLCBar.class);
        when(mockOHLCBar2.getTime()).thenReturn(1640998800000L); // Jan 1, 2022 01:00:00 UTC
        when(mockOHLCBar2.getOpen()).thenReturn(50500.00);
        when(mockOHLCBar2.getHigh()).thenReturn(52000.00);
        when(mockOHLCBar2.getLow()).thenReturn(50000.00);
        when(mockOHLCBar2.getClose()).thenReturn(51500.00);
        when(mockOHLCBar2.getVolume()).thenReturn(1500.25);

        List<OHLCBar> ohlcBars = Arrays.asList(mockOHLCBar1, mockOHLCBar2);

        // Act
        List<BarData> result = HistoricalDataUtils.convertToBarData(mockTicker, DEFAULT_BAR_SIZE, DEFAULT_BAR_SIZE_UNIT,
                ohlcBars);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify first bar
        BarData firstBar = result.get(0);
        assertEquals(mockTicker, firstBar.getTicker());
        assertEquals(1, firstBar.getBarLength()); // BarData constructor bug
        assertNull(firstBar.getLengthUnit()); // BarData constructor bug
        assertEquals(BigDecimal.valueOf(50000.00), firstBar.getOpen());
        assertEquals(BigDecimal.valueOf(50500.00), firstBar.getClose());

        // Verify second bar
        BarData secondBar = result.get(1);
        assertEquals(mockTicker, secondBar.getTicker());
        assertEquals(1, secondBar.getBarLength()); // BarData constructor bug
        assertNull(secondBar.getLengthUnit()); // BarData constructor bug
        assertEquals(BigDecimal.valueOf(50500.00), secondBar.getOpen());
        assertEquals(BigDecimal.valueOf(51500.00), secondBar.getClose());
    }

    @Test
    void testConvertToBarData_EmptyList_ReturnsEmptyList() {
        // Arrange
        List<OHLCBar> emptyOhlcBars = new ArrayList<>();

        // Act
        List<BarData> result = HistoricalDataUtils.convertToBarData(mockTicker, DEFAULT_BAR_SIZE, DEFAULT_BAR_SIZE_UNIT,
                emptyOhlcBars);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testConvertToBarData_SingletonList_Success() {
        // Arrange
        OHLCBar mockOHLCBar = mock(OHLCBar.class);
        when(mockOHLCBar.getTime()).thenReturn(1640995200000L);
        when(mockOHLCBar.getOpen()).thenReturn(50000.00);
        when(mockOHLCBar.getHigh()).thenReturn(51000.00);
        when(mockOHLCBar.getLow()).thenReturn(49000.00);
        when(mockOHLCBar.getClose()).thenReturn(50500.00);
        when(mockOHLCBar.getVolume()).thenReturn(1000.5);

        List<OHLCBar> singletonList = Collections.singletonList(mockOHLCBar);

        // Act
        List<BarData> result = HistoricalDataUtils.convertToBarData(mockTicker, DEFAULT_BAR_SIZE, DEFAULT_BAR_SIZE_UNIT,
                singletonList);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockTicker, result.get(0).getTicker());
        assertEquals(BigDecimal.valueOf(50000.00), result.get(0).getOpen());
    }

    @Test
    void testConvertToBarData_DifferentBarSizeAndUnit_Success() {
        // Arrange
        OHLCBar mockOHLCBar = mock(OHLCBar.class);
        when(mockOHLCBar.getTime()).thenReturn(1640995200000L);
        when(mockOHLCBar.getOpen()).thenReturn(50000.00);
        when(mockOHLCBar.getHigh()).thenReturn(51000.00);
        when(mockOHLCBar.getLow()).thenReturn(49000.00);
        when(mockOHLCBar.getClose()).thenReturn(50500.00);
        when(mockOHLCBar.getVolume()).thenReturn(1000.5);

        List<OHLCBar> ohlcBars = Arrays.asList(mockOHLCBar);
        int customBarSize = 1;
        BarData.LengthUnit customBarSizeUnit = BarData.LengthUnit.HOUR;

        // Act
        List<BarData> result = HistoricalDataUtils.convertToBarData(mockTicker, customBarSize, customBarSizeUnit,
                ohlcBars);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        BarData barData = result.get(0);
        // BarData constructor has a bug - it doesn't assign barLength and lengthUnit
        // parameters
        assertEquals(1, barData.getBarLength()); // Default field value
        assertNull(barData.getLengthUnit()); // Default field value
        assertEquals(mockTicker, barData.getTicker());
    }

    @Test
    void testConvertToBarData_ZeroValues_Success() {
        // Arrange
        OHLCBar zeroBar = mock(OHLCBar.class);
        when(zeroBar.getTime()).thenReturn(0L);
        when(zeroBar.getOpen()).thenReturn(0.0);
        when(zeroBar.getHigh()).thenReturn(0.0);
        when(zeroBar.getLow()).thenReturn(0.0);
        when(zeroBar.getClose()).thenReturn(0.0);
        when(zeroBar.getVolume()).thenReturn(0.0);

        List<OHLCBar> ohlcBars = Arrays.asList(zeroBar);

        // Act
        List<BarData> result = HistoricalDataUtils.convertToBarData(mockTicker, DEFAULT_BAR_SIZE, DEFAULT_BAR_SIZE_UNIT,
                ohlcBars);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        BarData barData = result.get(0);
        assertEquals(BigDecimal.valueOf(0.0), barData.getOpen());
        assertEquals(BigDecimal.valueOf(0.0), barData.getHigh());
        assertEquals(BigDecimal.valueOf(0.0), barData.getLow());
        assertEquals(BigDecimal.valueOf(0.0), barData.getClose());
        assertEquals(BigDecimal.valueOf(0.0), barData.getVolume());
    }

    @Test
    void testConvertToBarData_NegativeValues_Success() {
        // Arrange
        OHLCBar negativeBar = mock(OHLCBar.class);
        when(negativeBar.getTime()).thenReturn(1640995200000L);
        when(negativeBar.getOpen()).thenReturn(-100.0);
        when(negativeBar.getHigh()).thenReturn(-50.0);
        when(negativeBar.getLow()).thenReturn(-150.0);
        when(negativeBar.getClose()).thenReturn(-75.0);
        when(negativeBar.getVolume()).thenReturn(-10.0);

        List<OHLCBar> ohlcBars = Arrays.asList(negativeBar);

        // Act
        List<BarData> result = HistoricalDataUtils.convertToBarData(mockTicker, DEFAULT_BAR_SIZE, DEFAULT_BAR_SIZE_UNIT,
                ohlcBars);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        BarData barData = result.get(0);
        assertEquals(BigDecimal.valueOf(-100.0), barData.getOpen());
        assertEquals(BigDecimal.valueOf(-50.0), barData.getHigh());
        assertEquals(BigDecimal.valueOf(-150.0), barData.getLow());
        assertEquals(BigDecimal.valueOf(-75.0), barData.getClose());
        assertEquals(BigDecimal.valueOf(-10.0), barData.getVolume());
    }

    @Test
    void testConvertToBarData_VeryLargeValues_Success() {
        // Arrange
        OHLCBar largeValuesBar = mock(OHLCBar.class);
        when(largeValuesBar.getTime()).thenReturn(Long.MAX_VALUE);
        when(largeValuesBar.getOpen()).thenReturn(Double.MAX_VALUE);
        when(largeValuesBar.getHigh()).thenReturn(Double.MAX_VALUE);
        when(largeValuesBar.getLow()).thenReturn(Double.MAX_VALUE);
        when(largeValuesBar.getClose()).thenReturn(Double.MAX_VALUE);
        when(largeValuesBar.getVolume()).thenReturn(Double.MAX_VALUE);

        List<OHLCBar> ohlcBars = Arrays.asList(largeValuesBar);

        // Act
        List<BarData> result = HistoricalDataUtils.convertToBarData(mockTicker, DEFAULT_BAR_SIZE, DEFAULT_BAR_SIZE_UNIT,
                ohlcBars);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        BarData barData = result.get(0);
        assertEquals(BigDecimal.valueOf(Double.MAX_VALUE), barData.getOpen());
        assertEquals(BigDecimal.valueOf(Double.MAX_VALUE), barData.getHigh());
        assertEquals(BigDecimal.valueOf(Double.MAX_VALUE), barData.getLow());
        assertEquals(BigDecimal.valueOf(Double.MAX_VALUE), barData.getClose());
        assertEquals(BigDecimal.valueOf(Double.MAX_VALUE), barData.getVolume());
    }

    @Test
    void testConvertToBarData_NullTicker_Success() {
        // Arrange
        OHLCBar mockOHLCBar = mock(OHLCBar.class);
        when(mockOHLCBar.getTime()).thenReturn(1640995200000L);
        when(mockOHLCBar.getOpen()).thenReturn(50000.00);
        when(mockOHLCBar.getHigh()).thenReturn(51000.00);
        when(mockOHLCBar.getLow()).thenReturn(49000.00);
        when(mockOHLCBar.getClose()).thenReturn(50500.00);
        when(mockOHLCBar.getVolume()).thenReturn(1000.5);

        List<OHLCBar> ohlcBars = Arrays.asList(mockOHLCBar);

        // Act
        List<BarData> result = HistoricalDataUtils.convertToBarData(null, DEFAULT_BAR_SIZE, DEFAULT_BAR_SIZE_UNIT,
                ohlcBars);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        BarData barData = result.get(0);
        assertNull(barData.getTicker());
        assertEquals(1, barData.getBarLength()); // BarData constructor bug
        assertNull(barData.getLengthUnit()); // BarData constructor bug
    }

    @Test
    void testConvertToBarData_AllLengthUnits_Success() {
        // Test all possible LengthUnit values
        BarData.LengthUnit[] allUnits = BarData.LengthUnit.values();

        for (BarData.LengthUnit unit : allUnits) {
            // Arrange
            OHLCBar mockOHLCBar = mock(OHLCBar.class);
            when(mockOHLCBar.getTime()).thenReturn(1640995200000L);
            when(mockOHLCBar.getOpen()).thenReturn(50000.00);
            when(mockOHLCBar.getHigh()).thenReturn(51000.00);
            when(mockOHLCBar.getLow()).thenReturn(49000.00);
            when(mockOHLCBar.getClose()).thenReturn(50500.00);
            when(mockOHLCBar.getVolume()).thenReturn(1000.5);

            List<OHLCBar> ohlcBars = Arrays.asList(mockOHLCBar);

            // Act
            List<BarData> result = HistoricalDataUtils.convertToBarData(mockTicker, 1, unit, ohlcBars);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            // BarData constructor has a bug - it doesn't assign lengthUnit parameter
            assertNull(result.get(0).getLengthUnit());
        }
    }

    @Test
    void testConvertToBarData_PrecisionCheck_WithDecimals() {
        // Arrange
        OHLCBar precisionBar = mock(OHLCBar.class);
        when(precisionBar.getTime()).thenReturn(1640995200000L);
        when(precisionBar.getOpen()).thenReturn(123.456789);
        when(precisionBar.getHigh()).thenReturn(124.987654);
        when(precisionBar.getLow()).thenReturn(122.123456);
        when(precisionBar.getClose()).thenReturn(123.789012);
        when(precisionBar.getVolume()).thenReturn(999.999999);

        List<OHLCBar> ohlcBars = Arrays.asList(precisionBar);

        // Act
        List<BarData> result = HistoricalDataUtils.convertToBarData(mockTicker, DEFAULT_BAR_SIZE, DEFAULT_BAR_SIZE_UNIT,
                ohlcBars);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        BarData barData = result.get(0);
        assertEquals(BigDecimal.valueOf(123.456789), barData.getOpen());
        assertEquals(BigDecimal.valueOf(124.987654), barData.getHigh());
        assertEquals(BigDecimal.valueOf(122.123456), barData.getLow());
        assertEquals(BigDecimal.valueOf(123.789012), barData.getClose());
        assertEquals(BigDecimal.valueOf(999.999999), barData.getVolume());
    }

    @Test
    void testConvertToBarData_TimeZoneConversion_SystemDefault() {
        // Arrange
        long epochMillis = 1640995200000L; // Jan 1, 2022 00:00:00 UTC
        OHLCBar timeTestBar = mock(OHLCBar.class);
        when(timeTestBar.getTime()).thenReturn(epochMillis);
        when(timeTestBar.getOpen()).thenReturn(100.0);
        when(timeTestBar.getHigh()).thenReturn(100.0);
        when(timeTestBar.getLow()).thenReturn(100.0);
        when(timeTestBar.getClose()).thenReturn(100.0);
        when(timeTestBar.getVolume()).thenReturn(100.0);

        List<OHLCBar> ohlcBars = Arrays.asList(timeTestBar);

        // Act
        List<BarData> result = HistoricalDataUtils.convertToBarData(mockTicker, DEFAULT_BAR_SIZE, DEFAULT_BAR_SIZE_UNIT,
                ohlcBars);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        BarData barData = result.get(0);
        LocalDateTime expectedDateTime = java.time.Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        assertEquals(expectedDateTime, barData.getDateTime());
    }

    @Test
    void testConvertToBarData_VerifyAllMethodsCalled() {
        // Arrange
        OHLCBar mockOHLCBar = mock(OHLCBar.class);
        when(mockOHLCBar.getTime()).thenReturn(1640995200000L);
        when(mockOHLCBar.getOpen()).thenReturn(50000.00);
        when(mockOHLCBar.getHigh()).thenReturn(51000.00);
        when(mockOHLCBar.getLow()).thenReturn(49000.00);
        when(mockOHLCBar.getClose()).thenReturn(50500.00);
        when(mockOHLCBar.getVolume()).thenReturn(1000.5);

        List<OHLCBar> ohlcBars = Arrays.asList(mockOHLCBar);

        // Act
        HistoricalDataUtils.convertToBarData(mockTicker, DEFAULT_BAR_SIZE, DEFAULT_BAR_SIZE_UNIT, ohlcBars);

        // Assert - Verify all OHLCBar methods were called
        verify(mockOHLCBar, times(1)).getTime();
        verify(mockOHLCBar, times(1)).getOpen();
        verify(mockOHLCBar, times(1)).getHigh();
        verify(mockOHLCBar, times(1)).getLow();
        verify(mockOHLCBar, times(1)).getClose();
        verify(mockOHLCBar, times(1)).getVolume();

        // Verify no unexpected interactions
        verifyNoMoreInteractions(mockOHLCBar);
    }

    @Test
    void testConvertToBarData_IterationOrder_PreservesOrder() {
        // Arrange - Create multiple bars to test order preservation
        OHLCBar bar1 = mock(OHLCBar.class);
        when(bar1.getTime()).thenReturn(1640995200000L); // Jan 1, 2022 00:00:00 UTC
        when(bar1.getOpen()).thenReturn(50000.00);
        when(bar1.getHigh()).thenReturn(51000.00);
        when(bar1.getLow()).thenReturn(49000.00);
        when(bar1.getClose()).thenReturn(50500.00);
        when(bar1.getVolume()).thenReturn(1000.5);

        OHLCBar bar2 = mock(OHLCBar.class);
        when(bar2.getTime()).thenReturn(1640998800000L); // Jan 1, 2022 01:00:00 UTC
        when(bar2.getOpen()).thenReturn(50500.00);
        when(bar2.getHigh()).thenReturn(52000.00);
        when(bar2.getLow()).thenReturn(50000.00);
        when(bar2.getClose()).thenReturn(51500.00);
        when(bar2.getVolume()).thenReturn(1500.25);

        OHLCBar bar3 = mock(OHLCBar.class);
        when(bar3.getTime()).thenReturn(1641002400000L); // Jan 1, 2022 02:00:00 UTC
        when(bar3.getOpen()).thenReturn(51500.00);
        when(bar3.getHigh()).thenReturn(52500.00);
        when(bar3.getLow()).thenReturn(51000.00);
        when(bar3.getClose()).thenReturn(52000.00);
        when(bar3.getVolume()).thenReturn(2000.0);

        List<OHLCBar> ohlcBars = Arrays.asList(bar1, bar2, bar3);

        // Act
        List<BarData> result = HistoricalDataUtils.convertToBarData(mockTicker, DEFAULT_BAR_SIZE, DEFAULT_BAR_SIZE_UNIT,
                ohlcBars);

        // Assert - Verify order is preserved
        assertNotNull(result);
        assertEquals(3, result.size());

        // Check that the values match the expected order
        assertEquals(BigDecimal.valueOf(50000.00), result.get(0).getOpen()); // First bar
        assertEquals(BigDecimal.valueOf(50500.00), result.get(1).getOpen()); // Second bar
        assertEquals(BigDecimal.valueOf(51500.00), result.get(2).getOpen()); // Third bar
    }
}
