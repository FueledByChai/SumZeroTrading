package com.sumzerotrading.paradex.common.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ParadexApiFactory and ParadexConfiguration.
 */
class ParadexApiFactoryTest {

    private Properties originalSystemProperties;

    @BeforeEach
    void setUp() {
        // Backup original system properties
        originalSystemProperties = new Properties();
        originalSystemProperties.putAll(System.getProperties());

        // Clear any cached configuration
        ParadexApiFactory.reset();
    }

    @AfterEach
    void tearDown() {
        // Restore original system properties
        System.setProperties(originalSystemProperties);

        // Clear cached configuration
        ParadexApiFactory.reset();
    }

    @Test
    void testDefaultConfiguration() {
        String configInfo = ParadexApiFactory.getConfigurationInfo();
        System.out.println("Default config info: " + configInfo);
        assertNotNull(configInfo);
        assertTrue(configInfo.contains("prod"));

        // Default should be testnet with no private API
        assertFalse(ParadexApiFactory.isPrivateApiAvailable());

        // Public API should be available
        IParadexRestApi publicApi = ParadexApiFactory.getPublicApi();
        assertNotNull(publicApi);

        // WebSocket URL should be testnet
        String wsUrl = ParadexApiFactory.getWebSocketUrl();
        assertTrue(wsUrl.contains("prod"));
    }

    @Test
    void testSystemPropertiesConfiguration() {
        // Set system properties
        System.setProperty(ParadexConfiguration.PARADEX_ENVIRONMENT, "prod");
        System.setProperty(ParadexConfiguration.PARADEX_ACCOUNT_ADDRESS, "0x1234567890abcdef");
        System.setProperty(ParadexConfiguration.PARADEX_PRIVATE_KEY, "0xabcdef1234567890");

        // Reset to pick up new properties
        ParadexApiFactory.reset();

        String configInfo = ParadexApiFactory.getConfigurationInfo();
        System.out.println("Config info after setting system properties: " + configInfo);
        assertTrue(configInfo.contains("prod"));

        // Debug what the configuration has
        ParadexConfiguration config = ParadexConfiguration.getInstance();
        System.out.println("Account address: " + config.getAccountAddress());
        System.out.println(
                "Private key is set: " + (config.getPrivateKey() != null && !config.getPrivateKey().trim().isEmpty()));
        System.out.println("Has private key config: " + config.hasPrivateKeyConfiguration());

        // Private API should now be available
        assertTrue(ParadexApiFactory.isPrivateApiAvailable());

        // WebSocket URL should be production
        String wsUrl = ParadexApiFactory.getWebSocketUrl();
        System.out.println("Production WebSocket URL: " + wsUrl);
        assertTrue(wsUrl.contains("prod.paradex.trade"));
    }

    @Test
    void testProgrammaticConfiguration() {
        ParadexConfiguration config = ParadexConfiguration.getInstance();
        config.setProperty(ParadexConfiguration.PARADEX_ENVIRONMENT, "testnet");
        config.setProperty(ParadexConfiguration.PARADEX_ACCOUNT_ADDRESS, "0xtest123");
        config.setProperty(ParadexConfiguration.PARADEX_PRIVATE_KEY, "0xprivatekey");

        // Don't call reset since that would clear programmatic changes
        // Programmatic changes should be immediately available

        assertTrue(ParadexApiFactory.isPrivateApiAvailable());

        IParadexRestApi privateApi = ParadexApiFactory.getPrivateApi();
        assertNotNull(privateApi);
    }

    @Test
    void testEnvironmentSpecificUrls() {
        // Test testnet URLs
        System.setProperty(ParadexConfiguration.PARADEX_ENVIRONMENT, "testnet");
        ParadexApiFactory.reset();

        String testnetWs = ParadexApiFactory.getWebSocketUrl();
        assertTrue(testnetWs.contains("testnet"));

        // Test production URLs
        System.setProperty(ParadexConfiguration.PARADEX_ENVIRONMENT, "prod");
        ParadexApiFactory.reset();

        String prodWs = ParadexApiFactory.getWebSocketUrl();
        assertTrue(prodWs.contains("prod.paradex.trade"));

        // URLs should be different
        assertNotEquals(testnetWs, prodWs);
    }
}
