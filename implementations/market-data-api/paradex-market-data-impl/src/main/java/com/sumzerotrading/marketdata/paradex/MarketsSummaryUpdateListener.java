package com.sumzerotrading.marketdata.paradex;

public interface MarketsSummaryUpdateListener {

    void newSummaryUpdate(long createdAtTimestamp, String symbol, String bid, String ask, String last,
            String mark_price, String openInterest, String volume24h, String underlyingPrice, String fundingRate);
}
