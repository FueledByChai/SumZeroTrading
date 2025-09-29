package com.sumzerotrading.hyperliquid.ws;

import com.sumzerotrading.hyperliquid.ws.json.OrderAction;
import com.sumzerotrading.hyperliquid.ws.json.ws.SubmitPostResponse;

public interface IHyperliquidWebsocketApi {

    SubmitPostResponse submitOrders(OrderAction orderAction);

    void connect();

}