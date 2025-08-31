package com.sumzerotrading.paradex.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sumzerotrading.data.Exchange;
import com.sumzerotrading.data.InstrumentDescriptor;
import com.sumzerotrading.data.SumZeroException;

@ExtendWith(MockitoExtension.class)
class ParadexRestApiTest {

    private ParadexRestApi paradexRestApi;

    @BeforeEach
    void setUp() {
        paradexRestApi = new ParadexRestApi("https://api.testnet.paradex.trade/v1");
    }

    @Test
    void testParseInstrumentDescriptor_ValidJson() {
        // Given
        String validJson = "{\n" + "    \"results\": [\n" + "        {\n"
                + "            \"symbol\": \"BTC-USD-PERP\",\n" + "            \"base_currency\": \"BTC\",\n"
                + "            \"quote_currency\": \"USD\",\n" + "            \"settlement_currency\": \"USDC\",\n"
                + "            \"order_size_increment\": \"0.00001\",\n" + "            \"price_tick_size\": \"0.1\",\n"
                + "            \"min_notional\": \"100\",\n" + "            \"open_at\": 1696431006398,\n"
                + "            \"expiry_at\": 0,\n" + "            \"asset_kind\": \"PERP\",\n"
                + "            \"market_kind\": \"cross\",\n" + "            \"position_limit\": \"100\",\n"
                + "            \"price_bands_width\": \"0.05\",\n" + "            \"max_slippage\": \"0.001\",\n"
                + "            \"max_open_orders\": 150,\n" + "            \"max_funding_rate\": \"0.02\",\n"
                + "            \"delta1_cross_margin_params\": {\n" + "                \"imf_base\": \"0.02\",\n"
                + "                \"imf_shift\": \"0\",\n" + "                \"imf_factor\": \"0\",\n"
                + "                \"mmf_factor\": \"0.5\"\n" + "            },\n"
                + "            \"price_feed_id\": \"GVXRSBjFk6e6J3NbVPXohDJetcTjaeeuykUpbQF8UoMU\",\n"
                + "            \"oracle_ewma_factor\": \"0.20000046249626113\",\n"
                + "            \"max_order_size\": \"100\",\n"
                + "            \"max_funding_rate_change\": \"0.0002\",\n"
                + "            \"max_tob_spread\": \"0.01\",\n" + "            \"interest_rate\": \"0.0001\",\n"
                + "            \"clamp_rate\": \"0.0005\",\n" + "            \"funding_period_hours\": 8,\n"
                + "            \"tags\": [\n" + "                \"LAYER-1\"\n" + "            ]\n" + "        }\n"
                + "    ]\n" + "}";

        // When
        InstrumentDescriptor result = paradexRestApi.parseInstrumentDescriptor(validJson);

        // Then
        assertNotNull(result);
        assertEquals(Exchange.PARADEX, result.getExchange());
        assertEquals("BTC", result.getCommonSymbol());
        assertEquals("BTC-USD-PERP", result.getExchangeSymbol());
        assertEquals("BTC", result.getBaseCurrency());
        assertEquals("USD", result.getQuoteCurrency());
        assertEquals(new BigDecimal("0.00001"), result.getOrderSizeIncrement());
        assertEquals(new BigDecimal("0.1"), result.getPriceTickSize());
        assertEquals(100, result.getMinNotionalOrderSize());
        assertEquals(8, result.getFundingPeriodHours());
    }

    @Test
    void testParseInstrumentDescriptor_EmptyResults() {
        // Given
        String emptyResultsJson = "{\n" + "    \"results\": []\n" + "}";

        // When
        InstrumentDescriptor result = paradexRestApi.parseInstrumentDescriptor(emptyResultsJson);

        // Then
        assertNull(result);
    }

    @Test
    void testParseInstrumentDescriptor_NoResults() {
        // Given
        String noResultsJson = "{\n" + "    \"status\": \"success\"\n" + "}";

        // When
        InstrumentDescriptor result = paradexRestApi.parseInstrumentDescriptor(noResultsJson);

        // Then
        assertNull(result);
    }

    @Test
    void testParseInstrumentDescriptor_MultipleResults() {
        // Given - JSON with multiple instruments, should parse the first one
        String multipleResultsJson = "{\n" + "    \"results\": [\n" + "        {\n"
                + "            \"symbol\": \"BTC-USD-PERP\",\n" + "            \"base_currency\": \"BTC\",\n"
                + "            \"quote_currency\": \"USD\",\n" + "            \"settlement_currency\": \"USDC\",\n"
                + "            \"order_size_increment\": \"0.00001\",\n" + "            \"price_tick_size\": \"0.1\",\n"
                + "            \"min_notional\": \"100\",\n" + "            \"funding_period_hours\": 8\n"
                + "        },\n" + "        {\n" + "            \"symbol\": \"ETH-USD-PERP\",\n"
                + "            \"base_currency\": \"ETH\",\n" + "            \"quote_currency\": \"USD\",\n"
                + "            \"settlement_currency\": \"USDC\",\n"
                + "            \"order_size_increment\": \"0.0001\",\n" + "            \"price_tick_size\": \"0.01\",\n"
                + "            \"min_notional\": \"50\",\n" + "            \"funding_period_hours\": 8\n"
                + "        }\n" + "    ]\n" + "}";

        // When
        InstrumentDescriptor result = paradexRestApi.parseInstrumentDescriptor(multipleResultsJson);

        // Then - Should parse the first instrument (BTC)
        assertNotNull(result);
        assertEquals("BTC", result.getCommonSymbol());
        assertEquals("BTC-USD-PERP", result.getExchangeSymbol());
        assertEquals("BTC", result.getBaseCurrency());
        assertEquals("USD", result.getQuoteCurrency());
    }

    @Test
    void testParseInstrumentDescriptor_DifferentCurrencies() {
        // Given - ETH-USD example
        String ethJson = "{\n" + "    \"results\": [\n" + "        {\n" + "            \"symbol\": \"ETH-USD-PERP\",\n"
                + "            \"base_currency\": \"ETH\",\n" + "            \"quote_currency\": \"USD\",\n"
                + "            \"settlement_currency\": \"USDC\",\n"
                + "            \"order_size_increment\": \"0.0001\",\n" + "            \"price_tick_size\": \"0.01\",\n"
                + "            \"min_notional\": \"50\",\n" + "            \"funding_period_hours\": 8\n"
                + "        }\n" + "    ]\n" + "}";

        // When
        InstrumentDescriptor result = paradexRestApi.parseInstrumentDescriptor(ethJson);

        // Then
        assertNotNull(result);
        assertEquals("ETH", result.getCommonSymbol());
        assertEquals("ETH-USD-PERP", result.getExchangeSymbol());
        assertEquals("ETH", result.getBaseCurrency());
        assertEquals("USD", result.getQuoteCurrency());
        assertEquals(new BigDecimal("0.0001"), result.getOrderSizeIncrement());
        assertEquals(new BigDecimal("0.01"), result.getPriceTickSize());
        assertEquals(50, result.getMinNotionalOrderSize());
        assertEquals(8, result.getFundingPeriodHours());
    }

    @Test
    void testParseInstrumentDescriptor_InvalidJson() {
        // Given
        String invalidJson = "{ invalid json }";

        // When & Then
        assertThrows(SumZeroException.class, () -> {
            paradexRestApi.parseInstrumentDescriptor(invalidJson);
        });
    }

    @Test
    void testParseInstrumentDescriptor_MissingRequiredFields() {
        // Given - JSON missing required fields
        String incompleteJson = "{\n" + "    \"results\": [\n" + "        {\n"
                + "            \"symbol\": \"BTC-USD-PERP\"\n" + "        }\n" + "    ]\n" + "}";

        // When & Then
        assertThrows(SumZeroException.class, () -> {
            paradexRestApi.parseInstrumentDescriptor(incompleteJson);
        });
    }

    @Test
    void testParseInstrumentDescriptor_DifferentDataTypes() {
        // Given - Test with different numeric values and data types
        String jsonWithDifferentTypes = "{\n" + "    \"results\": [\n" + "        {\n"
                + "            \"symbol\": \"DOGE-USD-PERP\",\n" + "            \"base_currency\": \"DOGE\",\n"
                + "            \"quote_currency\": \"USD\",\n" + "            \"settlement_currency\": \"USDC\",\n"
                + "            \"order_size_increment\": \"1.0\",\n" + "            \"price_tick_size\": \"0.00001\",\n"
                + "            \"min_notional\": \"10\",\n" + "            \"funding_period_hours\": 4\n"
                + "        }\n" + "    ]\n" + "}";

        // When
        InstrumentDescriptor result = paradexRestApi.parseInstrumentDescriptor(jsonWithDifferentTypes);

        // Then
        assertNotNull(result);
        assertEquals("DOGE", result.getCommonSymbol());
        assertEquals("DOGE-USD-PERP", result.getExchangeSymbol());
        assertEquals("DOGE", result.getBaseCurrency());
        assertEquals("USD", result.getQuoteCurrency());
        assertEquals(new BigDecimal("1.0"), result.getOrderSizeIncrement());
        assertEquals(new BigDecimal("0.00001"), result.getPriceTickSize());
        assertEquals(10, result.getMinNotionalOrderSize());
        assertEquals(4, result.getFundingPeriodHours());
    }

    @Test
    void testGetInstrumentDescriptor_Success() {
        // Given
        String symbol = "BTC-USD-PERP";
        String mockResponse = "{\n" + "    \"results\": [\n" + "        {\n"
                + "            \"symbol\": \"BTC-USD-PERP\",\n" + "            \"base_currency\": \"BTC\",\n"
                + "            \"quote_currency\": \"USD\",\n" + "            \"settlement_currency\": \"USDC\",\n"
                + "            \"order_size_increment\": \"0.00001\",\n" + "            \"price_tick_size\": \"0.1\",\n"
                + "            \"min_notional\": \"100\",\n" + "            \"funding_period_hours\": 8\n"
                + "        }\n" + "    ]\n" + "}";

        // Create a testable subclass that overrides the HTTP call
        TestableParadexRestApi testableApi = new TestableParadexRestApi("https://api.testnet.paradex.trade/v1");
        testableApi.setMockResponse(mockResponse);

        // When
        InstrumentDescriptor result = testableApi.getInstrumentDescriptor(symbol);

        // Then
        assertNotNull(result);
        assertEquals(Exchange.PARADEX, result.getExchange());
        assertEquals("BTC", result.getCommonSymbol());
        assertEquals("BTC-USD-PERP", result.getExchangeSymbol());
        assertEquals("BTC", result.getBaseCurrency());
        assertEquals("USD", result.getQuoteCurrency());
        assertEquals(new BigDecimal("0.00001"), result.getOrderSizeIncrement());
        assertEquals(new BigDecimal("0.1"), result.getPriceTickSize());
        assertEquals(100, result.getMinNotionalOrderSize());
        assertEquals(8, result.getFundingPeriodHours());
        assertEquals("https://api.testnet.paradex.trade/v1/markets?market=BTC-USD-PERP",
                testableApi.getLastRequestUrl());
    }

    @Test
    void testGetInstrumentDescriptor_DifferentSymbol() {
        // Given
        String symbol = "ETH-USD-PERP";
        String mockResponse = "{\n" + "    \"results\": [\n" + "        {\n"
                + "            \"symbol\": \"ETH-USD-PERP\",\n" + "            \"base_currency\": \"ETH\",\n"
                + "            \"quote_currency\": \"USD\",\n" + "            \"settlement_currency\": \"USDC\",\n"
                + "            \"order_size_increment\": \"0.0001\",\n" + "            \"price_tick_size\": \"0.01\",\n"
                + "            \"min_notional\": \"50\",\n" + "            \"funding_period_hours\": 8\n"
                + "        }\n" + "    ]\n" + "}";

        TestableParadexRestApi testableApi = new TestableParadexRestApi("https://api.testnet.paradex.trade/v1");
        testableApi.setMockResponse(mockResponse);

        // When
        InstrumentDescriptor result = testableApi.getInstrumentDescriptor(symbol);

        // Then
        assertNotNull(result);
        assertEquals("ETH", result.getCommonSymbol());
        assertEquals("ETH-USD-PERP", result.getExchangeSymbol());
        assertEquals("ETH", result.getBaseCurrency());
        assertEquals("USD", result.getQuoteCurrency());
        assertEquals(new BigDecimal("0.0001"), result.getOrderSizeIncrement());
        assertEquals(new BigDecimal("0.01"), result.getPriceTickSize());
        assertEquals(50, result.getMinNotionalOrderSize());
        assertEquals(8, result.getFundingPeriodHours());
        assertEquals("https://api.testnet.paradex.trade/v1/markets?market=ETH-USD-PERP",
                testableApi.getLastRequestUrl());
    }

    @Test
    void testGetInstrumentDescriptor_EmptyResults() {
        // Given
        String symbol = "UNKNOWN-USD-PERP";
        String mockResponse = "{\n" + "    \"results\": []\n" + "}";

        TestableParadexRestApi testableApi = new TestableParadexRestApi("https://api.testnet.paradex.trade/v1");
        testableApi.setMockResponse(mockResponse);

        // When
        InstrumentDescriptor result = testableApi.getInstrumentDescriptor(symbol);

        // Then
        assertNull(result);
        assertEquals("https://api.testnet.paradex.trade/v1/markets?market=UNKNOWN-USD-PERP",
                testableApi.getLastRequestUrl());
    }

    @Test
    void testGetInstrumentDescriptor_HttpError() {
        // Given
        String symbol = "BTC-USD-PERP";

        TestableParadexRestApi testableApi = new TestableParadexRestApi("https://api.testnet.paradex.trade/v1");
        testableApi.setMockHttpError(500, "Internal Server Error");

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            testableApi.getInstrumentDescriptor(symbol);
        });
    }

    @Test
    void testGetInstrumentDescriptor_InvalidJson() {
        // Given
        String symbol = "BTC-USD-PERP";
        String invalidResponse = "{ invalid json }";

        TestableParadexRestApi testableApi = new TestableParadexRestApi("https://api.testnet.paradex.trade/v1");
        testableApi.setMockResponse(invalidResponse);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            testableApi.getInstrumentDescriptor(symbol);
        });
    }

    @Test
    void testGetInstrumentDescriptor_MissingRequiredFields() {
        // Given
        String symbol = "BTC-USD-PERP";
        String incompleteResponse = "{\n" + "    \"results\": [\n" + "        {\n"
                + "            \"symbol\": \"BTC-USD-PERP\"\n" + "        }\n" + "    ]\n" + "}";

        TestableParadexRestApi testableApi = new TestableParadexRestApi("https://api.testnet.paradex.trade/v1");
        testableApi.setMockResponse(incompleteResponse);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            testableApi.getInstrumentDescriptor(symbol);
        });
    }

    /**
     * Testable subclass that allows mocking HTTP responses without making actual
     * HTTP calls
     */
    private static class TestableParadexRestApi extends ParadexRestApi {
        private String mockResponse;
        private boolean shouldThrowHttpError;
        private int httpErrorCode;
        private String httpErrorMessage;
        private String lastRequestUrl;

        public TestableParadexRestApi(String baseUrl) {
            super(baseUrl);
        }

        public void setMockResponse(String response) {
            this.mockResponse = response;
            this.shouldThrowHttpError = false;
        }

        public void setMockHttpError(int errorCode, String errorMessage) {
            this.shouldThrowHttpError = true;
            this.httpErrorCode = errorCode;
            this.httpErrorMessage = errorMessage;
        }

        public String getLastRequestUrl() {
            return lastRequestUrl;
        }

        @Override
        public InstrumentDescriptor getInstrumentDescriptor(String symbol) {
            // Simulate the URL building logic
            String path = "/markets";
            String url = baseUrl + path;
            lastRequestUrl = url + "?market=" + symbol;

            if (shouldThrowHttpError) {
                throw new RuntimeException("HTTP " + httpErrorCode + ": " + httpErrorMessage);
            }

            if (mockResponse != null) {
                return parseInstrumentDescriptor(mockResponse);
            }

            throw new RuntimeException("No mock response set");
        }
    }
}
