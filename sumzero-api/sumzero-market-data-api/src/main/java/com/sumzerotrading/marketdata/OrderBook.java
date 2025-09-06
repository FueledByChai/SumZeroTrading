package com.sumzerotrading.marketdata;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.data.Ticker;

public class OrderBook implements IOrderBook {

    protected static final Logger logger = LoggerFactory.getLogger(OrderBook.class);
    protected final OrderBookSide buySide;
    protected final OrderBookSide sellSide;
    protected boolean initialized = false;
    protected BigDecimal tickSize;
    protected BigDecimal bestBid = BigDecimal.ZERO; // Best bid price
    protected BigDecimal bestAsk = BigDecimal.ZERO; // Best ask price
    protected List<OrderBookUpdateListener> orderbookUpdateListeners = new ArrayList<>();
    protected Ticker ticker;
    protected double obiLambda = 0.75; // Default lambda for OBI calculation

    // Executor for asynchronous Level1Quote listener notifications
    protected volatile ExecutorService listenerExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "OrderBook-listener-" + System.nanoTime());
            t.setDaemon(true);
            return t;
        }
    });

    /**
     * Build a new order book for the specified ticker
     * 
     * @param ticker
     */
    public OrderBook(Ticker ticker) {
        this(ticker, ticker.getMinimumTickSize());

    }

    /**
     * Build a order book agreegated by the specified tickSize
     * 
     * @param ticker
     * @param tickSize
     */
    public OrderBook(Ticker ticker, BigDecimal tickSize) {
        this.ticker = ticker;
        this.buySide = new OrderBookSide(true); // true for descending order
        this.sellSide = new OrderBookSide(false); // false for ascending order
        this.tickSize = tickSize;
    }

    @Override
    public void clearOrderBook() {
        buySide.clear();
        sellSide.clear();
        initialized = false;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public BigDecimal getBestBid() {
        return getBestBid(tickSize);
    }

    @Override
    public BigDecimal getBestBid(BigDecimal tickSize) {
        return buySide.getBestPrice(tickSize);
    }

    @Override
    public BigDecimal getBestAsk() {
        return getBestAsk(tickSize);
    }

    @Override
    public BigDecimal getBestAsk(BigDecimal tickSize) {
        return sellSide.getBestPrice(tickSize);
    }

    @Override
    public BigDecimal getMidpoint() {
        return getMidpoint(tickSize);
    }

    @Override
    public BigDecimal getMidpoint(BigDecimal tickSize) {
        BigDecimal bestBid = getBestBid(tickSize);
        BigDecimal bestAsk = getBestAsk(tickSize);

        if (bestBid == null || bestAsk == null) {
            return null;
        }

        return bestBid.add(bestAsk).divide(BigDecimal.valueOf(2));
    }

    @Override
    public double calculateWeightedOrderBookImbalance(double lambda) {
        return calculateWeightedOrderBookImbalance(lambda, tickSize);
    }

    @Override
    public double calculateWeightedOrderBookImbalance(double lambda, BigDecimal tickSize) {
        BigDecimal midpoint = getMidpoint(tickSize);
        if (midpoint == null) {
            logger.warn(
                    "OrderBook midpoint is null (no bids or asks). BuySide size: {}, SellSide size: {}. Returning 0 for OBI.",
                    buySide.orders.size(), sellSide.orders.size());
            logger.debug("BuySide keys: {}", buySide.orders.keySet());
            logger.debug("SellSide keys: {}", sellSide.orders.keySet());
        }
        double midPrice = midpoint.doubleValue();
        double totalWeightedBidVolume = buySide.calculateWeightedVolume(midPrice, lambda, tickSize);
        double totalWeightedAskVolume = sellSide.calculateWeightedVolume(midPrice, lambda, tickSize);

        // Avoid division by zero
        double totalWeightedVolume = totalWeightedBidVolume + totalWeightedAskVolume;
        if (totalWeightedVolume == 0) {
            logger.warn("Total weighted volume is zero. BidVol: {}, AskVol: {}", totalWeightedBidVolume,
                    totalWeightedAskVolume);
            return 0.0;
        }

        // Calculate order book imbalance
        return (totalWeightedBidVolume - totalWeightedAskVolume) / totalWeightedVolume * 100.0;
    }

    @Override
    public BigDecimal getCenterOfGravityMidpoint(int levels) {
        return getCenterOfGravityMidpoint(levels, tickSize);
    }

    /**
     * Returns the volume-weighted midpoint for the top N levels at the given tick
     * size.
     */
    @Override
    public BigDecimal getCenterOfGravityMidpoint(int levels, BigDecimal tickSize) {
        // Get top N bids
        Map<BigDecimal, Double> bidLevels = buySide.aggregateOrders(tickSize).entrySet().stream()
                .sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey())) // Descending
                .limit(levels).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Double::sum,
                        java.util.LinkedHashMap::new));

        // Get top N asks
        Map<BigDecimal, Double> askLevels = sellSide.aggregateOrders(tickSize).entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // Ascending
                .limit(levels).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Double::sum,
                        java.util.LinkedHashMap::new));

        double bidVolume = bidLevels.values().stream().mapToDouble(Double::doubleValue).sum();
        double askVolume = askLevels.values().stream().mapToDouble(Double::doubleValue).sum();

        double bidWeightedPrice = bidLevels.entrySet().stream()
                .mapToDouble(e -> e.getKey().doubleValue() * e.getValue()).sum();
        double askWeightedPrice = askLevels.entrySet().stream()
                .mapToDouble(e -> e.getKey().doubleValue() * e.getValue()).sum();

        double totalVolume = bidVolume + askVolume;
        if (totalVolume == 0) {
            return null;
        }

        double vwmid = (bidWeightedPrice + askWeightedPrice) / totalVolume;
        return BigDecimal.valueOf(vwmid).setScale(tickSize.scale(), RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getVWAPMidpoint(int levels) {
        return getVWAPMidpoint(levels, tickSize);
    }

    /**
     * Returns the volume-weighted midpoint for the top N levels at the given tick
     * size.
     */
    @Override
    public BigDecimal getVWAPMidpoint(int levels, BigDecimal tickSize) {
        // Get top N bids
        Map<BigDecimal, Double> bidLevels = buySide.aggregateOrders(tickSize).entrySet().stream()
                .sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey())) // Descending
                .limit(levels).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Double::sum,
                        java.util.LinkedHashMap::new));

        // Get top N asks
        Map<BigDecimal, Double> askLevels = sellSide.aggregateOrders(tickSize).entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // Ascending
                .limit(levels).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Double::sum,
                        java.util.LinkedHashMap::new));

        double bidVolume = bidLevels.values().stream().mapToDouble(Double::doubleValue).sum();
        double askVolume = askLevels.values().stream().mapToDouble(Double::doubleValue).sum();

        double bidWeightedPrice = bidLevels.entrySet().stream()
                .mapToDouble(e -> e.getKey().doubleValue() * e.getValue()).sum();
        double askWeightedPrice = askLevels.entrySet().stream()
                .mapToDouble(e -> e.getKey().doubleValue() * e.getValue()).sum();

        double totalVolume = bidVolume + askVolume;
        if (totalVolume == 0) {
            return null;
        }

        double vwmid = (bidWeightedPrice / bidVolume + askWeightedPrice / askVolume) / 2.0;
        return BigDecimal.valueOf(vwmid).setScale(tickSize.scale(), RoundingMode.HALF_UP);
    }

    @Override
    public void printTopLevels(int levels) {
        printTopLevels(levels, tickSize);
    }

    @Override
    public void printTopLevels(int levels, BigDecimal tickSize) {
        logger.info("Top " + levels + " levels of Buy Side:");
        buySide.printTopLevels(levels, tickSize);

        logger.info("Top " + levels + " levels of Sell Side:");
        sellSide.printTopLevels(levels, tickSize);
    }

    @Override
    public void addOrderBookUpdateListener(OrderBookUpdateListener listener) {
        orderbookUpdateListeners.add(listener);
    }

    @Override
    public void removeOrderBookUpdateListener(OrderBookUpdateListener listener) {
        orderbookUpdateListeners.remove(listener);
    }

    protected void notifyOrderBookUpdateListenersNewBid(BigDecimal bestBid, ZonedDateTime timestamp) {
        try {
            for (OrderBookUpdateListener listener : orderbookUpdateListeners) {
                listenerExecutor.submit(() -> {
                    try {
                        listener.bestBidUpdated(ticker, bestBid, timestamp);
                    } catch (Throwable t) {
                        logger.error("Listener threw exception for {}: {}", ticker, t.getMessage(), t);
                    }
                });
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    protected void notifyOrderBookUpdateListenersNewAsk(BigDecimal bestAsk, ZonedDateTime timestamp) {
        try {
            for (OrderBookUpdateListener listener : orderbookUpdateListeners) {
                listenerExecutor.submit(() -> {
                    try {
                        listener.bestAskUpdated(ticker, bestAsk, timestamp);
                    } catch (Throwable t) {
                        logger.error("Listener threw exception for {}: {}", ticker, t.getMessage(), t);
                    }
                });
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    protected void notifyOrderBookUpdateListenersImbalance(BigDecimal imbalance, ZonedDateTime timestamp) {
        try {
            for (OrderBookUpdateListener listener : orderbookUpdateListeners) {
                listenerExecutor.submit(() -> {
                    try {
                        listener.orderBookImbalanceUpdated(ticker, imbalance, timestamp);
                    } catch (Throwable t) {
                        logger.error("Listener threw exception for {}: {}", ticker, t.getMessage(), t);
                    }
                });
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    protected class OrderBookSide {
        private final ConcurrentHashMap<BigDecimal, Double> orders;
        private final boolean descending;

        OrderBookSide(boolean descending) {
            this.orders = new ConcurrentHashMap<>();
            this.descending = descending;
        }

        public void insert(BigDecimal price, Double size, ZonedDateTime timestamp) {
            orders.put(price, size);
            updateBestPrice(timestamp);
        }

        public void update(BigDecimal price, Double size, ZonedDateTime timestamp) {
            orders.put(price, size);
            updateBestPrice(timestamp);
        }

        public void remove(BigDecimal price, ZonedDateTime timestamp) {
            orders.remove(price);
            updateBestPrice(timestamp);
        }

        public void clear() {
            orders.clear();
        }

        public void printTopLevels(int levels, BigDecimal tickSize) {
            Map<BigDecimal, Double> aggregatedOrders = aggregateOrders(tickSize);
            List<Map.Entry<BigDecimal, Double>> sortedOrders = aggregatedOrders.entrySet().stream()
                    .sorted((entry1, entry2) -> descending ? entry2.getKey().compareTo(entry1.getKey())
                            : entry1.getKey().compareTo(entry2.getKey()))
                    .limit(levels).collect(Collectors.toList());

            for (Map.Entry<BigDecimal, Double> entry : sortedOrders) {
                logger.info("Price: " + entry.getKey() + ", Size: " + entry.getValue());
            }
        }

        public BigDecimal getBestPrice(BigDecimal tickSize) {
            Map<BigDecimal, Double> aggregatedOrders = aggregateOrders(tickSize);
            return aggregatedOrders.keySet().stream().sorted(descending ? (price1, price2) -> price2.compareTo(price1)
                    : (price1, price2) -> price1.compareTo(price2)).findFirst().orElse(null);
        }

        public double calculateWeightedVolume(double midPrice, double lambda, BigDecimal tickSize) {
            Map<BigDecimal, Double> aggregatedOrders = aggregateOrders(tickSize);
            return aggregatedOrders.entrySet().stream().mapToDouble(entry -> {
                double price = entry.getKey().doubleValue();
                double size = entry.getValue();
                double distance = descending ? midPrice - price : price - midPrice;
                double weight = Math.exp(-lambda * distance);
                return weight * size;
            }).sum();
        }

        public Map<BigDecimal, Double> aggregateOrders(BigDecimal tickSize) {
            int scale = tickSize.scale();
            return orders.entrySet().stream().collect(Collectors.toMap(entry -> {
                if (descending) {
                    // For bids, round down
                    return entry.getKey().divide(tickSize).setScale(scale, RoundingMode.DOWN).multiply(tickSize)
                            .setScale(scale, RoundingMode.DOWN);
                } else {
                    // For asks, round up
                    return entry.getKey().divide(tickSize).setScale(scale, RoundingMode.UP).multiply(tickSize)
                            .setScale(scale, RoundingMode.UP);
                }
            }, Map.Entry::getValue, Double::sum, ConcurrentHashMap::new));
        }

        protected void updateBestPrice(ZonedDateTime timestamp) {
            BigDecimal bestPrice = orders.keySet().stream()
                    .sorted(descending ? (price1, price2) -> price2.compareTo(price1)
                            : (price1, price2) -> price1.compareTo(price2))
                    .findFirst().orElse(null);

            if (descending) {
                // Update best bid
                synchronized (OrderBook.this) {
                    if (!bestPrice.equals(bestBid)) {
                        logger.debug("Best Bid updated from {} to {}", bestBid, bestPrice);
                        notifyOrderBookUpdateListenersNewBid(bestPrice, timestamp);
                    }
                    bestBid = bestPrice;
                }
            } else {
                // Update best ask
                synchronized (OrderBook.this) {
                    if (!bestPrice.equals(bestAsk)) {
                        logger.debug("Best Ask updated from {} to {}", bestAsk, bestPrice);
                        notifyOrderBookUpdateListenersNewAsk(bestPrice, timestamp);
                    }
                    bestAsk = bestPrice;
                }
            }
        }
    }
}
