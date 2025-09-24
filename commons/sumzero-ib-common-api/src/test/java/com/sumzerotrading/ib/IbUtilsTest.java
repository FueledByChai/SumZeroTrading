/**
 * MIT License
 *
 * Copyright (c) 2015  Rob Terpilowski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.sumzerotrading.ib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ib.client.TagValue;
import com.sumzerotrading.broker.order.OrderStatus;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.QuoteType;

/**
 *
 * @author Rob Terpilowski
 */
public class IbUtilsTest {

    public IbUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetTif() {
        OrderTicket.Duration[] values = OrderTicket.Duration.values();
        for (OrderTicket.Duration value : values) {
            assertNotNull(IbUtils.getTif(value, null));
        }

        assertEquals("DAY", IbUtils.getTif(OrderTicket.Duration.DAY, null));
        assertEquals("GTC", IbUtils.getTif(OrderTicket.Duration.GOOD_UNTIL_CANCELED, null));
        assertEquals("FOK", IbUtils.getTif(OrderTicket.Duration.FILL_OR_KILL, null));
        assertEquals("IOC", IbUtils.getTif(OrderTicket.Duration.IMMEDIATE_OR_CANCEL, null));
        assertEquals("GTD", IbUtils.getTif(OrderTicket.Duration.GOOD_UNTIL_TIME, null));
        assertEquals("OPG", IbUtils.getTif(null, OrderTicket.Type.MARKET_ON_OPEN));
        assertEquals("DAY", IbUtils.getTif(null, null));
    }

    @Test
    public void testGetAction() {
        TradeDirection[] values = TradeDirection.values();
        for (TradeDirection value : values) {
            assertNotNull(IbUtils.getAction(value));
        }

        assertEquals("BUY", IbUtils.getAction(TradeDirection.BUY));
        assertEquals("SELL", IbUtils.getAction(TradeDirection.SELL));
        assertEquals("SELL", IbUtils.getAction(TradeDirection.SELL_SHORT));
        assertEquals("BUY", IbUtils.getAction(TradeDirection.BUY_TO_COVER));
        try {
            IbUtils.getAction(null);
            fail();
        } catch (IllegalStateException ex) {
            // this should happen
        }
    }

    @Test
    public void testGetOrderType() {

        // First make sure we have a mapping for all types.
        OrderTicket.Type[] values = OrderTicket.Type.values();

        assertEquals("MKT", IbUtils.getOrderType(OrderTicket.Type.MARKET));
        assertEquals("LMT", IbUtils.getOrderType(OrderTicket.Type.LIMIT));
        assertEquals("STP", IbUtils.getOrderType(OrderTicket.Type.STOP));
        assertEquals("MKT", IbUtils.getOrderType(OrderTicket.Type.MARKET_ON_OPEN));
        assertEquals("MOC", IbUtils.getOrderType(OrderTicket.Type.MARKET_ON_CLOSE));

        try {
            IbUtils.getOrderType(null);
            fail();
        } catch (IllegalStateException ex) {
            // this should happen
        }

        try {
            IbUtils.getOrderType(OrderTicket.Type.STOP_LIMIT);
            fail();
        } catch (IllegalStateException ex) {
            // this should happen
        }

        try {
            IbUtils.getOrderType(OrderTicket.Type.TRAILING_STOP);
            fail();
        } catch (IllegalStateException ex) {
            // this should happen
        }

        try {
            IbUtils.getOrderType(OrderTicket.Type.TRAILING_STOP_LIMIT);
            fail();
        } catch (IllegalStateException ex) {
            // this should happen
        }
    }

    @Test
    public void testGetSecurityType() {
        InstrumentType[] values = InstrumentType.values();
        for (InstrumentType value : values) {
            if (value != InstrumentType.CRYPTO_SPOT) {
                // assertNotNull(IbUtils.getSecurityType(value));
                // TODO: FIX THIS
            }
        }

        assertEquals("STK", IbUtils.getSecurityType(InstrumentType.STOCK));
        assertEquals("OPT", IbUtils.getSecurityType(InstrumentType.OPTION));
        assertEquals("CASH", IbUtils.getSecurityType(InstrumentType.CURRENCY));
        assertEquals("FUT", IbUtils.getSecurityType(InstrumentType.FUTURES));
        assertEquals("IND", IbUtils.getSecurityType(InstrumentType.INDEX));
        assertEquals("BAG", IbUtils.getSecurityType(InstrumentType.COMBO));
    }

    @Test
    public void testGetQuoteType() {
        // BID_SIZE
        assertEquals(QuoteType.BID_SIZE, IbUtils.getQuoteType(0));
        assertEquals(QuoteType.BID_SIZE, IbUtils.getQuoteType(69));
        // BID
        assertEquals(QuoteType.BID, IbUtils.getQuoteType(1));
        assertEquals(QuoteType.BID, IbUtils.getQuoteType(66));
        // ASK
        assertEquals(QuoteType.ASK, IbUtils.getQuoteType(2));
        assertEquals(QuoteType.ASK, IbUtils.getQuoteType(67));
        // ASK_SIZE
        assertEquals(QuoteType.ASK_SIZE, IbUtils.getQuoteType(3));
        assertEquals(QuoteType.ASK_SIZE, IbUtils.getQuoteType(70));
        // LAST
        assertEquals(QuoteType.LAST, IbUtils.getQuoteType(4));
        assertEquals(QuoteType.LAST, IbUtils.getQuoteType(68));
        // LAST_SIZE
        assertEquals(QuoteType.LAST_SIZE, IbUtils.getQuoteType(5));
        assertEquals(QuoteType.LAST_SIZE, IbUtils.getQuoteType(71));
        // VOLUME
        assertEquals(QuoteType.VOLUME, IbUtils.getQuoteType(8));
        assertEquals(QuoteType.VOLUME, IbUtils.getQuoteType(74));
        // CLOSE
        assertEquals(QuoteType.CLOSE, IbUtils.getQuoteType(9));
        assertEquals(QuoteType.CLOSE, IbUtils.getQuoteType(75));
        // OPEN
        assertEquals(QuoteType.OPEN, IbUtils.getQuoteType(14));
        assertEquals(QuoteType.OPEN, IbUtils.getQuoteType(76));
        // UNKNOWN
        assertEquals(QuoteType.UNKNOWN, IbUtils.getQuoteType(99));
    }

    @Test
    public void testGetOrderStatus() {
        assertEquals(OrderStatus.Status.NEW, IbUtils.getOrderStatus("PendingSubmit"));
        assertEquals(OrderStatus.Status.PENDING_CANCEL, IbUtils.getOrderStatus("PendingCancel"));
        assertEquals(OrderStatus.Status.NEW, IbUtils.getOrderStatus("PreSubmitted"));
        assertEquals(OrderStatus.Status.NEW, IbUtils.getOrderStatus("Submitted"));
        assertEquals(OrderStatus.Status.CANCELED, IbUtils.getOrderStatus("Cancelled"));
        assertEquals(OrderStatus.Status.FILLED, IbUtils.getOrderStatus("Filled"));
        assertEquals(OrderStatus.Status.CANCELED, IbUtils.getOrderStatus("Inactive"));
        assertEquals(OrderStatus.Status.UNKNOWN, IbUtils.getOrderStatus("foo"));
    }

    @Test
    public void testGetExpiryString_MonthYear() {
        assertEquals("201709", IbUtils.getExpiryString(9, 2017));
        assertEquals("201710", IbUtils.getExpiryString(10, 2017));
    }

    @Test
    public void testGetExpiryString_DayMonthYear() {
        assertEquals("20170908", IbUtils.getExpiryString(8, 9, 2017));
        assertEquals("20171110", IbUtils.getExpiryString(10, 11, 2017));
    }

    @Test
    public void testGetOptionRight() {
        assertEquals("C", IbUtils.getOptionRight(Ticker.Right.CALL));
        assertEquals("P", IbUtils.getOptionRight(Ticker.Right.PUT));
    }

    @Test
    public void testTranslateFuturesSymbol() {
        assertEquals("CAD", IbUtils.translateToIbFuturesSymbol("6C"));
        assertEquals("EUR", IbUtils.translateToIbFuturesSymbol("6E"));
        assertEquals("JPY", IbUtils.translateToIbFuturesSymbol("6J"));
        assertEquals("CHF", IbUtils.translateToIbFuturesSymbol("6S"));
        assertEquals("GBP", IbUtils.translateToIbFuturesSymbol("6B"));
        assertEquals("VIX", IbUtils.translateToIbFuturesSymbol("VX"));
        assertEquals("GBP", IbUtils.translateToIbFuturesSymbol("GBP"));

    }

    @Test
    public void testGetContractMultiplier() {
        Ticker ticker = new Ticker().setInstrumentType(InstrumentType.FUTURES);
        ticker.setSymbol("ZC");
        ticker.setContractMultiplier(new BigDecimal(50.0));

        assertEquals(new BigDecimal(5000.0), IbUtils.getIbMultiplier(ticker));

        ticker.setSymbol("ZS");
        assertEquals(new BigDecimal(5000.0), IbUtils.getIbMultiplier(ticker));

        ticker.setSymbol("ZW");
        assertEquals(new BigDecimal(5000.0), IbUtils.getIbMultiplier(ticker));

        ticker.setSymbol("HG");
        assertEquals(new BigDecimal(50.0), IbUtils.getIbMultiplier(ticker));
    }

    @Test
    public void testGetContractMultiplier_NullMultiplier() {
        Ticker ticker = new Ticker().setInstrumentType(InstrumentType.FUTURES);
        ticker.setSymbol("HG");

        assertEquals(BigDecimal.ONE, IbUtils.getIbMultiplier(ticker));
    }

    @Test
    public void testGetDefaultTagVector() {
        Vector<TagValue> expected = new Vector<>();
        expected.add(new TagValue("XYZ", "XYZ"));

        assertEquals(expected, IbUtils.getDefaultTagVector());
    }

    @Test
    public void testGetDefaultTagList() {
        List<TagValue> expected = new ArrayList<>();
        expected.add(new TagValue("XYZ", "XYZ"));

        assertEquals(expected, IbUtils.getDefaultTagList());
    }

}
