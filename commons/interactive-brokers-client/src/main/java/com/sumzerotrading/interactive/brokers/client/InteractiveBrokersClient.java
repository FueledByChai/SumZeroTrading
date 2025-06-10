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

package com.sumzerotrading.interactive.brokers.client;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.broker.BrokerErrorListener;
import com.sumzerotrading.broker.IBroker;
import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.ib.InteractiveBrokersBroker;
import com.sumzerotrading.broker.order.OrderEventListener;
import com.sumzerotrading.broker.order.TradeOrder;
import com.sumzerotrading.data.BarData;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.historicaldata.IHistoricalDataProvider;
import com.sumzerotrading.ib.IBConnectionUtil;
import com.sumzerotrading.ib.IBSocket;
import com.sumzerotrading.ib.historical.IBHistoricalDataProvider;
import com.sumzerotrading.marketdata.Level1QuoteListener;
import com.sumzerotrading.marketdata.Level2QuoteListener;
import com.sumzerotrading.marketdata.QuoteEngine;
import com.sumzerotrading.marketdata.ib.IBQuoteEngine;
import com.sumzerotrading.realtime.bar.IRealtimeBarEngine;
import com.sumzerotrading.realtime.bar.RealtimeBarListener;
import com.sumzerotrading.realtime.bar.RealtimeBarRequest;
import com.sumzerotrading.realtime.bar.ib.IBRealTimeBarEngine;

/**
 *
 * @author RobTerpilowski
 */
public class InteractiveBrokersClient implements InteractiveBrokersClientInterface {

    protected static Logger logger = LoggerFactory.getLogger(InteractiveBrokersClient.class);
    protected String host;
    protected int port;
    protected int clientId;
    protected IBSocket ibSocket;
    protected QuoteEngine quoteEngine;
    protected IBroker broker;
    protected IHistoricalDataProvider historicalDataProvider;
    protected IRealtimeBarEngine realtimeBarProvider;
    protected static Map<String, InteractiveBrokersClientInterface> ibClientMap = new HashMap<>();

    public static InteractiveBrokersClientInterface getInstance(String host, int port, int clientId) {
        StringBuilder sb = new StringBuilder();
        sb.append(host).append("-").append(port).append("-").append(clientId);

        InteractiveBrokersClientInterface client = ibClientMap.get(sb.toString());
        if (client == null) {
            client = new InteractiveBrokersClient(host, port, clientId);
            ibClientMap.put(sb.toString(), client);
        }

        return client;

    }

    protected InteractiveBrokersClient(String host, int port, int clientId) {
        logger.info("Creating InteractiveBrokersClient for host: {}, port: {}, clientId: {}", host, port, clientId);
        this.host = host;
        this.port = port;
        this.clientId = clientId;
        IBConnectionUtil util = new IBConnectionUtil(host, port, clientId);
        ibSocket = util.getIBSocket();
        quoteEngine = new IBQuoteEngine(ibSocket);
        broker = new InteractiveBrokersBroker(ibSocket);
        historicalDataProvider = new IBHistoricalDataProvider(ibSocket);
        realtimeBarProvider = new IBRealTimeBarEngine(quoteEngine, historicalDataProvider);
    }

    @Override
    public void connect() {
        logger.info("connecting");
        ibSocket.connect();
        broker.connect();
        historicalDataProvider.connect();
        quoteEngine.startEngine();
    }

    @Override
    public void disconnect() {
        broker.disconnect();
        quoteEngine.stopEngine();
    }

    @Override
    public void subscribeLevel1(Ticker ticker, Level1QuoteListener listener) {
        quoteEngine.subscribeLevel1(ticker, listener);
    }

    @Override
    public void subscribeMarketDepth(Ticker ticker, Level2QuoteListener listener) {
        quoteEngine.subscribeMarketDepth(ticker, listener);
    }

    @Override
    public void unsubscribeLevel1(Ticker ticker, Level1QuoteListener listener) {
        quoteEngine.unsubscribeLevel1(ticker, listener);
    }

    @Override
    public void unsubscribeMarketDepth(Ticker ticker, Level2QuoteListener listener) {
        quoteEngine.unsubscribeMarketDepth(ticker, listener);
    }

    @Override
    public void addBrokerErrorListener(BrokerErrorListener listener) {
        broker.addBrokerErrorListener(listener);
    }

    @Override
    public void removeBrokerErrorListener(BrokerErrorListener listener) {
        broker.removeBrokerErrorListener(listener);
    }

    @Override
    public void useDelayedData(boolean b) {
        quoteEngine.useDelayedData(b);
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public int getClientId() {
        return clientId;
    }

    @Override
    public void placeOrder(TradeOrder order) {
        broker.placeOrder(order);
    }

    @Override
    public void addOrderStatusListener(OrderEventListener listener) {
        broker.addOrderEventListener(listener);
    }

    @Override
    public String getNextOrderId() {
        return broker.getNextOrderId();
    }

    @Override
    public List<TradeOrder> getOpenOrders() {
        return broker.getOpenOrders();
    }

    @Override
    public List<BarData> requestHistoricalData(Ticker ticker, Date endDateTime, int duration,
            BarData.LengthUnit durationLengthUnit, int barSize, BarData.LengthUnit barSizeUnit,
            IHistoricalDataProvider.ShowProperty whatToShow, boolean useRTH) throws IOException {
        return historicalDataProvider.requestHistoricalData(ticker, endDateTime, duration, durationLengthUnit, barSize,
                barSizeUnit, whatToShow, useRTH);
    }

    @Override
    public List<BarData> requestHistoricalData(Ticker ticker, int duration, BarData.LengthUnit lengthUnit, int barSize,
            BarData.LengthUnit barSizeUnit, IHistoricalDataProvider.ShowProperty showProperty) {
        return historicalDataProvider.requestHistoricalData(ticker, duration, lengthUnit, barSize, barSizeUnit,
                showProperty, true);
    }

    @Override
    public List<Position> getOpenPositions() {
        return broker.getAllPositions();
    }

    @Override
    public void subscribeRealtimeBar(RealtimeBarRequest request, RealtimeBarListener listener) {
        realtimeBarProvider.subscribeRealtimeBars(request, listener);
    }

    public static void setMockInteractiveBrokersClient(InteractiveBrokersClientInterface mockClient, String host,
            int port, int clientId) {
        StringBuffer sb = new StringBuffer();
        sb.append(host).append("-").append(port).append("-").append(clientId);
        ibClientMap.put(sb.toString(), mockClient);
    }

}
