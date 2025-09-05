/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.reporting.csv;

import static com.sumzerotrading.reporting.TradeReferenceLine.Direction.LONG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.reporting.TradeReferenceLine;

/**
 *
 * @author RobTerpilowski
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore("Ignore until API is fixed")
public class TradeRoundTripTest {

    protected TradeRoundTrip testRoundTrip;

    @Mock
    protected OrderTicket mockEntryOrder;

    @Mock
    protected OrderTicket mockExitOrder;

    public TradeRoundTripTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testRoundTrip = new TradeRoundTrip();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void addTradeReference_EntryOrder() {
        TradeReferenceLine line = new TradeReferenceLine();
        line.setCorrelationId("123").setDirection(TradeReferenceLine.Direction.LONG)
                .setSide(TradeReferenceLine.Side.ENTRY);
        testRoundTrip.addTradeReference(mockEntryOrder, line);

        assertEquals(LONG, testRoundTrip.direction);
        assertNotNull(testRoundTrip.entry);
        assertNull(testRoundTrip.exit);
        assertEquals("123", testRoundTrip.getCorrelationId());
    }

    @Test
    public void addTradeReference_ExitOrder() {
        TradeReferenceLine entryLine = new TradeReferenceLine();
        entryLine.setCorrelationId("123").setDirection(TradeReferenceLine.Direction.LONG)
                .setSide(TradeReferenceLine.Side.ENTRY);
        TradeReferenceLine exitLine = new TradeReferenceLine();
        exitLine.setCorrelationId("123").setDirection(TradeReferenceLine.Direction.LONG)
                .setSide(TradeReferenceLine.Side.EXIT);

        testRoundTrip.addTradeReference(mockEntryOrder, entryLine);
        testRoundTrip.addTradeReference(mockExitOrder, exitLine);

        assertEquals(LONG, testRoundTrip.direction);
        assertNotNull(testRoundTrip.entry);
        assertNotNull(testRoundTrip.exit);
        assertEquals("123", testRoundTrip.getCorrelationId());
    }

    @Test
    public void testGetResults() {
        ZonedDateTime entryFilledTime = ZonedDateTime.of(2017, 2, 8, 6, 18, 20, 0, ZoneId.systemDefault());
        ZonedDateTime exitFilledTime = ZonedDateTime.of(2017, 2, 8, 7, 18, 20, 0, ZoneId.systemDefault());
        testRoundTrip.direction = LONG;
        testRoundTrip.entry = mockEntryOrder;
        testRoundTrip.exit = mockExitOrder;
        Ticker ticker = new Ticker("QQQ").setInstrumentType(InstrumentType.STOCK);
        BigDecimal size = BigDecimal.valueOf(200);
        BigDecimal entryPrice = BigDecimal.valueOf(100.23);
        BigDecimal exitPrice = BigDecimal.valueOf(100.51);

        when(mockEntryOrder.getOrderFilledTime()).thenReturn(entryFilledTime);
        when(mockEntryOrder.getTicker()).thenReturn(ticker);
        when(mockEntryOrder.getSize()).thenReturn(size);
        when(mockEntryOrder.getFilledPrice()).thenReturn(entryPrice);

        when(mockExitOrder.getOrderFilledTime()).thenReturn(exitFilledTime);
        when(mockExitOrder.getFilledPrice()).thenReturn(exitPrice);

        String expectedString = "2017-02-08T06:18:20,LONG,QQQ,200,100.23,0,2017-02-08T07:18:20,100.51,0";

        assertEquals(expectedString, testRoundTrip.getResults());
    }

    @Test
    public void testIsComplete() {
        assertFalse(testRoundTrip.isComplete());

        testRoundTrip.entry = mockEntryOrder;
        assertFalse(testRoundTrip.isComplete());

        testRoundTrip.entry = null;
        testRoundTrip.exit = mockExitOrder;
        assertFalse(testRoundTrip.isComplete());

        testRoundTrip.entry = mockEntryOrder;
        assertTrue(testRoundTrip.isComplete());
    }

}
