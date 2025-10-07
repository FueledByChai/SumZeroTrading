package com.sumzerotrading.hyperliquid.ws.listeners.accountinfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.websocket.AbstractWebSocketProcessor;
import com.sumzerotrading.websocket.IWebSocketClosedListener;

public class AccountWebSocketProcessor extends AbstractWebSocketProcessor<IAccountUpdate> {

    private static final Logger logger = LoggerFactory.getLogger(AccountWebSocketProcessor.class);

    public AccountWebSocketProcessor(IWebSocketClosedListener listener) {
        super(listener);
    }

    @Override
    protected IAccountUpdate parseMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);

            // Check if this is a clearinghouseState message
            if (jsonObject.has("channel") && "clearinghouseState".equals(jsonObject.getString("channel"))) {
                return processClearinghouseState(jsonObject.getJSONObject("data"));
            }

            // Legacy message format handling
            if (!jsonObject.has("method")) {
                return null;
            }
            String method = jsonObject.getString("method");

            if ("subscription".equals(method)) {
                JSONObject params = jsonObject.getJSONObject("params");
                JSONObject data = params.getJSONObject("data");

                // Use safe parsing for legacy format too
                double accountValue = safeParseDouble(data, "account_value", 0.0);
                double maintMargin = safeParseDouble(data, "maintenance_margin_requirement", 0.0);

                IAccountUpdate accountInfo = new HyperliquidAccountInfoUpdate();
                accountInfo.setAccountValue(accountValue);
                accountInfo.setMaintenanceMargin(maintMargin);

                return accountInfo;
            } else {
                logger.warn("Unknown message type: " + method);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error parsing message: " + message, e);
            return null;
        }
    }

    private IAccountUpdate processClearinghouseState(JSONObject data) {
        try {
            JSONObject clearinghouseState = data.getJSONObject("clearinghouseState");
            JSONObject marginSummary = clearinghouseState.getJSONObject("marginSummary");

            // Extract account information with null safety
            double accountValue = safeParseDouble(marginSummary, "accountValue", 0.0);
            double totalMarginUsed = safeParseDouble(marginSummary, "totalMarginUsed", 0.0);

            // Also get cross maintenance margin if available
            double maintenanceMargin = totalMarginUsed;
            if (clearinghouseState.has("crossMaintenanceMarginUsed")) {
                maintenanceMargin = safeParseDouble(clearinghouseState, "crossMaintenanceMarginUsed", totalMarginUsed);
            }

            // Parse positions
            List<HyperliquidPositionUpdate> positions = new ArrayList<>();
            if (clearinghouseState.has("assetPositions")) {
                JSONArray assetPositions = clearinghouseState.getJSONArray("assetPositions");
                for (int i = 0; i < assetPositions.length(); i++) {
                    JSONObject assetPosition = assetPositions.getJSONObject(i);
                    if (assetPosition.has("position")) {
                        JSONObject position = assetPosition.getJSONObject("position");

                        // Extract position data with null safety
                        String ticker = position.optString("coin", "UNKNOWN");
                        BigDecimal size = safeParseBigDecimal(position, "szi", BigDecimal.ZERO);
                        BigDecimal entryPrice = safeParseBigDecimal(position, "entryPx", BigDecimal.ZERO);
                        BigDecimal unrealizedPnl = safeParseBigDecimal(position, "unrealizedPnl", BigDecimal.ZERO);
                        BigDecimal liquidationPrice = safeParseBigDecimal(position, "liquidationPx", BigDecimal.ZERO);

                        // Extract funding since open with null safety
                        BigDecimal fundingSinceOpen = BigDecimal.ZERO;
                        if (position.has("cumFunding")) {
                            JSONObject cumFunding = position.getJSONObject("cumFunding");
                            if (cumFunding.has("sinceOpen")) {
                                fundingSinceOpen = safeParseBigDecimal(cumFunding, "sinceOpen", BigDecimal.ZERO);
                            }
                        }

                        // Create position update
                        HyperliquidPositionUpdate positionUpdate = new HyperliquidPositionUpdate(ticker, size,
                                entryPrice, unrealizedPnl, liquidationPrice, fundingSinceOpen);
                        positions.add(positionUpdate);

                        logger.debug("Parsed position - Ticker: {}, Size: {}, Entry: {}, PnL: {}, Liq: {}, Funding: {}",
                                ticker, size, entryPrice, unrealizedPnl, liquidationPrice, fundingSinceOpen);
                    }
                }
            }

            HyperliquidAccountInfoUpdate accountInfo = new HyperliquidAccountInfoUpdate();
            accountInfo.setAccountValue(accountValue);
            accountInfo.setMaintenanceMargin(maintenanceMargin);
            accountInfo.setPositions(positions);

            logger.debug("Processed clearinghouse state - Account Value: {}, Maintenance Margin: {}, Positions: {}",
                    accountValue, maintenanceMargin, positions.size());

            return accountInfo;

        } catch (Exception e) {
            logger.error("Error processing clearinghouse state", e);
            logger.error("Data: " + data.toString());
            return null;
        }
    }

    /**
     * Safely parse a double value from JSON, handling null values and null strings
     */
    private double safeParseDouble(JSONObject jsonObject, String key, double defaultValue) {
        if (!jsonObject.has(key) || jsonObject.isNull(key)) {
            return defaultValue;
        }
        try {
            String value = jsonObject.getString(key);
            if (value == null || "null".equals(value) || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Double.parseDouble(value);
        } catch (JSONException e) {
            // Handle case where value is not a string (e.g., actual null)
            logger.warn("Failed to get string value for key '{}', using default: {}", key, defaultValue);
            return defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse double value for key '{}', using default: {}", key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Safely parse a BigDecimal value from JSON, handling null values and null
     * strings
     */
    private BigDecimal safeParseBigDecimal(JSONObject jsonObject, String key, BigDecimal defaultValue) {
        if (!jsonObject.has(key) || jsonObject.isNull(key)) {
            return defaultValue;
        }
        try {
            String value = jsonObject.getString(key);
            if (value == null || "null".equals(value) || value.trim().isEmpty()) {
                return defaultValue;
            }
            return new BigDecimal(value);
        } catch (JSONException e) {
            // Handle case where value is not a string (e.g., actual null)
            logger.warn("Failed to get string value for key '{}', using default: {}", key, defaultValue);
            return defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse BigDecimal value for key '{}', using default: {}", key, defaultValue);
            return defaultValue;
        }
    }

}
