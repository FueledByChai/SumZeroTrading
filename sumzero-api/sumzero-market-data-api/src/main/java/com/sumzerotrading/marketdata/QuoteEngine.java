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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.data.Ticker;

/**
 * @author Rob Terpilowski
 *
 * 
 */
public abstract class QuoteEngine implements IQuoteEngine {

    protected static final Logger logger = LoggerFactory.getLogger(QuoteEngine.class);

    // Thread pool for handling quote notifications
    private final ExecutorService quoteExecutor;

    // Custom ThreadFactory for naming quote processing threads
    private static class QuoteThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix = "quote-engine-";

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }

    protected List<ErrorListener> errorListeners;
    protected Map<Ticker, List<Level1QuoteListener>> level1ListenerMap = Collections
            .synchronizedMap(new HashMap<Ticker, List<Level1QuoteListener>>());
    protected Map<Ticker, List<Level2QuoteListener>> level2ListenerMap = Collections
            .synchronizedMap(new HashMap<Ticker, List<Level2QuoteListener>>());
    protected Map<Ticker, List<OrderFlowListener>> orderFlowListenerMap = Collections
            .synchronizedMap(new HashMap<Ticker, List<OrderFlowListener>>());

    public QuoteEngine() {
        this(100); // Default to 100 threads
    }

    /**
     * Constructor that allows specifying the number of threads for quote processing
     * 
     * @param threadPoolSize The number of threads to use for processing quotes
     */
    public QuoteEngine(int threadPoolSize) {
        errorListeners = new ArrayList<ErrorListener>();
        // Initialize thread pool with specified number of threads for quote processing
        quoteExecutor = Executors.newFixedThreadPool(threadPoolSize, new QuoteThreadFactory());
        logger.info("Initialized QuoteEngine with {} threads for quote processing", threadPoolSize);
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

    @Override
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

    @Override
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
    public void subscribeOrderFlow(Ticker ticker, OrderFlowListener listener) {
        synchronized (orderFlowListenerMap) {
            List<OrderFlowListener> listeners = orderFlowListenerMap.get(ticker);
            if (listeners == null) {
                listeners = Collections.synchronizedList(new ArrayList<OrderFlowListener>());
                orderFlowListenerMap.put(ticker, listeners);
            }
            synchronized (listeners) {
                listeners.add(listener);
            }
        }
    }

    @Override
    public void unsubscribeOrderFlow(Ticker ticker, OrderFlowListener listener) {
        synchronized (orderFlowListenerMap) {
            List<OrderFlowListener> listeners = orderFlowListenerMap.get(ticker);
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
                        quoteExecutor.submit(() -> {
                            try {
                                listener.quoteRecieved(quote);
                            } catch (Exception ex) {
                                logger.warn("Error processing Level1 quote for listener", ex);
                            }
                        });
                    }
                } catch (Exception ex) {
                    // don't let 1 listener blowing up prevent other listeners from getting the
                    // quote.
                    logger.warn("Error submitting Level1 quote task", ex);
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
                        quoteExecutor.submit(() -> {
                            try {
                                listener.level2QuoteReceived(quote);
                            } catch (Exception ex) {
                                logger.warn("Error processing Level2 quote for listener", ex);
                            }
                        });
                    }
                } catch (Exception ex) {
                    logger.warn("Error submitting Level2 quote task", ex);
                }
            }
        }
    }

    @Override
    public void fireOrderFlow(OrderFlow orderFlow) {
        synchronized (orderFlowListenerMap) {
            List<OrderFlowListener> listeners = orderFlowListenerMap.get(orderFlow.getTicker());
            if (listeners == null) {
                return;
            }
            for (OrderFlowListener listener : listeners) {
                try {
                    synchronized (listeners) {
                        quoteExecutor.submit(() -> {
                            try {
                                listener.orderflowReceived(orderFlow);
                            } catch (Exception ex) {
                                logger.warn("Error processing OrderFlow for listener", ex);
                            }
                        });
                    }
                } catch (Exception ex) {
                    logger.warn("Error submitting OrderFlow task", ex);
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

    /**
     * Shuts down the quote processing thread pool. Should be called when the
     * QuoteEngine is no longer needed to prevent resource leaks.
     */
    public void shutdown() {
        if (quoteExecutor != null && !quoteExecutor.isShutdown()) {
            logger.info("Shutting down quote processing thread pool");
            quoteExecutor.shutdown();
        }
    }

    /**
     * Immediately shuts down the quote processing thread pool. Should be called in
     * emergency situations to force immediate shutdown.
     */
    public void shutdownNow() {
        if (quoteExecutor != null && !quoteExecutor.isShutdown()) {
            logger.info("Force shutting down quote processing thread pool");
            quoteExecutor.shutdownNow();
        }
    }

    /**
     * Checks if the quote processing thread pool has been shut down
     * 
     * @return true if the thread pool has been shut down, false otherwise
     */
    public boolean isShutdown() {
        return quoteExecutor == null || quoteExecutor.isShutdown();
    }
}
