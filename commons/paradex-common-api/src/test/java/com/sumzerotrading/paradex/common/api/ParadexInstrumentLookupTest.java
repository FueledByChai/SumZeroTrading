package com.sumzerotrading.paradex.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sumzerotrading.data.Exchange;
import com.sumzerotrading.data.InstrumentDescriptor;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;

@ExtendWith(MockitoExtension.class)
class ParadexInstrumentLookupTest {

    @Mock
    private IParadexRestApi mockApi;

    @Mock
    private InstrumentDescriptor mockInstrumentDescriptor;

    private ParadexInstrumentLookup instrumentLookup;

    @BeforeEach
    void setUp() {
        instrumentLookup = new TestableParadexInstrumentLookup(mockApi);
    }

    @Test
    void testLookupByCommonSymbol_Success() {
        // Given
        String commonSymbol = "BTC";
        String expectedParadexSymbol = "BTC-USD-PERP";

        InstrumentDescriptor expectedDescriptor = createBitcoinDescriptor();

        when(mockApi.getInstrumentDescriptor(expectedParadexSymbol)).thenReturn(expectedDescriptor);

        // When
        InstrumentDescriptor result = instrumentLookup.lookupByCommonSymbol(commonSymbol);

        // Then
        assertNotNull(result);
        assertEquals(expectedDescriptor, result);
        verify(mockApi).getInstrumentDescriptor(expectedParadexSymbol);
    }

    @Test
    void testLookupByCommonSymbol_DifferentSymbol() {
        // Given
        String commonSymbol = "ETH";
        String expectedParadexSymbol = "ETH-USD-PERP";

        InstrumentDescriptor expectedDescriptor = createEthereumDescriptor();

        when(mockApi.getInstrumentDescriptor(expectedParadexSymbol)).thenReturn(expectedDescriptor);

        // When
        InstrumentDescriptor result = instrumentLookup.lookupByCommonSymbol(commonSymbol);

        // Then
        assertNotNull(result);
        assertEquals(expectedDescriptor, result);
        assertEquals("ETH", result.getCommonSymbol());
        assertEquals("ETH-USD-PERP", result.getExchangeSymbol());
        verify(mockApi).getInstrumentDescriptor(expectedParadexSymbol);
    }

    @Test
    void testLookupByCommonSymbol_SymbolNotFound() {
        // Given
        String commonSymbol = "UNKNOWN";
        String expectedParadexSymbol = "UNKNOWN-USD-PERP";

        when(mockApi.getInstrumentDescriptor(expectedParadexSymbol)).thenReturn(null);

        // When
        InstrumentDescriptor result = instrumentLookup.lookupByCommonSymbol(commonSymbol);

        // Then
        assertNull(result);
        verify(mockApi).getInstrumentDescriptor(expectedParadexSymbol);
    }

    @Test
    void testLookupByExchangeSymbol_Success() {
        // Given
        String exchangeSymbol = "BTC-USD-PERP";
        InstrumentDescriptor expectedDescriptor = createBitcoinDescriptor();

        when(mockApi.getInstrumentDescriptor(exchangeSymbol)).thenReturn(expectedDescriptor);

        // When
        InstrumentDescriptor result = instrumentLookup.lookupByExchangeSymbol(exchangeSymbol);

        // Then
        assertNotNull(result);
        assertEquals(expectedDescriptor, result);
        verify(mockApi).getInstrumentDescriptor(exchangeSymbol);
    }

    @Test
    void testLookupByExchangeSymbol_DifferentSymbol() {
        // Given
        String exchangeSymbol = "ETH-USD-PERP";
        InstrumentDescriptor expectedDescriptor = createEthereumDescriptor();

        when(mockApi.getInstrumentDescriptor(exchangeSymbol)).thenReturn(expectedDescriptor);

        // When
        InstrumentDescriptor result = instrumentLookup.lookupByExchangeSymbol(exchangeSymbol);

        // Then
        assertNotNull(result);
        assertEquals(expectedDescriptor, result);
        verify(mockApi).getInstrumentDescriptor(exchangeSymbol);
    }

    @Test
    void testLookupByExchangeSymbol_SymbolNotFound() {
        // Given
        String exchangeSymbol = "UNKNOWN-USD-PERP";

        when(mockApi.getInstrumentDescriptor(exchangeSymbol)).thenReturn(null);

        // When
        InstrumentDescriptor result = instrumentLookup.lookupByExchangeSymbol(exchangeSymbol);

        // Then
        assertNull(result);
        verify(mockApi).getInstrumentDescriptor(exchangeSymbol);
    }

    @Test
    void testLookupByTicker_CryptoTicker() {
        // Given
        Ticker ticker = new Ticker("BTC-USD-PERP").setExchange(Exchange.PARADEX)
                .setInstrumentType(InstrumentType.PERPETUAL_FUTURES);
        InstrumentDescriptor expectedDescriptor = createBitcoinDescriptor();

        when(mockApi.getInstrumentDescriptor("BTC-USD-PERP")).thenReturn(expectedDescriptor);

        // When
        InstrumentDescriptor result = instrumentLookup.lookupByTicker(ticker);

        // Then
        assertNotNull(result);
        assertEquals(expectedDescriptor, result);
        verify(mockApi).getInstrumentDescriptor("BTC-USD-PERP");
    }

    @Test
    void testLookupByTicker_StockTicker() {
        // Given
        Ticker ticker = new Ticker("AAPL").setExchange(Exchange.INTERACTIVE_BROKERS_SMART)
                .setInstrumentType(InstrumentType.STOCK);
        InstrumentDescriptor expectedDescriptor = mock(InstrumentDescriptor.class);

        when(mockApi.getInstrumentDescriptor("AAPL")).thenReturn(expectedDescriptor);

        // When
        InstrumentDescriptor result = instrumentLookup.lookupByTicker(ticker);

        // Then
        assertNotNull(result);
        assertEquals(expectedDescriptor, result);
        verify(mockApi).getInstrumentDescriptor("AAPL");
    }

    @Test
    void testLookupByTicker_TickerNotFound() {
        // Given
        Ticker ticker = new Ticker("UNKNOWN-USD-PERP").setExchange(Exchange.PARADEX)
                .setInstrumentType(InstrumentType.PERPETUAL_FUTURES);

        when(mockApi.getInstrumentDescriptor("UNKNOWN-USD-PERP")).thenReturn(null);

        // When
        InstrumentDescriptor result = instrumentLookup.lookupByTicker(ticker);

        // Then
        assertNull(result);
        verify(mockApi).getInstrumentDescriptor("UNKNOWN-USD-PERP");
    }

    @Test
    void testLookupByTicker_NullTicker() {
        // When & Then - This should throw NullPointerException
        try {
            instrumentLookup.lookupByTicker(null);
        } catch (NullPointerException e) {
            // Expected behavior
        }

        // Verify no API calls were made
        verify(mockApi, never()).getInstrumentDescriptor(anyString());
    }

    @Test
    void testMultipleLookups_VerifyDifferentCalls() {
        // Given
        String commonSymbol = "BTC";
        String exchangeSymbol = "ETH-USD-PERP";
        Ticker ticker = new Ticker("DOGE-USD-PERP").setExchange(Exchange.PARADEX)
                .setInstrumentType(InstrumentType.PERPETUAL_FUTURES);

        InstrumentDescriptor btcDescriptor = createBitcoinDescriptor();
        InstrumentDescriptor ethDescriptor = createEthereumDescriptor();
        InstrumentDescriptor dogeDescriptor = createDogeDescriptor();

        when(mockApi.getInstrumentDescriptor("BTC-USD-PERP")).thenReturn(btcDescriptor);
        when(mockApi.getInstrumentDescriptor("ETH-USD-PERP")).thenReturn(ethDescriptor);
        when(mockApi.getInstrumentDescriptor("DOGE-USD-PERP")).thenReturn(dogeDescriptor);

        // When
        InstrumentDescriptor result1 = instrumentLookup.lookupByCommonSymbol(commonSymbol);
        InstrumentDescriptor result2 = instrumentLookup.lookupByExchangeSymbol(exchangeSymbol);
        InstrumentDescriptor result3 = instrumentLookup.lookupByTicker(ticker);

        // Then
        assertEquals(btcDescriptor, result1);
        assertEquals(ethDescriptor, result2);
        assertEquals(dogeDescriptor, result3);

        verify(mockApi).getInstrumentDescriptor("BTC-USD-PERP");
        verify(mockApi).getInstrumentDescriptor("ETH-USD-PERP");
        verify(mockApi).getInstrumentDescriptor("DOGE-USD-PERP");
        verify(mockApi, times(3)).getInstrumentDescriptor(anyString());
    }

    @Test
    void testCommonSymbolToParadexSymbolConversion() {
        // Test that the conversion method is being called correctly
        try (MockedStatic<ParadexUtil> mockedUtil = Mockito.mockStatic(ParadexUtil.class)) {
            // Given
            String commonSymbol = "BTC";
            String expectedParadexSymbol = "BTC-USD-PERP";

            mockedUtil.when(() -> ParadexUtil.commonSymbolToParadexSymbol(commonSymbol))
                    .thenReturn(expectedParadexSymbol);

            when(mockApi.getInstrumentDescriptor(expectedParadexSymbol)).thenReturn(mockInstrumentDescriptor);

            // When
            instrumentLookup.lookupByCommonSymbol(commonSymbol);

            // Then
            mockedUtil.verify(() -> ParadexUtil.commonSymbolToParadexSymbol(commonSymbol));
            verify(mockApi).getInstrumentDescriptor(expectedParadexSymbol);
        }
    }

    @Test
    void testApiIntegration_UsesCorrectApiInstance() {
        // Test that the class uses the API factory correctly
        try (MockedStatic<ParadexApiFactory> mockedFactory = Mockito.mockStatic(ParadexApiFactory.class)) {
            // Given
            IParadexRestApi factoryApi = mock(IParadexRestApi.class);
            mockedFactory.when(ParadexApiFactory::getPublicApi).thenReturn(factoryApi);

            when(factoryApi.getInstrumentDescriptor("BTC-USD-PERP")).thenReturn(mockInstrumentDescriptor);

            // When
            ParadexInstrumentLookup newLookup = new ParadexInstrumentLookup();
            newLookup.lookupByExchangeSymbol("BTC-USD-PERP");

            // Then
            mockedFactory.verify(ParadexApiFactory::getPublicApi);
            verify(factoryApi).getInstrumentDescriptor("BTC-USD-PERP");
        }
    }

    // Helper methods to create test data
    private InstrumentDescriptor createBitcoinDescriptor() {
        return new InstrumentDescriptor(InstrumentType.PERPETUAL_FUTURES, Exchange.PARADEX, "BTC", "BTC-USD-PERP",
                "BTC", "USD", new BigDecimal("0.00001"), new BigDecimal("0.1"), 100, BigDecimal.ONE, 8, BigDecimal.ONE,
                1, "");
    }

    private InstrumentDescriptor createEthereumDescriptor() {
        return new InstrumentDescriptor(InstrumentType.PERPETUAL_FUTURES, Exchange.PARADEX, "ETH", "ETH-USD-PERP",
                "ETH", "USD", new BigDecimal("0.0001"), new BigDecimal("0.01"), 50, BigDecimal.ONE, 8, BigDecimal.ONE,
                1, "");
    }

    private InstrumentDescriptor createDogeDescriptor() {
        return new InstrumentDescriptor(InstrumentType.PERPETUAL_FUTURES, Exchange.PARADEX, "DOGE", "DOGE-USD-PERP",
                "DOGE", "USD", new BigDecimal("1.0"), new BigDecimal("0.00001"), 10, BigDecimal.ONE, 4, BigDecimal.ONE,
                1, "");
    }

    /**
     * Testable subclass that allows injection of a mock API
     */
    private static class TestableParadexInstrumentLookup extends ParadexInstrumentLookup {
        public TestableParadexInstrumentLookup(IParadexRestApi api) {
            this.api = api;
        }
    }
}
