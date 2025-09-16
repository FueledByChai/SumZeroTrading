package com.sumzerotrading.hyperliquid.websocket;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.sumzerotrading.data.Exchange;
import com.sumzerotrading.data.InstrumentDescriptor;
import com.sumzerotrading.data.InstrumentType;

public class HyperliquidUniverseParsingTest {

    @Test
    public void testParseInstrumentDescriptors() {
        // Sample Hyperliquid universe JSON format
        String universeJson = "{" + "\"universe\": [" + "  {" + "    \"name\": \"BTC\"," + "    \"szDecimals\": 5,"
                + "    \"maxLeverage\": 50," + "    \"onlyIsolated\": false," + "    \"marginTableId\": null" + "  },"
                + "  {" + "    \"name\": \"ETH\"," + "    \"szDecimals\": 4," + "    \"maxLeverage\": 30,"
                + "    \"onlyIsolated\": false," + "    \"marginTableId\": null" + "  }," + "  {"
                + "    \"name\": \"DELIST-COIN\"," + "    \"szDecimals\": 3," + "    \"maxLeverage\": 10,"
                + "    \"onlyIsolated\": true," + "    \"marginTableId\": 1" + "  }" + "]" + "}";

        HyperliquidRestApi api = new HyperliquidRestApi("https://api.hyperliquid.xyz");
        InstrumentDescriptor[] descriptorsArray = api.parseInstrumentDescriptors(InstrumentType.PERPETUAL_FUTURES,
                universeJson);
        List<InstrumentDescriptor> descriptors = Arrays.asList(descriptorsArray);

        // Should filter out delisted instruments (those with marginTableId != null)
        assertEquals(2, descriptors.size());

        // Test BTC descriptor
        InstrumentDescriptor btc = descriptors.stream().filter(d -> "BTC".equals(d.getCommonSymbol())).findFirst()
                .orElse(null);

        assertNotNull(btc);
        assertEquals("BTC", btc.getCommonSymbol());
        assertEquals(Exchange.HYPERLIQUID, btc.getExchange());
        assertEquals(InstrumentType.PERPETUAL_FUTURES, btc.getInstrumentType());
        assertEquals(new BigDecimal("0.00001"), btc.getOrderSizeIncrement()); // 10^-5

        // Test ETH descriptor
        InstrumentDescriptor eth = descriptors.stream().filter(d -> "ETH".equals(d.getCommonSymbol())).findFirst()
                .orElse(null);

        assertNotNull(eth);
        assertEquals("ETH", eth.getCommonSymbol());
        assertEquals(Exchange.HYPERLIQUID, eth.getExchange());
        assertEquals(InstrumentType.PERPETUAL_FUTURES, eth.getInstrumentType());
        assertEquals(new BigDecimal("0.0001"), eth.getOrderSizeIncrement()); // 10^-4

        // Verify delisted instrument is filtered out
        boolean hasDelistedCoin = descriptors.stream().anyMatch(d -> "DELIST-COIN".equals(d.getCommonSymbol()));
        assertFalse("Delisted instruments should be filtered out", hasDelistedCoin);
    }

    @Test
    public void testParseInstrumentDescriptorsEmptyUniverse() {
        String emptyJson = "{\"universe\": []}";

        HyperliquidRestApi api = new HyperliquidRestApi("https://api.hyperliquid.xyz");
        InstrumentDescriptor[] descriptors = api.parseInstrumentDescriptors(InstrumentType.PERPETUAL_FUTURES,
                emptyJson);

        assertNotNull(descriptors);
        assertEquals(0, descriptors.length);
    }

    @Test
    public void testParseInstrumentDescriptorsWithNullValues() {
        String jsonWithNulls = "{" + "\"universe\": [" + "  {" + "    \"name\": \"SOL\"," + "    \"szDecimals\": 3,"
                + "    \"maxLeverage\": 20," + "    \"onlyIsolated\": false," + "    \"marginTableId\": null" + "  }"
                + "]" + "}";

        HyperliquidRestApi api = new HyperliquidRestApi("https://api.hyperliquid.xyz");
        InstrumentDescriptor[] descriptorsArray = api.parseInstrumentDescriptors(InstrumentType.PERPETUAL_FUTURES,
                jsonWithNulls);

        assertEquals(1, descriptorsArray.length);
        InstrumentDescriptor sol = descriptorsArray[0];
        assertEquals("SOL", sol.getCommonSymbol());
        assertEquals(new BigDecimal("0.001"), sol.getOrderSizeIncrement()); // 10^-3
    }
}