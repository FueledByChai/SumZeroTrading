/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.broker;

import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.broker.order.OrderEvent;
import com.sumzerotrading.broker.order.OrderEventListener;
import com.sumzerotrading.time.TimeUpdatedListener;

/**
 *
 * @author RobTerpilowski
 */
public abstract class AbstractBroker implements IBroker {

    protected Set<OrderEventListener> orderEventListeners = new TreeSet<>();
    protected Set<BrokerErrorListener> brokerErrorListeners = new TreeSet<>();
    protected Set<TimeUpdatedListener> timeUpdatedListeners = new TreeSet<>();
    protected static Logger logger = LoggerFactory.getLogger(AbstractBroker.class);

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
                try {
                    listener.orderEvent(event);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
    }

    protected void fireBrokerError(BrokerError error) {
        synchronized (brokerErrorListeners) {
            for (BrokerErrorListener listener : brokerErrorListeners) {
                try {
                    listener.brokerErrorFired(error);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
    }

    protected void fireTimeUpdate() {

    }
}
