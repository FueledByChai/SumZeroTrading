package com.sumzerotrading.hyperliquid.websocket;

import com.sumzerotrading.data.IInstrumentLookup;
import com.sumzerotrading.data.InstrumentDescriptor;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;

public class HyperliquidInstrumentLookup implements IInstrumentLookup {

    IHyperliquidRestApi api = HyperliquidApiFactory.getPublicApi();

    @Override
    public InstrumentDescriptor lookupByCommonSymbol(String commonSymbol) {
        return lookupByExchangeSymbol(commonSymbol);
    }

    @Override
    public InstrumentDescriptor lookupByExchangeSymbol(String exchangeSymbol) {
        return api.getInstrumentDescriptor(exchangeSymbol);
    }

    @Override
    public InstrumentDescriptor lookupByTicker(Ticker ticker) {
        return lookupByExchangeSymbol(ticker.getSymbol());
    }

    @Override
    public InstrumentDescriptor[] getAllInstrumentsForType(InstrumentType instrumentType) {
        if (instrumentType != InstrumentType.PERPETUAL_FUTURES) {
            throw new IllegalArgumentException("Only perpetual futures are supported at this time.");
        }
        return api.getAllInstrumentsForType(instrumentType);
    }

}
