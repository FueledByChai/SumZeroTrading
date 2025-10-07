package com.sumzerotrading.hyperliquid.ws.listeners.accountinfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class AccountWebSocketProcessorNullSafetyTest {

    @Test
    public void testProcessClearinghouseStateWithNullValues() {
        AccountWebSocketProcessor processor = new AccountWebSocketProcessor(null);

        // Create a JSON message with null string values that could cause NPE
        JSONObject message = new JSONObject();
        message.put("channel", "clearinghouseState");

        JSONObject data = new JSONObject();
        JSONObject clearinghouseState = new JSONObject();
        JSONObject marginSummary = new JSONObject();

        // Test with null string values
        marginSummary.put("accountValue", "null");
        marginSummary.put("totalMarginUsed", (String) null);

        clearinghouseState.put("marginSummary", marginSummary);
        clearinghouseState.put("crossMaintenanceMarginUsed", "");

        data.put("clearinghouseState", clearinghouseState);
        message.put("data", data);

        // This should not throw an exception
        IAccountUpdate result = processor.parseMessage(message.toString());

        assertNotNull(result);
        assertEquals(0.0, result.getAccountValue()); // Should use default value
        assertEquals(0.0, result.getMaintenanceMargin()); // Should use default value
    }

    @Test
    public void testProcessLegacyFormatWithNullValues() {
        AccountWebSocketProcessor processor = new AccountWebSocketProcessor(null);

        // Create a legacy format message with null values
        JSONObject message = new JSONObject();
        message.put("method", "subscription");

        JSONObject params = new JSONObject();
        JSONObject data = new JSONObject();

        data.put("account_value", "null");
        data.put("maintenance_margin_requirement", (String) null);

        params.put("data", data);
        message.put("params", params);

        // This should not throw an exception
        IAccountUpdate result = processor.parseMessage(message.toString());

        assertNotNull(result);
        assertEquals(0.0, result.getAccountValue()); // Should use default value
        assertEquals(0.0, result.getMaintenanceMargin()); // Should use default value
    }

    @Test
    public void testProcessClearinghouseStateWithActualJsonNulls() {
        AccountWebSocketProcessor processor = new AccountWebSocketProcessor(null);

        // Create a JSON message with actual JSON null values (like liquidationPx: null)
        JSONObject message = new JSONObject();
        message.put("channel", "clearinghouseState");

        JSONObject data = new JSONObject();
        JSONObject clearinghouseState = new JSONObject();
        JSONObject marginSummary = new JSONObject();

        marginSummary.put("accountValue", "100.0");
        marginSummary.put("totalMarginUsed", "50.0");

        clearinghouseState.put("marginSummary", marginSummary);

        // Add position with actual JSON null for liquidationPx (this was causing the
        // exception)
        JSONArray assetPositions = new JSONArray();
        JSONObject assetPosition = new JSONObject();
        JSONObject position = new JSONObject();

        position.put("coin", "BTC");
        position.put("szi", "1.0");
        position.put("entryPx", "50000.0");
        position.put("unrealizedPnl", "1000.0");
        position.put("liquidationPx", JSONObject.NULL); // This is actual JSON null, not string "null"

        assetPosition.put("position", position);
        assetPositions.put(assetPosition);
        clearinghouseState.put("assetPositions", assetPositions);

        data.put("clearinghouseState", clearinghouseState);
        message.put("data", data);

        // This should not throw JSONException anymore
        IAccountUpdate result = processor.parseMessage(message.toString());

        assertNotNull(result);
        assertEquals(100.0, result.getAccountValue());
        assertEquals(50.0, result.getMaintenanceMargin());
    }
}