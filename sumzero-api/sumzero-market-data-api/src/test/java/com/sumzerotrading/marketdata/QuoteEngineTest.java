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

package com.sumzerotrading.marketdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;

/**
 *
 * @author Rob Terpilowski
 */
public class QuoteEngineTest {

    Mockery mockery;

    public QuoteEngineTest() {
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
    public void testAddAndRemoveErrorListener() {
        QuoteEngine engine = createNewQuoteEngine();
        ErrorListener listener = new ErrorListener() {

            public void quoteEngineError(QuoteError error) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        engine.addErrorListener(listener);
        assertEquals(1, engine.errorListeners.size());

        engine.removeErrorListener(listener);
        assertEquals(0, engine.errorListeners.size());
    }

    @Test
    public void testFireErrorEvent() {
        QuoteEngine engine = createNewQuoteEngine();
        QuoteError error = new QuoteError("foo");
        final StringBuffer sb = new StringBuffer();

        ErrorListener listener = new ErrorListener() {

            public void quoteEngineError(QuoteError error) {
                sb.append(error.getMessage());
            }

        };

        engine.addErrorListener(listener);
        engine.fireErrorEvent(error);
        assertEquals("foo", sb.toString());

    }

    @Test
    public void testSubscribeAndUnsubscribeLevel1() {
        QuoteEngine engine = createNewQuoteEngine();
        Level1QuoteListener listener1 = new Level1QuoteListener() {

            public void quoteRecieved(ILevel1Quote quote) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        Level1QuoteListener listener2 = new Level1QuoteListener() {

            public void quoteRecieved(ILevel1Quote quote) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        Ticker ticker = new Ticker("ABC").setInstrumentType(InstrumentType.STOCK);

        assertEquals(0, engine.level1ListenerMap.size());

        engine.subscribeLevel1(ticker, listener1);
        assertEquals(1, engine.level1ListenerMap.size());
        assertEquals(1, engine.level1ListenerMap.get(ticker).size());

        engine.subscribeLevel1(ticker, listener2);
        assertEquals(1, engine.level1ListenerMap.size());
        assertEquals(2, engine.level1ListenerMap.get(ticker).size());

        engine.unsubscribeLevel1(ticker, listener2);
        assertEquals(1, engine.level1ListenerMap.size());
        assertEquals(1, engine.level1ListenerMap.get(ticker).size());

        engine.unsubscribeLevel1(ticker, listener1);
        assertEquals(1, engine.level1ListenerMap.size());
        assertEquals(0, engine.level1ListenerMap.get(ticker).size());

    }

    @Test
    public void testSubscribeAndUnsubscribeMarketDepth() {
        QuoteEngine engine = createNewQuoteEngine();
        Level2QuoteListener listener1 = new Level2QuoteListener() {

            public void level2QuoteReceived(ILevel2Quote level2Quote) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        Level2QuoteListener listener2 = new Level2QuoteListener() {

            public void level2QuoteReceived(ILevel2Quote level2Quote) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        Ticker ticker = new Ticker("ABC").setInstrumentType(InstrumentType.STOCK);

        assertEquals(0, engine.level1ListenerMap.size());

        engine.subscribeMarketDepth(ticker, listener1);
        assertEquals(1, engine.level2ListenerMap.size());
        assertEquals(1, engine.level2ListenerMap.get(ticker).size());

        engine.subscribeMarketDepth(ticker, listener2);
        assertEquals(1, engine.level2ListenerMap.size());
        assertEquals(2, engine.level2ListenerMap.get(ticker).size());

        engine.unsubscribeMarketDepth(ticker, listener2);
        assertEquals(1, engine.level2ListenerMap.size());
        assertEquals(1, engine.level2ListenerMap.get(ticker).size());

        engine.unsubscribeMarketDepth(ticker, listener1);
        assertEquals(1, engine.level2ListenerMap.size());
        assertEquals(0, engine.level2ListenerMap.get(ticker).size());
    }

    @Test
    public void testFireLevel1Quote() throws Exception {

        QuoteEngine quoteEngine = createNewQuoteEngine();
        final ILevel1Quote mockQuote = mock(ILevel1Quote.class);
        final Map<Ticker, List<Level1QuoteListener>> listenerMap = new HashMap<>();
        final Ticker ticker = new Ticker("ABC").setInstrumentType(InstrumentType.STOCK);
        final Level1QuoteListener mockListener = mock(Level1QuoteListener.class);
        final List<Level1QuoteListener> listenerList = new ArrayList<>();
        listenerList.add(mockListener);
        listenerMap.put(ticker, listenerList);
        quoteEngine.level1ListenerMap = listenerMap;

        when(mockQuote.getTicker()).thenReturn(ticker);

        quoteEngine.fireLevel1Quote(mockQuote);

        // Quote engine fires on a new thread, so pause while it has a chance to do so.
        Thread.sleep(1000);

        verify(mockListener).quoteRecieved(mockQuote);

    }

    @Test
    public void testFireLevel1Quote_ThrowsException() {
        QuoteEngine quoteEngine = createNewQuoteEngine();
        final ILevel1Quote mockQuote = mock(ILevel1Quote.class);
        final Map mockLevel1ListenerMap = mock(Map.class);
        final Ticker ticker = new Ticker("ABC").setInstrumentType(InstrumentType.STOCK);
        final Level1QuoteListener mockListener = mock(Level1QuoteListener.class);
        final List<Level1QuoteListener> mockListenerList = new ArrayList<Level1QuoteListener>();
        mockListenerList.add(mockListener);
        quoteEngine.level1ListenerMap = mockLevel1ListenerMap;

        when(mockQuote.getTicker()).thenReturn(ticker);
        when(mockLevel1ListenerMap.get(ticker)).thenReturn(mockListenerList);
        doThrow(new IllegalStateException("bogus")).when(mockListener).quoteRecieved(mockQuote);

        try {
            quoteEngine.fireLevel1Quote(mockQuote);
        } catch (Exception ex) {
            fail();
        }

    }

    @Test
    public void testFireLevel1Quote_NoListeners() {
        QuoteEngine quoteEngine = createNewQuoteEngine();
        final ILevel1Quote mockQuote = mockery.mock(ILevel1Quote.class);
        final Map mockLevel1ListenerMap = mockery.mock(Map.class);
        final Ticker ticker = new Ticker("ABC").setInstrumentType(InstrumentType.STOCK);
        quoteEngine.level1ListenerMap = mockLevel1ListenerMap;

        mockery.checking(new Expectations() {
            {
                one(mockQuote).getTicker();
                will(returnValue(ticker));

                one(mockLevel1ListenerMap).get(ticker);
                will(returnValue(null));

            }
        });

        quoteEngine.fireLevel1Quote(mockQuote);
        mockery.assertIsSatisfied();

    }

    @Test
    public void testFireMarketDepthQuote() throws Exception {
        QuoteEngine quoteEngine = createNewQuoteEngine();
        final ILevel2Quote mockQuote = mockery.mock(ILevel2Quote.class);
        final Map mockLevel2ListenerMap = mockery.mock(Map.class);
        final Ticker ticker = new Ticker("ABC").setInstrumentType(InstrumentType.STOCK);
        final Level2QuoteListener mockListener = mockery.mock(Level2QuoteListener.class);
        final List<Level2QuoteListener> mockListenerList = new ArrayList<Level2QuoteListener>();
        mockListenerList.add(mockListener);
        quoteEngine.level2ListenerMap = mockLevel2ListenerMap;

        mockery.checking(new Expectations() {
            {
                one(mockQuote).getTicker();
                will(returnValue(ticker));

                one(mockLevel2ListenerMap).get(ticker);
                will(returnValue(mockListenerList));

                one(mockListener).level2QuoteReceived(mockQuote);

            }
        });

        try {
            quoteEngine.fireMarketDepthQuote(mockQuote);
        } catch (Exception ex) {
            fail();
        }

        Thread.sleep(1000);
        mockery.assertIsSatisfied();

    }

    @Test
    public void testFireLevelMarketDepthQuote_NoListeners() {
        QuoteEngine quoteEngine = createNewQuoteEngine();
        final ILevel2Quote mockQuote = mockery.mock(ILevel2Quote.class);
        final Map mockLevel2ListenerMap = mockery.mock(Map.class);
        final Ticker ticker = new Ticker("ABC").setInstrumentType(InstrumentType.STOCK);
        quoteEngine.level2ListenerMap = mockLevel2ListenerMap;

        mockery.checking(new Expectations() {
            {
                one(mockQuote).getTicker();
                will(returnValue(ticker));

                one(mockLevel2ListenerMap).get(ticker);
                will(returnValue(null));

            }
        });

        quoteEngine.fireMarketDepthQuote(mockQuote);
        mockery.assertIsSatisfied();

    }

    protected QuoteEngine createNewQuoteEngine() {
        return new QuoteEngine() {

            @Override
            public boolean isConnected() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void startEngine() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void startEngine(Properties props) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void stopEngine() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Date getServerTime() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean started() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void fireOrderFlow(OrderFlow orderFlow) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }

            @Override
            public void subscribeOrderFlow(Ticker ticker, OrderFlowListener listener) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }

            @Override
            public void unsubscribeOrderFlow(Ticker ticker, OrderFlowListener listener) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }

            @Override
            public void useDelayedData(boolean useDelayed) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }

        };
    }

}
