package com.sumzerotrading.hyperliquid.ws.listeners.accountinfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
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
                String accountValueString = data.getString("account_value");
                String maintMarginString = data.getString("maintenance_margin_requirement");

                IAccountUpdate accountInfo = new HyperliquidAccountInfoUpdate();
                accountInfo.setAccountValue(Double.parseDouble(accountValueString));
                accountInfo.setMaintenanceMargin(Double.parseDouble(maintMarginString));

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

            // Extract account information
            double accountValue = Double.parseDouble(marginSummary.getString("accountValue"));
            double totalMarginUsed = Double.parseDouble(marginSummary.getString("totalMarginUsed"));

            // Also get cross maintenance margin if available
            double maintenanceMargin = totalMarginUsed;
            if (clearinghouseState.has("crossMaintenanceMarginUsed")) {
                maintenanceMargin = Double.parseDouble(clearinghouseState.getString("crossMaintenanceMarginUsed"));
            }

            // Parse positions
            List<HyperliquidPositionUpdate> positions = new ArrayList<>();
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
            return null;
        }
    }

}
