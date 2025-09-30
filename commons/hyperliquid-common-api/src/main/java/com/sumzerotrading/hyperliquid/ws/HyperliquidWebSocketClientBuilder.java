package com.sumzerotrading.hyperliquid.ws;

import java.util.Map;

import com.sumzerotrading.websocket.IWebSocketProcessor;

public class HyperliquidWebSocketClientBuilder {

    public static final String WS_TYPE_ORDER_UPDATES = "orderUpdates";
    public static final String WS_TYPE_ORDER_BOOK_UPDATES = "l2Book";
    public static final String WS_TYPE_BBO = "bbo";
    public static final String WS_TYPE_ACTIVE_ASSET_CTX = "activeAssetCtx";
    public static final String WS_TYPE_TRADES = "trades";
    public static final String WS_TYPE_ACCOUNT_INFO = "clearinghouseState";
    public static final String WS_TYPE_USER_FILLS = "userFills";

    public static HyperliquidWebSocketClient buildOrderUpdateClient(String url, String userAddress,
            IWebSocketProcessor processor) throws Exception {
        return new HyperliquidWebSocketClient(url, WS_TYPE_ORDER_UPDATES, Map.of("user", userAddress), processor);
    }

    public static HyperliquidWebSocketClient buildOrderBookUpdateClient(String url, String assetSymbol,
            IWebSocketProcessor processor) throws Exception {
        return new HyperliquidWebSocketClient(url, WS_TYPE_ORDER_BOOK_UPDATES, Map.of("coin", assetSymbol), processor);
    }

    public static HyperliquidWebSocketClient buildBBOClient(String url, String assetSymbol,
            IWebSocketProcessor processor) throws Exception {
        return new HyperliquidWebSocketClient(url, WS_TYPE_BBO, Map.of("coin", assetSymbol), processor);
    }

    public static HyperliquidWebSocketClient buildActiveAssetCtxClient(String url, String assetSymbol,
            IWebSocketProcessor processor) throws Exception {
        return new HyperliquidWebSocketClient(url, WS_TYPE_ACTIVE_ASSET_CTX, Map.of("coin", assetSymbol), processor);

    }

    public static HyperliquidWebSocketClient buildTradesClient(String url, String assetSymbol,
            IWebSocketProcessor processor) throws Exception {
        return new HyperliquidWebSocketClient(url, WS_TYPE_TRADES, Map.of("coin", assetSymbol), processor);
    }

    public static HyperliquidWebSocketClient buildAccountInfoClient(String url, String userAddress,
            IWebSocketProcessor processor) throws Exception {
        return new HyperliquidWebSocketClient(url, WS_TYPE_ACCOUNT_INFO, Map.of("user", userAddress), processor);
    }

    public static HyperliquidWebSocketClient buildUserFillsClient(String url, String userAddress,
            IWebSocketProcessor processor) throws Exception {
        return new HyperliquidWebSocketClient(url, WS_TYPE_USER_FILLS, Map.of("user", userAddress), processor);
    }

}
