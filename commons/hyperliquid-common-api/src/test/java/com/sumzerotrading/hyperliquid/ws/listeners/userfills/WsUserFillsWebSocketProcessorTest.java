package com.sumzerotrading.hyperliquid.ws.listeners.userfills;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class WsUserFillsWebSocketProcessorTest {
    @Test
    void testParseMessage_validUserFills() {
        String json = "{" + "\"channel\":\"userFills\"," + "\"isSnapshot\":true," + "\"user\":\"testuser\","
                + "\"fills\":[{"
                + "\"coin\":\"BTC\",\"px\":\"110000.0\",\"sz\":\"0.01\",\"side\":\"B\",\"time\":1759193135807,"
                + "\"startPosition\":\"0.00\",\"dir\":\"buy\",\"closedPnl\":\"0.00\",\"hash\":\"0xabc\",\"oid\":39878035067,"
                + "\"crossed\":true,\"fee\":\"-0.01\",\"tid\":12345,\"feeToken\":\"USDC\",\"builderFee\":\"0.001\"}]"
                + "}";
        WsUserFillsWebSocketProcessor processor = new WsUserFillsWebSocketProcessor(null);
        WsUserFill userFill = processor.parseMessage(json);
        assertNotNull(userFill);
        assertTrue(userFill.isSnapshot());
        assertEquals("testuser", userFill.getUser());
        List<WsFill> fills = userFill.getFills();
        assertEquals(1, fills.size());
        WsFill fill = fills.get(0);
        assertEquals("BTC", fill.getCoin());
        assertEquals("110000.0", fill.getPrice());
        assertEquals("0.01", fill.getSize());
        assertEquals("B", fill.getSide());
        assertEquals(1759193135807L, fill.getTime());
        assertEquals("0.00", fill.getStartPosition());
        assertEquals("buy", fill.getDir());
        assertEquals("0.00", fill.getClosedPnl());
        assertEquals("0xabc", fill.getHash());
        assertEquals(39878035067L, fill.getOrderId());
        assertTrue(fill.isTaker());
        assertEquals("-0.01", fill.getFee());
        assertEquals(12345L, fill.getTradeId());
        assertEquals("USDC", fill.getFeeToken());
        assertEquals("0.001", fill.getBuilderFee());
    }

    @Test
    void testParseMessage_emptyFills() {
        String json = "{\"channel\":\"userFills\",\"user\":\"testuser\",\"fills\":[]}";
        WsUserFillsWebSocketProcessor processor = new WsUserFillsWebSocketProcessor(null);
        WsUserFill userFill = processor.parseMessage(json);
        assertNotNull(userFill);
        assertEquals("testuser", userFill.getUser());
        assertTrue(userFill.getFills().isEmpty());
    }

    @Test
    void testParseMessage_missingUser() {
        String json = "{\"channel\":\"userFills\",\"fills\":[]}";
        WsUserFillsWebSocketProcessor processor = new WsUserFillsWebSocketProcessor(null);
        WsUserFill userFill = processor.parseMessage(json);
        assertNotNull(userFill);
        assertNull(userFill.getUser());
        assertTrue(userFill.getFills().isEmpty());
    }

    @Test
    void testParseMessage_nullFills() {
        String json = "{\"channel\":\"userFills\",\"user\":\"testuser\"}";
        WsUserFillsWebSocketProcessor processor = new WsUserFillsWebSocketProcessor(null);
        WsUserFill userFill = processor.parseMessage(json);
        assertNotNull(userFill);
        assertEquals("testuser", userFill.getUser());
        assertTrue(userFill.getFills().isEmpty());
    }
}
