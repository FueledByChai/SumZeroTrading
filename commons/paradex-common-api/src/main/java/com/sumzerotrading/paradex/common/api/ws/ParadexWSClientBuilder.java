package com.sumzerotrading.paradex.common.api.ws;

import com.sumzerotrading.websocket.IWebSocketProcessor;

public class ParadexWSClientBuilder {

    public static final String WS_TYPE_ORDER_STATUS = "orders.ALL";
    public static final String WS_TYPE_FILLS = "fills.ALL";
    public static final String WS_TYPE_ACCOUNT_INFO = "account";

    public static ParadexWebSocketClient buildOrderStatusClient(String url, IWebSocketProcessor processor,
            String jwtToken) throws Exception {
        ParadexWebSocketClient client = new ParadexWebSocketClient(url, WS_TYPE_ORDER_STATUS, processor, jwtToken);
        return client;
    }

    public static ParadexWebSocketClient buildFillsClient(String url, IWebSocketProcessor processor, String jwtToken)
            throws Exception {
        ParadexWebSocketClient client = new ParadexWebSocketClient(url, WS_TYPE_FILLS, processor, jwtToken);
        return client;
    }

    public static ParadexWebSocketClient buildAccountInfoClient(String url, IWebSocketProcessor processor,
            String jwtToken) throws Exception {
        ParadexWebSocketClient client = new ParadexWebSocketClient(url, WS_TYPE_ACCOUNT_INFO, processor, jwtToken);
        return client;
    }

}
