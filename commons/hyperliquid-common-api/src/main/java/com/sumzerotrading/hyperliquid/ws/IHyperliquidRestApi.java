package com.sumzerotrading.hyperliquid.ws;

import com.sumzerotrading.data.InstrumentDescriptor;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.hyperliquid.ws.json.SignableExchangeOrderRequest;

public interface IHyperliquidRestApi {

    boolean isPublicApiOnly();

    String placeOrder(SignableExchangeOrderRequest order);

    InstrumentDescriptor getInstrumentDescriptor(String symbol);

    InstrumentDescriptor[] getAllInstrumentsForType(InstrumentType instrumentType);

}