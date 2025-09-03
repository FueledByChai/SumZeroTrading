package com.sumzerotrading.marketdata;

public interface OrderFlowListener {

    /**
     * Invoked when a new orderflow message is received.
     * 
     * @param orderflow The orderflow message that was received.
     */
    public void orderflowReceived(OrderFlow orderflow);
}
