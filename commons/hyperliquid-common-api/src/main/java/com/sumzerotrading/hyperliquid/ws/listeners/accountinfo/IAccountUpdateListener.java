package com.sumzerotrading.hyperliquid.ws.listeners.accountinfo;

import com.sumzerotrading.websocket.IWebSocketEventListener;

/**
 * Listener interface for account updates. Extends the generic
 * IWebSocketEventListener to work with the AbstractWebSocketProcessor.
 */
public interface IAccountUpdateListener extends IWebSocketEventListener<IAccountUpdate> {

    /**
     * Called when an account update is received. This method delegates to the
     * generic onEvent method for compatibility.
     * 
     * @param accountInfo The account update information
     */
    public void accountUpdated(IAccountUpdate accountInfo);

    /**
     * Default implementation of the generic onEvent method. This allows the
     * AbstractWebSocketProcessor to call listeners generically while maintaining
     * the specific accountUpdated method for backwards compatibility.
     */
    @Override
    default void onWebSocketEvent(IAccountUpdate event) {
        accountUpdated(event);
    }
}
