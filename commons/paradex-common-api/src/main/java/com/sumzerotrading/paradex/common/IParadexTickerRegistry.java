package com.sumzerotrading.paradex.common;

import com.sumzerotrading.data.Ticker;

public interface IParadexTickerRegistry {

    Ticker lookupByBrokerSymbol(String tickerString);

    Ticker lookupByCommonSymbol(String commonSymbol);

}