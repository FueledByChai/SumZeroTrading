package com.sumzerotrading.hyperliquid.websocket;

import com.sumzerotrading.data.InstrumentDescriptor;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.hyperliquid.websocket.json.PlaceOrderRequest;

public interface IHyperliquidRestApi {

    boolean isPublicApiOnly();

    String placeOrder(PlaceOrderRequest order);

    InstrumentDescriptor getInstrumentDescriptor(String symbol);

    InstrumentDescriptor[] getAllInstrumentsForType(InstrumentType instrumentType);

}