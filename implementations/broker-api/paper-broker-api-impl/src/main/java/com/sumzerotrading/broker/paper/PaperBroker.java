package com.sumzerotrading.broker.paper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.broker.BrokerAccountInfoListener;
import com.sumzerotrading.broker.BrokerErrorListener;
import com.sumzerotrading.broker.IBroker;
import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.order.OrderEvent;
import com.sumzerotrading.broker.order.OrderEventListener;
import com.sumzerotrading.broker.order.OrderStatus;
import com.sumzerotrading.broker.order.OrderStatus.CancelReason;
import com.sumzerotrading.broker.order.OrderStatus.Status;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.broker.order.TradeOrder;
import com.sumzerotrading.broker.order.TradeOrder.Modifier;
import com.sumzerotrading.broker.order.TradeOrder.Type;
import com.sumzerotrading.data.ComboTicker;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.ILevel1Quote;
import com.sumzerotrading.marketdata.Level1QuoteListener;
import com.sumzerotrading.marketdata.OrderBook;
import com.sumzerotrading.marketdata.QuoteEngine;
import com.sumzerotrading.time.TimeUpdatedListener;

public class PaperBroker implements IBroker, Level1QuoteListener {

    protected Logger logger = LoggerFactory.getLogger(PaperBroker.class);

    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Thread pool with 10 threads

    protected String asset;

    private Timer accountUpdateTimer = new Timer(true); // Timer to schedule account updates
    protected double makerFee = 0.005 / 100.0; // 0.005% maker rebate
    protected double takerFee = -0.03 / 100.0; // 0.03% taker fee

    protected double startingAccountBalance = 100000.0; // Starting account balance for paper trading
    protected double currentAccountBalance = startingAccountBalance; // Current account balance

    protected ConcurrentHashMap<String, TradeOrder> openBids = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<String, TradeOrder> openAsks = new ConcurrentHashMap<>();

    protected double bestBidPrice = 0.0; // Best bid price
    protected double bestAskPrice = Double.MAX_VALUE; // Best ask price
    protected double midPrice = 0.0; // Mid price (average of best bid and ask

    protected double markPrice = 0.0;
    protected double fundingRate = 0.0; // Funding rate for the asset
    protected double fundingAccruedOrPaid = 0.0; // Total funding accrued or paid
    protected long lastFundingTimestamp = 0; // Last funding timestamp

    private final Lock bidsLock = new ReentrantLock();
    private final Lock asksLock = new ReentrantLock();

    protected BigDecimal currentPosition = BigDecimal.ZERO; // Current position size
    protected double averageEntryPrice = 0.0; // Average entry price for the current position
    protected double realizedPnL = 0.0; // Closed profit and loss
    protected double unrealizedPnL = 0.0; // Unrealized profit and loss
    protected double totalPnL = 0.0; // Total profit and loss
    protected double totalFees = 0.0; // Total fees paid

    protected List<BrokerAccountInfoListener> brokerAccountInfoListeners = new ArrayList<>(); // Listeners for account
                                                                                              // info updates

    protected List<OrderEventListener> orderEventListeners = new ArrayList<>();

    protected boolean firstTradeWrittenToFile = false;

    protected Map<String, TradeOrder> openOrders = new LinkedHashMap<>(); // Map to hold open orders, ordered by
                                                                          // insertion
    protected final Set<TradeOrder> executedOrders = java.util.Collections
            .synchronizedSet(new TreeSet<TradeOrder>(new java.util.Comparator<TradeOrder>() {
                @Override
                public int compare(TradeOrder o1, TradeOrder o2) {
                    return o2.getOrderEntryTime().compareTo(o1.getOrderEntryTime());
                }
            }));

    protected OrderBook orderBook; // Order book for managing market data

    protected IPaperBrokerStatus brokerStatus; // Broker status for reporting

    // protected ISystemConfig systemConfig; // System configuration for the broker
    protected Ticker ticker;

    protected QuoteEngine quoteEngine;

    protected int totalOrdersPlaced = 0; // Total number of orders placed
    protected double dollarVolume = 0.0; // Total value of orders placed
    String csvFilePath;

    private final Deque<SpreadEntry> spreadHistory = new ArrayDeque<>();
    private final long timeWindowMillis = 6000; // 6 seconds
    private final double dislocationMultiplier = 7.5; // Multiplier for dislocation threshold

    private static class SpreadEntry {
        double spread;
        long timestamp;

        SpreadEntry(double spread, long timestamp) {
            this.spread = spread;
            this.timestamp = timestamp;
        }
    }

    protected void startAccountUpdateTask() {
        logger.warn("PaperBroker @PostConstruct startAccountUpdateTask called: {}", System.identityHashCode(this));
        // Read starting balance from file
        String balanceFilePath = "paperbroker-startingbalance.txt";
        File balanceFile = new File(balanceFilePath);
        if (balanceFile.exists()) {
            try {
                String balanceContent = new String(Files.readAllBytes(Paths.get(balanceFilePath)));
                startingAccountBalance = Double.parseDouble(balanceContent.trim());
                currentAccountBalance = startingAccountBalance;
                logger.info("Starting account balance read from file: {}", startingAccountBalance);
            } catch (Exception e) {
                logger.error("Failed to read starting balance from file. Using default value." + startingAccountBalance,
                        e);
            }
        }

        csvFilePath = generateCsvFilename(ticker.getSymbol());
        asset = ticker.getSymbol();
        brokerStatus.setAsset(asset);
        brokerStatus.setOpenOrders(openOrders.values()); // Set the open orders in the broker status
        brokerStatus.setExecutedOrders(executedOrders);

        accountUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    fireAccountUpdate(); // Call the method once per second
                } catch (Exception e) {
                    logger.error("Error during account update: {}", e.getMessage(), e);
                }

                try {
                    updateStatus(); // Update the broker status every second
                } catch (Exception e) {
                    logger.error("Error updating broker status: {}", e.getMessage(), e);
                }

                try {
                    writeCurrentBalanceToFile(balanceFilePath); // Write the current balance to the file
                } catch (Exception e) {
                    logger.error("Error writing current balance to file: {}", e.getMessage(), e);
                }
            }
        }, 0, 1000); // Schedule with a delay of 0 and period of 1000ms (1 second)
    }

    private void writeCurrentBalanceToFile(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(String.valueOf(currentAccountBalance));
        } catch (IOException e) {
            logger.error("Failed to write current balance to file.", e);
        }
    }

    public void setBrokerStatus(IPaperBrokerStatus brokerStatus) {
        this.brokerStatus = brokerStatus;
    }

    public void markPriceUpdated(String symbol, BigDecimal markPrice, long timestamp) {
        logger.info("Mark Price Updated: {} - {} at {}", symbol, markPrice, timestamp);
        this.markPrice = markPrice.doubleValue();
    }

    public void fundingRateUpdated(String symbol, BigDecimal rate, long currentTimestamp) {
        logger.info("Funding Rate Updated: {} - {} at {}", symbol, fundingRate, currentTimestamp);
        this.fundingRate = rate.doubleValue();
        if (lastFundingTimestamp > 0 && currentTimestamp > lastFundingTimestamp) {
            double hours = (currentTimestamp - lastFundingTimestamp) / 3600000.0;
            // Funding is paid/collected continuously: funding = size * fundingRate * hours
            double fundingThisPeriod = (markPrice * currentPosition.doubleValue()) * (-fundingRate / 8.0) * (hours);
            this.fundingAccruedOrPaid += fundingThisPeriod;
            logger.info("Funding accrued/paid this period: {} total funding: {}", fundingThisPeriod,
                    fundingAccruedOrPaid);
            currentAccountBalance += fundingThisPeriod; // Update account balance with funding
        }
        this.lastFundingTimestamp = currentTimestamp;
    }

    @Override
    public synchronized void cancelAllOrders(Ticker ticker) {
        executorService.submit(() -> {
            delay(); // Simulate network delay
            Iterator<Map.Entry<String, TradeOrder>> bidIterator = openBids.entrySet().iterator();
            while (bidIterator.hasNext()) {
                Map.Entry<String, TradeOrder> entry = bidIterator.next();
                try {
                    cancelOrderSubmitWithDelay(entry.getKey(), false); // Call cancelOrder for each order ID
                    bidIterator.remove(); // Remove the entry from the map
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

            // Safely iterate over openAsks using an iterator
            Iterator<Map.Entry<String, TradeOrder>> askIterator = openAsks.entrySet().iterator();
            while (askIterator.hasNext()) {
                Map.Entry<String, TradeOrder> entry = askIterator.next();
                try {
                    cancelOrderSubmitWithDelay(entry.getKey(), false); // Call cancelOrder for each order ID
                    askIterator.remove(); // Remove the entry from the map
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }

    @Override
    public synchronized void cancelOrder(String orderId) {
        executorService.submit(() -> {
            cancelOrderSubmitWithDelay(orderId, true); // Call the method with no delay
        });
    }

    public void cancelOrderSubmitWithDelay(String orderId, boolean shouldDelay) {

        if (shouldDelay) {
            delay(); // Simulate network delay
        }

        if (openBids.containsKey(orderId)) {
            TradeOrder order = openBids.remove(orderId); // Remove the order from open bids
            if (order != null) {
                cancelOrder(orderId, CancelReason.USER_CANCELED); // Notify listeners of cancellation
            }
            logger.info("Order {} cancelled from bids.", orderId);
        } else if (openAsks.containsKey(orderId)) {
            TradeOrder order = openAsks.remove(orderId); // Remove the order from open asks
            if (order != null) {
                cancelOrder(orderId, CancelReason.USER_CANCELED); // Notify listeners of cancellation
            }
            logger.info("Order {} cancelled from asks.", orderId);
        } else {
            logger.warn("Order {} not found in either bids or asks.", orderId);
        }

    }

    public List<TradeOrder> getOpenOrders(Ticker ticker) {
        delay(); // Simulate network delay
        List<TradeOrder> openOrders = new ArrayList<>(); // List to hold open orders
        // Add all open bids to the list
        for (TradeOrder order : openBids.values()) {
            openOrders.add(order);
        }
        // Add all open asks to the list
        for (TradeOrder order : openAsks.values()) {
            openOrders.add(order);
        }

        return openOrders; // Return the list of open orders
    }

    @Override
    public List<Position> getAllPositions() {
        delay(); // Simulate network delay
        List<Position> positions = new ArrayList<>(); // List to hold position info
        if (!currentPosition.equals(BigDecimal.ZERO)) { // Only add position if there is an inventory
            Position position = new Position(ticker, currentPosition, BigDecimal.valueOf(averageEntryPrice));
            positions.add(position);
        }
        return positions; // Return the list of positions
    }

    @Override
    public void placeOrder(TradeOrder order) {

        String orderId = System.currentTimeMillis() + "-" + (int) (Math.random() * 10000); // Generate a unique order ID
        order.setOrderId(orderId); // Set the generated order ID
        order.setOrderEntryTime(ZonedDateTime.now());
        openOrders.put(orderId, order); // Add the order to open orders

        executorService.submit(() -> {
            try {
                delay(); // Simulate network delay
                if (order.getType() == Type.LIMIT) {
                    if (order.getDirection() == TradeDirection.BUY) {
                        if (order.containsModifier(Modifier.POST_ONLY)
                                && order.getLimitPrice().doubleValue() >= bestAskPrice) {
                            logger.warn("Limit buy order would cross the best ask price. Cancelling order.");
                            cancelOrder(orderId, CancelReason.POST_ONLY_WOULD_CROSS);
                            return;
                        }
                        openBids.put(orderId, order);
                        logger.info("Limit buy order placed: {}", order);
                    } else if (order.getDirection() == TradeDirection.SELL) {
                        if (order.containsModifier(Modifier.POST_ONLY)
                                && order.getLimitPrice().doubleValue() <= bestBidPrice) {
                            logger.warn("Limit sell order would cross the best bid price. Cancelling order.");
                            cancelOrder(orderId, CancelReason.POST_ONLY_WOULD_CROSS);
                            return;
                        }
                        openAsks.put(orderId, order);
                        logger.info("Limit sell order placed: {}", order);
                    }
                } else if (order.getType() == Type.MARKET) {
                    if (order.getDirection() == TradeDirection.BUY) {
                        fillOrder(order, bestAskPrice);
                        logger.info("Market buy executed: {}", order);
                    } else if (order.getDirection() == TradeDirection.SELL) {
                        fillOrder(order, bestBidPrice);
                        logger.info("Market sell executed: {}", order);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        // return orderId; // Return immediately
    }

    @Override
    public void cancelAllOrders() {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    public double getUnrealizedPnL() {
        if (markPrice == 0 || currentPosition.doubleValue() == 0) {
            return 0.0; // Avoid division by zero if markPrice or position size is zero
        } else {
            return (markPrice - averageEntryPrice) * currentPosition.doubleValue(); // Calculate unrealized PnL
        }

    }

    public double getNetAccountValue() {
        return currentAccountBalance + getUnrealizedPnL(); // Calculate net account value
    }

    protected boolean isSpreadDislocated() {
        double spread = bestAskPrice - bestBidPrice;
        if (midPrice == 0) {
            return false; // Avoid division by zero if midPrice is not initialized
        }

        long currentTime = System.currentTimeMillis();

        synchronized (spreadHistory) {
            // Remove outdated entries from the spread history, but keep a minimum of 20
            // items
            while (spreadHistory.size() > 20 && !spreadHistory.isEmpty()
                    && (currentTime - spreadHistory.peekFirst().timestamp > timeWindowMillis)) {
                spreadHistory.removeFirst();
            }

            // Add the current spread to the history
            spreadHistory.addLast(new SpreadEntry(spread, currentTime));

            // Calculate the moving average of spreads within the time window
            double movingAverageSpread = spreadHistory.stream().filter(entry -> entry != null) // Filter out null
                                                                                               // entries
                    .mapToDouble(entry -> entry.spread).average().orElse(0.0);

            // Determine if the current spread exceeds the dislocation threshold
            double spreadThreshold = movingAverageSpread * dislocationMultiplier;

            if (spread > spreadThreshold) {
                logger.warn("Spread dislocation detected: {} > {}", spread, spreadThreshold);
                logger.warn("Best Bid: {}, Best Ask: {}, Mid Price: {}", bestBidPrice, bestAskPrice, midPrice);
            }

            return spread > spreadThreshold;
        }
    }

    public void askUpdated(BigDecimal newAsk) {
        bestAskPrice = newAsk.doubleValue();

        checkBidsFills(newAsk.doubleValue()); // Check for filled orders based on the new ask price

    }

    public void bidUpdated(BigDecimal newBid) {
        bestBidPrice = newBid.doubleValue();
        checkAsksFills(newBid.doubleValue()); // Check for filled orders based on the new bid price

    }

    protected List<TradeOrder> checkAsksFills(double bestBid) {
        List<TradeOrder> filledOrders = new ArrayList<>();
        bidsLock.lock(); // Lock the bids to ensure thread safety
        try {
            bestBidPrice = bestBid; // Update the best ask price
            midPrice = (bestBidPrice + bestAskPrice) / 2.0; // Update the mid price

            for (Iterator<Map.Entry<String, TradeOrder>> it = openAsks.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, TradeOrder> entry = it.next();
                TradeOrder order = entry.getValue();

                boolean filled = order.getLimitPrice().doubleValue() < bestBidPrice; // Check if the order can be filled
                                                                                     // at
                                                                                     // the best bid price

                if (filled) {
                    it.remove();
                    fillOrder(order, order.getLimitPrice().doubleValue()); // Fill the order at the best ask price
                    filledOrders.add(order);
                }
            }
        } finally {
            bidsLock.unlock(); // Ensure the lock is released after processing
        }

        return filledOrders;
    }

    protected List<TradeOrder> checkBidsFills(double askPrice) {
        List<TradeOrder> filledOrders = new ArrayList<>();
        asksLock.lock(); // Lock the asks to ensure thread safety
        try {
            bestAskPrice = askPrice; // Update the best bid price
            midPrice = (bestBidPrice + bestAskPrice) / 2.0; // Update the mid price

            for (Iterator<Map.Entry<String, TradeOrder>> it = openBids.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, TradeOrder> entry = it.next();
                TradeOrder order = entry.getValue();

                boolean filled = order.getLimitPrice().doubleValue() > askPrice; // Check if the order can be filled at
                                                                                 // the
                                                                                 // best ask price

                if (filled) {
                    it.remove();
                    fillOrder(order, order.getLimitPrice().doubleValue()); // Fill the order at the best ask price
                    filledOrders.add(order);
                }
            }
        } finally {
            asksLock.unlock(); // Ensure the lock is released after processing
        }

        return filledOrders;
    }

    protected void updateStatus() {
        synchronized (brokerStatus) { // Ensure thread-safe access
            brokerStatus.setCurrentPosition(currentPosition.doubleValue()); // Update current position
            brokerStatus.setAccountValue(getNetAccountValue()); // Update account value
            brokerStatus.setDollarVolume(dollarVolume); // Calculate dollar volume
            brokerStatus.setFeesCollectedOrPaid(totalFees); // Update total fees paidasdfasdf
            brokerStatus.setTotalTrades(totalOrdersPlaced); // Update total trades
            brokerStatus.setBestAsk(bestAskPrice); // Update best ask price
            brokerStatus.setBestBid(bestBidPrice); // Update best bid price
            brokerStatus.setMidPoint(midPrice); // Update mid price
            brokerStatus.setVWAPMidpoint(orderBook.getVWAPMidpoint(30).doubleValue()); // Update VWAP midpoint from
            brokerStatus.setCOGMidpoint(orderBook.getCenterOfGravityMidpoint(30).doubleValue()); // Update COG midpoint
                                                                                                 // from order
            brokerStatus.setRealizedPnL(realizedPnL); // Update realized PnL
            brokerStatus.setUnrealizedPnL(getUnrealizedPnL()); // Update unrealized PnL
            brokerStatus.setTotalPnL(realizedPnL + getUnrealizedPnL()); // Update total PnL
            brokerStatus.setPnlWithFees(realizedPnL + getUnrealizedPnL() + totalFees); // Update PnL with fees
            brokerStatus.setPnlWithFeesAndFunding(fundingAccruedOrPaid + realizedPnL + getUnrealizedPnL() + totalFees); // Update
            brokerStatus.setOpenOrdersCount(openBids.size() + openAsks.size()); // Update the count of open orders
            brokerStatus.setFundingAccruedOrPaid(fundingAccruedOrPaid); // Update total funding paid or collected
            brokerStatus.setFundingRateAnnualized(((fundingRate / 8.0) * 24.0 * 365.0) * 100.0);

            logger.info("Broker status updated: {}", brokerStatus); // Log the updated status
        }
    }

    protected void fillOrder(TradeOrder order, double price) {
        logger.debug("Attempting to remove order with ID: {} from openOrders", order.getOrderId());
        if (openOrders.containsKey(order.getOrderId())) {
            logger.debug("Order with ID: {} exists in openOrders before removal", order.getOrderId());
        } else {
            logger.debug("Order with ID: {} does not exist in openOrders before removal", order.getOrderId());
        }
        openOrders.remove(order.getOrderId());
        if (openOrders.containsKey(order.getOrderId())) {
            logger.debug("Order with ID: {} still exists in openOrders after removal", order.getOrderId());
        } else {
            logger.debug("Order with ID: {} successfully removed from openOrders", order.getOrderId());
        }
        String orderId = order.getOrderId();
        BigDecimal remainingSize = BigDecimal.ZERO;
        BigDecimal originalSize = order.getSize();
        BigDecimal averageFillPrice = BigDecimal.valueOf(price); // Use the fill price
        order.setFilledPrice(averageFillPrice);
        order.setFilledSize(originalSize);
        order.setCurrentStatus(Status.FILLED);
        order.setOrderFilledTime(getCurrentTime());
        // Increment the total number of orders placed

        logger.info("Filling order: {} at price: {} with size: {}", order, price, order.getSize());

        try {
            updatePosition(order.getTradeDirection(), order.getSize(), price, order.getType() != Type.MARKET);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        OrderStatus orderStatus = new OrderStatus(Status.FILLED, orderId, orderId, originalSize, remainingSize,
                averageFillPrice, ticker, getCurrentTime());
        OrderEvent event = new OrderEvent(order, orderStatus);

        executedOrders.add(order);
        fireOrderStatusUpdate(event); // Notify listeners of the order status update

        writeTradeToCsv(order, price, calcFee(price, order.getSize().doubleValue(), order.getType() != Type.MARKET));

    }

    protected synchronized void cancelOrder(String orderId, CancelReason reason) {
        logger.debug("Attempting to remove order with ID: {} from openOrders", orderId);
        TradeOrder order = openOrders.remove(orderId);
        BigDecimal remainingSize = BigDecimal.ZERO;

        Status status = OrderStatus.Status.CANCELED;
        String cancelReasonAsString = reason.name(); // Use the name of the cancel reason enum
        BigDecimal averageFillPrice = BigDecimal.ZERO; // No fill price since it's cancelled

        OrderStatus orderStatus = new OrderStatus(status, orderId, orderId, averageFillPrice, remainingSize,
                averageFillPrice, ticker, getCurrentTime());
        orderStatus.setCancelReason(reason);

        OrderEvent event = new OrderEvent(order, orderStatus);

        fireOrderStatusUpdate(event); // Notify listeners of the cancellation
    }

    protected void fireOrderStatusUpdate(OrderEvent orderStatus) {
        for (OrderEventListener listener : orderEventListeners) {
            try {
                executorService.submit(() -> {
                    try {
                        listener.orderEvent(orderStatus);
                    } catch (Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }
                });
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
    }

    protected void fireAccountUpdate() {
        for (BrokerAccountInfoListener listener : brokerAccountInfoListeners) {
            try {
                executorService.submit(() -> {
                    try {
                        listener.accountEquityUpdated(getNetAccountValue()); // Notify the listener with the account
                                                                             // equity update
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                });
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    protected void updatePosition(TradeDirection side, BigDecimal orderSize, double price, boolean isMaker) {
        BigDecimal size = orderSize; // Use the initial size passed to the method
        double notional = size.doubleValue() * price;
        if (side == TradeDirection.BUY) {
            if (currentPosition.compareTo(BigDecimal.ZERO) < 0) {
                BigDecimal closingSize = currentPosition.negate().min(size);

                double pnl = (averageEntryPrice - price) * closingSize.doubleValue();
                realizedPnL += pnl;
                currentAccountBalance += pnl; // Add realized PnL to the account balance
                currentPosition = currentPosition.add(closingSize);
                size = size.subtract(closingSize);

                if (currentPosition.compareTo(BigDecimal.ZERO) == 0)
                    averageEntryPrice = 0;

                if (size.compareTo(BigDecimal.ZERO) > 0) {
                    averageEntryPrice = price;
                    // currentAccountBalance -= size * price;
                    currentPosition = currentPosition.add(size);
                }
            } else {
                averageEntryPrice = ((averageEntryPrice * currentPosition.doubleValue()) + (price * size.doubleValue()))
                        / (currentPosition.doubleValue() + size.doubleValue());
                // currentAccountBalance -= size * price;
                currentPosition = currentPosition.add(size);
            }
        } else { // SELL
            if (currentPosition.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal closingSize = currentPosition.min(size);
                double pnl = (price - averageEntryPrice) * closingSize.doubleValue();
                realizedPnL += pnl;
                currentAccountBalance += pnl; // Add realized PnL to the account balance
                currentPosition = currentPosition.subtract(closingSize);
                size = size.subtract(closingSize);

                if (currentPosition.compareTo(BigDecimal.ZERO) == 0)
                    averageEntryPrice = 0;

                if (size.compareTo(BigDecimal.ZERO) > 0) {
                    averageEntryPrice = price;
                    // currentAccountBalance += size * price;
                    currentPosition = currentPosition.subtract(size);
                }
            } else {
                averageEntryPrice = ((averageEntryPrice * -currentPosition.doubleValue())
                        + (price * size.doubleValue())) / (-currentPosition.doubleValue() + size.doubleValue());
                // currentAccountBalance += size * price;
                currentPosition = currentPosition.subtract(size);
            }
        }

        // Apply maker or taker fee

        double fee = calcFee(price, orderSize.doubleValue(), isMaker);
        currentAccountBalance += fee;
        totalFees += fee; // Update total fees paid

        dollarVolume += Math.abs(notional);
        brokerStatus.setDollarVolume(dollarVolume); // Update the dollar volume in the broker status
        totalOrdersPlaced++;
        brokerStatus.setTotalTrades(totalOrdersPlaced); // Update total trades in the broker status
        brokerStatus.setFeesCollectedOrPaid(totalFees); // Update total fees in the broker status
        brokerStatus.setAccountValue(getNetAccountValue()); // Update the account value in the broker status

    }

    protected double calcFee(double price, double size, boolean isMaker) {
        double notional = Math.abs(price * size); // Calculate the notional value of the trade
        double feeRate = isMaker ? makerFee : takerFee; // Determine the fee rate based on order type
        return notional * feeRate; // Calculate and return the fee
    }

    protected void writeTradeToCsv(TradeOrder order, double price, double fee) {
        executorService.submit(() -> {
            try {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath, true))) {

                    if (!firstTradeWrittenToFile) {
                        // Write the header only once
                        writer.write("####");
                        writer.newLine();
                        firstTradeWrittenToFile = true;
                    }

                    // Prepare the trade details as a CSV line
                    String csvLine = String.format("%s,%s,%s,%.5f,%s,%s,%s,%.5f,%.5f,%d", asset, order.getOrderId(), // Order
                                                                                                                     // ID
                            order.getDirection(), // Side (BUY/SELL)
                            order.getSize(), // Size
                            order.getType(), order.getOrderEntryTime().format(DateTimeFormatter.ISO_INSTANT), // Submitted
                                                                                                              // Time
                            order.getOrderFilledTime().format(DateTimeFormatter.ISO_INSTANT), // Filled Time
                            price, // Price
                            fee, // Fee
                            System.currentTimeMillis()); // Timestamp

                    writer.write(csvLine); // Write the line to the file
                    writer.newLine(); // Add a newline
                } catch (IOException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

    }

    protected String generateCsvFilename(String symbol) {
        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();

        // Format the date and time as yyyyMMdd-HHmm
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
        String timestamp = now.format(formatter);

        // Construct the filename
        return String.format("%s-%s-Trades.csv", timestamp, symbol);
    }

    protected void delay() {
        // Simulate network delay or processing time
        try {
            Thread.sleep(500); // 1 second delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            System.err.println("Delay interrupted: " + e.getMessage());
        }
    }

    @Override
    public void addBrokerAccountInfoListener(BrokerAccountInfoListener listener) {
        brokerAccountInfoListeners.add(listener);
    }

    @Override
    public void addBrokerErrorListener(BrokerErrorListener listener) {
        throw new UnsupportedOperationException("Not yet supported in Paper Broker");

    }

    @Override
    public void addOrderEventListener(OrderEventListener listener) {
        orderEventListeners.add(listener);

    }

    @Override
    public void addTimeUpdateListener(TimeUpdatedListener listener) {
        throw new UnsupportedOperationException("Not yet supported in Paper Broker");

    }

    @Override
    public void aquireLock() {
        throw new UnsupportedOperationException("aquireLock not supported in PaperBroker");
    }

    @Override
    public ComboTicker buildComboTicker(Ticker ticker1, Ticker ticker2) {
        throw new UnsupportedOperationException("Not yet supported in Paper Broker");
    }

    @Override
    public ComboTicker buildComboTicker(Ticker ticker1, int ratio1, Ticker ticker2, int ratio2) {
        throw new UnsupportedOperationException("Not yet supported in Paper Broker");
    }

    @Override
    public void cancelAndReplaceOrder(String originalOrderId, TradeOrder newOrder) {
        throw new UnsupportedOperationException("Cancel and replace not supported in PaperBroker");
    }

    @Override
    public void cancelOrder(TradeOrder order) {
        throw new UnsupportedOperationException("Cancel order not supported in PaperBroker");

    }

    @Override
    public void connect() {
        throw new UnsupportedOperationException("Connect not supported in PaperBroker");
    }

    @Override
    public void disconnect() {
        throw new UnsupportedOperationException("Disconnect not supported in PaperBroker");

    }

    @Override
    public ZonedDateTime getCurrentTime() {
        throw new UnsupportedOperationException("getCurrentTime not supported in PaperBroker");
    }

    @Override
    public String getFormattedDate(int hour, int minute, int second) {
        throw new UnsupportedOperationException("getFormattedDate not supported in PaperBroker");
    }

    @Override
    public String getFormattedDate(ZonedDateTime date) {
        throw new UnsupportedOperationException("getFormattedDate not supported in PaperBroker");
    }

    @Override
    public String getNextOrderId() {
        return "";
    }

    @Override
    public List<TradeOrder> getOpenOrders() {
        throw new UnsupportedOperationException("getOpenOrders not supported in PaperBroker");
    }

    @Override
    public boolean isConnected() {
        throw new UnsupportedOperationException("isConnected not supported in PaperBroker");
    }

    @Override
    public void releaseLock() {
        throw new UnsupportedOperationException("releaseLock not supported in PaperBroker");
    }

    @Override
    public void removeBrokerAccountInfoListener(BrokerAccountInfoListener listener) {
        brokerAccountInfoListeners.remove(listener);

    }

    @Override
    public void removeBrokerErrorListener(BrokerErrorListener listener) {
        throw new UnsupportedOperationException("removeBrokerErrorListener not supported in PaperBroker");
    }

    @Override
    public void removeOrderEventListener(OrderEventListener listener) {
        orderEventListeners.remove(listener);

    }

    @Override
    public void removeTimeUpdateListener(TimeUpdatedListener listener) {
        throw new UnsupportedOperationException("removeTimeUpdateListener not supported in PaperBroker");

    }

    @Override
    public TradeOrder requestOrderStatus(String orderId) {
        throw new UnsupportedOperationException("requestOrderStatus not supported in PaperBroker");
    }

    @Override
    public void quoteRecieved(ILevel1Quote quote) {
        throw new UnsupportedOperationException("quoteRecieved not supported in PaperBroker");
    }

}
