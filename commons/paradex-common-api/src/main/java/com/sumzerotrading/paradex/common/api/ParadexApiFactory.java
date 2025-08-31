package com.sumzerotrading.paradex.common.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton factory for ParadexRestApi instances with centralized configuration
 * management. Provides thread-safe access to configured API instances without
 * requiring users to know configuration details.
 */
public class ParadexApiFactory {
    private static final Logger logger = LoggerFactory.getLogger(ParadexApiFactory.class);

    private static volatile IParadexRestApi publicApiInstance;
    private static volatile IParadexRestApi privateApiInstance;
    private static final Object lock = new Object();

    private ParadexApiFactory() {
        // Prevent instantiation
    }

    /**
     * Get a public API instance that can only access public endpoints. Uses
     * centralized configuration for URL settings.
     * 
     * @return ParadexRestApi instance for public endpoints
     */
    public static IParadexRestApi getPublicApi() {
        if (publicApiInstance == null) {
            synchronized (lock) {
                if (publicApiInstance == null) {
                    ParadexConfiguration config = ParadexConfiguration.getInstance();
                    publicApiInstance = new ParadexRestApi(config.getRestUrl());
                    logger.info("Created public ParadexRestApi instance for URL: {}", config.getRestUrl());
                }
            }
        }
        return publicApiInstance;
    }

    /**
     * Get a private API instance that can access both public and private endpoints.
     * Uses centralized configuration for URL, account address, and private key.
     * 
     * @return ParadexRestApi instance for private endpoints
     * @throws IllegalStateException if private key configuration is not available
     */
    public static IParadexRestApi getPrivateApi() {
        if (privateApiInstance == null) {
            synchronized (lock) {
                if (privateApiInstance == null) {
                    ParadexConfiguration config = ParadexConfiguration.getInstance();

                    if (!config.hasPrivateKeyConfiguration()) {
                        throw new IllegalStateException("Private key configuration not available. Please set "
                                + ParadexConfiguration.PARADEX_ACCOUNT_ADDRESS + " and "
                                + ParadexConfiguration.PARADEX_PRIVATE_KEY + " properties.");
                    }

                    privateApiInstance = new ParadexRestApi(config.getRestUrl(), config.getAccountAddress(),
                            config.getPrivateKey());
                    logger.info("Created private ParadexRestApi instance for URL: {} and address: {}",
                            config.getRestUrl(), config.getAccountAddress());
                }
            }
        }
        return privateApiInstance;
    }

    /**
     * Get the appropriate API instance based on available configuration. Returns
     * private API if credentials are configured, otherwise returns public API.
     * 
     * @return ParadexRestApi instance (private if configured, otherwise public)
     */
    public static IParadexRestApi getApi() {
        ParadexConfiguration config = ParadexConfiguration.getInstance();
        if (config.hasPrivateKeyConfiguration()) {
            return getPrivateApi();
        } else {
            logger.info("Private key not configured, returning public API instance");
            return getPublicApi();
        }
    }

    /**
     * Check if private API configuration is available.
     * 
     * @return true if private key and account address are configured
     */
    public static boolean isPrivateApiAvailable() {
        return ParadexConfiguration.getInstance().hasPrivateKeyConfiguration();
    }

    /**
     * Get the WebSocket URL from configuration.
     * 
     * @return WebSocket URL for the current environment
     */
    public static String getWebSocketUrl() {
        return ParadexConfiguration.getInstance().getWebSocketUrl();
    }

    /**
     * Get the current environment (testnet/production).
     * 
     * @return environment name
     */
    public static String getEnvironment() {
        return ParadexConfiguration.getInstance().getEnvironment();
    }

    /**
     * Reset all cached instances (useful for testing or configuration changes).
     * Note: This will force recreation of instances on next access.
     */
    public static void reset() {
        synchronized (lock) {
            publicApiInstance = null;
            privateApiInstance = null;
            ParadexConfiguration.reset();
            logger.info("Reset all ParadexApi instances and configuration");
        }
    }

    /**
     * Get configuration details for debugging.
     * 
     * @return configuration string (without sensitive information)
     */
    public static String getConfigurationInfo() {
        ParadexConfiguration config = ParadexConfiguration.getInstance();
        return String.format("Environment: %s, REST URL: %s, WebSocket URL: %s, Private API Available: %s",
                config.getEnvironment(), config.getRestUrl(), config.getWebSocketUrl(),
                config.hasPrivateKeyConfiguration());
    }
}
