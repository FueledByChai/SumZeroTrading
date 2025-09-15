package com.sumzerotrading.broker.paradex;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.websocket.IWebSocketClosedListener;
import com.sumzerotrading.websocket.IWebSocketProcessor;

public class AccountWebSocketProcessor implements IWebSocketProcessor {

    protected ExecutorService executorService = Executors.newFixedThreadPool(10);
    protected static final Logger logger = LoggerFactory.getLogger(AccountWebSocketProcessor.class);
    protected IWebSocketClosedListener websocketClosedListener;
    protected List<IAccountUpdateListener> accountUpdateListeners = new ArrayList<>();

    public AccountWebSocketProcessor(IWebSocketClosedListener listener) {
        this.websocketClosedListener = listener;
    }

    @Override
    public void connectionClosed(int code, String reason, boolean remote) {
        logger.info("Disconnected from Paradex Account WebSocket: " + reason);
        websocketClosedListener.connectionClosed();

    }

    @Override
    public void connectionError(Exception error) {
        logger.error(error.getMessage(), error);
        websocketClosedListener.connectionClosed();

    }

    @Override
    public void connectionEstablished() {
        logger.info("Connection Established to Paradex Account WebSocket");

    }

    @Override
    public void connectionOpened() {
        // TODO Auto-generated method stub

    }

    public void addAccountUpdateListener(IAccountUpdateListener accountUpdateListener) {
        if (!accountUpdateListeners.contains(accountUpdateListener)) {
            accountUpdateListeners.add(accountUpdateListener);
        }
    }

    public void removeAccountUpdateListener(IAccountUpdateListener accountUpdateListener) {
        accountUpdateListeners.remove(accountUpdateListener);
    }

    @Override
    public void messageReceived(String message) {
        try {
            logger.debug("Received message from Paradex Account WebSocket: {}", message);
            JSONObject jsonObject = new JSONObject(message);
            if (!jsonObject.has("method")) {
                return;
            }
            String method = jsonObject.getString("method");

            if ("subscription".equals(method)) {
                JSONObject params = jsonObject.getJSONObject("params");
                JSONObject data = params.getJSONObject("data");
                String accountValueString = data.getString("account_value");
                String maintMarginString = data.getString("maintenance_margin_requirement");

                IAccountUpdate accountInfo = new ParadexAccountInfoUpdate();
                accountInfo.setAccountValue(Double.parseDouble(accountValueString));
                accountInfo.setMaintenanceMargin(Double.parseDouble(maintMarginString));

                for (IAccountUpdateListener accountUpdateListener : accountUpdateListeners) {

                    executorService.execute(() -> {
                        try {
                            // Ensure the listener is called in a thread-safe manner
                            accountUpdateListener.accountUpdated(accountInfo);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    });

                }

            } else {
                logger.warn("Unknown message type: " + method);
            }
        } catch (Exception e) {
            logger.error("Error processing message: " + message, e);
        }

    }

}
