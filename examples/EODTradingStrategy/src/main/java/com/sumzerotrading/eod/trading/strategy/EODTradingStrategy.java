/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.sumzerotrading.eod.trading.strategy;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.broker.BrokerError;
import com.sumzerotrading.broker.BrokerErrorListener;
import com.sumzerotrading.broker.order.OrderEvent;
import com.sumzerotrading.broker.order.OrderEventListener;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.broker.order.TradeOrder;
import com.sumzerotrading.data.StockTicker;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.interactive.brokers.client.InteractiveBrokersClient;
import com.sumzerotrading.interactive.brokers.client.InteractiveBrokersClientInterface;
import com.sumzerotrading.marketdata.ILevel1Quote;
import com.sumzerotrading.marketdata.Level1QuoteListener;
import com.sumzerotrading.marketdata.QuoteType;

/**
 *
 * @author RobTerpilowski
 */
public class EODTradingStrategy implements Level1QuoteListener, OrderEventListener, BrokerErrorListener {

    protected static Logger logger = LoggerFactory.getLogger(EODTradingStrategy.class);

    protected InteractiveBrokersClientInterface ibClient;
    protected Map<Ticker, Ticker> longShortPairMap = new HashMap<>();
    protected Map<Ticker, Double> lastPriceMap = new HashMap<>();
    protected String ibHost;
    protected int ibPort;
    protected int ibClientId;
    protected String strategyDirectory;
    protected TradeOrder.Type entryOrderType;
    protected TradeOrder.Type exitOrderType;
    protected int exitSeconds = 0;

    protected int orderSizeInDollars;
    protected boolean ordersPlaced = false;
    protected boolean allPricesInitialized = false;
    protected LocalTime timeToPlaceOrders;
    protected LocalTime marketCloseTime;
    protected LocalTime exitTime;
    protected EODSystemProperties systemProperties;
    protected IReportGenerator reportGenerator;

    public void start(String propFile) {
        initProps(propFile);
        ibClient = InteractiveBrokersClient.getInstance(ibHost, ibPort, ibClientId);
        logger.info("Connecting to IB client at:" + ibHost + ":" + ibPort + " with clientID: " + ibClientId);

        reportGenerator = getReportGenerator(strategyDirectory);
        ibClient.addOrderStatusListener(this);
        ibClient.addOrderStatusListener(reportGenerator);
        ibClient.connect();

        List<TradeOrder> openOrders = ibClient.getOpenOrders();
        logger.debug("Found " + openOrders.size() + " open orders");

        ordersPlaced = checkOpenOrders(openOrders, longShortPairMap);
        logger.info("Checking if orders have already been placed today: " + ordersPlaced);
        logger.info("Strategy will place orders after: " + timeToPlaceOrders.toString());

        longShortPairMap.keySet().stream().map((ticker) -> {
            ibClient.subscribeLevel1(ticker, this);
            logger.info("Subscribing to: " + ticker);
            return ticker;
        }).forEach((ticker) -> {
            logger.info("Subscribing to: " + longShortPairMap.get(ticker));
            ibClient.subscribeLevel1(longShortPairMap.get(ticker), this);
        });

    }

    public synchronized void placeMOCOrders(Ticker longTicker, Ticker shortTicker, ZonedDateTime lastReportedTime) {

        String correlationId = getUUID();
        BigDecimal longSize = getOrderSize(longTicker);
        BigDecimal shortSize = getOrderSize(shortTicker);

        logger.info("Placing long orders for: " + longSize + " shares of: " + longTicker);
        logger.info("Placing short orders for: " + shortSize + " shares of: " + shortTicker);

        TradeOrder longOrder = new TradeOrder(ibClient.getNextOrderId(), longTicker, longSize, TradeDirection.BUY);
        longOrder.setType(entryOrderType);
        longOrder.setReference("EOD-Pair-Strategy:" + correlationId + ":Entry:Long*");

        TradeOrder longExitOrder = new TradeOrder(ibClient.getNextOrderId(), longTicker, longSize, TradeDirection.SELL);
        longExitOrder.setType(exitOrderType);
        longExitOrder.setGoodAfterTime(getNextBusinessDay(lastReportedTime));
        longExitOrder.setReference("EOD-Pair-Strategy:" + correlationId + ":Exit:Long*");

        logger.info("Placing long orders: " + longOrder);
        longOrder.addChildOrder(longExitOrder);

        TradeOrder shortOrder = new TradeOrder(ibClient.getNextOrderId(), shortTicker, shortSize,
                TradeDirection.SELL_SHORT);
        shortOrder.setType(entryOrderType);
        shortOrder.setReference("EOD-Pair-Strategy:" + correlationId + ":Entry:Short*");

        TradeOrder shortExitOrder = new TradeOrder(ibClient.getNextOrderId(), shortTicker, shortSize,
                TradeDirection.BUY_TO_COVER);
        shortExitOrder.setType(exitOrderType);
        shortExitOrder.setGoodAfterTime(getNextBusinessDay(lastReportedTime));
        shortExitOrder.setReference("EOD-Pair-Strategy:" + correlationId + ":Exit:Short*");

        logger.info("Placing short orders: " + shortOrder);
        shortOrder.addChildOrder(shortExitOrder);

        ibClient.placeOrder(longOrder);
        ibClient.placeOrder(shortOrder);

    }

    @Override
    public void orderEvent(OrderEvent event) {
        logger.info("Received order event: " + event);
    }

    @Override
    public void quoteRecieved(ILevel1Quote quote) {
        if (quote.containsType(QuoteType.LAST)) {
            lastPriceMap.put(quote.getTicker(), quote.getValue(QuoteType.LAST).doubleValue());
            if (!allPricesInitialized) {
                allPricesInitialized = setAllPricesInitialized();
            }
        } else {
            return;
        }

        LocalTime currentTime = quote.getTimeStamp().withZoneSameInstant(ZoneId.systemDefault()).toLocalTime();
        if (currentTime.isAfter(marketCloseTime)) {
            return;
        }
        if ((currentTime.isBefore(marketCloseTime)) && currentTime.isAfter(timeToPlaceOrders) && !ordersPlaced) {
            if (allPricesInitialized) {
                ordersPlaced = true;
                longShortPairMap.keySet().stream().forEach((longTicker) -> {
                    placeMOCOrders(longTicker, longShortPairMap.get(longTicker), quote.getTimeStamp());
                });
            }
        } else if (!currentTime.isAfter(timeToPlaceOrders)) {
            ordersPlaced = false;
        }
    }

    public ZonedDateTime getNextBusinessDay(ZonedDateTime date) {
        if (exitSeconds != 0) {
            date = date.plusSeconds(exitSeconds);
            return date;
        }
        date = date.plusDays(1);

        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        date = ZonedDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), exitTime.getHour(),
                exitTime.getMinute(), exitTime.getSecond(), 0, ZoneId.systemDefault());
        return date;
    }

    @Override
    public void brokerErrorFired(BrokerError error) {
        logger.error("Error: " + error.getErrorCode() + ": " + error.getMessage());
    }

    protected IReportGenerator getReportGenerator(String homeDir) {
        return new ReportGenerator(homeDir);
    }

    protected void initProps(String filename) {
        try {
            EODSystemProperties props = new EODSystemProperties(filename);
            ibHost = props.getTwsHost();
            ibPort = props.getTwsPort();
            ibClientId = props.getTwsClientId();
            orderSizeInDollars = props.getTradeSizeDollars();
            timeToPlaceOrders = props.getStartTime();
            marketCloseTime = props.getMarketCloseTime();
            exitTime = props.getExitTime();
            Map<String, String> tickers = props.getLongShortTickerMap();
            strategyDirectory = props.getStrategyDirectory();
            tickers.keySet().stream().forEach((ticker) -> {
                longShortPairMap.put(new StockTicker(ticker), new StockTicker(tickers.get(ticker)));
            });
            entryOrderType = props.getEntryOrderType();
            exitOrderType = props.getExitOrderType();
            exitSeconds = props.getExitSeconds();

            logger.info("Loaded properties: " + props);

        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

    }

    protected String getUUID() {
        return UUID.randomUUID().toString();
    }

    protected BigDecimal getOrderSize(Ticker ticker) {
        double lastPrice = lastPriceMap.get(ticker);
        return BigDecimal.valueOf(Math.round(orderSizeInDollars / lastPrice));
    }

    protected boolean setAllPricesInitialized() {
        if (allPricesInitialized) {
            return true;
        }

        for (Ticker ticker : longShortPairMap.keySet()) {
            if (lastPriceMap.get(ticker) == null) {
                return false;
            }
            if (lastPriceMap.get(longShortPairMap.get(ticker)) == null) {
                return false;
            }
        }
        return true;
    }

    protected boolean checkOpenOrders(List<TradeOrder> orders, Map<Ticker, Ticker> tickers) {
        if (orders.isEmpty()) {
            return false;
        }
        for (TradeOrder order : orders) {
            if (order.getType() == TradeOrder.Type.MARKET_ON_CLOSE) {
                for (Ticker ticker : tickers.keySet()) {
                    if (order.getTicker().equals(ticker)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        String propFile = args[0];
        // String propFile =
        // "/Users/RobTerpilowski/Downloads/ZoiData/EodTest/eod.props";
        EODTradingStrategy strategy = new EODTradingStrategy();
        strategy.start(propFile);
    }

}
