package com.sumzerotrading.paradex.common.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.broker.order.OrderTicket.Modifier;
import com.sumzerotrading.data.Exchange;
import com.sumzerotrading.data.InstrumentDescriptor;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.paradex.common.api.historical.OHLCBar;
import com.sumzerotrading.paradex.common.api.order.OrderType;
import com.sumzerotrading.paradex.common.api.order.ParadexOrder;
import com.swmansion.starknet.data.TypedData;
import com.swmansion.starknet.data.types.Felt;
import com.swmansion.starknet.signer.StarkCurveSigner;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ParadexRestApi implements IParadexRestApi {
    protected static Logger logger = LoggerFactory.getLogger(ParadexRestApi.class);
    private final Gson gson;

    protected static IParadexRestApi publicOnlyApi;
    protected static IParadexRestApi privateApi;

    protected String starkPublicKeyHex = null;

    public static IParadexRestApi getPublicOnlyApi(String baseUrl, boolean isTestnet) {
        if (publicOnlyApi == null) {
            publicOnlyApi = new ParadexRestApi(baseUrl, isTestnet);
        }
        return publicOnlyApi;
    }

    public static IParadexRestApi getPrivateApi(String baseUrl, String accountAddress, String privateKey,
            boolean isTestnet) {
        if (privateApi == null) {
            privateApi = new ParadexRestApi(baseUrl, accountAddress, privateKey, isTestnet);
        }
        return privateApi;
    }

    @FunctionalInterface
    public interface RetryableAction {
        void run() throws Exception; // Allows throwing checked exceptions
    }

    protected OkHttpClient client;
    protected String baseUrl;
    protected String accountAddressString;
    protected String privateKeyString;
    protected boolean publicApiOnly = true;

    // Mainnet and testnet chain IDs see the /config API endpoint for the text value
    // of these.
    public static final BigInteger prodChainId = new BigInteger(
            "8458834024819506728615521019831122032732688838300957472069977523540");
    public static final BigInteger testnetChainId = new BigInteger(
            "7693264728749915528729180568779831130134670232771119425");
    protected BigInteger chainID = prodChainId;

    public ParadexRestApi(String baseUrl, boolean isTestnet) {
        this(baseUrl, null, null, isTestnet);
    }

    public ParadexRestApi(String baseUrl, String accountAddressString, String privateKeyString, boolean isTestnet) {
        this.client = new OkHttpClient();
        this.baseUrl = baseUrl;
        this.accountAddressString = accountAddressString;
        this.privateKeyString = privateKeyString;
        // Register the custom adapter
        this.gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter()).create();
        publicApiOnly = accountAddressString == null || privateKeyString == null;
        if (isTestnet) {
            chainID = testnetChainId;
        }
    }

    @Override
    public boolean onboardAccount(String ethereumAddress, String starketAddress, boolean isTestnet) throws Exception {

        // String jwtToken = getJwtToken();

        BigInteger chainId = isTestnet ? testnetChainId : prodChainId;
        String chainIdHex = "0x" + chainId.toString(16).toUpperCase();
        String message = createOnboardingMessage(chainIdHex);
        String signatureString = getOrderMessageSignature(message);

        String path = "/onboarding";
        String url = baseUrl + path;

        Headers headers = new Headers.Builder().add("PARADEX-ETHEREUM-ACCOUNT", ethereumAddress)
                .add("PARADEX-STARKNET-ACCOUNT", starketAddress).add("PARADEX-STARKNET-SIGNATURE", signatureString)
                // .add("Authorization", "Bearer " + jwtToken)
                .build();

        RequestBody requestBody = RequestBody.create("{\"public_key\": \"" + starkPublicKeyHex + "\"}",
                MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder().headers(headers).url(url).post(requestBody).build();
        logger.info("Request: " + request);

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Error response: " + response.body().string());
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();
            logger.info("Response output: " + responseBody);
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            if (jsonResponse.has("success")) {
                return jsonResponse.get("success").getAsBoolean();
            } else {
                return false;
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Position> getPositionInfo(String jwtToken) {
        checkPrivateApi();
        return executeWithRetry(() -> {

            String path = "/positions";
            String url = baseUrl + path;
            Request request = new Request.Builder().url(url).get().addHeader("Authorization", "Bearer " + jwtToken)
                    .build();
            logger.info("Request: " + request);

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Error response: " + response.body().string());
                    throw new IOException("Unexpected code " + response);
                }

                String responseBody = response.body().string();
                logger.info("Response output: " + responseBody);

                return parsePositionInfo(responseBody);

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, 3, 500); // Retry up to 3 times with 500ms backoff
    }

    /**
     * Supported resolutions
     * 
     * resolution in minutes: 1, 3, 5, 15, 30, 60
     * 
     * 
     * @param symbol
     * @param resolutionInMinutes
     * @param lookbackInMinutes
     * @param priceKind
     * @return
     */
    @Override
    public List<OHLCBar> getOHLCBars(String symbol, int resolutionInMinutes, int lookbackInMinutes,
            HistoricalPriceKind priceKind) {

        if (resolutionInMinutes != 1 && resolutionInMinutes != 3 && resolutionInMinutes != 5
                && resolutionInMinutes != 15 && resolutionInMinutes != 30 && resolutionInMinutes != 60) {
            throw new IllegalArgumentException("Unsupported resolution: " + resolutionInMinutes
                    + ". Supported resolutions are 1, 3, 5, 15, 30, 60 minutes.");
        }

        return executeWithRetry(() -> {

            String path = "/markets/klines";
            String url = baseUrl + path;
            long endTime = System.currentTimeMillis();
            long startTime = endTime - resolutionInMinutes * lookbackInMinutes * 60 * 1000;
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            urlBuilder.addQueryParameter("symbol", symbol);
            urlBuilder.addQueryParameter("resolution", resolutionInMinutes + "");
            urlBuilder.addQueryParameter("start_at", startTime + "");
            urlBuilder.addQueryParameter("end_at", endTime + "");
            urlBuilder.addQueryParameter("price_kind", priceKind.name().toLowerCase());
            String newUrl = urlBuilder.build().toString();

            Request request = new Request.Builder().url(newUrl).get().build();
            logger.info("Request: " + request);

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Error response: " + response.body().string());
                    throw new IOException("Unexpected code " + response);
                }

                String responseBody = response.body().string();
                logger.info("Response output: " + responseBody);

                return parseOHLCBars(responseBody);

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, 3, 500); // Retry up to 3 times with 500ms backoff
    }

    @Override
    public List<OrderTicket> getOpenOrders(String jwtToken, String market) {
        checkPrivateApi();
        return executeWithRetry(() -> {

            String path = "/orders";
            String url = baseUrl + path;
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            urlBuilder.addQueryParameter("market", market);
            String newUrl = urlBuilder.build().toString();

            Request.Builder requestBuilder = new Request.Builder().url(newUrl).get().addHeader("Authorization",
                    "Bearer " + jwtToken);

            Request request = requestBuilder.build();
            logger.info("Request: " + request);

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Error response: " + response.body().string());
                    throw new IOException("Unexpected code " + response);
                }

                String responseBody = response.body().string();
                logger.info("Response output: " + responseBody);

                return parseParadoxOrders(responseBody);

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, 3, 500); // Retry up to 3 times with 500ms backoff
    }

    @Override
    public RestResponse cancelOrder(String jwtToken, String orderId) {
        checkPrivateApi();
        return executeWithRetry(() -> {

            String path = "/orders/" + orderId;
            String url = baseUrl + path;
            Request.Builder requestBuilder = new Request.Builder().url(url).delete().addHeader("Authorization",
                    "Bearer " + jwtToken);

            Request request = requestBuilder.build();
            logger.info("Request: " + request);

            try (Response response = client.newCall(request).execute()) {
                /**
                 * 2025-10-03T17:03:29.662+0000 [pool-3-thread-1] INFO
                 * c.f.service.MarketDataService - Canceling expired buy order:
                 * 1759510877650201709231040002 (age: 132289ms) 2025-10-03T17:03:29.764+0000
                 * [pool-3-thread-1] ERROR c.s.p.common.api.ParadexRestApi - Error response:
                 * {"error":"ORDER_IS_CLOSED","message":"order is closed:
                 * 1759510877650201709231040002"}
                 */
                if (response.code() == 400) {
                    RestResponse restResponse = new RestResponse(response.code(), response.body().string());
                    return restResponse;
                } else if (!response.isSuccessful()) {
                    logger.error("Error response: " + response.body().string());
                    throw new IOException("Unexpected code " + response);
                }

                String responseBody = response.body().string();
                logger.info("Response output: " + responseBody);

                return new RestResponse(response.code(), responseBody);

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }

        }, 3, 500); // Retry up to 3 times with 500ms backoff

    }

    @Override
    public InstrumentDescriptor[] getAllInstrumentsForType(InstrumentType instrumentType) {
        return executeWithRetry(() -> {
            String path = "/markets";
            String url = baseUrl + path;
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            String newUrl = urlBuilder.build().toString();

            Request request = new Request.Builder().url(newUrl).get().build();
            logger.info("Request: " + request);

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Error response: " + response.body().string());
                    throw new IOException("Unexpected code " + response);
                }

                String responseBody = response.body().string();
                logger.info("Response output: " + responseBody);
                return parseInstrumentDescriptors(instrumentType, responseBody);

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, 3, 500);
    }

    protected void cancelAllOrders(String jwtToken, String market) {
        checkPrivateApi();
        executeWithRetry(() -> {
            String path = "/orders";
            String url = baseUrl + path;
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            urlBuilder.addQueryParameter("market", market);
            String newUrl = urlBuilder.build().toString();

            Request.Builder requestBuilder = new Request.Builder().url(newUrl).delete().addHeader("Authorization",
                    "Bearer " + jwtToken);

            Request request = requestBuilder.build();
            logger.info("Request: " + request);

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Error response: " + response.body().string());
                    throw new IOException("Unexpected code " + response);
                }

                String responseBody = response.body().string();
                logger.info("Response output: " + responseBody);

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }

        }, 3, 500); // Retry up to 3 times with 500ms backoff
    }

    @Override
    public String placeOrder(String jwtToken, OrderTicket tradeOrder) {
        checkPrivateApi();
        return executeWithRetry(() -> {
            ParadexOrder order = ParadexUtil.translateOrder(tradeOrder);
            String path = "/orders";
            String url = baseUrl + path;
            long timestamp = System.currentTimeMillis();
            // BigInteger chainID = new
            // BigInteger("8458834024819506728615521019831122032732688838300957472069977523540");
            String chainIdHex = "0x" + chainID.toString(16).toUpperCase();
            String orderMessage = createOrderMessage(timestamp, chainIdHex, order);
            String signatureString = getOrderMessageSignature(orderMessage);

            JsonObject orderJson = new JsonObject();
            orderJson.addProperty("client_id", order.getClientId());
            orderJson.addProperty("market", order.getTicker());
            orderJson.addProperty("side", order.getSide().toString());
            orderJson.addProperty("signature_timestamp", timestamp);
            orderJson.addProperty("size", order.getSize().toString());
            orderJson.addProperty("type", order.getOrderType().toString());
            if (order.getOrderType().equals(OrderType.LIMIT)) {
                orderJson.addProperty("price", order.getLimitPrice().toString());
                orderJson.addProperty("instruction", "POST_ONLY");
            }
            if (tradeOrder.getModifiers().contains(Modifier.REDUCE_ONLY)) {
                // add an array to the orderJson and then add reduce_only to the array
                JsonArray flags = new JsonArray();
                flags.add("REDUCE_ONLY");
                orderJson.add("flags", flags);
            }
            orderJson.addProperty("signature", signatureString);

            RequestBody requestBody = RequestBody.create(orderJson.toString(),
                    MediaType.get("application/json; charset=utf-8"));

            Request.Builder requestBuilder = new Request.Builder().url(url).post(requestBody).addHeader("Authorization",
                    "Bearer " + jwtToken);

            Request request = requestBuilder.build();
            logger.info("Request: " + request);
            logger.info("Request body: " + orderJson.toString());

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Error response: " + response.body().string());
                    throw new IOException("Unexpected code " + response);
                }

                String responseBody = response.body().string();
                logger.info("Response output: " + responseBody);
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                if (jsonResponse.has("id")) {
                    return jsonResponse.get("id").getAsString();
                } else {
                    return "";
                }

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, 3, 500); // Retry up to 3 times with 1 second backoff
    }

    @Override
    public String getJwtToken() {
        checkPrivateApi();
        return executeWithRetry(() -> {

            // Fallback to single try if no headers are used
            return getJwtTokenSingleTry();

        }, 3, 500); // Retry up to 3 times with 500ms backoff
    }

    @Override
    public String getJwtToken(Map<String, String> headers) throws IOException {
        checkPrivateApi();
        String path = "/auth";
        String url = baseUrl + path;
        Request.Builder requestBuilder = new Request.Builder().url(url)
                .post(RequestBody.create("{}", MediaType.get("application/json; charset=utf-8")));

        headers.forEach(requestBuilder::addHeader);

        Request request = requestBuilder.build();
        logger.info("Request: " + request);

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Error response: " + response.body().string());
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();

            // Parse the JSON response to extract the jwt_token
            JSONObject jsonResponse = new JSONObject(responseBody);
            if (jsonResponse.has("jwt_token")) {
                return jsonResponse.getString("jwt_token");
            }
        }

        throw new IllegalStateException("JWT Token not found in response");

    }

    @Override
    public InstrumentDescriptor getInstrumentDescriptor(String symbol) {
        return executeWithRetry(() -> {
            String path = "/markets";
            String url = baseUrl + path;
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            urlBuilder.addQueryParameter("market", symbol);
            String newUrl = urlBuilder.build().toString();

            Request request = new Request.Builder().url(newUrl).get().build();
            logger.info("Request: " + request);

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Error response: " + response.body().string());
                    throw new IOException("Unexpected code " + response);
                }

                String responseBody = response.body().string();
                logger.info("Response output: " + responseBody);
                return parseInstrumentDescriptor(responseBody);

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, 3, 500);
    }

    protected void executeWithRetry(RetryableAction action, int maxRetries, long retryDelayMillis) {
        int retries = 0;
        while (true) {
            try {
                action.run(); // Execute the action
                return; // Exit after successful execution
            } catch (java.net.SocketTimeoutException | IllegalStateException e) {
                if (retries < maxRetries) {
                    retries++;
                    logger.error("Request failed. Retrying... Attempt " + retries, e);
                    try {
                        Thread.sleep(retryDelayMillis * retries); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Retry interrupted", ie);
                    }
                } else {
                    logger.error("Max retries reached. Failing request.", e);
                    throw new IllegalStateException("Max retries reached", e);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage(), ex); // Handle other exceptions (e.g., IOException, etc.)
            }
        }
    }

    protected <T> T executeWithRetry(Callable<T> action, int maxRetries, long retryDelayMillis) {
        int retries = 0;
        while (true) {
            try {
                return action.call(); // Execute the HTTP request
            } catch (java.net.SocketTimeoutException | IllegalStateException e) {
                if (retries < maxRetries) {
                    retries++;
                    logger.error("Request timed out. Retrying... Attempt " + retries, e);
                    try {
                        Thread.sleep(retryDelayMillis * retries); // Exponential backoff
                    } catch (InterruptedException ie) {
                        throw new IllegalStateException("Retry interrupted", ie);
                    }
                } else {
                    logger.error("Max retries reached. Failing request.", e);
                    throw new RuntimeException(e); // Rethrow the exception after max retries
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e); // Handle other exceptions (e.g., IOException, etc.)
            }
        }
    }

    protected String getJwtTokenSingleTry() throws Exception {

        logger.info("Getting JWT token for account: " + accountAddressString);
        logger.info("Chain ID: " + chainID);
        // Convert the account address and private key to Felt types
        Felt accountAddress = Felt.fromHex(accountAddressString);
        Felt privateKey = Felt.fromHex(privateKeyString);

        // Get current timestamp in seconds
        long timestamp = System.currentTimeMillis() / 1000;
        long expiry = timestamp + 60 * 60; // now + 1 hour
        // 0x505249564154455f534e5f504f54435f5345504f4c4941
        // BigInteger chainID = new
        // BigInteger("8458834024819506728615521019831122032732688838300957472069977523540");

        String chainIdHex = "0x" + chainID.toString(16).toUpperCase();

        // Create the auth message
        String authMessage = createAuthMessage(timestamp, expiry, chainIdHex);

        // Convert the auth message to a typed data object
        TypedData typedData = TypedData.fromJsonString(authMessage);
        Felt messageHash = typedData.getMessageHash(accountAddress);

        // Create new StarkCurveSigner with the private key
        StarkCurveSigner scSigner = new StarkCurveSigner(privateKey);

        // Sign the typed data
        List<Felt> signature = scSigner.signTypedData(typedData, accountAddress);

        // Convert the signature to a string
        List<BigInteger> signatureBigInt = signature.stream().map(Felt::getValue).collect(Collectors.toList());
        String signatureStr = convertBigIntListToString(signatureBigInt);

        // Call the auth endpoint

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("PARADEX-STARKNET-ACCOUNT", accountAddressString);
        requestHeaders.put("PARADEX-STARKNET-SIGNATURE", signatureStr);
        requestHeaders.put("PARADEX-TIMESTAMP", Long.toString(timestamp));
        requestHeaders.put("PARADEX-SIGNATURE-EXPIRATION", Long.toString(expiry));

        logger.info("JWT Expiration date: " + LocalDateTime.ofEpochSecond(expiry, 0, ZonedDateTime.now().getOffset()));

        return getJwtToken(requestHeaders);
    }

    @Override
    public String getOrderMessageSignature(String orderMessage) throws Exception {

        // Convert the account address and private key to Felt types
        Felt accountAddress = Felt.fromHex(accountAddressString);
        Felt privateKey = Felt.fromHex(privateKeyString);

        logger.info("Using account: " + accountAddressString.toString());
        logger.info("Chain ID: " + chainID);
        // System.out.println("Key value: " + privateKey.getValue());

        // Get current timestamp in seconds
        long timestamp = System.currentTimeMillis() / 1000;
        long expiry = timestamp + 24 * 60 * 60; // now + 24 hours

        // Convert the auth message to a typed data object
        TypedData typedData = TypedData.fromJsonString(orderMessage);
        Felt messageHash = typedData.getMessageHash(accountAddress);

        // Create new StarkCurveSigner with the private key
        StarkCurveSigner scSigner = new StarkCurveSigner(privateKey);

        Felt publicKey = scSigner.getPublicKey();
        starkPublicKeyHex = publicKey.hexString();

        // Sign the typed data
        List<Felt> signature = scSigner.signTypedData(typedData, accountAddress);

        // Convert the signature to a string
        List<BigInteger> signatureBigInt = signature.stream().map(Felt::getValue).collect(Collectors.toList());
        String signatureStr = convertBigIntListToString(signatureBigInt);

        // Log the values being sent

        // System.out.println("Signature: " + signatureStr);
        // System.out.println("Message Hash: " + messageHash.hexString().toString());
        // System.out.println("Timestamp: " + timestamp);
        // System.out.println("Signature Expiration: " + expiry);

        return signatureStr;
    }

    @Override
    public boolean isPublicApiOnly() {
        return publicApiOnly;
    }

    private static String convertBigIntListToString(List<BigInteger> list) {
        String jsonArray = list.stream().map(bigInt -> "\"" + bigInt.toString() + "\"")
                .collect(Collectors.joining(","));
        return "[" + jsonArray + "]";
    }

    private static String createAuthMessage(long timestamp, long expiration, String chainIdHex) {
        return String.format("""
                {
                     "message": {
                         "method": "POST",
                         "path": "/v1/auth",
                         "body": "",
                         "timestamp": %s,
                         "expiration": %s
                     },
                     "domain": {"name": "Paradex", "chainId": "%s", "version": "1"},
                     "primaryType": "Request",
                     "types": {
                         "StarkNetDomain": [
                             {"name": "name", "type": "felt"},
                             {"name": "chainId", "type": "felt"},
                             {"name": "version", "type": "felt"}
                         ],
                         "Request": [
                             {"name": "method", "type": "felt"},
                             {"name": "path", "type": "felt"},
                             {"name": "body", "type": "felt"},
                             {"name": "timestamp", "type": "felt"},
                             {"name": "expiration", "type": "felt"}
                         ]
                     }
                 }
                 """, timestamp, expiration, chainIdHex);
    }

    private static String createOnboardingMessage(String chainIdHex) {
        logger.info("Using chainId: " + chainIdHex);
        return String.format("""
                {
                     "message": {
                        "action": "Onboarding"
                     },
                     "domain": {"name": "Paradex", "chainId": "%s", "version": "1"},
                     "primaryType": "Constant",
                     "types": {
                         "StarkNetDomain": [
                             {"name": "name", "type": "felt"},
                             {"name": "chainId", "type": "felt"},
                             {"name": "version", "type": "felt"}
                         ],
                         "Constant": [
                             {"name": "action", "type": "felt"}
                         ]
                     }
                 }
                 """, chainIdHex);
    }

    private static String createOrderMessage(long timestamp, String chainIdHex, ParadexOrder order) {
        return String.format("""
                            {
                                "domain": {"name": "Paradex", "chainId": "%s", "version": "1"},
                                "primaryType": "Order",
                                "types": {
                                    "StarkNetDomain": [
                                        {"name": "name", "type": "felt"},
                                        {"name": "chainId", "type": "felt"},
                                        {"name": "version", "type": "felt"}
                                    ],
                                    "Order": [
                                        {
                                            "name": "timestamp",
                                            "type": "felt"
                                        },
                                        {"name": "market", "type": "felt"},
                                        {"name": "side", "type": "felt"},
                                        {"name": "orderType", "type": "felt"},
                                        {"name": "size", "type": "felt"},
                                        {
                                            "name": "price",
                                            "type": "felt"
                                        }
                                    ]
                                },
                                "message": {
                                    "timestamp": %s,
                                    "market": "%s",
                                    "side": "%s",
                                    "orderType": "%s",
                                    "size": "%s",
                                    "price": "%s"
                                }
                }

                                         """, chainIdHex, timestamp, order.getTicker(), order.getSide().getChainSide(),
                order.getOrderType().toString(), order.getChainSize(), order.getChainLimitPrice());
    }

    protected List<OrderTicket> parseParadoxOrders(String responseBody) {

        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray resultsArray = jsonObject.getAsJsonArray("results");

        Type listType = new TypeToken<List<ParadexOrder>>() {
        }.getType();

        List<ParadexOrder> orders = gson.fromJson(resultsArray, listType);
        return ParadexUtil.translateOrders(orders);
    }

    protected List<OHLCBar> parseOHLCBars(String responseBody) {
        List<OHLCBar> ohlcBars = new ArrayList<>();
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray resultsArray = jsonObject.getAsJsonArray("results");

        for (int i = 0; i < resultsArray.size(); i++) {
            JsonArray barArray = resultsArray.get(i).getAsJsonArray();
            OHLCBar ohlcBar = new OHLCBar();
            ohlcBar.setTime(barArray.get(0).getAsLong());
            ohlcBar.setOpen(barArray.get(1).getAsDouble());
            ohlcBar.setHigh(barArray.get(2).getAsDouble());
            ohlcBar.setLow(barArray.get(3).getAsDouble());
            ohlcBar.setClose(barArray.get(4).getAsDouble());
            ohlcBar.setVolume(barArray.get(5).getAsDouble());
            ohlcBars.add(ohlcBar);
        }

        return ohlcBars;
    }

    protected InstrumentDescriptor parseInstrumentDescriptor(String responseBody) {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray results = root.getAsJsonArray("results");

            if (results == null || results.size() == 0) {
                logger.warn("No results found in instrument descriptor response");
                return null;
            }

            // Parse the first instrument from the results array
            JsonObject instrumentObj = results.get(0).getAsJsonObject();

            // Validate that this is a perpetual futures instrument
            String assetKind = null;
            if (instrumentObj.has("asset_kind") && !instrumentObj.get("asset_kind").isJsonNull()) {
                assetKind = instrumentObj.get("asset_kind").getAsString();
            }

            if (!"PERP".equals(assetKind)) {
                String symbol = instrumentObj.has("symbol") ? instrumentObj.get("symbol").getAsString() : "unknown";
                logger.error("Expected PERP asset_kind for instrument '{}', but found '{}'", symbol, assetKind);
                throw new SumZeroException("Invalid asset_kind '" + assetKind + "' for instrument '" + symbol
                        + "'. Expected 'PERP' for perpetual futures.");
            }

            // Extract required fields for InstrumentDescriptor
            String symbol = instrumentObj.get("symbol").getAsString();
            String baseCurrency = instrumentObj.get("base_currency").getAsString();
            String quoteCurrency = instrumentObj.get("quote_currency").getAsString();

            // Create common symbol (without exchange-specific suffix)
            String commonSymbol = symbol.split("-")[0];
            String exchangeSymbol = symbol;

            // Parse tick size and order size increment from the JSON
            BigDecimal priceTickSize = instrumentObj.get("price_tick_size").getAsBigDecimal();
            BigDecimal orderSizeIncrement = instrumentObj.get("order_size_increment").getAsBigDecimal();

            // Parse min_notional and funding_period_hours
            int minNotionalOrderSize = instrumentObj.get("min_notional").getAsInt();
            int fundingPeriodHours = instrumentObj.get("funding_period_hours").getAsInt();

            // Create and return the InstrumentDescriptor
            return new InstrumentDescriptor(InstrumentType.PERPETUAL_FUTURES, Exchange.PARADEX, commonSymbol,
                    exchangeSymbol, baseCurrency, quoteCurrency, orderSizeIncrement, priceTickSize,
                    minNotionalOrderSize, BigDecimal.ZERO, fundingPeriodHours, BigDecimal.ONE, 1, "");

        } catch (Exception e) {
            logger.error("Error parsing instrument descriptor: " + e.getMessage(), e);
            throw new SumZeroException(e);
        }
    }

    protected InstrumentDescriptor[] parseInstrumentDescriptors(InstrumentType instrumentType, String responseBody) {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray results = null;

            // Handle the case where results might be null or not an array
            if (root.has("results") && !root.get("results").isJsonNull()) {
                results = root.getAsJsonArray("results");
            }

            if (results == null || results.size() == 0) {
                logger.warn("No results found in instrument descriptors response");
                return new InstrumentDescriptor[0];
            }

            List<InstrumentDescriptor> descriptors = new ArrayList<>();

            // Parse each instrument from the results array
            for (int i = 0; i < results.size(); i++) {
                JsonObject instrumentObj = results.get(i).getAsJsonObject();

                // Validate asset_kind matches the expected instrument type
                String assetKind = null;
                if (instrumentObj.has("asset_kind") && !instrumentObj.get("asset_kind").isJsonNull()) {
                    assetKind = instrumentObj.get("asset_kind").getAsString();
                }

                if (assetKind == null || !isValidAssetKindForInstrumentType(assetKind, instrumentType)) {
                    logger.warn("Skipping instrument with asset_kind '{}' as it doesn't match expected type '{}'",
                            assetKind, instrumentType);
                    continue;
                }

                // Extract required fields for InstrumentDescriptor
                String symbol = instrumentObj.get("symbol").getAsString();
                String baseCurrency = instrumentObj.get("base_currency").getAsString();
                String quoteCurrency = instrumentObj.get("quote_currency").getAsString();

                // Create common symbol (without exchange-specific suffix)
                String commonSymbol = symbol.split("-")[0];
                String exchangeSymbol = symbol;

                // Parse tick size and order size increment from the JSON
                BigDecimal priceTickSize = instrumentObj.get("price_tick_size").getAsBigDecimal();
                BigDecimal orderSizeIncrement = instrumentObj.get("order_size_increment").getAsBigDecimal();

                // Parse min_notional and funding_period_hours
                int minNotionalOrderSize = instrumentObj.get("min_notional").getAsInt();
                int fundingPeriodHours = instrumentObj.get("funding_period_hours").getAsInt();

                // Create the InstrumentDescriptor with the provided instrument type
                InstrumentDescriptor descriptor = new InstrumentDescriptor(instrumentType, Exchange.PARADEX,
                        commonSymbol, exchangeSymbol, baseCurrency, quoteCurrency, orderSizeIncrement, priceTickSize,
                        minNotionalOrderSize, BigDecimal.ZERO, fundingPeriodHours, BigDecimal.ONE, 1, "");

                descriptors.add(descriptor);
            }

            return descriptors.toArray(new InstrumentDescriptor[0]);

        } catch (Exception e) {
            logger.error("Error parsing instrument descriptors: " + e.getMessage(), e);
            throw new SumZeroException(e);
        }
    }

    /**
     * Validates that the asset_kind from JSON matches the expected InstrumentType
     */
    protected boolean isValidAssetKindForInstrumentType(String assetKind, InstrumentType instrumentType) {
        // Handle null asset_kind
        if (assetKind == null) {
            return false;
        }

        switch (instrumentType) {
        case PERPETUAL_FUTURES:
            return "PERP".equals(assetKind);
        case FUTURES:
            return "FUTURE".equals(assetKind);
        case OPTION:
            return "OPTION".equals(assetKind);
        case CRYPTO_SPOT:
            return "SPOT".equals(assetKind);
        default:
            // For unknown instrument types, log a warning but allow processing
            logger.warn("Unknown instrument type '{}' - allowing asset_kind '{}'", instrumentType, assetKind);
            return true;
        }
    }

    protected List<Position> parsePositionInfo(String responseBody) {
        List<Position> positionInfoList = new ArrayList<>();
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray resultsArray = jsonObject.getAsJsonArray("results");

        for (int i = 0; i < resultsArray.size(); i++) {
            JsonObject positionObject = resultsArray.get(i).getAsJsonObject();
            String tickerString = positionObject.get("market").getAsString();
            double inventory = positionObject.get("size").getAsDouble();
            double liquidationPrice = 0;
            double cost_usd = 0;
            try {
                liquidationPrice = positionObject.get("liquidation_price").getAsDouble();
            } catch (NumberFormatException e) {
                logger.info("Can't parse liquidation price: " + e.getMessage());
            }

            try {
                cost_usd = positionObject.get("average_entry_price_usd").getAsDouble();
            } catch (NumberFormatException e) {
                logger.info("Can't parse average_entry_price_usd: " + e.getMessage());
            }

            Position position = new Position(ParadexTickerBuilder.getTicker(tickerString));
            position.setSize(new BigDecimal(inventory));
            position.setLiquidationPrice(new BigDecimal(liquidationPrice));
            position.setAverageCost(new BigDecimal(cost_usd));
            positionInfoList.add(position);
        }

        return positionInfoList;
    }

    private class ZonedDateTimeAdapter extends TypeAdapter<ZonedDateTime> {

        private final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

        @Override
        public void write(JsonWriter out, ZonedDateTime value) throws IOException {
            out.value(value.format(FORMATTER));
        }

        @Override
        public ZonedDateTime read(JsonReader in) throws IOException {
            return ZonedDateTime.parse(in.nextString(), FORMATTER);
        }
    }

    protected void checkPrivateApi() {
        if (privateKeyString == null || privateKeyString.isEmpty()) {
            throw new SumZeroException("Private key not set, public API only mode enabled.");
        }
    }

}
