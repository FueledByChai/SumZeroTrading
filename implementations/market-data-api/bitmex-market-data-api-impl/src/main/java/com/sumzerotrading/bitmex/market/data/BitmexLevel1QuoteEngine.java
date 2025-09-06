/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.market.data;

import com.sumzerotrading.bitmex.client.BitmexRestClient;
import com.sumzerotrading.bitmex.client.BitmexWebsocketClient;
import com.sumzerotrading.bitmex.common.api.BitmexClientRegistry;
import com.sumzerotrading.bitmex.entity.BitmexQuote;
import com.sumzerotrading.bitmex.entity.BitmexTrade;
import com.sumzerotrading.bitmex.listener.IQuoteListener;
import com.sumzerotrading.bitmex.listener.ITradeListener;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.Level1Quote;
import com.sumzerotrading.marketdata.Level1QuoteListener;
import com.sumzerotrading.marketdata.QuoteEngine;
import com.sumzerotrading.marketdata.QuoteType;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.eclipse.jetty.websocket.client.WebSocketClient;

/**
 *
 * @author RobTerpilowski
 */
public class BitmexLevel1QuoteEngine extends QuoteEngine implements IQuoteListener, ITradeListener {

    protected BitmexRestClient restClient;
    protected BitmexWebsocketClient websocketClient;
    protected boolean isStarted = false;
    protected Map<String, Ticker> tickerMap = new HashMap<>();

    @Override
    public void startEngine() {
        websocketClient = BitmexClientRegistry.getInstance().getWebsocketClient();
        isStarted = true;
    }

    @Override
    public void startEngine(Properties props) {
    }

    @Override
    public void stopEngine() {
    }

    @Override
    public Date getServerTime() {
        return new Date();
    }

    @Override
    public boolean started() {
        return isStarted;
    }

    @Override
    public boolean isConnected() {
        return isStarted;
    }

    @Override
    public void useDelayedData(boolean useDelayed) {
    }

    @Override
    public void subscribeLevel1(Ticker ticker, Level1QuoteListener listener) {
        super.subscribeLevel1(ticker, listener);
        tickerMap.put(ticker.getSymbol(), ticker);
        websocketClient.subscribeQuotes(ticker, this);
        websocketClient.subscribeTrades(ticker, this);
    }

    @Override
    public void quoteUpdated(BitmexQuote quoteData) {
        logger.debug("Received from Bitmex: " + quoteData);
        Ticker ticker = tickerMap.get(quoteData.getSymbol());
        Map<QuoteType, BigDecimal> quoteMap = new HashMap<>();
        quoteMap.put(QuoteType.ASK, new BigDecimal(quoteData.getAskPrice()));
        quoteMap.put(QuoteType.BID, new BigDecimal(quoteData.getBidPrice()));
        quoteMap.put(QuoteType.BID_SIZE, new BigDecimal(quoteData.getBidSize()));
        quoteMap.put(QuoteType.ASK_SIZE, new BigDecimal(quoteData.getAskSize()));
        quoteMap.put(QuoteType.MIDPOINT, new BigDecimal((quoteData.getAskPrice() + quoteData.getBidPrice()) / 2.0));
        ZonedDateTime timestamp = getTimestamp(quoteData.getTimestamp());

        Level1Quote quote = new Level1Quote(ticker, timestamp, quoteMap);
        fireLevel1Quote(quote);

    }

    @Override
    public void tradeUpdated(BitmexTrade trade) {
        Ticker ticker = tickerMap.get(trade.getSymbol());
        Map<QuoteType, BigDecimal> quoteMap = new HashMap<>();
        quoteMap.put(QuoteType.LAST, new BigDecimal(trade.getPrice()));
        quoteMap.put(QuoteType.LAST_SIZE, new BigDecimal(trade.getSize()));
        ZonedDateTime timestamp = getTimestamp(trade.getTimestamp());

        Level1Quote quote = new Level1Quote(ticker, timestamp, quoteMap);
        fireLevel1Quote(quote);
    }

    public int getMessageProcessorQueueSize() {
        return websocketClient.getMessageProcessorCount();
    }

    protected ZonedDateTime getTimestamp(String timestamp) {
        return ZonedDateTime.parse(timestamp);
    }

}
