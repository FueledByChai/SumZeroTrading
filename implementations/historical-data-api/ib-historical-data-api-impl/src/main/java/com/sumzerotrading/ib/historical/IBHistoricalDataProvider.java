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

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.sumzerotrading.data.BarData;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.historicaldata.IHistoricalDataProvider;
import com.sumzerotrading.ib.BaseIBConnectionDelegate;
import com.sumzerotrading.ib.ContractBuilderFactory;
import com.sumzerotrading.ib.IBConnectionInterface;
import com.sumzerotrading.ib.IBSocket;
import com.sumzerotrading.ib.IbUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Rob Terpilowski
 */
public class IBHistoricalDataProvider extends BaseIBConnectionDelegate implements IHistoricalDataProvider {

    protected Logger logger = LoggerFactory.getLogger(IBHistoricalDataProvider.class);
    protected EClientSocket ibConnection;
    protected IBConnectionInterface callbackInterface;
    protected IBSocket ibSocket;
    protected BlockingQueue<List<BarData>> dataQueue = new SynchronousQueue<List<BarData>>();
    protected static int requestId = 1;
    protected Map<Integer, IBHistoricalDataEventProcessor> historicalProcessorMap = new HashMap<Integer, IBHistoricalDataEventProcessor>();
    // protected Logger logger = Logger.getLogger(IBHistoricalDataProvider.class);
    protected SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    public IBHistoricalDataProvider(IBSocket ibSocket) {
        this.ibSocket = ibSocket;
        this.ibConnection = ibSocket.getClientSocket();
        this.callbackInterface = ibSocket.getConnection();
        callbackInterface.addIbConnectionDelegate(this);
    }

    @Override
    public boolean isConnected() {
        return ibSocket.isConnected();
    }

    @Override
    public void connect() {
        logger.info("Connecting to Interactive Brokers for Historical Data");
        ibSocket.connect();
    }

    public void init(Properties props) {
        // do nothing
    }

    @Override
    public List<BarData> requestHistoricalData(Ticker ticker, int duration, BarData.LengthUnit durationLengthUnit,
            int barSize, BarData.LengthUnit barSizeUnit, ShowProperty whatToShow, boolean useRTH) {
        return this.requestHistoricalData(ticker, null, duration, durationLengthUnit, barSize, barSizeUnit, whatToShow,
                useRTH);
    }

    @Override
    public List<BarData> requestHistoricalData(Ticker ticker, Date endDateTime, int duration,
            BarData.LengthUnit durationLengthUnit, int barSize, BarData.LengthUnit barSizeUnit, ShowProperty whatToShow,
            boolean useRTH) {
        int id = requestId++;
        Contract contract = ContractBuilderFactory.getContractBuilder(ticker).buildContract(ticker);
        String durationString = HistoricalDataUtils.buildDurationString(duration, durationLengthUnit);
        String barSizeString = HistoricalDataUtils.buildBarDataSizeString(barSize, barSizeUnit);
        String whatToShowString = HistoricalDataUtils.showPropertyToString(whatToShow);
        IBHistoricalDataEventProcessor processor = new IBHistoricalDataEventProcessor(requestId, barSizeUnit);
        int rth = 1;
        if (useRTH == false) {
            rth = 0;
        }
        historicalProcessorMap.put(id, processor);
        if (endDateTime == null) {
            endDateTime = new Date();
        }
        String endDate = dateFormatter.format(endDateTime);

        // ibConnection.reqHistoricalData(id, contract, new Date(), null, null, null,
        // barSize, duration);
        // ibConnection.reqHistoricalData(id, contract, endDate, durationString,
        // barSizeString, whatToShowString, rth, 1, IbUtils.getDefaultTagVector());
        IbUtils.throwUnsupportedException();
        List<BarData> bars = processor.getHistoricalData();
        historicalProcessorMap.remove(id);
        return bars;
    }

    public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume,
            int count, double WAP, boolean hasGaps) {
        IBHistoricalDataEventProcessor processor = historicalProcessorMap.get(reqId);
        if (date != null && date.indexOf("finished") != -1) {
            if (processor != null) {
                processor.finished();
                return;
            }
        }
        GregorianCalendar calendarDate = new GregorianCalendar();
        try {
            if (processor != null) {
                Date tempDate = processor.getDateFormatter().parse(date);
                calendarDate.setTime(tempDate);
            } else {
                logger.error("Unable to find Historical Data Processor for requestId: " + reqId);
            }
        } catch (ParseException ex) {
            throw new IllegalStateException(ex);
        }
        if (processor != null) {
            processor.addHistoricalData(
                    new HistoricalData(reqId, calendarDate, open, high, low, close, volume, count, WAP, hasGaps));
        } else {
            logger.error("Unable to find Historical Data Processor for requestId: " + reqId);
        }
    }
}
