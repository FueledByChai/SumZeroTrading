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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.sumzerotrading.data.BarData;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.paradex.common.api.historical.OHLCBar;

/**
 * Utility class for converting and handling historical data formats.
 */
public class HistoricalDataUtils {

    public static List<BarData> convertToBarData(Ticker ticker, int barSize, BarData.LengthUnit barSizeUnit,
            List<OHLCBar> ohlcBars) {
        List<BarData> barDataList = new ArrayList<>();
        for (OHLCBar ohlc : ohlcBars) {
            BarData bar = new BarData(ticker,
                    java.time.Instant.ofEpochMilli(ohlc.getTime()).atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime(),
                    BigDecimal.valueOf(ohlc.getOpen()), BigDecimal.valueOf(ohlc.getHigh()),
                    BigDecimal.valueOf(ohlc.getLow()), BigDecimal.valueOf(ohlc.getClose()),
                    BigDecimal.valueOf(ohlc.getVolume()), barSize, barSizeUnit);
            barDataList.add(bar);
        }
        return barDataList;
    }

}
