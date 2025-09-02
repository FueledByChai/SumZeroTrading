/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.eod.trading.strategy;

import static com.sumzerotrading.eod.trading.strategy.TradeReferenceLine.Direction.LONG;
import static com.sumzerotrading.eod.trading.strategy.TradeReferenceLine.Direction.SHORT;
import static com.sumzerotrading.eod.trading.strategy.TradeReferenceLine.Side.ENTRY;
import static com.sumzerotrading.eod.trading.strategy.TradeReferenceLine.Side.EXIT;
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
        assertEquals(order, roundTrip.longEntry);
        assertNull(roundTrip.longExit);
        assertNull(roundTrip.shortEntry);
        assertNull(roundTrip.shortExit);

    }

    @Test
    public void testAddTradeReferenceLine_LongExit() {
        referenceLine.direction = LONG;
        referenceLine.side = EXIT;
        roundTrip.addTradeReference(order, referenceLine);

        assertEquals("999", roundTrip.getCorrelationId());
        assertEquals(order, roundTrip.longExit);
        assertNull(roundTrip.longEntry);
        assertNull(roundTrip.shortEntry);
        assertNull(roundTrip.shortExit);

    }

    @Test
    public void testAddTradeReferenceLine_ShortEntry() {
        referenceLine.direction = SHORT;
        referenceLine.side = ENTRY;
        roundTrip.addTradeReference(order, referenceLine);

        assertEquals("999", roundTrip.getCorrelationId());
        assertEquals(order, roundTrip.shortEntry);
        assertNull(roundTrip.longEntry);
        assertNull(roundTrip.longExit);
        assertNull(roundTrip.shortExit);

    }

    @Test
    public void testAddTradeReferenceLine_ShortExit() {
        referenceLine.direction = SHORT;
        referenceLine.side = EXIT;
        roundTrip.addTradeReference(order, referenceLine);

        assertEquals("999", roundTrip.getCorrelationId());
        assertEquals(order, roundTrip.shortExit);
        assertNull(roundTrip.longEntry);
        assertNull(roundTrip.longExit);
        assertNull(roundTrip.shortEntry);

    }

    // Long-entry-date, longTicker, LongShares, Long Entry price, Long Entry
    // Commission,
    // LongExitDate, Long Exit Price, shortTicker, shortShares, shortEntryPrice,
    // shortEntryCommissions,
    // shortExitPrice, shortExitCommissions
    @Test
    public void testGetResults() {
        Ticker shortTicker = new Ticker("SPY").setInstrumentType(InstrumentType.STOCK);
        String expected = "2016-03-03T05:30:45,Long,QQQ,100,50.43,0,2016-03-04T12:45:00,51.46,0,Short,SPY,50,1.23,0,2.34,0";
        ZonedDateTime entryDate = ZonedDateTime.of(2016, 3, 3, 5, 30, 45, 0, ZoneId.of("UTC"));
        ZonedDateTime exitDate = ZonedDateTime.of(2016, 3, 4, 12, 45, 00, 0, ZoneId.of("UTC"));

        TradeOrder longEntryOrder = new TradeOrder("123", ticker, BigDecimal.valueOf(100), TradeDirection.BUY);
        longEntryOrder.setFilledPrice(BigDecimal.valueOf(50.43));
        longEntryOrder.setOrderFilledTime(entryDate);

        TradeOrder longExitOrder = new TradeOrder("234", ticker, BigDecimal.valueOf(100), TradeDirection.SELL);
        longExitOrder.setFilledPrice(BigDecimal.valueOf(51.46));
        longExitOrder.setOrderFilledTime(exitDate);

        TradeOrder shortEntryOrder = new TradeOrder("345", shortTicker, BigDecimal.valueOf(50), TradeDirection.SELL);
        shortEntryOrder.setFilledPrice(BigDecimal.valueOf(1.23));

        TradeOrder shortExitOrder = new TradeOrder("456", shortTicker, BigDecimal.valueOf(50), TradeDirection.BUY);
        shortExitOrder.setFilledPrice(BigDecimal.valueOf(2.34));
        shortExitOrder.setOrderFilledTime(exitDate);

        roundTrip.longEntry = longEntryOrder;
        roundTrip.shortEntry = shortEntryOrder;
        roundTrip.longExit = longExitOrder;
        roundTrip.shortExit = shortExitOrder;

        assertEquals(expected, roundTrip.getResults());

    }

    @Test
    public void testIsComplete() {

        assertFalse(roundTrip.isComplete());

        roundTrip.longEntry = order;
        roundTrip.shortEntry = order;
        roundTrip.longExit = order;
        roundTrip.shortExit = order;

        assertTrue(roundTrip.isComplete());
        roundTrip.longEntry = null;

        assertFalse(roundTrip.isComplete());

        roundTrip.longEntry = order;
        roundTrip.shortEntry = null;
        assertFalse(roundTrip.isComplete());

        roundTrip.shortEntry = order;
        roundTrip.longExit = null;
        assertFalse(roundTrip.isComplete());

        roundTrip.longExit = order;
        roundTrip.shortExit = null;
        assertFalse(roundTrip.isComplete());

        roundTrip.shortExit = order;
        assertTrue(roundTrip.isComplete());
    }
}
