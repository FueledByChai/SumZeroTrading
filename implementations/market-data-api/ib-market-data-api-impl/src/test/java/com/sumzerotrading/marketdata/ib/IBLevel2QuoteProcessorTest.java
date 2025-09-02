/**
 * MIT License

Copyright (c) 2015  Rob Terpilowski

Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
and associated documentation files (the "Software"), to deal in the Software without restriction, 
including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.sumzerotrading.marketdata.ib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.concurrent.BlockingQueue;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.IMarketDepthBook;
import com.sumzerotrading.marketdata.IQuoteEngine;
import com.sumzerotrading.marketdata.Level2Quote;
import com.sumzerotrading.marketdata.MarketDepthBook;
import com.sumzerotrading.marketdata.MarketDepthLevel;
import com.sumzerotrading.marketdata.QuoteType;
import com.sumzerotrading.util.QuoteUtil;

/**
 *
 * @author Rob Terpilowski
 */
public class IBLevel2QuoteProcessorTest {

    protected Mockery mockery;

    public IBLevel2QuoteProcessorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        mockery = new Mockery();
        // Seems that other unit tests are leaving a mock MarketDepthBook in memory,
        // even though the unit tests are set to always fork for each new test class.
        MarketDepthBook.setTestInstance(null);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testProcessData_BidSide_EmptyBook() {
        MockIBLevel2QuoteProcessor processor = buildQuoteProcessor();
        Ticker ticker = getCurrencyTicker();
        BigDecimal price = new BigDecimal(1.3553).setScale(4, RoundingMode.HALF_UP);
        Level2QuoteData data = new Level2QuoteData(ticker, 0, processor.OP_INSERT, processor.SIDE_BID,
                price.doubleValue(), 100);

        processor.processData(data);

        Ticker actualTicker = processor.buildAndFireEventTicker;
        IMarketDepthBook book = processor.buildAndFireEventBook;

        assertEquals(ticker, actualTicker);
        assertEquals(MarketDepthBook.Side.BID, book.getSide());
        assertEquals(1, book.getLevelCount());
        assertEquals(100, book.getTotalSize().intValue());

        MarketDepthLevel level = book.getLevelAt(0);
        assertEquals(MarketDepthBook.Side.BID, level.getSide());
        assertEquals(100, level.getSize().intValue());
        assertEquals(price, level.getPrice());

    }

    @Test
    public void testProcessData_BidSide_NonEmptyBook() {
        MockIBLevel2QuoteProcessor processor = buildQuoteProcessor();
        Ticker ticker = getCurrencyTicker();
        IMarketDepthBook book = MarketDepthBook.newInstance();
        book.setSide(MarketDepthBook.Side.BID);
        processor.bidBookMap.put(ticker, book);

        BigDecimal price = new BigDecimal(1.3553).setScale(4, RoundingMode.HALF_UP);
        Level2QuoteData data = new Level2QuoteData(ticker, 0, processor.OP_INSERT, processor.SIDE_BID,
                price.doubleValue(), 100);

        processor.processData(data);

        Ticker actualTicker = processor.buildAndFireEventTicker;
        IMarketDepthBook actualBook = processor.buildAndFireEventBook;

        assertEquals(ticker, actualTicker);
        assertEquals(MarketDepthBook.Side.BID, book.getSide());
        assertEquals(1, book.getLevelCount());
        assertEquals(100, book.getTotalSize().intValue());

        MarketDepthLevel level = book.getLevelAt(0);
        assertEquals(MarketDepthBook.Side.BID, level.getSide());
        assertEquals(100, level.getSize().intValue());
        assertEquals(price, level.getPrice());

    }

    @Test
    public void testProcessData_AskSide_EmptyBook() {

        MockIBLevel2QuoteProcessor processor = buildQuoteProcessor();
        Ticker ticker = getCurrencyTicker();
        BigDecimal price = new BigDecimal(1.3553).setScale(4, RoundingMode.HALF_UP);
        Level2QuoteData data = new Level2QuoteData(ticker, 0, processor.OP_INSERT, processor.SIDE_ASK,
                price.doubleValue(), 100);

        processor.processData(data);

        Ticker actualTicker = processor.buildAndFireEventTicker;
        IMarketDepthBook book = processor.buildAndFireEventBook;

        assertEquals(ticker, actualTicker);
        assertEquals(MarketDepthBook.Side.ASK, book.getSide());
        assertEquals(1, book.getLevelCount());
        assertEquals(100, book.getTotalSize().intValue());

        MarketDepthLevel level = book.getLevelAt(0);
        assertEquals(MarketDepthBook.Side.ASK, level.getSide());
        assertEquals(100, level.getSize().intValue());
        assertEquals(price, level.getPrice());
    }

    @Test
    public void testProcessData_AskSide_NonEmptyBook() {
        MockIBLevel2QuoteProcessor processor = buildQuoteProcessor();
        Ticker ticker = getCurrencyTicker();
        IMarketDepthBook book = MarketDepthBook.newInstance();
        book.setSide(MarketDepthBook.Side.ASK);
        processor.askBookMap.put(ticker, book);

        BigDecimal price = new BigDecimal(1.3553).setScale(4, RoundingMode.HALF_UP);
        Level2QuoteData data = new Level2QuoteData(ticker, 0, processor.OP_INSERT, processor.SIDE_ASK,
                price.doubleValue(), 100);

        processor.processData(data);

        Ticker actualTicker = processor.buildAndFireEventTicker;
        IMarketDepthBook actualBook = processor.buildAndFireEventBook;

        assertEquals(ticker, actualTicker);
        assertEquals(MarketDepthBook.Side.ASK, book.getSide());
        assertEquals(1, book.getLevelCount());
        assertEquals(100, book.getTotalSize().intValue());

        MarketDepthLevel level = book.getLevelAt(0);
        assertEquals(MarketDepthBook.Side.ASK, level.getSide());
        assertEquals(100, level.getSize().intValue());
        assertEquals(price, level.getPrice());
    }

    @Test
    public void testUpdateLevel() {
        MockIBLevel2QuoteProcessor processor = buildQuoteProcessor();
        final IMarketDepthBook mockBook = mockery.mock(IMarketDepthBook.class);
        final int index = 0;

        MarketDepthBook.setTestInstance(mockBook);
        Ticker ticker = getCurrencyTicker();
        processor.askBookMap.put(ticker, mockBook);
        double price = 1.3551;
        BigDecimal bdPrice = QuoteUtil.getBigDecimalValue(ticker, price);
        Level2QuoteData data = new Level2QuoteData(ticker, 0, processor.OP_UPDATE, processor.SIDE_BID,
                bdPrice.doubleValue(), 500);
        final MarketDepthLevel level = new MarketDepthLevel(MarketDepthBook.Side.BID, bdPrice, 500);

        mockery.checking(new Expectations() {

            {
                one(mockBook).setSide(MarketDepthBook.Side.BID);
                one(mockBook).updateLevel(index, level);
            }
        });

        processor.processData(data);
        mockery.assertIsSatisfied();

    }

    @Test
    public void testDeleteLevel() {
        MockIBLevel2QuoteProcessor processor = buildQuoteProcessor();
        final IMarketDepthBook mockBook = mockery.mock(IMarketDepthBook.class);
        final int index = 0;

        MarketDepthBook.setTestInstance(mockBook);
        Ticker ticker = getCurrencyTicker();
        processor.askBookMap.put(ticker, mockBook);
        double price = 1.3551;
        BigDecimal bdPrice = QuoteUtil.getBigDecimalValue(ticker, price);
        Level2QuoteData data = new Level2QuoteData(ticker, 0, processor.OP_DELETE, processor.SIDE_BID,
                bdPrice.doubleValue(), 500);

        mockery.checking(new Expectations() {

            {
                one(mockBook).setSide(MarketDepthBook.Side.BID);
                one(mockBook).deleteLevel(index);
            }
        });

        processor.processData(data);
        mockery.assertIsSatisfied();

    }

    @Test
    public void testUnknownOperation() {
        MockIBLevel2QuoteProcessor processor = buildQuoteProcessor();
        final IMarketDepthBook mockBook = mockery.mock(IMarketDepthBook.class);
        final int index = 0;

        MarketDepthBook.setTestInstance(mockBook);
        Ticker ticker = getCurrencyTicker();
        processor.askBookMap.put(ticker, mockBook);
        double price = 1.3551;
        BigDecimal bdPrice = QuoteUtil.getBigDecimalValue(ticker, price);
        Level2QuoteData data = new Level2QuoteData(ticker, 0, 99, processor.SIDE_BID, bdPrice.doubleValue(), 500);

        mockery.checking(new Expectations() {

            {
                one(mockBook).setSide(MarketDepthBook.Side.BID);
            }
        });

        try {
            processor.processData(data);
            fail();
        } catch (IllegalStateException ex) {
            // this should happen
        }
        mockery.assertIsSatisfied();
    }

    @Test
    public void testRemoveTicker() {
        MockIBLevel2QuoteProcessor processor = buildQuoteProcessor();
        Ticker ticker = getCurrencyTicker();
        IMarketDepthBook bidBook = MarketDepthBook.newInstance();
        IMarketDepthBook askBook = MarketDepthBook.newInstance();

        processor.bidBookMap.put(ticker, bidBook);
        processor.askBookMap.put(ticker, askBook);

        assertEquals(1, processor.bidBookMap.size());
        assertEquals(1, processor.askBookMap.size());

        processor.removeTicker(ticker);

        assertTrue(processor.bidBookMap.isEmpty());
        assertTrue(processor.askBookMap.isEmpty());

    }

    @Test
    public void testBuildAndFireEvent_Bid() {
        final IQuoteEngine mockQuoteEngine = mockery.mock(IQuoteEngine.class, "new");
        final IMarketDepthBook mockBook = mockery.mock(IMarketDepthBook.class);
        QuoteType type;

        MockIBLevel2QuoteProcessor processor = buildQuoteProcessor(false);
        processor.quoteEngine = mockQuoteEngine;
        Ticker ticker = getCurrencyTicker();

        final Level2Quote quote = new Level2Quote(ticker, QuoteType.MARKET_DEPTH_BID, processor.date, mockBook);

        mockery.checking(new Expectations() {

            {
                one(mockBook).getSide();
                will(returnValue(MarketDepthBook.Side.BID));

                one(mockQuoteEngine).fireMarketDepthQuote(quote);
            }
        });

        processor.buildAndFireEvent(ticker, mockBook);
        mockery.assertIsSatisfied();

    }

    @Test
    public void testBuildAndFireEvent_Ask() {
        final IQuoteEngine mockQuoteEngine = mockery.mock(IQuoteEngine.class, "new");
        final IMarketDepthBook mockBook = mockery.mock(IMarketDepthBook.class);
        QuoteType type;

        MockIBLevel2QuoteProcessor processor = buildQuoteProcessor(false);
        processor.quoteEngine = mockQuoteEngine;
        Ticker ticker = getCurrencyTicker();

        final Level2Quote quote = new Level2Quote(ticker, QuoteType.MARKET_DEPTH_ASK, processor.date, mockBook);

        mockery.checking(new Expectations() {

            {
                one(mockBook).getSide();
                will(returnValue(MarketDepthBook.Side.ASK));

                one(mockQuoteEngine).fireMarketDepthQuote(quote);
            }
        });

        processor.buildAndFireEvent(ticker, mockBook);
        mockery.assertIsSatisfied();

    }

    protected Ticker getCurrencyTicker() {
        Ticker ticker = new Ticker().setInstrumentType(InstrumentType.CURRENCY);
        ticker.setCurrency("EUR");
        ticker.setMinimumTickSize(new BigDecimal("0.0001"));
        return ticker;
    }

    protected MockIBLevel2QuoteProcessor buildQuoteProcessor() {
        return buildQuoteProcessor(true);

    }

    protected MockIBLevel2QuoteProcessor buildQuoteProcessor(boolean override) {
        BlockingQueue<Level2QuoteData> mockQueue = mockery.mock(BlockingQueue.class);
        IQuoteEngine mockQuoteEngine = mockery.mock(IQuoteEngine.class);
        return new MockIBLevel2QuoteProcessor(mockQueue, mockQuoteEngine, override);

    }

    private static class MockIBLevel2QuoteProcessor extends IBLevel2QuoteProcessor {

        Ticker buildAndFireEventTicker;
        IMarketDepthBook buildAndFireEventBook;
        ZonedDateTime date = ZonedDateTime.now();
        boolean overrideBuildAndFireEvent = true;

        public MockIBLevel2QuoteProcessor(BlockingQueue<Level2QuoteData> queue, IQuoteEngine quoteEngine) {
            this(queue, quoteEngine, true);
        }

        public MockIBLevel2QuoteProcessor(BlockingQueue<Level2QuoteData> queue, IQuoteEngine quoteEngine,
                boolean override) {
            super(queue, quoteEngine);
            overrideBuildAndFireEvent = override;
        }

        @Override
        protected void buildAndFireEvent(Ticker ticker, IMarketDepthBook book) {
            if (overrideBuildAndFireEvent) {
                buildAndFireEventTicker = ticker;
                buildAndFireEventBook = book;
            } else {
                super.buildAndFireEvent(ticker, book);
            }
        }

        @Override
        protected ZonedDateTime getTime() {
            return date;
        }
    }
}
