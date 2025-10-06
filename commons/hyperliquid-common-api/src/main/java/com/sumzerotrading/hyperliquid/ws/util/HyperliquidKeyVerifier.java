package com.sumzerotrading.hyperliquid.ws.util;

import com.sumzerotrading.hyperliquid.ws.HyperliquidConfiguration;
import com.sumzerotrading.hyperliquid.ws.json.HLSigner;

/**
 * Utility class to verify Hyperliquid private key configuration. This helps
 * ensure your private key matches your expected account address.
 */
public class HyperliquidKeyVerifier {

    /**
     * Verify if the configured private key derives to the expected account address.
     * 
     * @param expectedAddress The address you expect (e.g.,
     *                        "0x3031d55bf17e6512b7ee35130955ab7bc3cf08d5")
     * @return true if the private key derives to the expected address, false
     *         otherwise
     */
    public static boolean verifyPrivateKey(String expectedAddress) {
        try {
            HyperliquidConfiguration config = HyperliquidConfiguration.getInstance();
            String privateKey = config.getPrivateKey();
            String configuredAddress = config.getAccountAddress();

            if (privateKey == null || privateKey.trim().isEmpty()) {
                System.err.println("ERROR: No private key configured!");
                System.err.println("Configure it via:");
                System.err.println("  - System property: -Dhyperliquid.private.key=0x...");
                System.err.println("  - Environment variable: HYPERLIQUID_PRIVATE_KEY=0x...");
                System.err.println("  - Properties file: hyperliquid.private.key=0x...");
                System.err.println("  - Keystore file: hyperliquid.keystore.path=/path/to/keyfile");
                return false;
            }

            HLSigner signer = new HLSigner(privateKey);
            String derivedAddress = ""; // signer.getAddress();

            System.out.println("=== Hyperliquid Private Key Verification ===");
            System.out.println("Configured account address: " + configuredAddress);
            System.out.println("Configured sub-account:     " + config.getSubAccountAddress());
            System.out.println("Trading account (used):     " + config.getTradingAccount());
            System.out.println("Expected address:           " + expectedAddress);
            System.out.println("Derived from private key:   " + derivedAddress);
            System.out.println("Environment:                " + config.getEnvironment());

            // Normalize addresses for comparison (remove 0x prefix and make lowercase)
            String normalizedExpected = normalizeAddress(expectedAddress);
            String normalizedDerived = normalizeAddress(derivedAddress);
            String normalizedConfigured = normalizeAddress(configuredAddress);

            boolean keyMatchesExpected = normalizedDerived.equals(normalizedExpected);
            boolean configMatchesDerived = normalizedConfigured.equals(normalizedDerived);
            String normalizedTradingAccount = normalizeAddress(config.getTradingAccount());
            boolean keyMatchesTradingAccount = normalizedDerived.equals(normalizedTradingAccount);

            System.out.println();
            System.out.println("=== Verification Results ===");
            System.out.println("Private key → Expected address:   " + (keyMatchesExpected ? "✓ MATCH" : "✗ MISMATCH"));
            System.out.println(
                    "Private key → Trading account:    " + (keyMatchesTradingAccount ? "✓ MATCH" : "✗ MISMATCH"));
            System.out
                    .println("Config address → Derived:         " + (configMatchesDerived ? "✓ MATCH" : "✗ MISMATCH"));

            if (!keyMatchesExpected) {
                System.err.println();
                System.err.println("ERROR: Your private key does NOT derive to the expected address!");
                System.err.println("This means you're using the wrong private key for account: " + expectedAddress);
            }

            if (!keyMatchesTradingAccount) {
                System.err.println();
                System.err.println("CRITICAL ERROR: Your private key does NOT match the trading account being used!");
                System.err.println("Trading account: " + config.getTradingAccount());
                System.err.println("Derived address: " + derivedAddress);
                if (config.getSubAccountAddress() != null && !config.getSubAccountAddress().trim().isEmpty()) {
                    System.err.println("You have a sub-account configured. Either:");
                    System.err.println("1. Remove hyperliquid.sub.address configuration, OR");
                    System.err.println("2. Use the private key for: " + config.getSubAccountAddress());
                }
            }

            if (!configMatchesDerived) {
                System.err.println();
                System.err.println("WARNING: Your configured account address doesn't match the derived address!");
                System.err.println("Update your configuration to use: " + derivedAddress);
            }

            return keyMatchesExpected && keyMatchesTradingAccount;

        } catch (Exception e) {
            System.err.println("ERROR verifying private key: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Print current configuration for debugging
     */
    public static void printCurrentConfig() {
        try {
            HyperliquidConfiguration config = HyperliquidConfiguration.getInstance();

            System.out.println("=== Current Hyperliquid Configuration ===");
            System.out.println("Environment:     " + config.getEnvironment());
            System.out.println("REST URL:        " + config.getRestUrl());
            System.out.println("WebSocket URL:   " + config.getWebSocketUrl());
            System.out.println("Account Address: " + config.getAccountAddress());
            System.out.println(
                    "Has Private Key: " + (config.getPrivateKey() != null && !config.getPrivateKey().trim().isEmpty()));
            System.out.println("Keystore Path:   " + config.getKeystorePath());

            if (config.getPrivateKey() != null && !config.getPrivateKey().trim().isEmpty()) {
                HLSigner signer = new HLSigner(config.getPrivateKey());
                System.out.println("Derived Address: " + "");// signer.getAddress());
            }
        } catch (Exception e) {
            System.err.println("ERROR reading configuration: " + e.getMessage());
        }
    }

    private static String normalizeAddress(String address) {
        if (address == null)
            return "";
        return address.toLowerCase().replaceFirst("^0x", "");
    }

    public static void main(String[] args) {
        System.out.println("Hyperliquid Private Key Verification Tool");
        System.out.println("========================================");

        if (args.length == 0) {
            printCurrentConfig();
            System.out.println();
            System.out.println("Usage: java HyperliquidKeyVerifier <expected_address>");
            System.out.println("Example: java HyperliquidKeyVerifier 0x3031d55bf17e6512b7ee35130955ab7bc3cf08d5");
        } else {
            String expectedAddress = args[0];
            verifyPrivateKey(expectedAddress);
        }
    }
}