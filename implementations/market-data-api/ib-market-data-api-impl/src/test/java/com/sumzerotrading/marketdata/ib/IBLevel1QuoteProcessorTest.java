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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ib.client.Decimal;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.ILevel1Quote;
import com.sumzerotrading.marketdata.IQuoteEngine;
import com.sumzerotrading.marketdata.QuoteType;
import com.sumzerotrading.util.QuoteUtil;

/**
 *
 * @author Rob Terpilowski
 */
public class IBLevel1QuoteProcessorTest {

    protected Mockery mockery;

    public IBLevel1QuoteProcessorTest() {
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
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testProcessData_sizeEvent() {
        Level1QuoteData data = new Level1QuoteData(null, 0, 0, false, Decimal.get(50));

        MockIBLevel1QuoteProcessor processor = buildMockQuoteProcessor(true, true);

        processor.processData(data);
        assertTrue(processor.processTickSizeCalled);
        assertFalse(processor.processTickPriceCalled);
    }

    @Test
    public void testProcessData_priceEvent() {
        Level1QuoteData data = new Level1QuoteData(null, 0, 45.5, false, Decimal.ZERO);

        MockIBLevel1QuoteProcessor processor = buildMockQuoteProcessor(true, true);

        processor.processData(data);
        assertFalse(processor.processTickSizeCalled);
        assertTrue(processor.processTickPriceCalled);
    }

    @Test
    public void testProcessTickSize_unknownQuoteType() {
        MockIBLevel1QuoteProcessor processor = buildMockQuoteProcessor(false, false);
        Level1QuoteData data = new Level1QuoteData(null, 99, 0, false, Decimal.get(10));

        processor.processTickSize(data);
        assertFalse(processor.processQuoteCalled);

    }

    @Test
    public void testProcessTickSize_StockTicker() {
        MockIBLevel1QuoteProcessor processor = buildMockQuoteProcessor(false, false);
        int size = 10;
        Level1QuoteData data = new Level1QuoteData(new Ticker("QQQ").setInstrumentType(InstrumentType.STOCK), 0, 0,
                false, Decimal.get(size));

        processor.processTickSize(data);

        BigDecimal formattedValue = QuoteUtil.getBigDecimalValue(size * 100);
        assertEquals(formattedValue, processor.formattedValue);

    }

    @Test
    public void testProcessTickSize_NonStockTicker() {
        MockIBLevel1QuoteProcessor processor = buildMockQuoteProcessor(false, false);
        int size = 10;
        Level1QuoteData data = new Level1QuoteData(new Ticker().setInstrumentType(InstrumentType.CURRENCY), 0, 0, false,
                Decimal.get(size));

        processor.processTickSize(data);

        BigDecimal formattedValue = QuoteUtil.getBigDecimalValue(size);
        assertEquals(formattedValue, processor.formattedValue);

    }

    @Test
    public void testProcessQuote() {
        BlockingQueue<Level1QuoteData> mockQueue = mockery.mock(BlockingQueue.class);
        final IQuoteEngine mockQuoteEngine = mockery.mock(IQuoteEngine.class);
        IBLevel1QuoteProcessor processor = new IBLevel1QuoteProcessor(mockQueue, mockQuoteEngine);

        mockery.checking(new Expectations() {
            {
                one(mockQuoteEngine).fireLevel1Quote(with(any(ILevel1Quote.class)));
            }
        });
    }

    protected MockIBLevel1QuoteProcessor buildMockQuoteProcessor(boolean overridePrice, boolean overrideSize) {
        BlockingQueue<Level1QuoteData> mockQueue = mockery.mock(BlockingQueue.class);
        IQuoteEngine mockQuoteEngine = mockery.mock(IQuoteEngine.class);

        MockIBLevel1QuoteProcessor processor = new MockIBLevel1QuoteProcessor(mockQueue, mockQuoteEngine, overrideSize,
                overridePrice);
        return processor;
    }

    private static class MockIBLevel1QuoteProcessor extends IBLevel1QuoteProcessor {

        boolean processTickSizeCalled = false;
        boolean processTickPriceCalled = false;
        boolean processQuoteCalled = false;
        BigDecimal formattedValue = BigDecimal.ZERO;
        boolean overrideProcessTickSize = false;
        boolean overrideProcessTickPrice = false;

        public MockIBLevel1QuoteProcessor(BlockingQueue<Level1QuoteData> queue, IQuoteEngine quoteEngine) {
            super(queue, quoteEngine);
        }

        public MockIBLevel1QuoteProcessor(BlockingQueue<Level1QuoteData> queue, IQuoteEngine quoteEngine,
                boolean overrideProcessTickSize, boolean overrideProcessTickPrice) {
            super(queue, quoteEngine);
            this.overrideProcessTickPrice = overrideProcessTickPrice;
            this.overrideProcessTickSize = overrideProcessTickSize;
        }

        @Override
        protected void processTickSize(Level1QuoteData data) {
            if (overrideProcessTickSize) {
                processTickSizeCalled = true;
            } else {
                super.processTickSize(data);
            }
        }

        @Override
        protected void processTickPrice(Level1QuoteData data) {
            if (overrideProcessTickPrice) {
                processTickPriceCalled = true;
            } else {
                super.processTickPrice(data);
            }
        }

        @Override
        protected void processQuote(Ticker ticker, QuoteType quoteType, BigDecimal value) {
            processQuoteCalled = true;
            formattedValue = value;
        }
    }
}
