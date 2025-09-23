package com.sumzerotrading.broker.hyperliquid;

import com.sumzerotrading.websocket.IWebSocketClosedListener;
import com.sumzerotrading.broker.hyperliquid.HyperliquidPositionUpdate;
import com.sumzerotrading.broker.hyperliquid.HyperliquidAccountInfoUpdate;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountWebSocketProcessorTest {

    @Mock
    IWebSocketClosedListener mockClosedListener;
    @Mock
    Logger mockLogger;

    AccountWebSocketProcessor processor;

    @BeforeEach
    public void setup() {
        processor = new AccountWebSocketProcessor(mockClosedListener);
        // Optionally inject mock logger if needed
        // Field injection via reflection if logger is used in tests
    }

    @Test
    public void testParseMessage_ClearinghouseState_Valid() {
        String json = "{" + "\"channel\":\"clearinghouseState\"," + "\"data\": {" + "  \"clearinghouseState\": {"
                + "    \"marginSummary\": {" + "      \"accountValue\": \"1000.5\","
                + "      \"totalMarginUsed\": \"200.5\"}," + "    \"crossMaintenanceMarginUsed\": \"150.0\","
                + "    \"assetPositions\": [" + "      {\"position\": {" + "        \"coin\": \"BTCUSD\","
                + "        \"szi\": \"2.0\"," + "        \"entryPx\": \"50000.0\","
                + "        \"unrealizedPnl\": \"100.0\"," + "        \"liquidationPx\": \"45000.0\","
                + "        \"cumFunding\": {\"sinceOpen\": \"5.0\"}" + "      }}" + "    ]" + "  }" + "}}";
        IAccountUpdate result = processor.parseMessage(json);
        assertNotNull(result);
        assertEquals(1000.5, result.getAccountValue());
        assertEquals(150.0, result.getMaintenanceMargin());
        assertTrue(result instanceof HyperliquidAccountInfoUpdate);
        List<HyperliquidPositionUpdate> positions = ((HyperliquidAccountInfoUpdate) result).getPositions();
        assertEquals(1, positions.size());
        HyperliquidPositionUpdate pos = positions.get(0);
        assertEquals("BTCUSD", pos.getTicker());
        assertEquals(BigDecimal.valueOf(2.0), pos.getSize());
        assertEquals(BigDecimal.valueOf(50000.0), pos.getEntryPrice());
        assertEquals(BigDecimal.valueOf(100.0), pos.getUnrealizedPnl());
        assertEquals(BigDecimal.valueOf(45000.0), pos.getLiquidationPrice());
        assertEquals(BigDecimal.valueOf(5.0), pos.getFundingSinceOpen());
    }

    @Test
    public void testParseMessage_ClearinghouseState_MissingOptionalFields() {
        String json = "{" + "\"channel\":\"clearinghouseState\"," + "\"data\": {" + "  \"clearinghouseState\": {"
                + "    \"marginSummary\": {" + "      \"accountValue\": \"1000.5\","
                + "      \"totalMarginUsed\": \"200.5\"}" + "  }" + "}}";
        IAccountUpdate result = processor.parseMessage(json);
        assertNotNull(result);
        assertEquals(1000.5, result.getAccountValue());
        assertEquals(200.5, result.getMaintenanceMargin());
        assertTrue(result instanceof HyperliquidAccountInfoUpdate);
        List<HyperliquidPositionUpdate> positions = ((HyperliquidAccountInfoUpdate) result).getPositions();
        assertTrue(positions == null || positions.isEmpty());
    }

    @Test
    public void testParseMessage_LegacySubscription_Valid() {
        String json = "{" + "\"method\":\"subscription\"," + "\"params\": {" + "  \"data\": {"
                + "    \"account_value\": \"1234.56\"," + "    \"maintenance_margin_requirement\": \"789.01\"}" + "  }"
                + "}}";
        IAccountUpdate result = processor.parseMessage(json);
        assertNotNull(result);
        assertEquals(1234.56, result.getAccountValue());
        assertEquals(789.01, result.getMaintenanceMargin());
    }

    @Test
    public void testParseMessage_LegacySubscription_MissingFields() {
        String json = "{" + "\"method\":\"subscription\"," + "\"params\": {" + "  \"data\": {"
                + "    \"account_value\": \"1234.56\"}" + "  }" + "}}";
        // Should throw exception or return null due to missing
        // maintenance_margin_requirement
        IAccountUpdate result = processor.parseMessage(json);
        assertNull(result);
    }

    @Test
    public void testParseMessage_UnknownMethod() {
        String json = "{" + "\"method\":\"unknownMethod\"}";
        IAccountUpdate result = processor.parseMessage(json);
        assertNull(result);
    }

    @Test
    public void testParseMessage_NoMethodField() {
        String json = "{" + "\"foo\":\"bar\"}";
        IAccountUpdate result = processor.parseMessage(json);
        assertNull(result);
    }

    @Test
    public void testParseMessage_InvalidJson() {
        String json = "not a valid json";
        IAccountUpdate result = processor.parseMessage(json);
        assertNull(result);
    }

    @Test
    public void testProcessClearinghouseState_Exception() {
        // Missing required fields
        IAccountUpdate result = processor.parseMessage("{\"channel\":\"clearinghouseState\",\"data\":{}}");
        assertNull(result);
    }
}
