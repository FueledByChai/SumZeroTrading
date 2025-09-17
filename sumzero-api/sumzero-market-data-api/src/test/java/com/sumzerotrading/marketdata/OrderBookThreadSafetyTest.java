package com.sumzerotrading.marketdata;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.OrderBook.PriceLevel;

/**
 * Test class to verify thread safety of OrderBook listener management
 */
public class OrderBookThreadSafetyTest {

    @Test
    public void testConcurrentListenerModification() throws InterruptedException {
        Ticker ticker = new Ticker("TEST");
        OrderBook orderBook = new OrderBook(ticker);

        final int numThreads = 10;
        final int operationsPerThread = 100;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch finishLatch = new CountDownLatch(numThreads);
        final AtomicInteger exceptionCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Create test listener
        OrderBookUpdateListener testListener = new OrderBookUpdateListener() {
            @Override
            public void bestBidUpdated(Ticker ticker, BigDecimal bestBid, Double size, ZonedDateTime timestamp) {
                // Simulate some processing time
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            @Override
            public void bestAskUpdated(Ticker ticker, BigDecimal bestAsk, Double size, ZonedDateTime timestamp) {
                // Simulate some processing time
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            @Override
            public void orderBookImbalanceUpdated(Ticker ticker, BigDecimal imbalance, ZonedDateTime timestamp) {
                // Simulate some processing time
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            @Override
            public void orderBookUpdated(Ticker ticker, IOrderBook book, ZonedDateTime timestamp) {
                // Simulate some processing time
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        // Submit concurrent tasks that add/remove listeners and trigger notifications
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    for (int j = 0; j < operationsPerThread; j++) {
                        try {
                            if (threadId % 2 == 0) {
                                // Even threads add and remove listeners
                                orderBook.addOrderBookUpdateListener(testListener);
                                Thread.sleep(1); // Small delay to increase contention
                                orderBook.removeOrderBookUpdateListener(testListener);
                            } else {
                                // Odd threads trigger order book updates (which iterate over listeners)
                                // Simulate order book updates by accessing internal methods
                                orderBook.buySide.insert(new BigDecimal("100.0"), 10.0, ZonedDateTime.now());
                                orderBook.sellSide.insert(new BigDecimal("101.0"), 5.0, ZonedDateTime.now());
                            }
                        } catch (Exception e) {
                            exceptionCount.incrementAndGet();
                            e.printStackTrace();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Start all threads
        startLatch.countDown();

        // Wait for all threads to complete
        assertTrue("Test should complete within 30 seconds", finishLatch.await(30, TimeUnit.SECONDS));

        executor.shutdown();
        assertTrue("Executor should terminate within 5 seconds", executor.awaitTermination(5, TimeUnit.SECONDS));

        // Verify no ConcurrentModificationExceptions occurred
        assertEquals("No exceptions should have occurred during concurrent operations", 0, exceptionCount.get());
    }

    @Test
    public void testHighFrequencyListenerNotifications() throws InterruptedException {
        Ticker ticker = new Ticker("HIGHFREQ");
        OrderBook orderBook = new OrderBook(ticker);

        final AtomicInteger notificationCount = new AtomicInteger(0);
        final AtomicInteger exceptionCount = new AtomicInteger(0);

        // Add a simple test listener
        OrderBookUpdateListener listener = new OrderBookUpdateListener() {
            @Override
            public void bestBidUpdated(Ticker ticker, BigDecimal bestBid, Double size, ZonedDateTime timestamp) {
                notificationCount.incrementAndGet();
            }

            @Override
            public void bestAskUpdated(Ticker ticker, BigDecimal bestAsk, Double size, ZonedDateTime timestamp) {
                notificationCount.incrementAndGet();
            }

            @Override
            public void orderBookImbalanceUpdated(Ticker ticker, BigDecimal imbalance, ZonedDateTime timestamp) {
                notificationCount.incrementAndGet();
            }

            @Override
            public void orderBookUpdated(Ticker ticker, IOrderBook book, ZonedDateTime timestamp) {
                notificationCount.incrementAndGet();
            }
        };

        orderBook.addOrderBookUpdateListener(listener);

        // Simulate high-frequency updates
        final int numUpdates = 1000;
        final CountDownLatch latch = new CountDownLatch(numUpdates);

        ExecutorService executor = Executors.newFixedThreadPool(4);

        for (int i = 0; i < numUpdates; i++) {
            final BigDecimal bidPrice = new BigDecimal("100." + String.format("%03d", i % 1000));
            final BigDecimal askPrice = bidPrice.add(new BigDecimal("0.01"));
            executor.submit(() -> {
                try {
                    // Use snapshot update instead of direct side manipulation
                    List<PriceLevel> bids = new ArrayList<>();
                    List<PriceLevel> asks = new ArrayList<>();
                    bids.add(new PriceLevel(bidPrice, 10.0));
                    asks.add(new PriceLevel(askPrice, 10.0));
                    orderBook.updateFromSnapshot(bids, asks, ZonedDateTime.now());
                } catch (Exception e) {
                    exceptionCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue("All updates should complete within 10 seconds", latch.await(10, TimeUnit.SECONDS));

        executor.shutdown();
        assertTrue("Executor should terminate within 5 seconds", executor.awaitTermination(5, TimeUnit.SECONDS));

        // Wait a bit for async notifications to complete
        Thread.sleep(1000);

        assertEquals("No exceptions should have occurred during high-frequency updates", 0, exceptionCount.get());

        assertTrue("Should have received notifications", notificationCount.get() > 0);
    }
}
