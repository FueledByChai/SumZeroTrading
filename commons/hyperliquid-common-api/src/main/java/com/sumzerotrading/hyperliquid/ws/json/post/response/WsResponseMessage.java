package com.sumzerotrading.hyperliquid.ws.json.post.response;

public sealed interface WsResponseMessage permits PongWsResponse, OrderPostResponse, UnknownPostResponse {

}
