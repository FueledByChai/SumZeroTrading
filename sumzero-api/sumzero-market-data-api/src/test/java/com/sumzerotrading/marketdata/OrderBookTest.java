package com.sumzerotrading.marketdata;

import org.junit.Test;
import static org.junit.Assert.*;
import com.sumzerotrading.data.Ticker;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class OrderBookTest {
    @Test
    public void testBestBidAndAskReturnPair() {
        Ticker ticker = new Ticker("TEST").setMinimumTickSize(BigDecimal.valueOf(0.01));
        OrderBook book = new OrderBook(ticker);
        ZonedDateTime now = ZonedDateTime.now();
        // Add bids and asks
        book.updateFromSnapshot(new BigDecimal[] { BigDecimal.valueOf(100), BigDecimal.valueOf(99) },
                new Double[] { 10.0, 5.0 }, new BigDecimal[] { BigDecimal.valueOf(101), BigDecimal.valueOf(102) },
                new Double[] { 7.0, 3.0 }, now);
        IOrderBook.BidSizePair bestBid = book.getBestBid();
        IOrderBook.BidSizePair bestAsk = book.getBestAsk();
        assertTrue(BigDecimal.valueOf(100).compareTo(bestBid.price) == 0);
        assertEquals(10.0, bestBid.size, 0.00001);
        assertTrue(BigDecimal.valueOf(101).compareTo(bestAsk.price) == 0);
        assertEquals(7.0, bestAsk.size, 0.00001);
    }

    @Test
    public void testBestBidAndAskWithTickSize() {
        Ticker ticker = new Ticker("TEST").setMinimumTickSize(BigDecimal.valueOf(0.01));
        OrderBook book = new OrderBook(ticker);
        ZonedDateTime now = ZonedDateTime.now();
        book.updateFromSnapshot(new BigDecimal[] { BigDecimal.valueOf(100.005), BigDecimal.valueOf(99.995) },
                new Double[] { 8.0, 4.0 },
                new BigDecimal[] { BigDecimal.valueOf(101.002), BigDecimal.valueOf(102.001) },
                new Double[] { 6.0, 2.0 }, now);
        IOrderBook.BidSizePair bestBid = book.getBestBid(BigDecimal.valueOf(0.01));
        IOrderBook.BidSizePair bestAsk = book.getBestAsk(BigDecimal.valueOf(0.01));
        assertTrue(BigDecimal.valueOf(100.00).setScale(2).compareTo(bestBid.price.setScale(2)) == 0);
        // The size at 100.00 is 0.0, because the test inserted 100.005 and 99.995,
        // which round to 100.00 and 100.00, so their sizes should be summed.
        assertEquals(8.0, bestBid.size, 0.00001);
        assertTrue(BigDecimal.valueOf(101.01).setScale(2).compareTo(bestAsk.price.setScale(2)) == 0);
        // The size at 101.01 is 6.0, as only one ask rounds to that price
        assertEquals(6.0, bestAsk.size, 0.00001);
    }
}
