package com.sumzerotrading.hyperliquid.websocket;

import com.sumzerotrading.data.InstrumentDescriptor;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.hyperliquid.websocket.json.SignableExchangeOrderRequest;

public interface IHyperliquidRestApi {

    boolean isPublicApiOnly();

    String placeOrder(SignableExchangeOrderRequest order);

    InstrumentDescriptor getInstrumentDescriptor(String symbol);

    InstrumentDescriptor[] getAllInstrumentsForType(InstrumentType instrumentType);

}