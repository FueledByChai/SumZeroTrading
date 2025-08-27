package com.sumzerotrading.paradex.common.api.example;

import com.sumzerotrading.paradex.common.api.ParadexApiFactory;
import com.sumzerotrading.paradex.common.api.ParadexConfiguration;
import com.sumzerotrading.paradex.common.api.ParadexRestApi;

/**
 * Example demonstrating how to use the centralized Paradex API configuration.
 */
public class ParadexApiExample {

    public static void main(String[] args) {
        // Example 1: Using configuration from properties/environment
        demonstrateConfiguratedUsage();

        // Example 2: Programmatic configuration
        demonstrateProgrammaticConfiguration();

        // Example 3: Using with ParadexBroker
        demonstrateBrokerUsage();
    }

    /**
     * Demonstrate usage with configuration loaded from properties/environment.
     */
    private static void demonstrateConfiguratedUsage() {
        System.out.println("=== Configured Usage Example ===");

        // Configuration is automatically loaded from:
        // 1. System properties (-Dparadex.environment=testnet)
        // 2. Environment variables (PARADEX_ENVIRONMENT=testnet)
        // 3. Properties file (paradex.properties)
        // 4. Default values

        // Get configuration info
        System.out.println("Configuration: " + ParadexApiFactory.getConfigurationInfo());

        // Check if private API is available
        if (ParadexApiFactory.isPrivateApiAvailable()) {
            System.out.println("Private API credentials configured");
            @SuppressWarnings("unused")
            ParadexRestApi privateApi = ParadexApiFactory.getPrivateApi();
            // Use private API...
        } else {
            System.out.println("Private API credentials not configured, using public API");
            @SuppressWarnings("unused")
            ParadexRestApi publicApi = ParadexApiFactory.getPublicApi();
            // Use public API...
        }

        // Get WebSocket URL for this environment
        String wsUrl = ParadexApiFactory.getWebSocketUrl();
        System.out.println("WebSocket URL: " + wsUrl);
    }

    /**
     * Demonstrate programmatic configuration (useful for testing or dynamic
     * config).
     */
    private static void demonstrateProgrammaticConfiguration() {
        System.out.println("\n=== Programmatic Configuration Example ===");

        // Reset to clear any cached instances
        ParadexApiFactory.reset();

        // Set configuration programmatically
        ParadexConfiguration config = ParadexConfiguration.getInstance();
        config.setProperty(ParadexConfiguration.PARADEX_ENVIRONMENT, "testnet");
        config.setProperty(ParadexConfiguration.PARADEX_ACCOUNT_ADDRESS, "0x1234...");
        config.setProperty(ParadexConfiguration.PARADEX_PRIVATE_KEY, "0xabcd...");

        System.out.println("Updated configuration: " + ParadexApiFactory.getConfigurationInfo());

        // Now private API should be available
        if (ParadexApiFactory.isPrivateApiAvailable()) {
            System.out.println("Private API now available with programmatic config");
        }
    }

    /**
     * Demonstrate usage with ParadexBroker. Note: This example requires the
     * ParadexBroker implementation module.
     */
    private static void demonstrateBrokerUsage() {
        System.out.println("\n=== Broker Usage Example ===");

        try {
            // ParadexBroker now automatically uses centralized configuration
            // ParadexBroker broker = new ParadexBroker();

            // The broker is automatically configured with:
            // - REST API URL (based on environment)
            // - WebSocket URL (based on environment)
            // - Account credentials (if configured)
            // - JWT refresh interval

            System.out.println("ParadexBroker would be created successfully with centralized config");

            // Connect and start trading
            // broker.connect();
            // ... use broker for trading ...
            // broker.disconnect();

        } catch (IllegalStateException e) {
            System.out.println("Failed to create broker: " + e.getMessage());
            System.out.println("Make sure to configure account credentials for private API access");
        }
    }
}

/*
 * Configuration Examples:
 * 
 * 1. Using System Properties: java -Dparadex.environment=prod \
 * -Dparadex.account.address=0x1234... \ -Dparadex.private.key=0xabcd... \
 * YourApplication
 * 
 * 2. Using Environment Variables: export PARADEX_ENVIRONMENT=prod export
 * PARADEX_ACCOUNT_ADDRESS=0x1234... export PARADEX_PRIVATE_KEY=0xabcd... java
 * YourApplication
 * 
 * 3. Using Properties File (paradex.properties): paradex.environment=prod
 * paradex.account.address=0x1234... paradex.private.key=0xabcd...
 * 
 * 4. Using External Properties File: java
 * -Dparadex.config.file=/path/to/config.properties YourApplication
 */
