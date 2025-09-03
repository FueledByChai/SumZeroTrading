package com.sumzerotrading.marketdata.paradex;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.paradex.common.api.ParadexWebSocketClient;

public class MarketsSummaryWebSocketClient {

    protected static final Logger logger = LoggerFactory.getLogger(MarketsSummaryWebSocketClient.class);
    protected Map<Ticker, ParadexOrderBook> orderBooks = new HashMap<>();
    protected String wsUrl = "wss://ws.api.prod.paradex.trade/v1";
    protected MarketsSummaryWebSocketProcessor marketsSummaryProcessor;

    public void startMarketsSummaryWSClient(Ticker ticker, MarketsSummaryUpdateListener marketsSummaryListener) {
        try {
            logger.info("Starting markets summary WebSocket client");
            ParadexWebSocketClient marketsSummaryWSClient = new ParadexWebSocketClient(wsUrl,
                    "markets_summary." + ticker.getSymbol(),
                    getMarketsSummaryProcessor(ticker, marketsSummaryListener));
            marketsSummaryWSClient.connect();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    protected MarketsSummaryWebSocketProcessor getMarketsSummaryProcessor(Ticker ticker,
            MarketsSummaryUpdateListener listener) {
        if (marketsSummaryProcessor == null) {
            marketsSummaryProcessor = new MarketsSummaryWebSocketProcessor(() -> {
                logger.info("Markets Summary WebSocket closed, trying to restart...");
                startMarketsSummaryWSClient(ticker, listener);

            });
            marketsSummaryProcessor.addMarketsSummaryUpdateListener(listener);
        }
        return marketsSummaryProcessor;
    }
}
