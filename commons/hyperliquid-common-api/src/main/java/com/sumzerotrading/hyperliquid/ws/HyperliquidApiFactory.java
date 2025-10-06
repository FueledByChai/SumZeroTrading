package com.sumzerotrading.hyperliquid.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton factory for ParadexRestApi instances with centralized configuration
 * management. Provides thread-safe access to configured API instances without
 * requiring users to know configuration details.
 */
public class HyperliquidApiFactory {
    private static final Logger logger = LoggerFactory.getLogger(HyperliquidApiFactory.class);

    private static volatile IHyperliquidRestApi restApiInstance;
    private static volatile IHyperliquidWebsocketApi websocketApiInstance;
    private static final Object lock = new Object();

    private HyperliquidApiFactory() {
        // Prevent instantiation
    }

    /**
     * Get a public API instance that can only access public endpoints. Uses
     * centralized configuration for URL settings.
     * 
     * @return ParadexRestApi instance for public endpoints
     */
    public static IHyperliquidRestApi getRestApi() {
        if (restApiInstance == null) {
            synchronized (lock) {
                if (restApiInstance == null) {
                    HyperliquidConfiguration config = HyperliquidConfiguration.getInstance();
                    restApiInstance = new HyperliquidRestApi(config.getRestUrl());
                    logger.info("Created public ParadexRestApi instance for URL: {}", config.getRestUrl());
                }
            }
        }
        return restApiInstance;
    }

    public static IHyperliquidWebsocketApi getWebsocketApi() {
        if (websocketApiInstance == null) {
            synchronized (lock) {
                if (websocketApiInstance == null) {
                    HyperliquidConfiguration config = HyperliquidConfiguration.getInstance();
                    websocketApiInstance = new HyperliquidWebsocketApi();
                    logger.info("Created HyperliquidWebsocketApi instance for URL: {}", config.getWebSocketUrl());
                }
            }
        }
        return websocketApiInstance;
    }

    /**
     * Reset all cached instances (useful for testing or configuration changes).
     * Note: This will force recreation of instances on next access.
     */
    public static void reset() {
        synchronized (lock) {
            restApiInstance = null;
            websocketApiInstance = null;
            HyperliquidConfiguration.reset();
            logger.info("Reset all ParadexApi instances and configuration");
        }
    }

    /**
     * Get configuration details for debugging.
     * 
     * @return configuration string (without sensitive information)
     */
    public static String getConfigurationInfo() {
        HyperliquidConfiguration config = HyperliquidConfiguration.getInstance();
        return String.format("Environment: %s, REST URL: %s, WebSocket URL: %s, Private API Available: %s",
                config.getEnvironment(), config.getRestUrl(), config.getWebSocketUrl(),
                config.hasPrivateKeyConfiguration());
    }
}
