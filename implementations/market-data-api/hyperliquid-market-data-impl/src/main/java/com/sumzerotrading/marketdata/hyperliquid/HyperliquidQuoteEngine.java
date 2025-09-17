package com.sumzerotrading.marketdata.hyperliquid;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.IOrderBook;
import com.sumzerotrading.marketdata.Level1Quote;
import com.sumzerotrading.marketdata.Level1QuoteListener;
import com.sumzerotrading.marketdata.Level2Quote;
import com.sumzerotrading.marketdata.Level2QuoteListener;
import com.sumzerotrading.marketdata.OrderBookUpdateListener;
import com.sumzerotrading.marketdata.QuoteEngine;
import com.sumzerotrading.marketdata.QuoteType;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HyperliquidQuoteEngine extends QuoteEngine implements OrderBookUpdateListener {

    protected static Logger logger = LoggerFactory.getLogger(HyperliquidQuoteEngine.class);

    public static final String SLEEP_TIME_PROPERTY_KEY = "sleep.time.in.seconds";
    public static final String INCLUDE_FUNDING_RATE_PROPERTY_KEY = "include.funding.rates";
    public static final String FUNDING_RATE_UPDATE_INTERVAL_PROPERTY_KEY = "funding.rate.update.interval.seconds";

    protected volatile boolean started = false;
    protected boolean threadCompleted = false;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private ScheduledExecutorService fundingRateExecutor;

    protected int sleepTimeMS = 500;
    protected int fundingRateUpdateIntervalSeconds = 5;
    protected ArrayList<String> urlStrings = new ArrayList<>();
    private OrderBookResponse orderBook;
    protected volatile Map<String, FundingData> allFundingRates;
    private static final String BASE_URL = "https://api.hyperliquid.xyz/info";
    protected boolean includeFundingRate = true;

    public HyperliquidQuoteEngine() {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.fundingRateExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "hyperliquid-funding-rate-updater");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public Date getServerTime() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isConnected() {
        return started;
    }

    @Override
    public void startEngine() {
        if (threadCompleted) {
            throw new IllegalStateException("Quote Engine was already stopped");
        }
        logger.info("starting engine with " + sleepTimeMS + " ms interval");
        started = true;

        // Start the funding rate update scheduler if funding rates are enabled
        if (includeFundingRate) {
            // startFundingRateUpdates();
        }

    }

    @Override
    public void startEngine(Properties props) {
        String sleepTimeString = props.getProperty(SLEEP_TIME_PROPERTY_KEY);
        if (sleepTimeString != null) {
            sleepTimeMS = Integer.parseInt(sleepTimeString);
        }
        String includeFundingRatesString = props.getProperty(INCLUDE_FUNDING_RATE_PROPERTY_KEY);
        if (includeFundingRatesString != null) {
            includeFundingRate = Boolean.parseBoolean(includeFundingRatesString);
        }
        String fundingRateIntervalString = props.getProperty(FUNDING_RATE_UPDATE_INTERVAL_PROPERTY_KEY);
        if (fundingRateIntervalString != null) {
            fundingRateUpdateIntervalSeconds = Integer.parseInt(fundingRateIntervalString);
        }
        startEngine();
    }

    @Override
    public boolean started() {
        return started;
    }

    @Override
    public void stopEngine() {
        started = false;
        // stopFundingRateUpdates();
    }

    @Override
    public void useDelayedData(boolean useDelayed) {
        logger.error("useDelayedData() Not supported for hyperliquid market data");
    }

    /**
     * Starts periodic funding rate refreshes using a background scheduler.
     * Delegates to a helper that sets up a fixed-rate timer every N seconds.
     */
    // private synchronized void startFundingRateUpdates() {
    // if (!includeFundingRate) {
    // return;
    // }
    // // (Re)create the scheduler if needed
    // if (fundingRateExecutor == null || fundingRateExecutor.isShutdown()) {
    // fundingRateExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
    // Thread t = new Thread(r, "hyperliquid-funding-rate-updater");
    // t.setDaemon(true);
    // return t;
    // });
    // }
    // startFundingRateTimer();
    // }

    /**
     * Schedules a fixed-rate task to fetch all funding rates and cache them.
     * Default cadence is every 5 seconds (configurable via property).
     */
    // private void startFundingRateTimer() {
    // // Avoid multiple schedules if already active
    // // We rely on single-thread executor; scheduleAtFixedRate can be called
    // multiple
    // // times
    // // but here we only set up one repeating task when the engine starts.
    // fundingRateExecutor.scheduleAtFixedRate(() -> {
    // try {
    // Map<String, FundingData> latest = getAllFundingRates();
    // allFundingRates = latest; // volatile ensures visibility
    // if (latest != null) {
    // logger.debug("Funding rates updated ({} assets)", latest.size());
    // }
    // } catch (Exception e) {
    // logger.error("Error refreshing funding rates", e);
    // }
    // }, 0, fundingRateUpdateIntervalSeconds, TimeUnit.SECONDS);
    // }

    /**
     * Stops the funding rate scheduler gracefully.
     */
    // private synchronized void stopFundingRateUpdates() {
    // if (fundingRateExecutor != null) {
    // fundingRateExecutor.shutdown();
    // try {
    // if (!fundingRateExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
    // fundingRateExecutor.shutdownNow();
    // }
    // } catch (InterruptedException e) {
    // fundingRateExecutor.shutdownNow();
    // Thread.currentThread().interrupt();
    // } finally {
    // fundingRateExecutor = null;
    // }
    // }
    // }

    @Override
    public void subscribeLevel1(Ticker ticker, Level1QuoteListener listener) {
        super.subscribeLevel1(ticker, listener);
    }

    @Override
    public void unsubscribeLevel1(Ticker ticker, Level1QuoteListener listener) {
        super.unsubscribeLevel1(ticker, listener);
    }

    @Override
    public void subscribeMarketDepth(Ticker ticker, Level2QuoteListener listener) {
        super.subscribeMarketDepth(ticker, listener);
        OrderBookRegistry.getInstance().getOrderBook(ticker).addOrderBookUpdateListener(this);
    }

    @Override
    public void unsubscribeMarketDepth(Ticker ticker, Level2QuoteListener listener) {
        // TODO Auto-generated method stub
        super.unsubscribeMarketDepth(ticker, listener);
    }

    @Override
    public void bestAskUpdated(Ticker ticker, BigDecimal bestAsk, Double askSize, ZonedDateTime timeStamp) {
        Level1Quote quote = new Level1Quote(ticker, timeStamp);
        quote.addQuote(QuoteType.ASK, ticker.formatPrice(bestAsk));
        quote.addQuote(QuoteType.ASK_SIZE, BigDecimal.valueOf(askSize));
        super.fireLevel1Quote(quote);

    }

    @Override
    public void bestBidUpdated(Ticker ticker, BigDecimal bestBid, Double bidSize, ZonedDateTime timeStamp) {
        Level1Quote quote = new Level1Quote(ticker, timeStamp);
        quote.addQuote(QuoteType.BID, ticker.formatPrice(bestBid));
        quote.addQuote(QuoteType.BID_SIZE, BigDecimal.valueOf(bidSize));
        super.fireLevel1Quote(quote);
    }

    @Override
    public void orderBookImbalanceUpdated(Ticker ticker, BigDecimal imbalance, ZonedDateTime timeStamp) {
        Level1Quote quote = new Level1Quote(ticker, timeStamp);
        quote.addQuote(QuoteType.ORDER_BOOK_IMBALANCE, ticker.formatPrice(imbalance));
        super.fireLevel1Quote(quote);
    }

    @Override
    public void orderBookUpdated(Ticker ticker, IOrderBook book, ZonedDateTime timeStamp) {
        Level2Quote quote = new Level2Quote(ticker, book, timeStamp);
        super.fireMarketDepthQuote(quote);
    }

    public Map<String, FundingData> getAllFundingRates() throws Exception {
        return parseResponse(fetchFundingRates());
    }

    public String fetchFundingRates() throws Exception {
        OkHttpClient client = new OkHttpClient();

        // Define the JSON request body
        String jsonBody = "{ \"type\": \"metaAndAssetCtxs\" }";

        // Build the HTTP request
        Request request = new Request.Builder().url(BASE_URL)
                .post(RequestBody.create(jsonBody, MediaType.get("application/json"))).build();

        // Execute the request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to fetch funding rates: " + response.code());
            }
            return response.body().string();
        }
    }

    public Map<String, FundingData> parseResponse(String jsonResponse) throws Exception {
        // Deserialize the response into two parts: Universe and FundingData
        List<Object> response = objectMapper.readValue(jsonResponse, new TypeReference<List<Object>>() {
        });

        UniverseData universeData = objectMapper.convertValue(response.get(0), UniverseData.class);
        List<FundingData> fundingDataList = objectMapper.convertValue(response.get(1),
                objectMapper.getTypeFactory().constructCollectionType(List.class, FundingData.class));

        // Map funding data by asset name
        Map<String, FundingData> fundingRates = new HashMap<>();
        for (int i = 0; i < fundingDataList.size(); i++) {
            FundingData fundingData = fundingDataList.get(i);
            fundingData.name = universeData.assets.get(i).name; // Assign name from universe data
            fundingRates.put(fundingData.name, fundingData);
        }

        return fundingRates;
    }

    // Classes for mapping JSON response
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class OrderBookResponse {
        private List<List<Map<String, String>>> levels;

        public List<List<Map<String, String>>> getLevels() {
            return levels;
        }

        public void setLevels(List<List<Map<String, String>>> levels) {
            this.levels = levels;
        }
    }

    // Define UniverseData and FundingData classes for JSON parsing
    public static class UniverseData {
        @JsonProperty("universe")
        public List<Asset> assets;
    }

    public static class Asset {
        @JsonProperty("szDecimals")
        public int szDecimals;
        @JsonProperty("name")
        public String name;
        @JsonProperty("maxLeverage")
        public int maxLeverage;
    }

    public static class FundingData {
        @JsonProperty("funding")
        public String funding;
        @JsonProperty("openInterest")
        public String openInterest;
        @JsonProperty("prevDayPx")
        public String prevDayPx;
        @JsonProperty("dayNtlVlm")
        public String dayNtlVlm;
        @JsonProperty("premium")
        public String premium;
        @JsonProperty("oraclePx")
        public String oraclePx;
        @JsonProperty("markPx")
        public String markPx;
        @JsonProperty("midPx")
        public String midPx;
        @JsonProperty("impactPxs")
        public List<String> impactPxs;
        @JsonProperty("dayBaseVlm")
        public String dayBaseVlm;

        // Added to link funding data with asset name
        public String name;
    }

    public void setSleepTimeMS(int seconds) {
        this.sleepTimeMS = seconds;
    }

    public int getSleepTimeMS() {
        return sleepTimeMS;
    }

}
