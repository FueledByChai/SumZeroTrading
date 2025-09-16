package com.sumzerotrading.hyperliquid.websocket;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized configuration management for Paradex API settings. Supports
 * loading configuration from properties files, system properties, environment
 * variables, or programmatic configuration.
 */
public class HyperliquidConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(HyperliquidConfiguration.class);

    private static volatile HyperliquidConfiguration instance;
    private static final Object lock = new Object();

    // Configuration keys
    public static final String PARADEX_REST_URL = "paradex.rest.url";
    public static final String PARADEX_WS_URL = "paradex.ws.url";
    public static final String PARADEX_ACCOUNT_ADDRESS = "paradex.account.address";
    public static final String PARADEX_PRIVATE_KEY = "paradex.private.key";
    public static final String PARADEX_ENVIRONMENT = "paradex.environment";
    public static final String PARADEX_CHAIN_ID = "paradex.chain.id";
    public static final String PARADEX_JWT_REFRESH_SECONDS = "paradex.jwt.refresh.seconds";

    // Default values
    // private static final String DEFAULT_ENVIRONMENT = "testnet";
    private static final String DEFAULT_ENVIRONMENT = "prod";
    private static final String DEFAULT_TESTNET_REST_URL = "https://api.testnet.paradex.trade/v1";
    private static final String DEFAULT_TESTNET_WS_URL = "wss://ws.testnet.paradex.trade/v1";
    private static final String DEFAULT_PROD_REST_URL = "https://api.prod.paradex.trade/v1";
    private static final String DEFAULT_PROD_WS_URL = "wss://ws.prod.paradex.trade/v1";
    private static final String DEFAULT_TESTNET_CHAIN_ID = "7693264728749915528729180568779831130134670232771119425";
    private static final String DEFAULT_PROD_CHAIN_ID = "8458834024819506728615521019831122032732688838300957472069977523540";
    private static final int DEFAULT_JWT_REFRESH_SECONDS = 60;

    private final Properties properties;
    private final String environment;

    private HyperliquidConfiguration() {
        this.properties = new Properties();
        this.environment = loadConfiguration();
    }

    /**
     * Get the singleton instance of ParadexConfiguration. Thread-safe lazy
     * initialization using double-checked locking.
     */
    public static HyperliquidConfiguration getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new HyperliquidConfiguration();
                }
            }
        }
        return instance;
    }

    /**
     * Reset the configuration instance (useful for testing).
     */
    public static void reset() {
        synchronized (lock) {
            instance = null;
        }
    }

    /**
     * Load configuration from multiple sources in order of precedence: 1. System
     * properties 2. Environment variables 3. Properties file (paradex.properties)
     * 4. Classpath resource (paradex.properties) 5. Default values
     */
    private String loadConfiguration() {
        // Try to load from properties file first
        loadFromPropertiesFile();

        // Override with environment variables
        loadFromEnvironmentVariables();

        // Override with system properties (highest precedence)
        loadFromSystemProperties();

        // Determine environment and set defaults
        String env = properties.getProperty(PARADEX_ENVIRONMENT, DEFAULT_ENVIRONMENT);
        setEnvironmentDefaults(env);

        logger.info("Paradex configuration loaded for environment: {}", env);
        return env;
    }

    private void loadFromPropertiesFile() {
        // Try external properties file first
        String configFile = System.getProperty("paradex.config.file", "paradex.properties");
        try (InputStream is = new FileInputStream(configFile)) {
            properties.load(is);
            logger.info("Loaded configuration from file: {}", configFile);
            return;
        } catch (IOException e) {
            logger.debug("External config file not found: {}", configFile);
        }

        // Try classpath resource
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("paradex.properties")) {
            if (is != null) {
                properties.load(is);
                logger.info("Loaded configuration from classpath: paradex.properties");
            }
        } catch (IOException e) {
            logger.debug("Could not load paradex.properties from classpath", e);
        }
    }

    private void loadFromEnvironmentVariables() {
        // Convert property keys to environment variable format
        setIfPresent(PARADEX_REST_URL, System.getenv("PARADEX_REST_URL"));
        setIfPresent(PARADEX_WS_URL, System.getenv("PARADEX_WS_URL"));
        setIfPresent(PARADEX_ACCOUNT_ADDRESS, System.getenv("PARADEX_ACCOUNT_ADDRESS"));
        setIfPresent(PARADEX_PRIVATE_KEY, System.getenv("PARADEX_PRIVATE_KEY"));
        setIfPresent(PARADEX_ENVIRONMENT, System.getenv("PARADEX_ENVIRONMENT"));
        setIfPresent(PARADEX_CHAIN_ID, System.getenv("PARADEX_CHAIN_ID"));
        setIfPresent(PARADEX_JWT_REFRESH_SECONDS, System.getenv("PARADEX_JWT_REFRESH_SECONDS"));
    }

    private void loadFromSystemProperties() {
        for (String key : System.getProperties().stringPropertyNames()) {
            if (key.startsWith("paradex.")) {
                properties.setProperty(key, System.getProperty(key));
            }
        }
    }

    private void setIfPresent(String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            properties.setProperty(key, value);
        }
    }

    private void setEnvironmentDefaults(String env) {
        boolean isProduction = "prod".equalsIgnoreCase(env) || "production".equalsIgnoreCase(env);

        // Set default URLs if not specified
        if (!properties.containsKey(PARADEX_REST_URL)) {
            properties.setProperty(PARADEX_REST_URL, isProduction ? DEFAULT_PROD_REST_URL : DEFAULT_TESTNET_REST_URL);
        }

        if (!properties.containsKey(PARADEX_WS_URL)) {
            properties.setProperty(PARADEX_WS_URL, isProduction ? DEFAULT_PROD_WS_URL : DEFAULT_TESTNET_WS_URL);
        }

        if (!properties.containsKey(PARADEX_CHAIN_ID)) {
            properties.setProperty(PARADEX_CHAIN_ID, isProduction ? DEFAULT_PROD_CHAIN_ID : DEFAULT_TESTNET_CHAIN_ID);
        }

        if (!properties.containsKey(PARADEX_JWT_REFRESH_SECONDS)) {
            properties.setProperty(PARADEX_JWT_REFRESH_SECONDS, String.valueOf(DEFAULT_JWT_REFRESH_SECONDS));
        }
    }

    // Getter methods
    public String getRestUrl() {
        return properties.getProperty(PARADEX_REST_URL);
    }

    public String getWebSocketUrl() {
        return properties.getProperty(PARADEX_WS_URL);
    }

    public String getAccountAddress() {
        return properties.getProperty(PARADEX_ACCOUNT_ADDRESS);
    }

    public String getPrivateKey() {
        return properties.getProperty(PARADEX_PRIVATE_KEY);
    }

    public String getEnvironment() {
        return environment;
    }

    public String getChainId() {
        return properties.getProperty(PARADEX_CHAIN_ID);
    }

    public int getJwtRefreshSeconds() {
        return Integer.parseInt(
                properties.getProperty(PARADEX_JWT_REFRESH_SECONDS, String.valueOf(DEFAULT_JWT_REFRESH_SECONDS)));
    }

    public boolean isProductionEnvironment() {
        return "prod".equalsIgnoreCase(environment) || "production".equalsIgnoreCase(environment);
    }

    public boolean hasPrivateKeyConfiguration() {
        return getAccountAddress() != null && getPrivateKey() != null && !getAccountAddress().trim().isEmpty()
                && !getPrivateKey().trim().isEmpty();
    }

    /**
     * Get a custom property value.
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Get a custom property value with default.
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Set a property programmatically (useful for testing or dynamic
     * configuration).
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Get all configuration properties (for debugging).
     */
    public Properties getAllProperties() {
        return new Properties(properties);
    }

    @Override
    public String toString() {
        return String.format("ParadexConfiguration{environment='%s', restUrl='%s', wsUrl='%s', hasPrivateKey=%s}",
                environment, getRestUrl(), getWebSocketUrl(), hasPrivateKeyConfiguration());
    }
}
