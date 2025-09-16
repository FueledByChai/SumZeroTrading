package com.sumzerotrading.marketdata.hyperliquid;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.OrderBook;
import com.sumzerotrading.marketdata.OrderBookUpdateListener;
import com.sumzerotrading.websocket.IWebSocketClosedListener;

public class MarketBookWebSocketProcessorTest {

    private MarketBookWebSocketProcessor processor;
    private OrderBook orderBook;
    private Ticker ticker;
    private CountDownLatch connectionClosedLatch;

    @Before
    public void setUp() {
        ticker = new Ticker("SOL");
        ticker.setMinimumTickSize(new BigDecimal("0.01"));
        orderBook = new OrderBook(ticker);
        connectionClosedLatch = new CountDownLatch(1);

        IWebSocketClosedListener closedListener = () -> connectionClosedLatch.countDown();
        processor = new MarketBookWebSocketProcessor(orderBook, closedListener);
    }

    @Test
    public void testParseHyperliquidL2BookMessage() throws InterruptedException {
        // Sample Hyperliquid L2 book message from the user
        String message = """
                {
                    "channel": "l2Book",
                    "data": {
                        "coin": "SOL",
                        "time": 1757865689689,
                        "levels": [
                            [
                                {
                                    "px": "243.49",
                                    "sz": "244.82",
                                    "n": 6
                                },
                                {
                                    "px": "243.47",
                                    "sz": "8.21",
                                    "n": 1
                                },
                                {
                                    "px": "243.46",
                                    "sz": "12.32",
                                    "n": 1
                                }
                            ],
                            [
                                {
                                    "px": "243.50",
                                    "sz": "419.05",
                                    "n": 4
                                },
                                {
                                    "px": "243.51",
                                    "sz": "18.67",
                                    "n": 3
                                },
                                {
                                    "px": "243.52",
                                    "sz": "14.04",
                                    "n": 2
                                }
                            ]
                        ]
                    }
                }
                """;

        CountDownLatch updateLatch = new CountDownLatch(2); // Expecting both bid and ask updates
        AtomicReference<BigDecimal> receivedBid = new AtomicReference<>();
        AtomicReference<BigDecimal> receivedAsk = new AtomicReference<>();

        OrderBookUpdateListener listener = new OrderBookUpdateListener() {
            @Override
            public void bestBidUpdated(Ticker ticker, BigDecimal bestBid, ZonedDateTime timeStamp) {
                receivedBid.set(bestBid);
                updateLatch.countDown();
            }

            @Override
            public void bestAskUpdated(Ticker ticker, BigDecimal bestAsk, ZonedDateTime timeStamp) {
                receivedAsk.set(bestAsk);
                updateLatch.countDown();
            }

            @Override
            public void orderBookImbalanceUpdated(Ticker ticker, BigDecimal imbalance, ZonedDateTime timeStamp) {
                // Not tested here
            }
        };

        orderBook.addOrderBookUpdateListener(listener);

        // Process the message
        processor.messageReceived(message);

        // Verify the order book was updated correctly
        assertTrue("Order book should be initialized", orderBook.isInitialized());

        // Check best prices
        assertEquals("Best bid should be highest bid price", new BigDecimal("243.49"), orderBook.getBestBid());
        assertEquals("Best ask should be lowest ask price", new BigDecimal("243.50"), orderBook.getBestAsk());

        // Check midpoint
        BigDecimal expectedMidpoint = new BigDecimal("243.49").add(new BigDecimal("243.50"))
                .divide(new BigDecimal("2"));
        assertEquals("Midpoint should be average of best bid and ask", expectedMidpoint, orderBook.getMidpoint());

        // Wait for listener notifications (since this is first update, should receive
        // notifications)
        assertTrue("Should receive update notifications", updateLatch.await(1, TimeUnit.SECONDS));
        assertEquals("Listener should receive correct best bid", new BigDecimal("243.49"), receivedBid.get());
        assertEquals("Listener should receive correct best ask", new BigDecimal("243.50"), receivedAsk.get());

        orderBook.shutdown();
    }

    @Test
    public void testIgnoreNonL2BookMessages() {
        String nonL2Message = """
                {
                    "channel": "trades",
                    "data": {
                        "coin": "SOL",
                        "trades": []
                    }
                }
                """;

        // Process non-L2 message
        processor.messageReceived(nonL2Message);

        // Order book should not be initialized since no L2 data was processed
        assertFalse("Order book should not be initialized from non-L2 message", orderBook.isInitialized());
    }

    @Test
    public void testHandleEmptyLevels() {
        String emptyLevelsMessage = """
                {
                    "channel": "l2Book",
                    "data": {
                        "coin": "SOL",
                        "time": 1757865689689,
                        "levels": [[], []]
                    }
                }
                """;

        // Process message with empty levels
        processor.messageReceived(emptyLevelsMessage);

        // Order book should be initialized but with no best prices
        assertTrue("Order book should be initialized", orderBook.isInitialized());
        assertNull("Best bid should be null with empty levels", orderBook.getBestBid());
        assertNull("Best ask should be null with empty levels", orderBook.getBestAsk());
        assertNull("Midpoint should be null with empty levels", orderBook.getMidpoint());
    }

    @Test
    public void testConnectionHandling() throws InterruptedException {
        // Test connection closed
        processor.connectionClosed(1000, "Normal closure", false);
        assertTrue("Connection closed listener should be notified",
                connectionClosedLatch.await(100, TimeUnit.MILLISECONDS));

        // Reset latch for error test
        connectionClosedLatch = new CountDownLatch(1);
        IWebSocketClosedListener closedListener = () -> connectionClosedLatch.countDown();
        processor = new MarketBookWebSocketProcessor(orderBook, closedListener);

        // Test connection error
        processor.connectionError(new RuntimeException("Test error"));
        assertTrue("Connection closed listener should be notified on error",
                connectionClosedLatch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testMalformedJsonHandling() {
        String malformedJson = "{ invalid json }";

        // Should not throw exception, just log error
        processor.messageReceived(malformedJson);

        // Order book should remain uninitialized
        assertFalse("Order book should not be initialized from malformed JSON", orderBook.isInitialized());
    }
}