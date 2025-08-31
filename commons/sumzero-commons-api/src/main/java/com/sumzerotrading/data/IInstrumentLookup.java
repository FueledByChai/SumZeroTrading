package com.sumzerotrading.data;

public interface IInstrumentLookup {

    public InstrumentDescriptor lookupByCommonSymbol(String commonSymbol);

    public InstrumentDescriptor lookupByExchangeSymbol(String exchangeSymbol);

    public InstrumentDescriptor lookupByTicker(Ticker ticker);

    public InstrumentDescriptor[] getAllInstrumentsForType(InstrumentType type);

}
