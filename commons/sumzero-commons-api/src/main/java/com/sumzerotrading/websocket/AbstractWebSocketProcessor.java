package com.sumzerotrading.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract WebSocket processor that provides common listener management and
 * notification logic. Subclasses implement the specific message parsing logic
 * and define the event type.
 * 
 * This eliminates code duplication by handling: - Listener registration/removal
 * - Thread-safe notification using ExecutorService - Common WebSocket lifecycle
 * methods - Error handling and logging
 * 
 * Usage Pattern: 1. Extend this class with your specific event type (T) 2.
 * Implement parseMessage() to convert JSON/String messages to your event
 * objects 3. Your listeners should implement IWebSocketEventListener<T> 4.
 * Optionally override connection lifecycle methods for custom logging
 * 
 * Example: ```java public class AccountProcessor extends
 * AbstractWebSocketProcessor<AccountUpdate> {
 * 
 * protected AccountUpdate parseMessage(String message) { // Parse JSON message
 * and return AccountUpdate object return new AccountUpdate(...); } }
 * 
 * public interface IAccountUpdateListener extends
 * IWebSocketEventListener<AccountUpdate> { // Can add additional methods if
 * needed } ```
 * 
 * @param <T> The type of event/update object that will be passed to listeners
 */
public abstract class AbstractWebSocketProcessor<T> implements IWebSocketProcessor {

    protected ExecutorService executorService = Executors.newFixedThreadPool(10);
    protected static final Logger logger = LoggerFactory.getLogger(AbstractWebSocketProcessor.class);
    protected IWebSocketClosedListener websocketClosedListener;
    protected List<IWebSocketEventListener<T>> eventListeners = new ArrayList<>();

    public AbstractWebSocketProcessor(IWebSocketClosedListener listener) {
        this.websocketClosedListener = listener;
    }

    @Override
    public void connectionClosed(int code, String reason, boolean remote) {
        logger.info("Disconnected from WebSocket: " + reason);
        websocketClosedListener.connectionClosed();
    }

    @Override
    public void connectionError(Exception error) {
        logger.error(error.getMessage(), error);
        websocketClosedListener.connectionClosed();
    }

    @Override
    public void connectionEstablished() {
        logger.info("Connection Established to WebSocket");
    }

    @Override
    public void connectionOpened() {
        // Default implementation - subclasses can override if needed
    }

    /**
     * Add a listener to receive events from this processor
     */
    public void addEventListener(IWebSocketEventListener<T> listener) {
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }

    /**
     * Remove a listener from receiving events
     */
    public void removeEventListener(IWebSocketEventListener<T> listener) {
        eventListeners.remove(listener);
    }

    @Override
    public final void messageReceived(String message) {
        try {
            logger.debug("Received message from WebSocket: {}", message);

            // Let subclass parse the message and create the event object
            T event = parseMessage(message);

            // If parsing was successful, notify listeners
            if (event != null) {
                notifyListeners(event);
            }
        } catch (Exception e) {
            logger.error("Error processing message: " + message, e);
        }
    }

    /**
     * Parse the incoming message and create an event object. Subclasses implement
     * this method to handle their specific message format.
     * 
     * @param message The raw message received from the WebSocket
     * @return The parsed event object, or null if the message should be ignored
     */
    protected abstract T parseMessage(String message);

    /**
     * Notify all registered listeners about an event
     */
    protected final void notifyListeners(T event) {
        for (IWebSocketEventListener<T> listener : eventListeners) {
            executorService.execute(() -> {
                try {
                    listener.onWebSocketEvent(event);
                } catch (Exception e) {
                    logger.error("Error notifying listener", e);
                }
            });
        }
    }

    /**
     * Get the current number of registered listeners
     */
    public int getListenerCount() {
        return eventListeners.size();
    }

    /**
     * Shutdown the executor service when the processor is no longer needed
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}