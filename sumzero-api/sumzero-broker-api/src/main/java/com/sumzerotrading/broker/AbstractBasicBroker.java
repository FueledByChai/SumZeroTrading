/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.broker;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.broker.order.OrderEvent;
import com.sumzerotrading.broker.order.OrderEventListener;
import com.sumzerotrading.data.ComboTicker;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.time.TimeUpdatedListener;

/**
 *
 * @author RobTerpilowski
 */
public abstract class AbstractBasicBroker implements IBroker {
    /**
     * Disconnects the broker and releases resources. Subclasses should implement
     * onDisconnect() for custom cleanup. This method is final to guarantee resource
     * cleanup.
     */
    @Override
    public final void disconnect() {
        onDisconnect();
        shutdown();
    }

    /**
     * Subclass hook for custom disconnect logic. Called automatically by
     * disconnect().
     */
    protected abstract void onDisconnect();

    /**
     * Shuts down the event executor service. Should be called when the broker is no
     * longer needed.
     */
    public void shutdown() {
        eventExecutor.shutdown();
    }

    protected Set<OrderEventListener> orderEventListeners = new HashSet<>();
    protected Set<BrokerErrorListener> brokerErrorListeners = new HashSet<>();
    protected Set<TimeUpdatedListener> timeUpdatedListeners = new HashSet<>();
    protected Set<BrokerAccountInfoListener> brokerAccountInfoListeners = new HashSet<>();
    protected static Logger logger = LoggerFactory.getLogger(AbstractBasicBroker.class);
    protected final java.util.concurrent.ExecutorService eventExecutor = java.util.concurrent.Executors
            .newCachedThreadPool();

    @Override
    public void addOrderEventListener(OrderEventListener listener) {
        synchronized (orderEventListeners) {
            orderEventListeners.add(listener);
        }
    }

    @Override
    public void addBrokerErrorListener(BrokerErrorListener listener) {
        synchronized (brokerErrorListeners) {
            brokerErrorListeners.add(listener);
        }
    }

    @Override
    public void addTimeUpdateListener(TimeUpdatedListener listener) {
        synchronized (timeUpdatedListeners) {
            timeUpdatedListeners.add(listener);
        }
    }

    @Override
    public void removeOrderEventListener(OrderEventListener listener) {
        synchronized (orderEventListeners) {
            orderEventListeners.remove(listener);
        }
    }

    @Override
    public void removeBrokerErrorListener(BrokerErrorListener listener) {
        synchronized (brokerErrorListeners) {
            brokerErrorListeners.remove(listener);
        }
    }

    @Override
    public void removeTimeUpdateListener(TimeUpdatedListener listener) {
        synchronized (timeUpdatedListeners) {
            timeUpdatedListeners.remove(listener);
        }
    }

    protected void fireOrderEvent(OrderEvent event) {
        synchronized (orderEventListeners) {
            for (OrderEventListener listener : orderEventListeners) {
                eventExecutor.submit(() -> {
                    try {
                        listener.orderEvent(event);
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                });
            }
        }
    }

    protected void fireBrokerError(BrokerError error) {
        synchronized (brokerErrorListeners) {
            for (BrokerErrorListener listener : brokerErrorListeners) {
                eventExecutor.submit(() -> {
                    try {
                        listener.brokerErrorFired(error);
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                });
            }
        }
    }

    protected void fireAccountEquityUpdated(double equity) {
        synchronized (brokerAccountInfoListeners) {
            for (BrokerAccountInfoListener listener : brokerAccountInfoListeners) {
                eventExecutor.submit(() -> {
                    try {
                        listener.accountEquityUpdated(equity);
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                });
            }
        }
    }

    protected void fireAvailableFundsUpdated(double availableFunds) {
        synchronized (brokerAccountInfoListeners) {
            for (BrokerAccountInfoListener listener : brokerAccountInfoListeners) {
                eventExecutor.submit(() -> {
                    try {
                        listener.availableFundsUpdated(availableFunds);
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                });
            }
        }
    }

    @Override
    public void aquireLock() {
        throw new UnsupportedOperationException("Not supported"); // To change body of generated methods, choose Tools |
                                                                  // Templates.

    }

    @Override
    public ComboTicker buildComboTicker(Ticker ticker1, Ticker ticker2) {
        throw new UnsupportedOperationException("Not supported"); // To change body of generated methods, choose Tools |
                                                                  // Templates.
    }

    @Override
    public ComboTicker buildComboTicker(Ticker ticker1, int ratio1, Ticker ticker2, int ratio2) {
        throw new UnsupportedOperationException("Not supported"); // To change body of generated methods, choose Tools |
                                                                  // Templates.
    }

    @Override
    public ZonedDateTime getCurrentTime() {
        throw new UnsupportedOperationException("Not supported"); // To change body of generated methods, choose Tools |
                                                                  // Templates.
    }

    @Override
    public String getFormattedDate(int hour, int minute, int second) {
        throw new UnsupportedOperationException("Not supported"); // To change body of generated methods, choose Tools |
                                                                  // Templates.
    }

    @Override
    public String getFormattedDate(ZonedDateTime date) {
        throw new UnsupportedOperationException("Not supported"); // To change body of generated methods, choose Tools |
                                                                  // Templates.
    }

    @Override
    public void releaseLock() {
        throw new UnsupportedOperationException("Not supported"); // To change body of generated methods, choose Tools |
                                                                  // Templates.
    }

    @Override
    public void addBrokerAccountInfoListener(BrokerAccountInfoListener listener) {
        synchronized (brokerAccountInfoListeners) {
            brokerAccountInfoListeners.add(listener);
        }

    }

    @Override
    public void removeBrokerAccountInfoListener(BrokerAccountInfoListener listener) {
        synchronized (brokerAccountInfoListeners) {
            brokerAccountInfoListeners.remove(listener);
        }

    }

}
