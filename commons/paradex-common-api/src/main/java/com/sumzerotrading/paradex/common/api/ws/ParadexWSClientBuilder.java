package com.sumzerotrading.paradex.common.api.ws;

import com.sumzerotrading.websocket.IWebSocketProcessor;

public class ParadexWSClientBuilder {

    public static final String WS_TYPE_ORDER_STATUS = "orders.ALL";

    // orderStatusWSClient = new ParadexWebSocketClient(wsUrl, "orders.ALL",
    // orderStatusProcessor, jwtToken);

    public static ParadexWebSocketClient buildOrderStatusClient(String url, IWebSocketProcessor processor,
            String jwtToken) throws Exception {
        ParadexWebSocketClient client = new ParadexWebSocketClient(url, WS_TYPE_ORDER_STATUS, processor, jwtToken);
        return client;
    }

}
