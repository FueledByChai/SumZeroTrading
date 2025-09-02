/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.intraday.trading.strategy;

import static com.sumzerotrading.intraday.trading.strategy.TradeReferenceLine.Direction.LONG;
import static com.sumzerotrading.intraday.trading.strategy.TradeReferenceLine.Direction.SHORT;
import static com.sumzerotrading.intraday.trading.strategy.TradeReferenceLine.Side.ENTRY;
import static com.sumzerotrading.intraday.trading.strategy.TradeReferenceLine.Side.EXIT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.broker.order.TradeOrder;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;

/**
 *
 * @author RobTerpilowski
 */
public class RoundTripTest {

    protected RoundTrip roundTrip;
    protected TradeOrder order;
    protected TradeReferenceLine referenceLine;
    protected Ticker ticker;

    public RoundTripTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        roundTrip = new RoundTrip();
        ticker = new Ticker("QQQ").setInstrumentType(InstrumentType.STOCK);
        order = new TradeOrder("123", ticker, BigDecimal.valueOf(100), TradeDirection.BUY);
        referenceLine = new TradeReferenceLine();
        referenceLine.correlationId = "999";
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddTradeReferenceLine_LongEntry() {
        referenceLine.direction = LONG;
        referenceLine.side = ENTRY;
        roundTrip.addTradeReference(order, referenceLine);

        assertEquals("999", roundTrip.getCorrelationId());
        assertEquals(order, roundTrip.entry);
        assertNull(roundTrip.exit);
        assertEquals(LONG, roundTrip.direction);
    }

    @Test
    public void testAddTradeReferenceLine_LongExit() {
        referenceLine.direction = LONG;
        referenceLine.side = EXIT;
        roundTrip.addTradeReference(order, referenceLine);

        assertEquals("999", roundTrip.getCorrelationId());
        assertEquals(order, roundTrip.exit);
        assertNull(roundTrip.entry);
    }

    @Test
    public void testAddTradeReferenceLine_ShortEntry() {
        referenceLine.direction = SHORT;
        referenceLine.side = ENTRY;
        roundTrip.addTradeReference(order, referenceLine);

        assertEquals("999", roundTrip.getCorrelationId());
        assertEquals(order, roundTrip.entry);
        assertEquals(SHORT, roundTrip.direction);
        assertNull(roundTrip.exit);
    }

    @Test
    public void testAddTradeReferenceLine_ShortExit() {
        referenceLine.direction = SHORT;
        referenceLine.side = EXIT;
        roundTrip.addTradeReference(order, referenceLine);

        assertEquals("999", roundTrip.getCorrelationId());
        assertEquals(order, roundTrip.exit);

    }

    // Long-entry-date, longTicker, LongShares, Long Entry price, Long Entry
    // Commission,
    // LongExitDate, Long Exit Price, shortTicker, shortShares, shortEntryPrice,
    // shortEntryCommissions,
    // shortExitPrice, shortExitCommissions
    @Test
    public void testGetResults() {
        String expected = "2016-03-03T05:30:45,LONG,QQQ,100,50.43,0,2016-03-04T12:45:00,51.46,0";
        ZonedDateTime entryDate = ZonedDateTime.of(2016, 3, 3, 5, 30, 45, 0, ZoneId.systemDefault());
        ZonedDateTime exitDate = ZonedDateTime.of(2016, 3, 4, 12, 45, 00, 0, ZoneId.systemDefault());
        TradeOrder longEntryOrder = new TradeOrder("123", ticker, BigDecimal.valueOf(100), TradeDirection.BUY);
        longEntryOrder.setFilledPrice(BigDecimal.valueOf(50.43));
        longEntryOrder.setOrderFilledTime(entryDate);

        TradeOrder longExitOrder = new TradeOrder("234", ticker, BigDecimal.valueOf(100), TradeDirection.SELL);
        longExitOrder.setFilledPrice(BigDecimal.valueOf(51.46));
        longExitOrder.setOrderFilledTime(exitDate);

        TradeReferenceLine entry = new TradeReferenceLine();
        entry.correlationId = "123";
        entry.direction = TradeReferenceLine.Direction.LONG;
        entry.side = ENTRY;

        TradeReferenceLine exit = new TradeReferenceLine();
        exit.correlationId = "234";
        exit.direction = LONG;
        exit.side = EXIT;

        roundTrip.addTradeReference(longEntryOrder, entry);
        roundTrip.addTradeReference(longExitOrder, exit);

        assertEquals(expected, roundTrip.getResults());

    }

    @Test
    public void testIsComplete() {

        assertFalse(roundTrip.isComplete());

        roundTrip.entry = order;
        // roundTrip.shortEntry = order;
        roundTrip.exit = order;
        // roundTrip.shortExit = order;

        assertTrue(roundTrip.isComplete());
        roundTrip.entry = null;

        assertFalse(roundTrip.isComplete());

        roundTrip.entry = order;
        assertTrue(roundTrip.isComplete());

        roundTrip.exit = null;
        assertFalse(roundTrip.isComplete());

        roundTrip.exit = order;
        assertTrue(roundTrip.isComplete());
    }
}
