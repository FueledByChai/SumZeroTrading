package com.sumzerotrading.websocket;

/**
 * Generic listener interface for WebSocket events. All specific WebSocket
 * listeners should extend this interface.
 * 
 * This allows the AbstractWebSocketProcessor to handle notifications
 * generically without requiring subclasses to implement a notifyListener
 * method.
 * 
 * Example usage: ```java public interface IAccountUpdateListener extends
 * IWebSocketEventListener<IAccountUpdate> { // Can add additional specific
 * methods if needed }
 * 
 * // Implementation public class MyAccountListener implements
 * IAccountUpdateListener {
 * 
 * @Override public void onEvent(IAccountUpdate event) { // Handle account
 *           update } } ```
 * 
 * @param <T> The type of event this listener handles
 */
public interface IWebSocketEventListener<T> {

    /**
     * Called when a WebSocket event occurs.
     * 
     * @param event The event data
     */
    void onWebSocketEvent(T event);
}