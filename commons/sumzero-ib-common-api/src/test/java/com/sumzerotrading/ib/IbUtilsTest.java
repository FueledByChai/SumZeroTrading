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

import com.ib.client.TagValue;
import com.sumzerotrading.broker.order.OrderStatus;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.broker.order.TradeOrder;
import com.sumzerotrading.data.FuturesTicker;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.OptionTicker;
import com.sumzerotrading.marketdata.QuoteType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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
        TradeOrder.Duration[] values = TradeOrder.Duration.values();
        for (TradeOrder.Duration value : values) {
            assertNotNull(IbUtils.getTif(value));
        }

        assertEquals("DAY", IbUtils.getTif(TradeOrder.Duration.DAY));
        assertEquals("GTC", IbUtils.getTif(TradeOrder.Duration.GOOD_UNTIL_CANCELED));
        assertEquals("IOC", IbUtils.getTif(TradeOrder.Duration.FILL_OR_KILL));
        assertEquals("GTD", IbUtils.getTif(TradeOrder.Duration.GOOD_UNTIL_TIME));
        assertEquals("OPG", IbUtils.getTif(TradeOrder.Duration.MARKET_ON_OPEN));
        assertEquals("DAY", IbUtils.getTif(null));
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
        TradeOrder.Type[] values = TradeOrder.Type.values();

        assertEquals("MKT", IbUtils.getOrderType(TradeOrder.Type.MARKET));
        assertEquals("LMT", IbUtils.getOrderType(TradeOrder.Type.LIMIT));
        assertEquals("STP", IbUtils.getOrderType(TradeOrder.Type.STOP));
        assertEquals("MKT", IbUtils.getOrderType(TradeOrder.Type.MARKET_ON_OPEN));
        assertEquals("MOC", IbUtils.getOrderType(TradeOrder.Type.MARKET_ON_CLOSE));

        try {
            IbUtils.getOrderType(null);
            fail();
        } catch (IllegalStateException ex) {
            // this should happen
        }

        try {
            IbUtils.getOrderType(TradeOrder.Type.STOP_LIMIT);
            fail();
        } catch (IllegalStateException ex) {
            // this should happen
        }

        try {
            IbUtils.getOrderType(TradeOrder.Type.TRAILING_STOP);
            fail();
        } catch (IllegalStateException ex) {
            // this should happen
        }

        try {
            IbUtils.getOrderType(TradeOrder.Type.TRAILING_STOP_LIMIT);
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
        assertEquals("CASH", IbUtils.getSecurityType(InstrumentType.FOREX));
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
        assertEquals("C", IbUtils.getOptionRight(OptionTicker.Right.Call));
        assertEquals("P", IbUtils.getOptionRight(OptionTicker.Right.Put));
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
        FuturesTicker ticker = new FuturesTicker();
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
        FuturesTicker ticker = new FuturesTicker();
        ticker.setSymbol("HG");

        assertNull(IbUtils.getIbMultiplier(ticker));
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
