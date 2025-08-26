package com.sumzerotrading.paradex.common;

import java.util.HashMap;
import java.util.Map;

import com.sumzerotrading.data.CryptoTicker;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.paradex.common.api.ParadexRestApi;

public class ParadexTickerRegistry {

    protected static Map<String, Ticker> tickerMap = new HashMap<>();
    protected static ParadexRestApi restApi = new ParadexRestApi("foo");

    public CryptoTicker lookupByBrokerSymbol(String tickerString) {
        return null;
        // return tickerMap.computeIfAbsent(tickerString, CryptoTicker::new);
    }

    public CryptoTicker lookupByCommonSymbol(String commonSymbol) {
        return null;
    }

}
