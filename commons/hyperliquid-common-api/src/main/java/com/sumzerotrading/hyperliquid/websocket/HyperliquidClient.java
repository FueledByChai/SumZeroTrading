package com.sumzerotrading.hyperliquid.websocket;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.web3j.crypto.Keys;

import com.sumzerotrading.hyperliquid.websocket.json.LimitType;
import com.sumzerotrading.hyperliquid.websocket.json.Mappers;
import com.sumzerotrading.hyperliquid.websocket.json.OrderAction;
import com.sumzerotrading.hyperliquid.websocket.json.OrderJson;
import com.sumzerotrading.hyperliquid.websocket.json.SignableExchangeOrderRequest;
import com.sumzerotrading.hyperliquid.websocket.json.SignatureFields;
import com.sumzerotrading.hyperliquid.websocket.json.SubmitExchangeRequest;

public class HyperliquidClient {
    private final HLSigner signer;
    private final HttpClient http = HttpClient.newHttpClient();
    private long startMethodMillis;
    private long completedMillis;
    private long presendMillis;

    public HyperliquidClient(String apiWalletPrivKeyHex) {
        this.signer = new HLSigner(apiWalletPrivKeyHex);
    }

    public String submit(SignableExchangeOrderRequest signable) throws Exception {
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

        // JSON body
        String json = Mappers.JSON.writeValueAsString(submit);
        System.out.println("[HyperliquidClient] Request JSON: " + json);

        // POST
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create("https://api.hyperliquid-testnet.xyz/exchange"))
                .header("Content-Type", "application/json").timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();

        presendMillis = System.currentTimeMillis();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("[HyperliquidClient] Response status: " + resp.statusCode());
        System.out.println("[HyperliquidClient] Response body: " + resp.body());
        return resp.body();
    }

    public void submitOrder() {
        startMethodMillis = System.currentTimeMillis();
        // Example private key; DO NOT USE IN PRODUCTION
        String privKey = "";

        OrderJson buy = new OrderJson();
        buy.assetId = 3;
        buy.isBuy = true;
        buy.price = "109100";
        buy.size = "0.01";
        buy.reduceOnly = false;
        buy.type = new LimitType(LimitType.TimeInForce.ALO);

        OrderAction action = new OrderAction();
        action.orders = java.util.Arrays.asList(buy);

        SignableExchangeOrderRequest signable = new SignableExchangeOrderRequest();
        signable.action = action;
        signable.nonceMs = System.currentTimeMillis();

        try {
            String resp = submit(signable);
            System.out.println("Submit returned: " + resp);
            completedMillis = System.currentTimeMillis();
            System.out.println("Time from sign to sign:" + (presendMillis - startMethodMillis));
            System.out.println("Time from Send to Response:" + (completedMillis - presendMillis));
            System.out.println("Time from sign to Response:" + (completedMillis - startMethodMillis));
        } catch (Exception e) {
            System.err.println("Submit failed:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        HyperliquidClient client = new HyperliquidClient(
                "0xd9b40f24bf7229659de5c01498a7a105001907aaef11de07dfc219483755672f");
        client.submitOrder();
    }
}
