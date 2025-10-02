package com.sumzerotrading.hyperliquid.ws.listeners.userfills;

import com.sumzerotrading.websocket.IWebSocketEventListener;

/**
 * Listener interface for user fill updates. Extends the generic
 * IWebSocketEventListener to work with the AbstractWebSocketProcessor.
 */
public interface IWsUserFillUpdateListener extends IWebSocketEventListener<WsUserFill> {

    /**
     * Called when an order update is received. This method delegates to the generic
     * onEvent method for compatibility.
     * 
     * @param userFill The user fill information
     */
    public void userFillUpdated(WsUserFill userFill);

    /**
     * Default implementation of the generic onEvent method. This allows the
     * AbstractWebSocketProcessor to call listeners generically while maintaining
     * the specific userFillUpdated method for backwards compatibility.
     */
    @Override
    default void onWebSocketEvent(WsUserFill event) {
        userFillUpdated(event);
    }
}
