package com.sumzerotrading.marketdata.paradex;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.paradex.common.api.ParadexWebSocketClient;

public class TradesWebSocketClient {

    protected static final Logger logger = LoggerFactory.getLogger(TradesWebSocketClient.class);
    protected Map<Ticker, ParadexOrderBook> orderBooks = new HashMap<>();
    protected String wsUrl = "wss://ws.api.prod.paradex.trade/v1";
    protected TradesWebSocketProcessor tradesProcessor;

    public void startTradesWSClient(Ticker ticker, TradesUpdateListener tradesListener) {
        try {
            logger.info("Starting trades WebSocket client");
            ParadexWebSocketClient tradesWSClient = new ParadexWebSocketClient(wsUrl, "trades." + ticker.getSymbol(),
                    getTradesProcessor(ticker, tradesListener));
            tradesWSClient.connect();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    protected TradesWebSocketProcessor getTradesProcessor(Ticker ticker, TradesUpdateListener listener) {
        if (tradesProcessor == null) {
            tradesProcessor = new TradesWebSocketProcessor(() -> {
                logger.info("Trades WebSocket closed, trying to restart...");
                startTradesWSClient(ticker, listener);

            });
            tradesProcessor.addTradesUpdateListener(listener);
        }
        return tradesProcessor;
    }
}
