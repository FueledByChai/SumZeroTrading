/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.market.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import com.sumzerotrading.bitmex.client.BitmexWebsocketClient;
import com.sumzerotrading.bitmex.common.api.BitmexClientRegistry;
import com.sumzerotrading.bitmex.entity.BitmexQuote;
import com.sumzerotrading.bitmex.entity.BitmexTrade;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.ILevel1Quote;
import com.sumzerotrading.marketdata.Level1Quote;
import com.sumzerotrading.marketdata.QuoteType;

/**
 *
 * @author RobTerpilowski
 */
@Ignore("Ignore until API fixed")
public class BitmexLevel1QuoteEngineTest {

    @Spy
    protected BitmexLevel1QuoteEngine testQuoteEngine;

    @Mock
    protected BitmexClientRegistry mockRegistry;

    @Mock
    protected BitmexWebsocketClient mockClient;

    public BitmexLevel1QuoteEngineTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        BitmexClientRegistry.setTestClientRegistry(mockRegistry);
        when(mockRegistry.getWebsocketClient()).thenReturn(mockClient);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testStartEngine() {

        assertNull(testQuoteEngine.websocketClient);
        assertFalse(testQuoteEngine.isStarted);
        assertFalse(testQuoteEngine.started());
        assertFalse(testQuoteEngine.isConnected());

        testQuoteEngine.startEngine();

        assertEquals(mockClient, testQuoteEngine.websocketClient);
        assertTrue(testQuoteEngine.isStarted);
        assertTrue(testQuoteEngine.started());
        assertTrue(testQuoteEngine.isConnected());

    }

    @Test
    public void testSubscribeLevel1() {
        testQuoteEngine.websocketClient = mockClient;
        Ticker ticker = new Ticker("ABC");
        testQuoteEngine.subscribeLevel1(ticker, (ILevel1Quote quote) -> {
        });

        assertEquals(ticker, testQuoteEngine.tickerMap.get("ABC"));
        verify(mockClient, times(1)).subscribeQuotes(ticker, testQuoteEngine);

    }

    @Test
    public void testQuoteUpdated() {
        Ticker ticker = new Ticker("ABC");
        testQuoteEngine.tickerMap.put("ABC", ticker);
        String timestampString = "2018-07-24T16:45:32.739Z";
        ZonedDateTime timestamp = ZonedDateTime.of(2018, 7, 24, 16, 45, 32, 739, ZoneId.of("UTC"));
        // doReturn(timestamp).when(testQuoteEngine).getTimestamp();
        doNothing().when(testQuoteEngine).fireLevel1Quote(any(Level1Quote.class));

        double bid = 1.53;
        double ask = 3.25;
        int bidSize = 22;
        int askSize = 333;

        BigDecimal expectedBid = new BigDecimal(bid);
        BigDecimal expectedAsk = new BigDecimal(ask);
        BigDecimal expectedBidSize = new BigDecimal(bidSize);
        BigDecimal expectedAskSize = new BigDecimal(askSize);
        BigDecimal expectedMidpoint = new BigDecimal((ask + bid) / 2.0);

        Map<QuoteType, BigDecimal> quoteMap = new HashMap<>();
        quoteMap.put(QuoteType.BID, expectedBid);
        quoteMap.put(QuoteType.ASK, expectedAsk);
        quoteMap.put(QuoteType.BID_SIZE, expectedBidSize);
        quoteMap.put(QuoteType.ASK_SIZE, expectedAskSize);
        quoteMap.put(QuoteType.MIDPOINT, expectedMidpoint);

        Level1Quote expectedQuote = new Level1Quote(ticker, timestamp, quoteMap);

        BitmexQuote data = new BitmexQuote();
        data.setAskPrice(ask);
        data.setAskSize(askSize);
        data.setBidPrice(bid);
        data.setBidSize(bidSize);
        data.setTimestamp(timestampString);

        testQuoteEngine.quoteUpdated(data);

        verify(testQuoteEngine, times(1)).fireLevel1Quote(expectedQuote);

        System.out.println("Quote is: " + expectedQuote);
    }

    @Test
    public void testTradeUpdate() {
        Ticker ticker = new Ticker("ABC");
        testQuoteEngine.tickerMap.put("ABC", ticker);
        String timestampString = "2018-07-24T16:45:32.739Z";
        ZonedDateTime timestamp = ZonedDateTime.of(2018, 7, 24, 16, 45, 32, 739, ZoneId.of("UTC"));
        doNothing().when(testQuoteEngine).fireLevel1Quote(any(Level1Quote.class));

        double last = 123.44;
        double lastSize = 4433;

        BigDecimal expectedLast = new BigDecimal(last);
        BigDecimal expectedLastSize = new BigDecimal(lastSize);

        Map<QuoteType, BigDecimal> quoteMap = new HashMap<>();
        quoteMap.put(QuoteType.LAST, expectedLast);
        quoteMap.put(QuoteType.LAST_SIZE, expectedLastSize);

        Level1Quote expectedQuote = new Level1Quote(ticker, timestamp, quoteMap);

        BitmexTrade trade = new BitmexTrade();
        trade.setPrice(last);
        trade.setSize(lastSize);
        trade.setSymbol("ABC");
        trade.setSide("Buy");
        trade.setTimestamp(timestampString);

        testQuoteEngine.tradeUpdated(trade);
        verify(testQuoteEngine, times(1)).fireLevel1Quote(expectedQuote);
        System.out.println("Quote is: " + expectedQuote);

    }

}
