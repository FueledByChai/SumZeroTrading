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
package com.sumzerotrading.paradex.historical;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.data.BarData;
import com.sumzerotrading.data.BarData.LengthUnit;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.historicaldata.IHistoricalDataProvider;
import com.sumzerotrading.paradex.common.api.ParadexRestApi;
import com.sumzerotrading.paradex.common.api.historical.OHLCBar;

/**
 *
 * @author
 */
public class ParadexHistoricalDataProvider implements IHistoricalDataProvider {

    protected Logger logger = LoggerFactory.getLogger(ParadexHistoricalDataProvider.class);
    protected boolean connected = true;
    protected ParadexRestApi paradoxApi;

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void connect() {
        // does nothing
    }

    @Override
    public void init(Properties props) {
        paradoxApi = ParadexRestApi.getPublicOnlyApi(props.getProperty("paradex.rest.url"));
        connected = true;
    }

    @Override
    public List<BarData> requestHistoricalData(Ticker ticker, int duration, BarData.LengthUnit durationLengthUnit,
            int barSize, BarData.LengthUnit barSizeUnit, ShowProperty whatToShow, boolean useRTH) {
        if (!(whatToShow == ShowProperty.TRADES || whatToShow == ShowProperty.MARK_PRICE)) {
            throw new SumZeroException("Only historical trades or mark price are supported");
        }

        if (!(barSizeUnit == LengthUnit.MINUTE || barSizeUnit == LengthUnit.HOUR)) {
            throw new IllegalArgumentException("Only minute and hour bar sizes are supported");
        }

        int resolutionInMinutes = barSize;
        if (barSizeUnit == BarData.LengthUnit.HOUR) {
            resolutionInMinutes *= 60;
        }

        if (resolutionInMinutes != 1 && resolutionInMinutes != 3 && resolutionInMinutes != 5
                && resolutionInMinutes != 15 && resolutionInMinutes != 30 && resolutionInMinutes != 60) {
            throw new IllegalArgumentException("Unsupported resolution: " + resolutionInMinutes
                    + ". Supported resolutions are 1, 3, 5, 15, 30, 60 minutes.");
        }

        if (durationLengthUnit == BarData.LengthUnit.TICK) {
            throw new IllegalArgumentException("Unsupported duration length unit: " + durationLengthUnit
                    + ". Supported units are MINUTE and HOUR.");
        }

        int lookbackInMinutes = getDurationInMinutes(duration, durationLengthUnit);

        List<OHLCBar> bars = paradoxApi.getOHLCBars(ticker.getSymbol(), resolutionInMinutes, lookbackInMinutes,
                whatToShow == ShowProperty.MARK_PRICE ? ParadexRestApi.HistoricalPriceKind.MARK
                        : ParadexRestApi.HistoricalPriceKind.LAST);

        return HistoricalDataUtils.convertToBarData(ticker, barSize, barSizeUnit, bars);

    }

    @Override
    public List<BarData> requestHistoricalData(Ticker ticker, Date endDateTime, int duration,
            BarData.LengthUnit durationLengthUnit, int barSize, BarData.LengthUnit barSizeUnit, ShowProperty whatToShow,
            boolean useRTH) {
        throw new SumZeroException("Not yet implemented");
    }

    protected int getDurationInMinutes(int duration, BarData.LengthUnit lengthUnit) {
        if (lengthUnit == BarData.LengthUnit.MINUTE) {
            return duration;
        } else if (lengthUnit == BarData.LengthUnit.HOUR) {
            return duration * 60;
        } else if (lengthUnit == BarData.LengthUnit.DAY) {
            return duration * 60 * 24;
        } else if (lengthUnit == BarData.LengthUnit.WEEK) {
            return duration * 60 * 24 * 7;
        } else if (lengthUnit == BarData.LengthUnit.MONTH) {
            return duration * 60 * 24 * 30;
        } else if (lengthUnit == BarData.LengthUnit.YEAR) {
            return duration * 60 * 24 * 365;
        }

        throw new IllegalArgumentException("Unsupported length unit: " + lengthUnit);
    }

}
