package com.sumzerotrading.paradex.common.api;

import java.util.HashMap;
import java.util.Map;

import com.sumzerotrading.data.Exchange;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;

public class ParadexTickerBuilder {

    protected static Map<String, Ticker> cryptoTickers = new HashMap<>();
    protected static ISystemConfig systemConfig;

    public static Ticker getTicker(String localSymbol) {
        Ticker ticker = cryptoTickers.get(localSymbol);
        if (ticker == null) {
            ticker = new Ticker(systemConfig.getParadexSymbol()).setExchange(Exchange.PARADEX)
                    .setInstrumentType(InstrumentType.PERPETUAL_FUTURES).setMinimumTickSize(systemConfig.getTickSize())
                    .setOrderSizeIncrement(systemConfig.getOrderSizeIncrement());

            cryptoTickers.put(localSymbol, ticker);
        }
        return ticker;
    }

}
