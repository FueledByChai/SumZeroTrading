package com.sumzerotrading.paradex.common.api;

import com.sumzerotrading.data.IInstrumentLookup;
import com.sumzerotrading.data.InstrumentDescriptor;
import com.sumzerotrading.data.Ticker;

public class ParadexInstrumentLookup implements IInstrumentLookup {

    IParadexRestApi api = ParadexApiFactory.getPublicApi();

    @Override
    public InstrumentDescriptor lookupByCommonSymbol(String commonSymbol) {
        return lookupByExchangeSymbol(ParadexUtil.commonSymbolToParadexSymbol(commonSymbol));
    }

    @Override
    public InstrumentDescriptor lookupByExchangeSymbol(String exchangeSymbol) {
        return api.getInstrumentDescriptor(exchangeSymbol);
    }

    @Override
    public InstrumentDescriptor lookupByTicker(Ticker ticker) {
        return lookupByExchangeSymbol(ticker.getSymbol());
    }
}
