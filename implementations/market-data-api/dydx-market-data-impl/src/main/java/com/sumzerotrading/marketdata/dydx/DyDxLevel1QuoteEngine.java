package com.sumzerotrading.marketdata.dydx;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.Level1Quote;
import com.sumzerotrading.marketdata.Level1QuoteListener;
import com.sumzerotrading.marketdata.QuoteEngine;
import com.sumzerotrading.marketdata.QuoteType;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DyDxLevel1QuoteEngine extends QuoteEngine implements Runnable {

    protected Logger logger = LoggerFactory.getLogger(DyDxLevel1QuoteEngine.class);

    public static final String SLEEP_TIME_PROPERTY_KEY = "sleep.time.in.seconds";
    public static final String INCLUDE_FUNDING_RATE_PROPERTY_KEY = "include.funding.rates";
    protected volatile boolean started = false;
    protected boolean threadCompleted = false;
    protected Thread mainThread = new Thread(this);
    protected Thread fundingThread = new Thread(this);
    protected Thread blockHeightThread = new Thread(this);
    private static final String BASE_URL = "https://indexer.dydx.trade/v4/";
    private static final String ORDER_BOOK_URL = BASE_URL + "orderbooks/perpetualMarket/";
    private static final String FUNDING_URL = BASE_URL + "perpetualMarkets/";
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    protected int sleepTimeInSeconds = 10;
    protected ArrayList<String> urlStrings = new ArrayList<>();
    private OrderBookResponse orderBook;
    protected MarketsResponse allFundingRates;
    protected boolean includeFundingRate = false;

    public DyDxLevel1QuoteEngine() {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public OrderBookResponse getOrderBook(String market) {
        String url = ORDER_BOOK_URL + market;
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Unexpected response code: " + response.code());
            }
            String jsonResponse = response.body().string();
            return objectMapper.readValue(jsonResponse, OrderBookResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching order book for market: " + market, e);
        }
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
        logger.info("starting engine with " + sleepTimeInSeconds + " second interval");
        started = true;
        mainThread.start();
    }

    @Override
    public void startEngine(Properties props) {
        String sleepTimeString = props.getProperty(SLEEP_TIME_PROPERTY_KEY);
        if (sleepTimeString != null) {
            sleepTimeInSeconds = Integer.parseInt(sleepTimeString);
        }

        String includeFundingRatesString = props.getProperty(INCLUDE_FUNDING_RATE_PROPERTY_KEY);
        if (includeFundingRatesString != null) {
            includeFundingRate = Boolean.parseBoolean(includeFundingRatesString);
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
    }

    @Override
    public void useDelayedData(boolean useDelayed) {
        logger.error(" useDelayedData() Not supported for dYdX market data");
    }

    @Override
    public void subscribeLevel1(Ticker ticker, Level1QuoteListener listener) {
        super.subscribeLevel1(ticker, listener);
    }

    @Override
    public void unsubscribeLevel1(Ticker ticker, Level1QuoteListener listener) {
        urlStrings.remove(BASE_URL + ticker.getSymbol());
        super.unsubscribeLevel1(ticker, listener);
    }

    @Override
    public void run() {
        while (started) {
            try {
                getQuotes();
                Thread.sleep(sleepTimeInSeconds * 1000);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        threadCompleted = true;
    }

    protected void getQuotes() {
        if (includeFundingRate) {
            try {
                allFundingRates = getAllFundingRates();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        for (Ticker ticker : level1ListenerMap.keySet()) {
            try {
                if (!level1ListenerMap.get(ticker).isEmpty()) {
                    orderBook = getOrderBook(ticker.getSymbol());
                    HashMap<QuoteType, BigDecimal> quoteMap = new HashMap<>();
                    quoteMap.put(QuoteType.ASK, new BigDecimal(orderBook.asks[0].price));
                    quoteMap.put(QuoteType.ASK_SIZE, new BigDecimal(orderBook.asks[0].size));
                    quoteMap.put(QuoteType.BID, new BigDecimal(orderBook.bids[0].price));
                    quoteMap.put(QuoteType.BID_SIZE, new BigDecimal(orderBook.bids[0].size));
                    if (includeFundingRate) {
                        quoteMap.put(QuoteType.FUNDING_RATE_APR,
                                allFundingRates.getMarkets().get(ticker.getSymbol()).getAnnualizedFundingRate());
                    }
                    Level1Quote quote = new Level1Quote(ticker, ZonedDateTime.now(), quoteMap);
                    fireLevel1Quote(quote);
                }
            } catch (Exception ex) {
                logger.error("DyDxLevel1 Quote Engine Caught an exception, but will continue:  " + ex.getMessage(), ex);

            }
        }

    }

    public MarketsResponse getAllFundingRates() {
        Request request = new Request.Builder().url(FUNDING_URL).get().build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Unexpected response code: " + response.code());
            }
            String jsonResponse = response.body().string();
            return objectMapper.readValue(jsonResponse, MarketsResponse.class); // Parse JSON
        } catch (Exception e) {
            throw new RuntimeException("Error fetching funding rates", e);
        }
    }

    public void setSleepTimeInSeconds(int sleepTime) {
        this.sleepTimeInSeconds = sleepTime;
    }

    // Classes for mapping JSON response
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class OrderBookResponse {
        private Order[] bids;
        private Order[] asks;

        public Order[] getBids() {
            return bids;
        }

        public void setBids(Order[] bids) {
            this.bids = bids;
        }

        public Order[] getAsks() {
            return asks;
        }

        public void setAsks(Order[] asks) {
            this.asks = asks;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Order {
        private String price;
        private String size;

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        @Override
        public String toString() {
            return "Price: " + price + ", Size: " + size;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class MarketsResponse {
        private Map<String, Market> markets;

        public Map<String, Market> getMarkets() {
            return markets;
        }

        public void setMarkets(Map<String, Market> markets) {
            this.markets = markets;
        }

        @Override
        public String toString() {
            return "MarketsResponse [markets=" + markets + "]";
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Market {
        private String nextFundingRate;
        private BigDecimal annualizedFundingRate;

        public String getNextFundingRate() {
            return nextFundingRate;
        }

        public void setNextFundingRate(String nextFundingRate) {
            this.nextFundingRate = nextFundingRate;
            annualizedFundingRate = new BigDecimal(Double.parseDouble(nextFundingRate) * 24 * 365 * 100);
        }

        @Override
        public String toString() {
            return "Market [nextFundingRate=" + annualizedFundingRate + "]";
        }

        public BigDecimal getAnnualizedFundingRate() {
            return annualizedFundingRate;
        }

    }
}
