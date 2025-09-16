package com.sumzerotrading.hyperliquid.websocket;

import com.sumzerotrading.data.InstrumentDescriptor;
import com.sumzerotrading.data.InstrumentType;

public interface IHyperliquidRestApi {

    boolean isPublicApiOnly();

    InstrumentDescriptor getInstrumentDescriptor(String symbol);

    InstrumentDescriptor[] getAllInstrumentsForType(InstrumentType instrumentType);

}