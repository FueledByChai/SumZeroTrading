package com.sumzerotrading.hyperliquid.ws.listeners.orderupdates;

import java.util.List;

import com.sumzerotrading.websocket.IWebSocketEventListener;

/**
 * Listener interface for order updates. Extends the generic
 * IWebSocketEventListener to work with the AbstractWebSocketProcessor.
 */
public interface IWsOrderUpdateListener extends IWebSocketEventListener<List<WsOrderUpdate>> {

    /**
     * Called when an order update is received. This method delegates to the generic
     * onEvent method for compatibility.
     * 
     * @param orderUpdates The list of order update information
     */
    public void orderUpdated(List<WsOrderUpdate> orderUpdate);

    /**
     * Default implementation of the generic onEvent method. This allows the
     * AbstractWebSocketProcessor to call listeners generically while maintaining
     * the specific orderUpdated method for backwards compatibility.
     */
    @Override
    default void onWebSocketEvent(List<WsOrderUpdate> event) {
        orderUpdated(event);
    }
}
