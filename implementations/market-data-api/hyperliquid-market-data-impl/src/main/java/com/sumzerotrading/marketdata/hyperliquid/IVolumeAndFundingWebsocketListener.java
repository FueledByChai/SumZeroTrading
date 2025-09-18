package com.sumzerotrading.marketdata.hyperliquid;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.sumzerotrading.data.Ticker;

public interface IVolumeAndFundingWebsocketListener {

    void volumeAndFundingUpdate(Ticker ticker, BigDecimal volume, BigDecimal volumeNotional, BigDecimal fundingRate,
            BigDecimal markPrice, BigDecimal openInterest, ZonedDateTime timestamp);
}
