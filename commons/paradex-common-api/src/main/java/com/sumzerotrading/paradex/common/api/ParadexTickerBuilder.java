package com.sumzerotrading.paradex.common.api;

import java.util.HashMap;
import java.util.Map;

import com.sumzerotrading.data.CryptoTicker;
import com.sumzerotrading.data.Exchange;

public class ParadexTickerBuilder {

    protected static Map<String, CryptoTicker> cryptoTickers = new HashMap<>();
    protected static ISystemConfig systemConfig;

    public static CryptoTicker getTicker(String localSymbol) {
        CryptoTicker ticker = cryptoTickers.get(localSymbol);
        if (ticker == null) {
            ticker = new CryptoTicker(systemConfig.getParadexSymbol(), Exchange.PARADEX);
            ticker.setMinimumTickSize(systemConfig.getTickSize())
                    .setOrderSizeIncrement(systemConfig.getOrderSizeIncrement());

            cryptoTickers.put(localSymbol, ticker);
        }
        return ticker;
    }

}
