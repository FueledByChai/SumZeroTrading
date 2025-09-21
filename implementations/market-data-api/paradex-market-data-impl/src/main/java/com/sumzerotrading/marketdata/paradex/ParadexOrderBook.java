package com.sumzerotrading.marketdata.paradex;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.OrderBook;

public class ParadexOrderBook extends OrderBook implements IParadexOrderBook {

    private final ScheduledExecutorService notificationScheduler;
    private final AtomicBoolean hasUpdates = new AtomicBoolean(false);
    private final AtomicReference<ZonedDateTime> lastUpdateTimestamp = new AtomicReference<>();
    private static final long NOTIFICATION_INTERVAL_MS = 500; // 500ms between notifications

    public ParadexOrderBook(Ticker ticker) {
        super(ticker);
        this.notificationScheduler = createNotificationScheduler();
        startPeriodicNotifications();
    }

    public ParadexOrderBook(Ticker ticker, BigDecimal tickSize) {
        super(ticker, tickSize);
        this.notificationScheduler = createNotificationScheduler();
        startPeriodicNotifications();
    }

    private ScheduledExecutorService createNotificationScheduler() {
        return Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "ParadexOrderBook-notification-" + ticker.getSymbol());
                t.setDaemon(true);
                return t;
            }
        });
    }

    private void startPeriodicNotifications() {
        notificationScheduler.scheduleAtFixedRate(this::sendPeriodicNotifications, NOTIFICATION_INTERVAL_MS,
                NOTIFICATION_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void sendPeriodicNotifications() {
        // Only send notifications if there have been updates since last notification
        if (hasUpdates.compareAndSet(true, false) && initialized) {
            ZonedDateTime timestamp = lastUpdateTimestamp.get();
            if (timestamp != null) {
                double obi = calculateWeightedOrderBookImbalance(obiLambda);
                super.notifyOrderBookUpdateListenersImbalance(BigDecimal.valueOf(obi), timestamp);
                super.notifyOrderBookUpdateListenersNewOrderBookSnapshot(timestamp);
            }
        }
    }

    private void markUpdated(ZonedDateTime timestamp) {
        hasUpdates.set(true);
        lastUpdateTimestamp.set(timestamp);
    }

    @Override
    public void shutdown() {
        if (notificationScheduler != null && !notificationScheduler.isShutdown()) {
            notificationScheduler.shutdown();
            try {
                if (!notificationScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    notificationScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                notificationScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        super.shutdown();
    }

    @Override
    public void handleSnapshot(Map<String, Object> snapshot, ZonedDateTime timestamp) {
        // Clear existing order book
        buySide.clear();
        sellSide.clear();

        // Process snapshot data
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> inserts = (List<Map<String, Object>>) snapshot.get("inserts");

        for (Map<String, Object> orderData : inserts) {
            BigDecimal price = new BigDecimal((String) orderData.get("price"));
            Double size = Double.parseDouble((String) orderData.get("size"));
            String side = (String) orderData.get("side");

            if ("BUY".equals(side)) {
                buySide.insert(price, size, timestamp);
            } else {
                sellSide.insert(price, size, timestamp);
            }
        }
        initialized = true;
        markUpdated(timestamp);
    }

    @Override
    public void applyDelta(Map<String, Object> delta, ZonedDateTime timestamp) {
        // Process delta data
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> inserts = (List<Map<String, Object>>) delta.get("inserts");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> updates = (List<Map<String, Object>>) delta.get("updates");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> deletes = (List<Map<String, Object>>) delta.get("deletes");

        if (inserts != null) {
            for (Map<String, Object> orderData : inserts) {
                BigDecimal price = new BigDecimal((String) orderData.get("price"));
                Double size = Double.parseDouble((String) orderData.get("size"));
                String side = (String) orderData.get("side");

                if ("BUY".equals(side)) {
                    buySide.insert(price, size, timestamp);
                } else {
                    sellSide.insert(price, size, timestamp);
                }
            }
        }

        if (updates != null) {
            for (Map<String, Object> orderData : updates) {
                BigDecimal price = new BigDecimal((String) orderData.get("price"));
                Double size = Double.parseDouble((String) orderData.get("size"));
                String side = (String) orderData.get("side");

                if ("BUY".equals(side)) {
                    buySide.update(price, size, timestamp);
                } else {
                    sellSide.update(price, size, timestamp);
                }
            }
        }

        if (deletes != null) {
            for (Map<String, Object> orderData : deletes) {
                BigDecimal price = new BigDecimal((String) orderData.get("price"));
                String side = (String) orderData.get("side");

                if ("BUY".equals(side)) {
                    buySide.remove(price, timestamp);
                } else {
                    sellSide.remove(price, timestamp);
                }
            }
        }

        markUpdated(timestamp);
    }

}
