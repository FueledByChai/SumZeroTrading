package com.sumzerotrading.hyperliquid.ws;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.sumzerotrading.hyperliquid.HyperliquidUtil;
import com.sumzerotrading.hyperliquid.ws.json.HLSigner;
import com.sumzerotrading.hyperliquid.ws.json.LimitType;
import com.sumzerotrading.hyperliquid.ws.json.Mappers;
import com.sumzerotrading.hyperliquid.ws.json.OrderAction;
import com.sumzerotrading.hyperliquid.ws.json.OrderJson;
import com.sumzerotrading.hyperliquid.ws.json.SignableExchangeOrderRequest;
import com.sumzerotrading.hyperliquid.ws.json.SignatureFields;
import com.sumzerotrading.hyperliquid.ws.json.SubmitExchangeRequest;
import com.sumzerotrading.hyperliquid.ws.json.WebServicePostMessage;
import com.sumzerotrading.hyperliquid.ws.json.ws.SubmitPostResponse;

public class HyperliquidClient {
    protected static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HyperliquidClient.class);
    private final HLSigner signer;
    private final HttpClient http = HttpClient.newHttpClient();
    private long startMethodMillis;
    private long completedMillis;
    private long presendMillis;
    private int requestId = 1;
    HyperliquidWebSocketClient client;

    public HyperliquidClient(String apiWalletPrivKeyHex) throws Exception {
        this.signer = new HLSigner(apiWalletPrivKeyHex);
        String wsUrl = "wss://api.hyperliquid-testnet.xyz/ws"; // or mainnet URL
        HyperliquidPostWebSocketProcessor wsProcessor = new HyperliquidPostWebSocketProcessor(() -> {
            System.out.println("WebSocket connected callback");
        });
        this.client = new HyperliquidWebSocketClient(wsUrl, wsProcessor);
        client.connect();

    }

    public SubmitExchangeRequest getJson(SignableExchangeOrderRequest signable) throws Exception {
        // Nonce: typical pattern is current epoch millis
        if (signable.nonceMs == 0) {
            signable.nonceMs = System.currentTimeMillis();
        }

        // Sign
        // boolean isMainnet = apiBaseUrl.equals("https://api.hyperliquid.xyz"); // or
        // however you determine this
        boolean isMainnet = false; // Change as needed
        SignatureFields sig = signer.signL1OrderAction(signable.action, // ActionPayload
                signable.nonceMs, // ms epoch
                signable.vaultAddress, // null or "0x..."
                signable.expiresAfterMs, // nullable
                isMainnet);

        // Attach signature
        SubmitExchangeRequest submit = new SubmitExchangeRequest();
        submit.action = signable.action;
        submit.nonceMs = signable.nonceMs;
        submit.vaultAddress = signable.vaultAddress;
        submit.expiresAfterMs = signable.expiresAfterMs;
        submit.signature = sig;

        return submit;

    }

    public String submitViaRest(SubmitExchangeRequest submit) throws Exception {

        String json = Mappers.JSON.writeValueAsString(submit);

        // POST
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create("https://api.hyperliquid-testnet.xyz/exchange"))
                .header("Content-Type", "application/json").timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();

        presendMillis = System.currentTimeMillis();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }

    public void submitOrder(boolean useWebSocket) throws Exception {
        startMethodMillis = System.currentTimeMillis();
        // Example private key; DO NOT USE IN PRODUCTION
        String privKey = "";

        OrderJson buy = new OrderJson();
        buy.assetId = 3;
        buy.isBuy = true;
        buy.price = "113100";
        buy.size = "0.01";
        buy.reduceOnly = false;
        buy.type = new LimitType(LimitType.TimeInForce.ALO);
        // buy.clientOrderId = EncodeUtil.encode128BitHex("Hello World 123");
        // buy.clientOrderId =
        // "289c505c8da38d9d0bb8d43aace34d89a1f96ca2d6963bdba552b195604f0bb";

        OrderJson buy2 = new OrderJson();
        buy2.assetId = 3;
        buy2.isBuy = true;
        buy2.price = "111200";
        buy2.size = "0.005";
        buy2.reduceOnly = false;
        buy2.type = new LimitType(LimitType.TimeInForce.GTC);
        buy2.clientOrderId = HyperliquidUtil.encode128BitHex("Another Order 456");

        OrderAction action = new OrderAction();
        action.orders = java.util.Arrays.asList(buy, buy2);

        SignableExchangeOrderRequest signable = new SignableExchangeOrderRequest();
        signable.action = action;
        signable.nonceMs = System.currentTimeMillis();

        logger.info("json: {}", Mappers.JSON.writeValueAsString(signable));

        SubmitExchangeRequest reqest = getJson(signable);

        try {
            String resp;
            if (useWebSocket) {
                submitViaWebSocket(reqest);
                resp = "Submitted via WebSocket";
            } else {
                resp = submitViaRest(reqest);
            }
            completedMillis = System.currentTimeMillis();

        } catch (Exception e) {
            System.err.println("Submit failed:");
            e.printStackTrace();
        }
    }

    public void submitViaWebSocket(SubmitExchangeRequest submit) throws Exception {

        WebServicePostMessage wsRequest = new WebServicePostMessage();
        wsRequest.id = requestId++;
        wsRequest.setPayload(submit);

        String jsonToSend = Mappers.JSON.writeValueAsString(wsRequest);
        client.postMessage(jsonToSend);

    }

    public static void main(String[] args) throws Exception {

        IHyperliquidWebsocketApi api = new HyperliquidWebsocketApi();

        api.connect();
        OrderJson buy = new OrderJson();

        buy.isBuy = true;
        buy.price = "113100";
        buy.size = "0.01";
        buy.reduceOnly = false;
        buy.type = new LimitType(LimitType.TimeInForce.ALO);
        buy.clientOrderId = HyperliquidUtil.encode128BitHex("Hello World 123");

        OrderJson buy2 = new OrderJson();
        buy2.assetId = 3;
        buy2.isBuy = true;
        buy2.price = "111200";
        buy2.size = "0.005";
        buy2.reduceOnly = false;
        buy2.type = new LimitType(LimitType.TimeInForce.GTC);
        buy2.clientOrderId = HyperliquidUtil.encode128BitHex("Another Order 456");

        OrderAction action = new OrderAction();
        action.orders = java.util.Arrays.asList(buy, buy2);

        SubmitPostResponse

        submitOrders = api.submitOrders(action);

        logger.info("Submit response: {}", submitOrders);

        Thread.sleep(3000000);
    }
}
