/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.marketdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;

/**
 *
 * @author RobTerpilowski
 */
public class Level1QuoteTest {

    public Level1QuoteTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLevel1Quote() {
        QuoteType[] types = { QuoteType.ASK, QuoteType.BID };
        BigDecimal bid = new BigDecimal(2.5);
        BigDecimal ask = new BigDecimal(3.5);

        Ticker ticker = new Ticker("ABC").setInstrumentType(InstrumentType.STOCK);
        Map<QuoteType, BigDecimal> map = new HashMap<>();
        map.put(QuoteType.BID, bid);
        map.put(QuoteType.ASK, ask);

        ZonedDateTime now = ZonedDateTime.now();

        Level1Quote quote = new Level1Quote(ticker, now, map);

        List<QuoteType> returnedTypes = Arrays.asList(quote.getTypes());
        assertEquals(2, returnedTypes.size());
        assertTrue(returnedTypes.contains(QuoteType.BID));
        assertTrue(returnedTypes.contains(QuoteType.ASK));

        assertTrue(quote.containsType(QuoteType.BID));
        assertTrue(quote.containsType(QuoteType.ASK));
        assertFalse(quote.containsType(QuoteType.OPEN));

        assertEquals(bid, quote.getValue(QuoteType.BID));

        try {
            quote.getValue(QuoteType.OPEN);
            fail();
        } catch (SumZeroException ex) {
            // this should happen
        }

    }
}
