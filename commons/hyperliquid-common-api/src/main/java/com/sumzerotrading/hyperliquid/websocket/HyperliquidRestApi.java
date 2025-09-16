package com.sumzerotrading.hyperliquid.websocket;

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
import com.sumzerotrading.data.Exchange;
import com.sumzerotrading.data.InstrumentDescriptor;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.SumZeroException;

import com.swmansion.starknet.data.TypedData;
import com.swmansion.starknet.data.types.Felt;
import com.swmansion.starknet.signer.StarkCurveSigner;

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

    @Override
    public boolean isPublicApiOnly() {
        return publicApiOnly;
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
                    minNotionalOrderSize, BigDecimal.ZERO, fundingPeriodHours, BigDecimal.ONE);

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
                        minNotionalOrderSize, BigDecimal.ZERO, fundingPeriodHours, BigDecimal.ONE);

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

            Position position = new Position(HyperliquidTickerBuilder.getTicker(tickerString));
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
