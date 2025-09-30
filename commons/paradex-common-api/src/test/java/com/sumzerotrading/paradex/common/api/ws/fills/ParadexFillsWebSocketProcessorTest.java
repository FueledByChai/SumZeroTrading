package com.sumzerotrading.paradex.common.api.ws.fills;

import com.sumzerotrading.websocket.IWebSocketClosedListener;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParadexFillsWebSocketProcessorTest {

    @Test
    public void testParseMessage_validFill() {
        ParadexFillsWebSocketProcessor processor = new ParadexFillsWebSocketProcessor((IWebSocketClosedListener) null);
        String json = "{" + "\"method\":\"subscription\"," + "\"params\":{" + "    \"data\": {"
                + "        \"account\": \"acct1\"," + "        \"client_id\": \"cid123\","
                + "        \"created_at\": 1690000000," + "        \"fee\": \"0.01\","
                + "        \"fee_currency\": \"USDC\"," + "        \"fill_type\": \"FILL\","
                + "        \"flags\": [\"maker\"]," + "        \"id\": \"fillid1\","
                + "        \"liquidity\": \"MAKER\"," + "        \"market\": \"BTC-USD\","
                + "        \"order_id\": \"oid1\"," + "        \"price\": \"30000.5\","
                + "        \"realized_funding\": \"0.0\"," + "        \"realized_pnl\": \"1.5\","
                + "        \"remaining_size\": \"0.1\"," + "        \"side\": \"BUY\"," + "        \"size\": \"0.5\","
                + "        \"underlying_price\": \"29999.9\"" + "    }" + "  }" + "}";
        ParadexFill fill = processor.parseMessage(json);
        assertNotNull(fill);
        assertEquals("acct1", fill.account);
        assertEquals("cid123", fill.clientId);
        assertEquals(1690000000L, fill.createdAt);
        assertEquals("0.01", fill.fee);
        assertEquals("USDC", fill.feeCurrency);
        assertEquals(ParadexFill.FillType.FILL, fill.fillType);
        assertNotNull(fill.flags);
        assertTrue(fill.flags.contains("maker"));
        assertEquals("fillid1", fill.id);
        assertEquals(ParadexFill.LiquidityType.MAKER, fill.liquidity);
        assertEquals("BTC-USD", fill.market);
        assertEquals("oid1", fill.orderId);
        assertEquals("30000.5", fill.price);
        assertEquals("0.0", fill.realizedFunding);
        assertEquals("1.5", fill.realizedPnl);
        assertEquals("0.1", fill.remainingSize);
        assertEquals(ParadexFill.Side.BUY, fill.side);
        assertEquals("0.5", fill.size);
        assertEquals("29999.9", fill.underlyingPrice);
    }

    @Test
    public void testParseMessage_invalidMethod() {
        ParadexFillsWebSocketProcessor processor = new ParadexFillsWebSocketProcessor((IWebSocketClosedListener) null);
        String json = "{\"method\":\"other\"}";
        ParadexFill fill = processor.parseMessage(json);
        assertNull(fill);
    }

    @Test
    public void testParseMessage_missingMethod() {
        ParadexFillsWebSocketProcessor processor = new ParadexFillsWebSocketProcessor((IWebSocketClosedListener) null);
        String json = "{\"foo\":\"bar\"}";
        ParadexFill fill = processor.parseMessage(json);
        assertNull(fill);
    }

    @Test
    public void testParseMessage_malformedJson() {
        ParadexFillsWebSocketProcessor processor = new ParadexFillsWebSocketProcessor((IWebSocketClosedListener) null);
        String json = "not a json string";
        ParadexFill fill = processor.parseMessage(json);
        assertNull(fill);
    }
}
