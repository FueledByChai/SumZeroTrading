package com.sumzerotrading.util;

import com.sumzerotrading.data.Ticker;

public interface ITickerRegistry {

    Ticker lookupByBrokerSymbol(String tickerString);

    Ticker lookupByCommonSymbol(String commonSymbol);

}