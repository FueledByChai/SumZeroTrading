package com.sumzerotrading.broker.hyperliquid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
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

            // Check if this is a clearinghouseState message
            if (jsonObject.has("channel") && "clearinghouseState".equals(jsonObject.getString("channel"))) {
                processClearinghouseState(jsonObject.getJSONObject("data"));
                return;
            }

            // Legacy message format handling
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

                notifyListeners(accountInfo);

            } else {
                logger.warn("Unknown message type: " + method);
            }
        } catch (Exception e) {
            logger.error("Error processing message: " + message, e);
        }
    }

    private void processClearinghouseState(JSONObject data) {
        try {
            JSONObject clearinghouseState = data.getJSONObject("clearinghouseState");
            JSONObject marginSummary = clearinghouseState.getJSONObject("marginSummary");

            // Extract account information
            double accountValue = Double.parseDouble(marginSummary.getString("accountValue"));
            double totalMarginUsed = Double.parseDouble(marginSummary.getString("totalMarginUsed"));

            // Also get cross maintenance margin if available
            double maintenanceMargin = totalMarginUsed;
            if (clearinghouseState.has("crossMaintenanceMarginUsed")) {
                maintenanceMargin = Double.parseDouble(clearinghouseState.getString("crossMaintenanceMarginUsed"));
            }

            // Parse positions
            List<IPositionUpdate> positions = new ArrayList<>();
            if (clearinghouseState.has("assetPositions")) {
                JSONArray assetPositions = clearinghouseState.getJSONArray("assetPositions");
                for (int i = 0; i < assetPositions.length(); i++) {
                    JSONObject assetPosition = assetPositions.getJSONObject(i);
                    if (assetPosition.has("position")) {
                        JSONObject position = assetPosition.getJSONObject("position");

                        // Extract position data
                        String ticker = position.getString("coin");
                        BigDecimal size = new BigDecimal(position.getString("szi"));
                        BigDecimal entryPrice = new BigDecimal(position.getString("entryPx"));
                        BigDecimal unrealizedPnl = new BigDecimal(position.getString("unrealizedPnl"));
                        BigDecimal liquidationPrice = new BigDecimal(position.getString("liquidationPx"));

                        // Extract funding since open
                        BigDecimal fundingSinceOpen = BigDecimal.ZERO;
                        if (position.has("cumFunding")) {
                            JSONObject cumFunding = position.getJSONObject("cumFunding");
                            if (cumFunding.has("sinceOpen")) {
                                fundingSinceOpen = new BigDecimal(cumFunding.getString("sinceOpen"));
                            }
                        }

                        // Create position update
                        IPositionUpdate positionUpdate = new ParadexPositionUpdate(ticker, size, entryPrice,
                                unrealizedPnl, liquidationPrice, fundingSinceOpen);
                        positions.add(positionUpdate);

                        logger.debug("Parsed position - Ticker: {}, Size: {}, Entry: {}, PnL: {}, Liq: {}, Funding: {}",
                                ticker, size, entryPrice, unrealizedPnl, liquidationPrice, fundingSinceOpen);
                    }
                }
            }

            IAccountUpdate accountInfo = new ParadexAccountInfoUpdate();
            accountInfo.setAccountValue(accountValue);
            accountInfo.setMaintenanceMargin(maintenanceMargin);
            accountInfo.setPositions(positions);

            logger.debug("Processed clearinghouse state - Account Value: {}, Maintenance Margin: {}, Positions: {}",
                    accountValue, maintenanceMargin, positions.size());

            notifyListeners(accountInfo);

        } catch (Exception e) {
            logger.error("Error processing clearinghouse state", e);
        }
    }

    private void notifyListeners(IAccountUpdate accountInfo) {
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
    }

}
