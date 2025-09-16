package com.sumzerotrading.marketdata.hyperliquid;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.websocket.HyperliquidWebSocketClient;

public class OrderBookRegistry {
    protected static final Logger logger = LoggerFactory.getLogger(OrderBookRegistry.class);
    protected static OrderBookRegistry instance = null;
    protected Map<Ticker, HyperliquidOrderBook> orderBooks = new HashMap<>();
    protected String wsUrl = "wss://ws.api.prod.paradex.trade/v1";

    public static OrderBookRegistry getInstance() {
        if (instance == null) {
            instance = new OrderBookRegistry();
        }
        return instance;
    }

    public HyperliquidOrderBook getOrderBook(Ticker ticker) {
        HyperliquidOrderBook orderBook = orderBooks.get(ticker);
        if (orderBook == null) {
            orderBook = new HyperliquidOrderBook(ticker);
            orderBooks.put(ticker, orderBook);
            startMarketBookWSClient(ticker, orderBook);
        }
        return orderBook;
    }

    public void startMarketBookWSClient(Ticker ticker, HyperliquidOrderBook orderBook) {
        try {
            logger.info("Starting order book WebSocket client");
            HyperliquidWebSocketClient orderBookWSClient = new HyperliquidWebSocketClient(wsUrl, "l2Book",
                    ticker.getSymbol(), new MarketBookWebSocketProcessor(orderBook, () -> {
                        logger.info("Order book WebSocket closed, trying to restart...");
                        startMarketBookWSClient(ticker, orderBook);
                    }));
            orderBookWSClient.connect();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

}
