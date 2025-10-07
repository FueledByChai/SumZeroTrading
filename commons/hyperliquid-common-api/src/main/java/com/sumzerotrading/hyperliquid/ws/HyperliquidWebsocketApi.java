package com.sumzerotrading.hyperliquid.ws;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.hyperliquid.ws.json.HLSigner;
import com.sumzerotrading.hyperliquid.ws.json.Mappers;
import com.sumzerotrading.hyperliquid.ws.json.OrderAction;
import com.sumzerotrading.hyperliquid.ws.json.SignableExchangeOrderRequest;
import com.sumzerotrading.hyperliquid.ws.json.SignatureFields;
import com.sumzerotrading.hyperliquid.ws.json.SubmitExchangeRequest;
import com.sumzerotrading.hyperliquid.ws.json.WebServicePostMessage;
import com.sumzerotrading.hyperliquid.ws.json.ws.SubmitPostResponse;
import com.sumzerotrading.websocket.IWebSocketEventListener;

public class HyperliquidWebsocketApi implements IWebSocketEventListener<SubmitPostResponse>, IHyperliquidWebsocketApi {

    protected static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HyperliquidWebsocketApi.class);

    protected HLSigner signer;
    protected HyperliquidWebSocketClient client;
    protected int requestId = 1;
    protected Map<Integer, OrderAction> pendingOrders = new HashMap<>();
    protected final ConcurrentHashMap<Integer, CompletableFuture<SubmitPostResponse>> pendingRequests = new ConcurrentHashMap<>();
    protected String wsUrl;

    protected HyperliquidConfiguration config = HyperliquidConfiguration.getInstance();
    protected boolean isMainnet;
    // protected String signingAddress = null;
    // protected String tradingAddress = null;
    protected String subAccountAddress = null;

    public HyperliquidWebsocketApi() {

        this.signer = new HLSigner(config.getPrivateKey());
        this.wsUrl = config.getWebSocketUrl();
        this.subAccountAddress = config.getSubAccountAddress();
        // this.signingAddress = config.getAccountAddress();
        // this.tradingAddress = config.getTradingAccount();
        this.isMainnet = config.getEnvironment().equals("prod");
        logger.info("Hyperliquid WebSocket API initialized with URL: {}", wsUrl);
        logger.info("Websocket API is using Mainnet: {}", isMainnet);
        connectToWebSocket();
    }

    @Override
    public SubmitPostResponse submitOrders(OrderAction orderAction) {
        SignableExchangeOrderRequest signable = new SignableExchangeOrderRequest();
        signable.action = orderAction;
        signable.nonceMs = System.currentTimeMillis();
        if (subAccountAddress != null) {
            signable.vaultAddress = subAccountAddress;
        }
        logger.info("Built order request");
        SubmitExchangeRequest request = getSignedRequest(signable);
        logger.info("Signed order request");
        WebServicePostMessage wsRequest = new WebServicePostMessage();
        wsRequest.id = requestId++;
        pendingOrders.put(wsRequest.id, orderAction);
        CompletableFuture<SubmitPostResponse> future = new CompletableFuture<>();
        pendingRequests.put(wsRequest.id, future);
        wsRequest.setPayload(request);

        try {
            logger.info("writing to json");
            String jsonToSend = Mappers.JSON.writeValueAsString(wsRequest);
            logger.info("Posting to websocket");
            client.postMessage(jsonToSend);
            logger.info("Posted to websocket");
        } catch (Exception e) {
            pendingRequests.remove(wsRequest.id);
            logger.error("Error sending WebSocket request id " + wsRequest.id, e);
            return null;
        }

        try {
            // Block until response arrives or timeout
            return future.get(2000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            pendingRequests.remove(wsRequest.id);
            logger.error("Timeout waiting for response to request id " + wsRequest.id, e);
        } catch (Exception e) {
            pendingRequests.remove(wsRequest.id);
            logger.error("Error waiting for response to request id " + wsRequest.id, e);
        }
        return null;
    }

    @Override
    public void connect() {
        while (!client.isOpen()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new SumZeroException("Error waiting for websocket to connect", e);
            }
        }
    }

    @Override
    public void onWebSocketEvent(SubmitPostResponse event) {
        logger.info("Received POST response: {}", event);
        CompletableFuture<SubmitPostResponse> future = pendingRequests.get(event.requestId);
        if (future != null) {
            // Complete the future with the event data
            future.complete(event);
        }
    }

    private void connectToWebSocket() {
        HyperliquidPostWebSocketProcessor wsProcessor = new HyperliquidPostWebSocketProcessor(() -> {
            connectToWebSocket();
        });
        try {
            this.client = new HyperliquidWebSocketClient(wsUrl, wsProcessor);
            wsProcessor.addEventListener(this);
            client.connect();
        } catch (Exception e) {
            throw new SumZeroException("Error connecting to Hyperliquid WebSocket", e);
        }
    }

    public SubmitExchangeRequest getSignedRequest(SignableExchangeOrderRequest signable) {
        // Nonce: typical pattern is current epoch millis
        if (signable.nonceMs == 0) {
            signable.nonceMs = System.currentTimeMillis();
        }

        logger.info("Starting signing");
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

}
