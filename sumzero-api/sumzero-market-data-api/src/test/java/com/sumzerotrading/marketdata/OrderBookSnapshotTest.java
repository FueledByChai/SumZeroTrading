package com.sumzerotrading.marketdata;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.OrderBook.PriceLevel;

public class OrderBookSnapshotTest {

    private OrderBook orderBook;
    private Ticker ticker;
    private ZonedDateTime timestamp;

    @Before
    public void setUp() {
        ticker = new Ticker("TEST");
        ticker.setMinimumTickSize(new BigDecimal("0.01"));
        orderBook = new OrderBook(ticker);
        timestamp = ZonedDateTime.now(ZoneId.of("GMT"));
    }

    @Test
    public void testBasicSnapshotUpdate() {
        // Arrange
        List<PriceLevel> bids = new ArrayList<>();
        List<PriceLevel> asks = new ArrayList<>();

        bids.add(new PriceLevel(new BigDecimal("100.50"), 500.0));
        bids.add(new PriceLevel(new BigDecimal("100.49"), 300.0));
        bids.add(new PriceLevel(new BigDecimal("100.48"), 200.0));

        asks.add(new PriceLevel(new BigDecimal("100.51"), 400.0));
        asks.add(new PriceLevel(new BigDecimal("100.52"), 600.0));
        asks.add(new PriceLevel(new BigDecimal("100.53"), 100.0));

        // Act
        orderBook.updateFromSnapshot(bids, asks, timestamp);

        // Assert
        assertTrue(orderBook.isInitialized());
        assertEquals(new BigDecimal("100.50"), orderBook.getBestBid());
        assertEquals(new BigDecimal("100.51"), orderBook.getBestAsk());
        assertEquals(new BigDecimal("100.505"), orderBook.getMidpoint());
    }

    @Test
    public void testArraySnapshotUpdate() {
        // Arrange
        BigDecimal[] bidPrices = { new BigDecimal("99.95"), new BigDecimal("99.94"), new BigDecimal("99.93") };
        Double[] bidSizes = { 1000.0, 800.0, 600.0 };
        BigDecimal[] askPrices = { new BigDecimal("99.96"), new BigDecimal("99.97"), new BigDecimal("99.98") };
        Double[] askSizes = { 700.0, 900.0, 500.0 };

        // Act
        orderBook.updateFromSnapshot(bidPrices, bidSizes, askPrices, askSizes, timestamp);

        // Assert
        assertTrue(orderBook.isInitialized());
        assertEquals(new BigDecimal("99.95"), orderBook.getBestBid());
        assertEquals(new BigDecimal("99.96"), orderBook.getBestAsk());
        assertEquals(new BigDecimal("99.955"), orderBook.getMidpoint());
    }

    @Test
    public void testAtomicSnapshotUpdate() throws InterruptedException {
        // Test that readers see consistent state during updates
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(1);
        AtomicInteger consistentReads = new AtomicInteger(0);
        AtomicInteger totalReads = new AtomicInteger(0);

        // Initial snapshot
        List<PriceLevel> initialBids = new ArrayList<>();
        List<PriceLevel> initialAsks = new ArrayList<>();
        initialBids.add(new PriceLevel(new BigDecimal("100.00"), 1000.0));
        initialAsks.add(new PriceLevel(new BigDecimal("100.01"), 1000.0));
        orderBook.updateFromSnapshot(initialBids, initialAsks, timestamp);

        // Reader thread
        Thread readerThread = new Thread(() -> {
            try {
                startLatch.await();
                while (!Thread.currentThread().isInterrupted()) {
                    BigDecimal bid = orderBook.getBestBid();
                    BigDecimal ask = orderBook.getBestAsk();
                    BigDecimal midpoint = orderBook.getMidpoint();

                    totalReads.incrementAndGet();

                    // Check consistency: midpoint should always be between bid and ask
                    if (bid != null && ask != null && midpoint != null) {
                        if (bid.compareTo(midpoint) <= 0 && midpoint.compareTo(ask) <= 0) {
                            consistentReads.incrementAndGet();
                        }
                    }

                    if (finishLatch.getCount() == 0)
                        break;
                    Thread.yield();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        readerThread.start();
        startLatch.countDown();

        // Perform multiple snapshot updates
        for (int i = 0; i < 100; i++) {
            List<PriceLevel> bids = new ArrayList<>();
            List<PriceLevel> asks = new ArrayList<>();

            BigDecimal basePrice = new BigDecimal("100.00").add(new BigDecimal(i * 0.01));
            bids.add(new PriceLevel(basePrice, 1000.0));
            asks.add(new PriceLevel(basePrice.add(new BigDecimal("0.01")), 1000.0));

            orderBook.updateFromSnapshot(bids, asks, timestamp);
            Thread.yield(); // Give reader a chance to read
        }

        finishLatch.countDown();
        readerThread.join(1000);

        // Assert that all reads were consistent
        int total = totalReads.get();
        int consistent = consistentReads.get();

        assertTrue("Should have done some reads", total > 0);
        assertEquals("All reads should be consistent", total, consistent);
    }

    @Test
    public void testListenerNotifications() throws InterruptedException {
        CountDownLatch bidLatch = new CountDownLatch(1);
        CountDownLatch askLatch = new CountDownLatch(1);
        AtomicReference<BigDecimal> receivedBid = new AtomicReference<>();
        AtomicReference<BigDecimal> receivedAsk = new AtomicReference<>();

        OrderBookUpdateListener listener = new OrderBookUpdateListener() {
            @Override
            public void bestBidUpdated(Ticker ticker, BigDecimal bestBid, ZonedDateTime timeStamp) {
                receivedBid.set(bestBid);
                bidLatch.countDown();
            }

            @Override
            public void bestAskUpdated(Ticker ticker, BigDecimal bestAsk, ZonedDateTime timeStamp) {
                receivedAsk.set(bestAsk);
                askLatch.countDown();
            }

            @Override
            public void orderBookImbalanceUpdated(Ticker ticker, BigDecimal imbalance, ZonedDateTime timeStamp) {
                // Not tested here
            }
        };

        orderBook.addOrderBookUpdateListener(listener);

        // First snapshot to initialize
        List<PriceLevel> bids1 = new ArrayList<>();
        List<PriceLevel> asks1 = new ArrayList<>();
        bids1.add(new PriceLevel(new BigDecimal("100.00"), 1000.0));
        asks1.add(new PriceLevel(new BigDecimal("100.01"), 1000.0));
        orderBook.updateFromSnapshot(bids1, asks1, timestamp);

        // Second snapshot with different prices should trigger notifications
        List<PriceLevel> bids2 = new ArrayList<>();
        List<PriceLevel> asks2 = new ArrayList<>();
        bids2.add(new PriceLevel(new BigDecimal("101.00"), 1000.0));
        asks2.add(new PriceLevel(new BigDecimal("101.01"), 1000.0));
        orderBook.updateFromSnapshot(bids2, asks2, timestamp);

        // Wait for notifications
        assertTrue("Should receive bid notification", bidLatch.await(1, TimeUnit.SECONDS));
        assertTrue("Should receive ask notification", askLatch.await(1, TimeUnit.SECONDS));

        assertEquals(new BigDecimal("101.00"), receivedBid.get());
        assertEquals(new BigDecimal("101.01"), receivedAsk.get());

        orderBook.shutdown();
    }

    @Test
    public void testClearOrderBook() {
        // Setup initial snapshot
        List<PriceLevel> bids = new ArrayList<>();
        List<PriceLevel> asks = new ArrayList<>();
        bids.add(new PriceLevel(new BigDecimal("100.00"), 1000.0));
        asks.add(new PriceLevel(new BigDecimal("100.01"), 1000.0));
        orderBook.updateFromSnapshot(bids, asks, timestamp);

        assertTrue(orderBook.isInitialized());
        assertNotNull(orderBook.getBestBid());
        assertNotNull(orderBook.getBestAsk());

        // Clear the order book
        orderBook.clearOrderBook();

        assertFalse(orderBook.isInitialized());
        assertEquals(BigDecimal.ZERO, orderBook.getBestBid());
        assertEquals(BigDecimal.ZERO, orderBook.getBestAsk());
    }
}