package com.sumzerotrading.hyperliquid.ws;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.data.SumZeroException;

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
    public static final String HYPERLIQUID_REST_URL = "hyperliquid.rest.url";
    public static final String HYPERLIQUID_WS_URL = "hyperliquid.ws.url";
    public static final String HYPERLIQUID_ACCOUNT_ADDRESS = "hyperliquid.account.address";
    public static final String HYPERLIQUID_PRIVATE_KEY = "hyperliquid.private.key";
    public static final String HYPERLIQUID_ENVIRONMENT = "hyperliquid.environment";
    public static final String HYPERLIQUID_KEYSTORE_PATH = "hyperliquid.keystore.path";

    // Default values
    // private static final String DEFAULT_ENVIRONMENT = "testnet";
    private static final String DEFAULT_ENVIRONMENT = "prod";
    private static final String DEFAULT_TESTNET_REST_URL = "https://api.hyperliquid-testnet.xyz";
    private static final String DEFAULT_TESTNET_WS_URL = "wss://api.hyperliquid-testnet.xyz/ws";
    private static final String DEFAULT_PROD_REST_URL = "https://api.hyperliquid.xyz";
    private static final String DEFAULT_PROD_WS_URL = "wss://api.hyperliquid.xyz/ws";

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
        String env = properties.getProperty(HYPERLIQUID_ENVIRONMENT, DEFAULT_ENVIRONMENT);
        setEnvironmentDefaults(env);

        logger.info("Hyperliquid configuration loaded for environment: {}", env);

        // read the private key from the keystore path if specified
        String privateKey = readPrivateKeyFromKeystore();
        properties.setProperty(HYPERLIQUID_PRIVATE_KEY, privateKey);

        return env;
    }

    private void loadFromPropertiesFile() {
        // Try external properties file first
        String configFile = System.getProperty("hyperliquid.config.file", "hyperliquid.properties");
        try (InputStream is = new FileInputStream(configFile)) {
            properties.load(is);
            logger.info("Loaded configuration from file: {}", configFile);
            return;
        } catch (IOException e) {
            logger.debug("External config file not found: {}", configFile);
        }

        // Try classpath resource
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("hyperliquid.properties")) {
            if (is != null) {
                properties.load(is);
                logger.info("Loaded configuration from classpath: hyperliquid.properties");
            }
        } catch (IOException e) {
            logger.debug("Could not load hyperliquid.properties from classpath", e);
        }
    }

    private void loadFromEnvironmentVariables() {
        // Convert property keys to environment variable format
        setIfPresent(HYPERLIQUID_REST_URL, System.getenv("HYPERLIQUID_REST_URL"));
        setIfPresent(HYPERLIQUID_WS_URL, System.getenv("HYPERLIQUID_WS_URL"));
        setIfPresent(HYPERLIQUID_ACCOUNT_ADDRESS, System.getenv("HYPERLIQUID_ACCOUNT_ADDRESS"));
        setIfPresent(HYPERLIQUID_ENVIRONMENT, System.getenv("HYPERLIQUID_ENVIRONMENT"));
        setIfPresent(HYPERLIQUID_KEYSTORE_PATH, System.getenv("HYPERLIQUID_KEYSTORE_PATH"));
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
        if (!properties.containsKey(HYPERLIQUID_REST_URL)) {
            properties.setProperty(HYPERLIQUID_REST_URL,
                    isProduction ? DEFAULT_PROD_REST_URL : DEFAULT_TESTNET_REST_URL);
        }

        if (!properties.containsKey(HYPERLIQUID_WS_URL)) {
            properties.setProperty(HYPERLIQUID_WS_URL, isProduction ? DEFAULT_PROD_WS_URL : DEFAULT_TESTNET_WS_URL);
        }

    }

    // Getter methods
    public String getRestUrl() {
        return properties.getProperty(HYPERLIQUID_REST_URL);
    }

    public String getWebSocketUrl() {
        return properties.getProperty(HYPERLIQUID_WS_URL);
    }

    public String getAccountAddress() {
        return properties.getProperty(HYPERLIQUID_ACCOUNT_ADDRESS);
    }

    public String getPrivateKey() {
        return properties.getProperty(HYPERLIQUID_PRIVATE_KEY);
    }

    public String getEnvironment() {
        return environment;
    }

    public String getKeystorePath() {
        return properties.getProperty(HYPERLIQUID_KEYSTORE_PATH);
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

    protected String readPrivateKeyFromKeystore() {
        String keystorePath = getKeystorePath();
        if (keystorePath != null && !keystorePath.trim().isEmpty()) {
            // the private key is the only thing in the file
            try (InputStream is = new FileInputStream(keystorePath)) {
                byte[] keyBytes = is.readAllBytes();
                String privateKey = new String(keyBytes).trim();
                if (!privateKey.isEmpty()) {
                    properties.setProperty(HYPERLIQUID_PRIVATE_KEY, privateKey);
                    logger.info("Loaded private key from keystore path: {}", keystorePath);
                } else {
                    logger.warn("Keystore file is empty: {}", keystorePath);
                }
                return privateKey;
            } catch (IOException e) {
                logger.error("Error reading keystore file: {}", keystorePath, e);
                throw new SumZeroException("Error reading keystore file: " + keystorePath, e);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("ParadexConfiguration{environment='%s', restUrl='%s', wsUrl='%s', hasPrivateKey=%s}",
                environment, getRestUrl(), getWebSocketUrl(), hasPrivateKeyConfiguration());
    }
}
