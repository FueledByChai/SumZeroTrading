package com.sumzerotrading.marketdata;

import java.math.BigDecimal;

import com.sumzerotrading.data.Ticker;

public interface OrderBookUpdateListener {

    void bestBidUpdated(Ticker ticker, BigDecimal bestBid);

    void bestAskUpdated(Ticker ticker, BigDecimal bestAsk);
}
