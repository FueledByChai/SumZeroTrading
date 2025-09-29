package com.sumzerotrading.hyperliquid.ws;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sumzerotrading.broker.Position;
import com.sumzerotrading.data.Exchange;
import com.sumzerotrading.data.InstrumentDescriptor;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.ws.json.SignableExchangeOrderRequest;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HyperliquidRestApi implements IHyperliquidRestApi {
    protected static Logger logger = LoggerFactory.getLogger(HyperliquidRestApi.class);
    private final Gson gson;

    protected static IHyperliquidRestApi publicOnlyApi;
    protected static IHyperliquidRestApi privateApi;

    public static IHyperliquidRestApi getPublicOnlyApi(String baseUrl) {
        if (publicOnlyApi == null) {
            publicOnlyApi = new HyperliquidRestApi(baseUrl);
        }
        return publicOnlyApi;
    }

    public static IHyperliquidRestApi getPrivateApi(String baseUrl, String accountAddress, String privateKey) {
        if (privateApi == null) {
            privateApi = new HyperliquidRestApi(baseUrl, accountAddress, privateKey);
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

    public HyperliquidRestApi(String baseUrl) {
        this(baseUrl, null, null);
    }

    public HyperliquidRestApi(String baseUrl, String accountAddressString, String privateKeyString) {
        this.client = new OkHttpClient();
        this.baseUrl = baseUrl;
        this.accountAddressString = accountAddressString;
        this.privateKeyString = privateKeyString;
        // Register the custom adapter
        this.gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter()).create();
        publicApiOnly = accountAddressString == null || privateKeyString == null;
    }

    public String placeOrder(SignableExchangeOrderRequest request) {
        if (publicApiOnly)
            throw new IllegalStateException("Cannot place order with public API only instance.");

        return executeWithRetry(() -> {
            String path = "/exchange";
            String url = baseUrl + path;
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            String newUrl = urlBuilder.build().toString();
            String json = gson.toJson(request);
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

            Request httpRequest = new Request.Builder().url(newUrl).post(body).build();
            logger.info("Request: " + httpRequest);

            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Error response: " + response.body().string());
                    throw new IOException("Unexpected code " + response);
                }

                String responseBody = response.body().string();
                logger.info("Response output: " + responseBody);
                return responseBody;
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, 3, 500);
    }

    @Override
    public InstrumentDescriptor[] getAllInstrumentsForType(InstrumentType instrumentType) {
        if (instrumentType != InstrumentType.PERPETUAL_FUTURES) {
            throw new IllegalArgumentException("Only perpetual futures are supported at this time.");
        }
        return executeWithRetry(() -> {
            String path = "/info";
            String url = baseUrl + path;
            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            String newUrl = urlBuilder.build().toString();
            RequestBody body = RequestBody.create("{\"type\":\"meta\"}", MediaType.parse("application/json"));

            Request request = new Request.Builder().url(newUrl).post(body).build();
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

    @Override
    public InstrumentDescriptor getInstrumentDescriptor(String symbol) {
        InstrumentDescriptor[] allDescriptors = getAllInstrumentsForType(InstrumentType.PERPETUAL_FUTURES);
        for (InstrumentDescriptor descriptor : allDescriptors) {
            if (descriptor.getExchangeSymbol().equals(symbol)) {
                return descriptor;
            }
        }
        throw new IllegalArgumentException("Instrument with symbol '" + symbol + "' not found.");
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

    @Override
    public boolean isPublicApiOnly() {
        return publicApiOnly;
    }

    protected InstrumentDescriptor[] parseInstrumentDescriptors(InstrumentType instrumentType, String responseBody) {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray universe = null;

            // Handle the new Hyperliquid universe format
            if (root.has("universe") && !root.get("universe").isJsonNull()) {
                universe = root.getAsJsonArray("universe");
            }

            if (universe == null || universe.size() == 0) {
                logger.warn("No universe found in instrument descriptors response");
                return new InstrumentDescriptor[0];
            }

            List<InstrumentDescriptor> descriptors = new ArrayList<>();

            // Parse each instrument from the universe array
            for (int i = 0; i < universe.size(); i++) {
                JsonObject instrumentObj = universe.get(i).getAsJsonObject();
                int assetId = i; // Assign a unique assetId based on index

                // Skip delisted instruments (those with marginTableId != null)
                boolean isDelisted = instrumentObj.has("isDelisted") && !instrumentObj.get("isDelisted").isJsonNull();
                if (isDelisted) {
                    logger.info("Skipping delisted instrument: {}",
                            instrumentObj.has("name") ? instrumentObj.get("name").getAsString() : "unknown");
                    continue;
                }

                // Extract required fields for InstrumentDescriptor
                String name = instrumentObj.get("name").getAsString();
                int szDecimals = instrumentObj.get("szDecimals").getAsInt();
                int maxLeverage = instrumentObj.get("maxLeverage").getAsInt();
                // Remove unused variables
                // int marginTableId = instrumentObj.get("marginTableId").getAsInt();

                // Check if it's only isolated margin (not currently used but available)
                // boolean onlyIsolated = instrumentObj.has("onlyIsolated") &&
                // !instrumentObj.get("onlyIsolated").isJsonNull() &&
                // instrumentObj.get("onlyIsolated").getAsBoolean();

                // Create symbols - Hyperliquid uses simple name format
                String commonSymbol = name;
                String exchangeSymbol = name;

                // For crypto perpetuals, assume USD as quote currency
                String baseCurrency = name;
                String quoteCurrency = "USD";

                // Calculate price tick size and order size increment from szDecimals
                // szDecimals represents the number of decimal places for size
                BigDecimal orderSizeIncrement = BigDecimal.ONE.divide(BigDecimal.TEN.pow(szDecimals));

                // For price tick size: priceTickSize = 10^-(6-szDecimals) = 10^(szDecimals-6)
                int priceDecimals = 6 - szDecimals;
                BigDecimal priceTickSize = BigDecimal.ONE.divide(BigDecimal.TEN.pow(priceDecimals));

                // Set minimum notional - $10 on Hyperliquid
                int minNotionalOrderSize = 10;

                // Hyperliquid uses 8-hour funding periods (3 times per day)
                int fundingPeriodHours = 8;

                // Create the InstrumentDescriptor for Hyperliquid
                InstrumentDescriptor descriptor = new InstrumentDescriptor(instrumentType, Exchange.HYPERLIQUID, // Changed
                                                                                                                 // from
                                                                                                                 // PARADEX
                                                                                                                 // to
                                                                                                                 // HYPERLIQUID
                        commonSymbol, exchangeSymbol, baseCurrency, quoteCurrency, orderSizeIncrement, priceTickSize,
                        minNotionalOrderSize, null, // no settlement price for perpetuals
                        fundingPeriodHours, BigDecimal.ONE, maxLeverage, assetId + ""); // Use assetId as instrumentId

                // Note: Additional Hyperliquid-specific properties like szDecimals,
                // marginTableId,
                // and onlyIsolated are available in the JSON but not stored in the descriptor
                // for simplicity. They can be accessed by re-parsing if needed.

                descriptors.add(descriptor);
            }

            logger.info("Parsed {} active instruments from Hyperliquid universe", descriptors.size());
            return descriptors.toArray(new InstrumentDescriptor[0]);

        } catch (Exception e) {
            logger.error("Error parsing Hyperliquid instrument descriptors: " + e.getMessage(), e);
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

            // Create a basic ticker for the position
            Ticker ticker = new Ticker(tickerString);
            ticker.setExchange(Exchange.HYPERLIQUID);
            ticker.setInstrumentType(InstrumentType.PERPETUAL_FUTURES);

            Position position = new Position(ticker);
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

}
