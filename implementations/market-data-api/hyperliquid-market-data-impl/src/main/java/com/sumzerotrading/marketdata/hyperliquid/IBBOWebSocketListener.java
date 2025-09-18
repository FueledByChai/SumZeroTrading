package com.sumzerotrading.marketdata.hyperliquid;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.sumzerotrading.data.Ticker;

public interface IBBOWebSocketListener {

    void onBBOUpdate(Ticker ticker, BigDecimal bidPrice, Double bidSize, BigDecimal askPrice, Double askSize,
            ZonedDateTime timestamp);
}
