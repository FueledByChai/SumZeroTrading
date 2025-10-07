package com.sumzerotrading.paradex.common.api;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.websocket.ProxyConfig;

/**
 * Centralized configuration management for Paradex API settings. Supports
 * loading configuration from properties files, system properties, environment
 * variables, or programmatic configuration.
 */
public class ParadexConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(ParadexConfiguration.class);

    private static volatile ParadexConfiguration instance;
    private static final Object lock = new Object();

    // Configuration keys
    public static final String PARADEX_TESTNET_REST_URL = "paradex.testnet.rest.url";
    public static final String PARADEX_TESTNET_WS_URL = "paradex.testnet.ws.url";
    public static final String PARADEX_MAINNET_REST_URL = "paradex.mainnet.rest.url";
    public static final String PARADEX_MAINNET_WS_URL = "paradex.mainnet.ws.url";
    public static final String PARADEX_ACCOUNT_ADDRESS = "paradex.account.address";
    public static final String PARADEX_PRIVATE_KEY = "paradex.private.key";
    public static final String PARADEX_ENVIRONMENT = "paradex.environment";
    public static final String PARADEX_CHAIN_ID = "paradex.chain.id";
    public static final String PARADEX_JWT_REFRESH_SECONDS = "paradex.jwt.refresh.seconds";
    public static final String PARADEX_KEYSTORE_PATH = "paradex.keystore.path";
    public static final String RUN_PROXY = "run.proxy";

    // Default values
    // private static final String DEFAULT_ENVIRONMENT = "testnet";
    private static final String DEFAULT_ENVIRONMENT = "prod";
    private static final String DEFAULT_TESTNET_REST_URL = "https://api.testnet.paradex.trade/v1";
    private static final String DEFAULT_TESTNET_WS_URL = "wss://ws.api.testnet.paradex.trade/v1";
    private static final String DEFAULT_PROD_REST_URL = "https://api.prod.paradex.trade/v1";
    private static final String DEFAULT_PROD_WS_URL = "wss://ws.api.prod.paradex.trade/v1";
    private static final String DEFAULT_TESTNET_CHAIN_ID = "7693264728749915528729180568779831130134670232771119425";
    private static final String DEFAULT_PROD_CHAIN_ID = "8458834024819506728615521019831122032732688838300957472069977523540";
    private static final int DEFAULT_JWT_REFRESH_SECONDS = 60;
    private static final boolean DEFAULT_RUN_PROXY = false;

    private final Properties properties;
    private final String environment;

    protected String wsUrl;
    protected String restUrl;

    private ParadexConfiguration() {
        this.properties = new Properties();
        this.environment = loadConfiguration();
    }

    /**
     * Get the singleton instance of ParadexConfiguration. Thread-safe lazy
     * initialization using double-checked locking.
     */
    public static ParadexConfiguration getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ParadexConfiguration();
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

        if (!properties.containsKey(PARADEX_PRIVATE_KEY)) {
            String privateKey = readPrivateKeyFromKeystore();
            properties.setProperty(PARADEX_PRIVATE_KEY, privateKey != null ? privateKey : "");
        }

        setProxySetting();

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
            logger.warn("External config file not found: {}", configFile);
        }

        // Try classpath resource
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("paradex.properties")) {
            if (is != null) {
                properties.load(is);
                logger.info("Loaded configuration from classpath: paradex.properties");
            }
        } catch (IOException e) {
            logger.warn("Could not load paradex.properties from classpath", e);
        }
    }

    private void loadFromEnvironmentVariables() {
        // Convert property keys to environment variable format
        setIfPresent(PARADEX_TESTNET_REST_URL, System.getenv("PARADEX_TESTNET_REST_URL"));
        setIfPresent(PARADEX_TESTNET_WS_URL, System.getenv("PARADEX_TESTNET_WS_URL"));
        setIfPresent(PARADEX_MAINNET_REST_URL, System.getenv("PARADEX_MAINNET_REST_URL"));
        setIfPresent(PARADEX_MAINNET_WS_URL, System.getenv("PARADEX_MAINNET_WS_URL"));
        setIfPresent(PARADEX_ACCOUNT_ADDRESS, System.getenv("PARADEX_ACCOUNT_ADDRESS"));
        setIfPresent(PARADEX_PRIVATE_KEY, System.getenv("PARADEX_PRIVATE_KEY"));
        setIfPresent(PARADEX_ENVIRONMENT, System.getenv("PARADEX_ENVIRONMENT"));
        setIfPresent(PARADEX_CHAIN_ID, System.getenv("PARADEX_CHAIN_ID"));
        setIfPresent(PARADEX_JWT_REFRESH_SECONDS, System.getenv("PARADEX_JWT_REFRESH_SECONDS"));
        setIfPresent(PARADEX_KEYSTORE_PATH, System.getenv("PARADEX_KEYSTORE_PATH"));
        setIfPresent(RUN_PROXY, System.getenv("PARADEX_RUN_PROXY"));
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

        if (isProduction) {
            restUrl = properties.getProperty(PARADEX_MAINNET_REST_URL, DEFAULT_PROD_REST_URL);
            wsUrl = properties.getProperty(PARADEX_MAINNET_WS_URL, DEFAULT_PROD_WS_URL);
        } else {
            restUrl = properties.getProperty(PARADEX_TESTNET_REST_URL, DEFAULT_TESTNET_REST_URL);
            wsUrl = properties.getProperty(PARADEX_TESTNET_WS_URL, DEFAULT_TESTNET_WS_URL);
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
        return restUrl;
    }

    public String getWebSocketUrl() {
        return wsUrl;
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

    public String getKeystorePath() {
        return properties.getProperty(PARADEX_KEYSTORE_PATH);
    }

    protected String readPrivateKeyFromKeystore() {
        String keystorePath = getKeystorePath();
        if (keystorePath != null && !keystorePath.trim().isEmpty()) {
            // the private key is the only thing in the file
            try (InputStream is = new FileInputStream(keystorePath)) {
                byte[] keyBytes = is.readAllBytes();
                String privateKey = new String(keyBytes).trim();
                if (!privateKey.isEmpty()) {
                    properties.setProperty(PARADEX_PRIVATE_KEY, privateKey);
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

    protected void setProxySetting() {
        String runProxyStr = properties.getProperty(RUN_PROXY);
        boolean runProxy = DEFAULT_RUN_PROXY;
        if (runProxyStr != null && !runProxyStr.trim().isEmpty()) {
            runProxy = Boolean.parseBoolean(runProxyStr);
        }
        ProxyConfig.getInstance().setRunningLocally(runProxy);
        logger.info("Proxy setting - runningLocally: {}", ProxyConfig.getInstance().isRunningLocally());
    }

    @Override
    public String toString() {
        return String.format("ParadexConfiguration{environment='%s', restUrl='%s', wsUrl='%s', hasPrivateKey=%s}",
                environment, getRestUrl(), getWebSocketUrl(), hasPrivateKeyConfiguration());
    }
}
