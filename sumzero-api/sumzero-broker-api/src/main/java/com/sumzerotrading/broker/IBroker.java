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

package com.sumzerotrading.broker;

import java.time.ZonedDateTime;
import java.util.List;

import com.sumzerotrading.broker.order.FillEventListener;
import com.sumzerotrading.broker.order.OrderEventListener;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.data.ComboTicker;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.time.TimeUpdatedListener;

/**
 */
public interface IBroker {

    /**
     * Cancels the order with the specified ID
     *
     * @param id The id of the order to cancel
     */
    public abstract BrokerRequestResult cancelOrder(String id);

    /**
     * Cancels the specified order
     *
     * @param order The order to cancel
     */
    public abstract BrokerRequestResult cancelOrder(OrderTicket order);

    /**
     * Cancels all orders for the specified ticker.
     * 
     * @param ticker The ticker for which to cancel all orders.
     */
    BrokerRequestResult cancelAllOrders(Ticker ticker);

    /**
     * Cancels all open orders.
     */
    BrokerRequestResult cancelAllOrders();

    /**
     * places the specified order with the broker
     *
     * @param order The order to place with the broker.
     */
    public abstract void placeOrder(OrderTicket order);

    /**
     * Gets the next order ID, or -1 if there was a problem getting the orderId
     *
     * @return the next order id or -1 if there was a problem getting the orderId
     */
    public abstract String getNextOrderId();

    /**
     * Add a listener to receive OrderEvents.
     *
     * @param listener
     */
    public abstract void addOrderEventListener(OrderEventListener listener);

    /**
     * Add a listener to receive FillEvents.
     *
     * @param listener
     */
    public abstract void addFillEventListener(FillEventListener listener);

    /**
     * Remove a listener from receiving fill events.
     *
     * @param listener
     */
    public abstract void removeFillEventListener(FillEventListener listener);

    /**
     * Remove a listener from receiving order events.
     *
     * @param listener
     */
    public abstract void removeOrderEventListener(OrderEventListener listener);

    void addBrokerAccountInfoListener(BrokerAccountInfoListener listener);

    void removeBrokerAccountInfoListener(BrokerAccountInfoListener listener);

    /**
     * Add a listener to receive broker errors
     * 
     * @param listener
     */
    public abstract void addBrokerErrorListener(BrokerErrorListener listener);

    /**
     * Remove the specified BrokerErrorListener
     * 
     * @param listener
     */
    public abstract void removeBrokerErrorListener(BrokerErrorListener listener);

    /**
     * Returns a properly formatted string for today's date with the specified hour
     * and minute
     *
     * @param hour
     * @param minute
     * @return a properly formatted string for today's date with the specified hour
     *         and minute. Formatted to the broker's specs.
     */
    public abstract String getFormattedDate(int hour, int minute, int second);

    /**
     * Returns a properly formatted string for the specified date
     *
     * @param date The date to format
     * @return A String in the brokers format for the specified date.
     */
    public abstract String getFormattedDate(ZonedDateTime date);

    /**
     * Gets the current date/time from the broker
     *
     * @return the current date/time from the broker.
     */
    public abstract ZonedDateTime getCurrentTime();

    /**
     * Establishes the initial connection to the broker.
     */
    public void connect();

    /**
     * Disconnects from the broker.
     */
    public void disconnect();

    /**
     *
     * @return true if connected to the broker
     */
    public boolean isConnected();

    /**
     * Used to lock the broker in order to create order IDs and transmit orders,
     * since IB does not tolerate orders IDs that are transmitted out of order.
     *
     * @throws InterruptedException
     */
    public void aquireLock();

    /**
     * Used to release the broker from the lock
     *
     * @throws InterruptedException
     */
    public void releaseLock();

    public ComboTicker buildComboTicker(Ticker ticker1, Ticker ticker2);

    public ComboTicker buildComboTicker(Ticker ticker1, int ratio1, Ticker ticker2, int ratio2);

    /**
     * Request the status of a specific order. The request will cause an
     * ExecutionReport message to be fired.
     *
     * @param orderId The orderId of the Order to retreive.
     * @return The TradeOrder for the specified ID, or null if no trade was found.
     */
    public OrderTicket requestOrderStatus(String orderId);

    /**
     * Requests all open orders
     * 
     * @return A list of all open orders.
     */
    public List<OrderTicket> getOpenOrders();

    /**
     * Cancels the order with the specified ID, and replaced it with the specified
     * order
     * 
     * @param originalOrderId The ID of the order to cancel
     * @param newOrder        The order to replace the canceled order with.
     */
    public void cancelAndReplaceOrder(String originalOrderId, OrderTicket newOrder);

    /**
     * The broker will send out updates once per second so that the trading
     * strategies are synced off the broker's time, and not from the PC's time.
     * Useful when backtestings
     * 
     * @param listener The listener to update.
     */
    public void addTimeUpdateListener(TimeUpdatedListener listener);

    /**
     * Remove the specified timeUpdateListener
     * 
     * @param listener
     */
    public void removeTimeUpdateListener(TimeUpdatedListener listener);

    /**
     * Request all open positions for this client.
     *
     * @return The open positions for this client.
     */
    public List<Position> getAllPositions();

}
