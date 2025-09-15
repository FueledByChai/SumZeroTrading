package com.sumzerotrading.marketdata.hyperliquid;

import java.math.BigDecimal;

import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.OrderBook;

public class HyperliquidOrderBook extends OrderBook {

    public HyperliquidOrderBook(Ticker ticker) {
        super(ticker);
    }

    public HyperliquidOrderBook(Ticker ticker, BigDecimal tickSize) {
        super(ticker, tickSize);
    }

}
