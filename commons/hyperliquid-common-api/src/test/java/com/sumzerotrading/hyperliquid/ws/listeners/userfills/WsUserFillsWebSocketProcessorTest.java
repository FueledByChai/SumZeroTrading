package com.sumzerotrading.hyperliquid.ws.listeners.userfills;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class WsUserFillsWebSocketProcessorTest {
    @Test
    void testParseMessage_withDataObject() {
        String json = "{" + "  \"channel\": \"userFills\"," + "  \"data\": {"
                + "    \"user\": \"0x4325acf0f1308627ac9eea448f9227823f272722\"," + "    \"fills\": [" + "      {"
                + "        \"coin\": \"BTC\"," + "        \"px\": \"117256.0\"," + "        \"sz\": \"0.01\","
                + "        \"side\": \"B\"," + "        \"time\": 1759350149634,"
                + "        \"startPosition\": \"0.01\"," + "        \"dir\": \"Open Long\","
                + "        \"closedPnl\": \"0.0\","
                + "        \"hash\": \"0x8966904246e84c278ae0041a3f6c55010d00a827e1eb6af92d2f3b9505ec2612\","
                + "        \"oid\": 39982072416," + "        \"crossed\": true," + "        \"fee\": \"0.527652\","
                + "        \"tid\": 246513242811435," + "        \"cloid\": \"0xc4ca4238a0b923820dcc509a6f75849b\","
                + "        \"feeToken\": \"USDC\"," + "        \"twapId\": null" + "      }" + "    ]" + "  }" + "}";

        WsUserFillsWebSocketProcessor processor = new WsUserFillsWebSocketProcessor(null);
        WsUserFill userFill = processor.parseMessage(json);
        assertNotNull(userFill);
        assertEquals("0x4325acf0f1308627ac9eea448f9227823f272722", userFill.getUser());
        assertNotNull(userFill.getFills());
        assertEquals(1, userFill.getFills().size());
        WsFill fill = userFill.getFills().get(0);
        assertEquals("BTC", fill.getCoin());
        assertEquals("117256.0", fill.getPrice());
        assertEquals("0.01", fill.getSize());
        assertEquals("B", fill.getSide());
        assertEquals(1759350149634L, fill.getTime());
        assertEquals("0.01", fill.getStartPosition());
        assertEquals("Open Long", fill.getDir());
        assertEquals("0.0", fill.getClosedPnl());
        assertEquals("0x8966904246e84c278ae0041a3f6c55010d00a827e1eb6af92d2f3b9505ec2612", fill.getHash());
        assertEquals(39982072416L, fill.getOrderId());
        assertTrue(fill.isTaker());
        assertEquals("0.527652", fill.getFee());
        assertEquals(246513242811435L, fill.getTradeId());
        assertEquals("USDC", fill.getFeeToken());
    }

    @Test
    void testParseMessage_providedJson() {
        String json = "{" + "  \"channel\": \"userFills\","
                + "  \"user\": \"0x4325acf0f1308627ac9eea448f9227823f272722\"," + "  \"fills\": [" + "    {"
                + "      \"coin\": \"BTC\"," + "      \"px\": \"117325.0\"," + "      \"sz\": \"0.01\","
                + "      \"side\": \"B\"," + "      \"time\": 1759345544745," + "      \"startPosition\": \"0.02\","
                + "      \"dir\": \"Open Long\"," + "      \"closedPnl\": \"0.0\","
                + "      \"hash\": \"0x93c7ade4c0f737fe9541041a3f0188011700c5ca5bfa56d0379059377ffb11e9\","
                + "      \"oid\": 39979575327," + "      \"crossed\": true," + "      \"fee\": \"0.527962\","
                + "      \"tid\": 393900969593217," + "      \"cloid\": \"0xc4ca4238a0b923820dcc509a6f75849b\","
                + "      \"feeToken\": \"USDC\"," + "      \"twapId\": null" + "    }" + "  ]" + "}";

        WsUserFillsWebSocketProcessor processor = new WsUserFillsWebSocketProcessor(null);
        WsUserFill userFill = processor.parseMessage(json);
        assertNotNull(userFill);
        assertEquals("0x4325acf0f1308627ac9eea448f9227823f272722", userFill.getUser());
        assertNotNull(userFill.getFills());
        assertEquals(1, userFill.getFills().size());
        WsFill fill = userFill.getFills().get(0);
        assertEquals("BTC", fill.getCoin());
        assertEquals("117325.0", fill.getPrice());
        assertEquals("0.01", fill.getSize());
        assertEquals("B", fill.getSide());
        assertEquals(1759345544745L, fill.getTime());
        assertEquals("0.02", fill.getStartPosition());
        assertEquals("Open Long", fill.getDir());
        assertEquals("0.0", fill.getClosedPnl());
        assertEquals("0x93c7ade4c0f737fe9541041a3f0188011700c5ca5bfa56d0379059377ffb11e9", fill.getHash());
        assertEquals(39979575327L, fill.getOrderId());
        assertTrue(fill.isTaker());
        assertEquals("0.527962", fill.getFee());
        assertEquals(393900969593217L, fill.getTradeId());
        assertEquals("USDC", fill.getFeeToken());
    }

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

    @Test
    void testParseMessage_isSnapshotWithDataWrapper_ShouldReadFromDataObject() {
        // This test demonstrates the bug: when JSON has a "data" wrapper,
        // isSnapshot should be read from the data object, not the root object
        String json = "{" + "\"channel\":\"userFills\"," + "\"data\":{" + "  \"isSnapshot\":true,"
                + "  \"user\":\"testuser\"," + "  \"fills\":[]" + "}" + "}";

        WsUserFillsWebSocketProcessor processor = new WsUserFillsWebSocketProcessor(null);
        WsUserFill userFill = processor.parseMessage(json);

        assertNotNull(userFill);
        assertEquals("testuser", userFill.getUser());
        // This assertion should pass but currently fails due to the bug
        assertTrue(userFill.isSnapshot(), "isSnapshot should be true when present in data object");
    }

    @Test
    void testParseMessage_isSnapshotWithoutDataWrapper_ShouldReadFromRootObject() {
        // This test shows the correct behavior when there's no data wrapper
        String json = "{" + "\"channel\":\"userFills\"," + "\"isSnapshot\":false," + "\"user\":\"testuser\","
                + "\"fills\":[]" + "}";

        WsUserFillsWebSocketProcessor processor = new WsUserFillsWebSocketProcessor(null);
        WsUserFill userFill = processor.parseMessage(json);

        assertNotNull(userFill);
        assertEquals("testuser", userFill.getUser());
        assertFalse(userFill.isSnapshot(), "isSnapshot should be false when set to false in root object");
    }
}
