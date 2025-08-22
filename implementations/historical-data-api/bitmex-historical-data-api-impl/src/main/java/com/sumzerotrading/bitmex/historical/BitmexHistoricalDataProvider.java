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
import com.sumzerotrading.bitmex.client.IBitmexClient;
import com.sumzerotrading.bitmex.common.api.BitmexClientRegistry;
import com.sumzerotrading.bitmex.entity.BitmexChartData;
import com.sumzerotrading.data.BarData;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.historicaldata.IHistoricalDataProvider;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Rob Terpilowski
 */
public class BitmexHistoricalDataProvider implements IHistoricalDataProvider {

    protected Logger logger = Logger.getLogger(BitmexHistoricalDataProvider.class);
    protected boolean connected = false;
    protected IBitmexClient client = null;

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void connect() {
        client = BitmexClientRegistry.getInstance().getBitmexClient();
        connected = true;
    }

    @Override
    public void init(Properties props) {
        throw new SumZeroException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<BarData> requestHistoricalData(Ticker ticker, int duration, BarData.LengthUnit durationLengthUnit, int barSize, BarData.LengthUnit barSizeUnit, ShowProperty whatToShow, boolean useRTH) {
        if (whatToShow != ShowProperty.TRADES) {
            throw new SumZeroException("Only historical trades are supported");
        }
        List<BitmexChartData> bitmexData = client.getChartData(ticker, duration, HistoricalDataUtils.getBinSize(barSize, barSizeUnit), "", true);
        return convertToBarData(ticker, barSize, barSizeUnit, bitmexData);
    }

    @Override
    public List<BarData> requestHistoricalData(Ticker ticker, Date endDateTime, int duration, BarData.LengthUnit durationLengthUnit, int barSize, BarData.LengthUnit barSizeUnit, ShowProperty whatToShow, boolean useRTH) {
        throw new SumZeroException("Not supported");
    }

    
    protected List<BarData> convertToBarData(Ticker ticker, int barlength, BarData.LengthUnit lengthUnit, List<BitmexChartData> chartData) {
        List<BarData> barList = new ArrayList<>();
        for (BitmexChartData data : chartData) {
            barList.add(HistoricalDataUtils.buildBarData(ticker, barlength, lengthUnit, data));
        }
        return barList;
    }

}
