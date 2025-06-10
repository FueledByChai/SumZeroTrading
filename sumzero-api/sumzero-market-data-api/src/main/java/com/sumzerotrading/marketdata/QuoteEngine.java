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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.sumzerotrading.marketdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.data.Ticker;

/**
 * @author Rob Terpilowski
 *
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class QuoteEngine implements IQuoteEngine {

    protected static final Logger logger = LoggerFactory.getLogger(QuoteEngine.class);

    protected List<ErrorListener> errorListeners;
    protected Map<Ticker, List<Level1QuoteListener>> level1ListenerMap = Collections
            .synchronizedMap(new HashMap<Ticker, List<Level1QuoteListener>>());
    protected Map<Ticker, List<Level2QuoteListener>> level2ListenerMap = Collections
            .synchronizedMap(new HashMap<Ticker, List<Level2QuoteListener>>());

    public QuoteEngine() {
        errorListeners = new ArrayList<ErrorListener>();
    }

    public void addErrorListener(ErrorListener listener) {
        synchronized (errorListeners) {
            errorListeners.add(listener);
        }
    }

    public void removeErrorListener(ErrorListener listener) {
        synchronized (errorListeners) {
            errorListeners.remove(listener);
        }
    }

    public void fireErrorEvent(QuoteError error) {
        synchronized (errorListeners) {
            for (int i = 0; i < errorListeners.size(); i++) {
                ((ErrorListener) errorListeners.get(i)).quoteEngineError(error);
            }
        }
    }

    public void subscribeLevel1(Ticker ticker, Level1QuoteListener listener) {
        synchronized (level1ListenerMap) {
            List<Level1QuoteListener> listeners = level1ListenerMap.get(ticker);
            if (listeners == null) {
                listeners = Collections.synchronizedList(new ArrayList<Level1QuoteListener>());
                level1ListenerMap.put(ticker, listeners);
            }
            synchronized (listeners) {
                listeners.add(listener);
            }
        }
    }

    public void unsubscribeLevel1(Ticker ticker, Level1QuoteListener listener) {
        synchronized (level1ListenerMap) {
            List<Level1QuoteListener> listeners = level1ListenerMap.get(ticker);
            if (listeners != null) {
                synchronized (listeners) {
                    listeners.remove(listener);
                }
            }
        }
    }

    @Override
    public void fireLevel1Quote(final ILevel1Quote quote) {
        synchronized (level1ListenerMap) {
            List<Level1QuoteListener> listeners = level1ListenerMap.get(quote.getTicker());
            if (listeners == null) {
                return;
            }
            for (final Level1QuoteListener listener : listeners) {
                try {
                    synchronized (listeners) {
                        Thread thread = new Thread(new Runnable() {
                            public void run() {
                                listener.quoteRecieved(quote);
                            }
                        });
                        thread.start();
                    }
                } catch (Exception ex) {
                    // don't let 1 listener blowing up prevent other listeners from getting the
                    // quote.
                    // logger.error(ex, ex);
                }
            }
        }
    }

    @Override
    public void fireMarketDepthQuote(ILevel2Quote quote) {
        synchronized (level2ListenerMap) {
            List<Level2QuoteListener> listeners = level2ListenerMap.get(quote.getTicker());
            if (listeners == null) {
                return;
            }
            for (Level2QuoteListener listener : listeners) {
                try {
                    synchronized (listeners) {
                        Thread thread = new Thread(() -> {
                            listener.level2QuoteReceived(quote);
                        });
                        thread.start();
                    }
                } catch (Exception ex) {
                    // logger.error(ex, ex);
                }
            }
        }
    }

    public void subscribeMarketDepth(Ticker ticker, Level2QuoteListener listener) {
        synchronized (ticker) {
            List<Level2QuoteListener> listeners = level2ListenerMap.get(ticker);
            if (listeners == null) {
                listeners = Collections.synchronizedList(new ArrayList<Level2QuoteListener>());
                level2ListenerMap.put(ticker, listeners);
            }
            listeners.add(listener);
        }
    }

    public void unsubscribeMarketDepth(Ticker ticker, Level2QuoteListener listener) {
        synchronized (ticker) {
            List<Level2QuoteListener> listeners = level2ListenerMap.get(ticker);
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }
}
