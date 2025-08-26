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

package com.sumzerotrading.historicaldata;

import com.sumzerotrading.data.BarData;
import com.sumzerotrading.data.Ticker;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Defines methods for a historical data provider.
 * 
 * @author Rob Terpilowski
 */
public interface IHistoricalDataProvider {

        public static enum ShowProperty {
                BID, ASK, MIDPOINT, TRADES, MARK_PRICE
        };

        /**
         * Initializes the Data provider with the specified properties
         * 
         * @param props The properties with which to initialize the provider
         */
        public abstract void init(Properties props);

        /**
         * Requests historical data for the specified ticker
         * 
         * @param ticker             The ticker for which to fetch the historical data
         * @param endDateTime        The end time of the historical data query
         * @param duration           The length of lookback time (Specify
         *                           Integer.MAX_VALUE to pull back the max amount of
         *                           historical data possible).
         * @param durationLengthUnit The unit of time for the lookback duration
         * @param barSize            The size of the bars to return
         * @param barSizeUnit        The unit for the bar size
         * @param whatToShow         What to return, bid/ask/mid/last
         * @param useRTH             true if this only return data during "Regular
         *                           Trading Hours" (Not supported by all data
         *                           providers)
         * @return An array of bars containing the historical data* @return
         */
        public abstract List<BarData> requestHistoricalData(Ticker ticker, Date endDateTime, int duration,
                        BarData.LengthUnit durationLengthUnit, int barSize, BarData.LengthUnit barSizeUnit,
                        ShowProperty whatToShow, boolean useRTH) throws IOException;

        /**
         * Requests historical data for the specified ticker with the current time as
         * the end time.
         * 
         * @param ticker             The ticker for which to fetch the historical data
         * @param duration           The length of lookback time (Specify
         *                           Integer.MAX_VALUE to pull back the max amount of
         *                           historical data possible).
         * @param durationLengthUnit The unit of time for the lookback duration
         * @param barSize            The size of the bars to return
         * @param barSizeUnit        The unit for the bar size
         * @param whatToShow         What to return, bid/ask/mid/last
         * @param useRTH             true if this only return data during "Regular
         *                           Trading Hours" (Not supported by all data
         *                           providers)
         * @return An array of bars containing the historical data
         */
        public abstract List<BarData> requestHistoricalData(Ticker ticker, int duration,
                        BarData.LengthUnit durationLengthUnit, int barSize, BarData.LengthUnit barSizeUnit,
                        ShowProperty whatToShow, boolean useRTH);

        /**
         * Checks to see if the application is connected to the historical data provider
         * 
         * @return true if connected to the provider
         */
        public abstract boolean isConnected();

        /**
         * Connects to the historical data provider.
         */
        public abstract void connect();

}
