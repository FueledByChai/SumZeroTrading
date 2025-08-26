package com.sumzerotrading.paradex.common;

import java.util.HashMap;
import java.util.Map;

import com.sumzerotrading.data.CryptoTicker;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.paradex.common.api.ParadexRestApi;

public class TickerRegistry {

    protected static Map<String, Ticker> tickerMap = new HashMap<>();
    protected static ParadexRestApi restApi = new ParadexRestApi("foo");

    public static Ticker lookup(String tickerString) {
        return null;
        // return tickerMap.computeIfAbsent(tickerString, CryptoTicker::new);
    }

}
