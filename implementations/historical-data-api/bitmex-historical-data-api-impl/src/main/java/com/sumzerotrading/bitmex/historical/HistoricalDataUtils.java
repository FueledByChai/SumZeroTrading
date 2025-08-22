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

package com.sumzerotrading.bitmex.historical;

import com.sumzerotrading.bitmex.client.BitmexRestClient;
import com.sumzerotrading.bitmex.entity.BitmexChartData;
import com.sumzerotrading.data.BarData;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.historicaldata.IHistoricalDataProvider.ShowProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 *
 * @author Rob Terpilowski
 */
public class HistoricalDataUtils {
    
    
    public static BitmexRestClient.ChartDataBinSize getBinSize( int length, BarData.LengthUnit lengthUnit ) {
        if( lengthUnit == BarData.LengthUnit.DAY ) {
            if( length == 1 ) {
                return BitmexRestClient.ChartDataBinSize.ONE_DAY;
            }
        } else if( lengthUnit == BarData.LengthUnit.HOUR ) {
            if( length == 1 ) {
                return BitmexRestClient.ChartDataBinSize.ONE_HOUR;
            }
        } else if( lengthUnit == BarData.LengthUnit.MINUTE ) {
            if( length == 5 ) {
                return BitmexRestClient.ChartDataBinSize.FIVE_MINUTES;
            } else if( length == 1 ) {
                return BitmexRestClient.ChartDataBinSize.ONE_MINUTE;
            }
        }
        
        throw new InvalidBarSizeException("Only bar size 1D, 1H, 5M, and 1M bars are supported" );
    }
    
    
    
    public static BarData buildBarData( Ticker ticker, int barlength, BarData.LengthUnit lengthUnit, BitmexChartData data ) {
          ZoneId localZone = ZoneId.systemDefault();
            ZonedDateTime localZonedTime = data.getTimestamp().withZoneSameInstant(localZone);
            LocalDateTime localTime = localZonedTime.toLocalDateTime();
            
            
            BarData bar = new BarData(ticker,
                    localTime,
                    new BigDecimal(data.getOpen()),
                    new BigDecimal(data.getHigh()),
                    new BigDecimal(data.getLow()),
                    new BigDecimal(data.getClose()),
                    new BigDecimal(data.getVolume()),
                    barlength,
                    lengthUnit);
        
        return bar;
    }
    
    
}
