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

package com.sumzerotrading.ib.historical;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.sumzerotrading.data.BarData;
import com.sumzerotrading.historicaldata.IHistoricalDataProvider.ShowProperty;

/**
 *
 * @author Rob Terpilowski
 */
public class HistoricalDataUtils {

    public static String buildDurationString(int length, BarData.LengthUnit lengthUnit) {
        if (lengthUnit == BarData.LengthUnit.TICK) {
            throw new IllegalStateException("Tick not supported for historical data");
        } else if (lengthUnit == BarData.LengthUnit.SECOND) {
            return length + " S";
        } else if (lengthUnit == BarData.LengthUnit.MINUTE) {
            throw new IllegalStateException("Minute duration not supported for Historical data");
        } else if (lengthUnit == BarData.LengthUnit.HOUR) {
            throw new IllegalStateException("Hour duration not supported for historical data");
        } else if (lengthUnit == BarData.LengthUnit.DAY) {
            return length + " D";
        } else if (lengthUnit == BarData.LengthUnit.WEEK) {
            return length + " W";
        } else if (lengthUnit == BarData.LengthUnit.MONTH) {
            return length + " M";
        } else if (lengthUnit == BarData.LengthUnit.YEAR) {
            return length + " Y";
        } else {
            throw new IllegalStateException("Unknown length: " + lengthUnit);
        }
    }

    public static String buildBarDataSizeString(int size, BarData.LengthUnit sizeUnit) {
        if (sizeUnit == BarData.LengthUnit.SECOND) {
            if (size != 1 && size != 5 && size != 15 && size != 30) {
                throw new InvalidBarSizeException("Valid values for seconds are 1, 5, 15 or 30");
            } else {
                if (size == 1) {
                    return "1 sec";
                } else {
                    return size + " secs";
                }
            }
        } else if (sizeUnit == BarData.LengthUnit.MINUTE) {
            if (size != 1 && size != 2 && size != 5 && size != 15 && size != 30) {
                throw new InvalidBarSizeException("Valid values for minutes are 1, 2, 3, 5, 15, or 30");
            } else {
                if (size == 1) {
                    return "1 min";
                } else {
                    return size + " mins";
                }
            }
        } else if (sizeUnit == BarData.LengthUnit.HOUR) {
            if (size != 1) {
                throw new InvalidBarSizeException("Valid value of hour is 1");
            } else {
                return "1 hour";
            }
        } else if (sizeUnit == BarData.LengthUnit.DAY) {
            if (size != 1) {
                throw new InvalidBarSizeException("Valid value of day is 1");
            } else {
                return "1 day";
            }
        } else {
            throw new InvalidBarSizeException("Invalid bar size unit: " + sizeUnit);
        }
    }

    public static BarData buildBarData(HistoricalData data) {
        BarData bar = new BarData();
        bar.setClose(new BigDecimal(data.getClose()));
        bar.setHigh(new BigDecimal(data.getHigh()));
        bar.setLow(new BigDecimal(data.getLow()));
        bar.setClose(new BigDecimal(data.getClose()));
        bar.setOpen(new BigDecimal(data.getOpen()));
        bar.setVolume(new BigDecimal(data.getVolume()));
        bar.setDateTime(ZonedDateTime.ofInstant(data.getDate().getTime().toInstant(), ZoneId.of("UTC")));

        return bar;
    }

    public static String showPropertyToString(ShowProperty property) {
        if (property == ShowProperty.ASK) {
            return "ASK";
        } else if (property == ShowProperty.BID) {
            return "BID";
        } else if (property == ShowProperty.MIDPOINT) {
            return "MIDPOINT";
        } else if (property == ShowProperty.TRADES) {
            return "TRADES";
        } else {
            throw new IllegalStateException("Unknown ShowProperty: " + property);
        }
    }
}
