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

package com.sumzerotrading.realtime.bar.ib;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.quartz.impl.StdSchedulerFactory;

import com.sumzerotrading.data.BarData;
import com.sumzerotrading.data.BarData.LengthUnit;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.historicaldata.IHistoricalDataProvider;
import com.sumzerotrading.marketdata.IQuoteEngine;
import com.sumzerotrading.realtime.bar.RealtimeBarListener;
import com.sumzerotrading.realtime.bar.RealtimeBarRequest;

import junit.framework.TestCase;

/**
 *
 * @author Rob Terpilowski
 */
public class IBRealTimeBarEngineTest extends TestCase {

    protected Mockery mockery;

    public IBRealTimeBarEngineTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockery = new Mockery();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testConstructor() {
        IQuoteEngine mockQuoteEngine = mockery.mock(IQuoteEngine.class);
        IHistoricalDataProvider mockHistoricalData = mockery.mock(IHistoricalDataProvider.class);

        IBRealTimeBarEngine engine = new IBRealTimeBarEngine(mockQuoteEngine, mockHistoricalData);

        assertEquals(mockQuoteEngine, engine.quoteEngine);
        assertEquals(mockHistoricalData, engine.historicalDataProvider);
        assertTrue(engine.schedulerFactory instanceof StdSchedulerFactory);
    }

    @Test
    public void testSubscribeRealtimeBars() {
        final Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        int timeInterval = 1;
        final IQuoteEngine mockQuoteEngine = mockery.mock(IQuoteEngine.class);
        final IBarBuilder mockBarBuilder = mockery.mock(IBarBuilder.class);
        IHistoricalDataProvider mockHistoricalData = mockery.mock(IHistoricalDataProvider.class);
        RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, timeInterval, LengthUnit.MINUTE);

        IBRealTimeBarEngine engine = new IBRealTimeBarEngine(mockQuoteEngine, mockHistoricalData);

        final RealtimeBarListener listener = new RealtimeBarListener() {

            public void realtimeBarReceived(int requestId, Ticker ticker, BarData bar) {
            }
        };

        mockery.checking(new Expectations() {
            {
                one(mockBarBuilder).addBarListener(listener);
            }
        });

        engine.barMap.put(request, mockBarBuilder);
        engine.subscribeRealtimeBars(request, listener);

        mockery.assertIsSatisfied();

    }

    @Test
    public void testSubscribeRealtimeBars_OneHourRequest() {
        final Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        int timeInterval = 1;
        final IQuoteEngine mockQuoteEngine = mockery.mock(IQuoteEngine.class);
        final IBarBuilder mockBarBuilder = mockery.mock(IBarBuilder.class);
        IHistoricalDataProvider mockHistoricalData = mockery.mock(IHistoricalDataProvider.class);
        RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, timeInterval, LengthUnit.HOUR);

        IBRealTimeBarEngine engine = new IBRealTimeBarEngine(mockQuoteEngine, mockHistoricalData);

        final RealtimeBarListener listener = new RealtimeBarListener() {

            public void realtimeBarReceived(int requestId, Ticker ticker, BarData bar) {
            }
        };

        mockery.checking(new Expectations() {
            {
                one(mockBarBuilder).addBarListener(listener);
            }
        });

        engine.barMap.put(request, mockBarBuilder);
        engine.subscribeRealtimeBars(request, listener);

        mockery.assertIsSatisfied();

    }

    @Test
    public void testSubscribeRealtimeBars_NoBuilderExists() {
        final Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        int timeInterval = 1;
        final IQuoteEngine mockQuoteEngine = mockery.mock(IQuoteEngine.class);
        final IBarBuilder mockBarBuilder = mockery.mock(IBarBuilder.class);
        IHistoricalDataProvider mockHistoricalData = mockery.mock(IHistoricalDataProvider.class);
        RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, timeInterval, LengthUnit.MINUTE);

        IBRealTimeBarEngine engine = new IBRealTimeBarEngine(mockQuoteEngine, mockHistoricalData);
        engine.testBarBuilder = mockBarBuilder;

        final RealtimeBarListener listener = new RealtimeBarListener() {

            public void realtimeBarReceived(int requestId, Ticker ticker, BarData bar) {
            }
        };

        mockery.checking(new Expectations() {
            {
                one(mockQuoteEngine).subscribeLevel1(ticker, mockBarBuilder);
                one(mockBarBuilder).addBarListener(listener);
            }
        });

        engine.subscribeRealtimeBars(request, listener);
        assertEquals(mockBarBuilder, engine.barMap.get(request));

        mockery.assertIsSatisfied();

    }

    @Test
    public void testSubscribeRealtimeBars_NonMinuteRequest() {
        final Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        int timeInterval = 2;
        final IQuoteEngine mockQuoteEngine = mockery.mock(IQuoteEngine.class);
        final IBarBuilder mockBarBuilder = mockery.mock(IBarBuilder.class);
        IHistoricalDataProvider mockHistoricalData = mockery.mock(IHistoricalDataProvider.class);
        RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, timeInterval, LengthUnit.HOUR);

        IBRealTimeBarEngine engine = new IBRealTimeBarEngine(mockQuoteEngine, mockHistoricalData);
        engine.testBarBuilder = mockBarBuilder;

        final RealtimeBarListener listener = new RealtimeBarListener() {

            public void realtimeBarReceived(int requestId, Ticker ticker, BarData bar) {
            }
        };

        try {
            engine.subscribeRealtimeBars(request, listener);
            fail();
        } catch (IllegalStateException ex) {
            // this should happen
        }

        mockery.assertIsSatisfied();

    }

    @Test
    public void testSubscribeRealtimeBars_ZeroMinuteRequest() {
        final Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        int timeInterval = 0;
        final IQuoteEngine mockQuoteEngine = mockery.mock(IQuoteEngine.class);
        final IBarBuilder mockBarBuilder = mockery.mock(IBarBuilder.class);
        IHistoricalDataProvider mockHistoricalData = mockery.mock(IHistoricalDataProvider.class);
        RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, timeInterval, LengthUnit.MINUTE);

        IBRealTimeBarEngine engine = new IBRealTimeBarEngine(mockQuoteEngine, mockHistoricalData);
        engine.testBarBuilder = mockBarBuilder;

        final RealtimeBarListener listener = new RealtimeBarListener() {

            public void realtimeBarReceived(int requestId, Ticker ticker, BarData bar) {
            }
        };

        try {
            engine.subscribeRealtimeBars(request, listener);
            fail();
        } catch (IllegalStateException ex) {
            // this should happen
        }

        mockery.assertIsSatisfied();

    }

    @Test
    public void testSubscribeRealtimeBars_61MinuteRequest() {
        final Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        int timeInterval = 61;
        final IQuoteEngine mockQuoteEngine = mockery.mock(IQuoteEngine.class);
        final IBarBuilder mockBarBuilder = mockery.mock(IBarBuilder.class);
        IHistoricalDataProvider mockHistoricalData = mockery.mock(IHistoricalDataProvider.class);
        RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, timeInterval, LengthUnit.MINUTE);

        IBRealTimeBarEngine engine = new IBRealTimeBarEngine(mockQuoteEngine, mockHistoricalData);
        engine.testBarBuilder = mockBarBuilder;

        final RealtimeBarListener listener = new RealtimeBarListener() {

            public void realtimeBarReceived(int requestId, Ticker ticker, BarData bar) {
            }
        };

        try {
            engine.subscribeRealtimeBars(request, listener);
            fail();
        } catch (IllegalStateException ex) {
            // this should happen
        }

        mockery.assertIsSatisfied();

    }

    @Test
    public void testUnsubscribeRealtimeBars() {
        Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        int timeInterval = 1;
        IQuoteEngine mockQuoteEngine = mockery.mock(IQuoteEngine.class);
        IHistoricalDataProvider mockHistoricalData = mockery.mock(IHistoricalDataProvider.class);
        RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, timeInterval, LengthUnit.MINUTE);
        final RealtimeBarListener listener = mockery.mock(RealtimeBarListener.class);
        final IBarBuilder mockBuilder = mockery.mock(IBarBuilder.class);

        IBRealTimeBarEngine engine = new IBRealTimeBarEngine(mockQuoteEngine, mockHistoricalData);
        engine.barMap.put(request, mockBuilder);

        mockery.checking(new Expectations() {
            {
                one(mockBuilder).removeBarListener(listener);

                one(mockBuilder).getListenerCount();
                will(returnValue(1));

            }
        });

        engine.unsubscribeRealtimeBars(request, listener);
        mockery.assertIsSatisfied();

    }

    @Test
    public void testUnsubscribeRealtimeBars_NoMoreListeners() {
        final Ticker ticker = new Ticker("qqq").setInstrumentType(InstrumentType.STOCK);
        int timeInterval = 1;
        final IQuoteEngine mockQuoteEngine = mockery.mock(IQuoteEngine.class);
        IHistoricalDataProvider mockHistoricalData = mockery.mock(IHistoricalDataProvider.class);
        RealtimeBarRequest request = new RealtimeBarRequest(1, ticker, timeInterval, LengthUnit.MINUTE);
        final RealtimeBarListener listener = mockery.mock(RealtimeBarListener.class);
        final IBarBuilder mockBuilder = mockery.mock(IBarBuilder.class);

        IBRealTimeBarEngine engine = new IBRealTimeBarEngine(mockQuoteEngine, mockHistoricalData);
        engine.barMap.put(request, mockBuilder);

        mockery.checking(new Expectations() {
            {
                one(mockBuilder).removeBarListener(listener);

                one(mockBuilder).getListenerCount();
                will(returnValue(0));

                one(mockQuoteEngine).unsubscribeLevel1(ticker, mockBuilder);

            }
        });

        engine.unsubscribeRealtimeBars(request, listener);
        assertNull(engine.barMap.get(request));
        mockery.assertIsSatisfied();

    }

    @Test
    public void testIsConnected() {
        final IQuoteEngine mockQuoteEngine = mockery.mock(IQuoteEngine.class);
        final IHistoricalDataProvider mockHistoricalData = mockery.mock(IHistoricalDataProvider.class);

        mockery.checking(new Expectations() {
            {
                one(mockQuoteEngine).isConnected();
                will(returnValue(false));

                one(mockQuoteEngine).isConnected();
                will(returnValue(true));
                one(mockHistoricalData).isConnected();
                will(returnValue(false));

                one(mockQuoteEngine).isConnected();
                will(returnValue(true));
                one(mockHistoricalData).isConnected();
                will(returnValue(true));
            }
        });

        IBRealTimeBarEngine engine = new IBRealTimeBarEngine(mockQuoteEngine, mockHistoricalData);
        assertFalse(engine.isConnected());

        assertFalse(engine.isConnected());
        assertTrue(engine.isConnected());
    }
}
