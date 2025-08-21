package com.sumzerotrading.paradex.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sumzerotrading.data.CryptoTicker;
import com.sumzerotrading.data.Exchange;

@ExtendWith(MockitoExtension.class)
class ParadexTickerBuilderTest {

    @Mock
    private ISystemConfig mockSystemConfig;

    private Map<String, CryptoTicker> originalCryptoTickers;
    private ISystemConfig originalSystemConfig;

    @BeforeEach
    void setUp() {
        // Save original static state
        originalCryptoTickers = ParadexTickerBuilder.cryptoTickers;
        originalSystemConfig = ParadexTickerBuilder.systemConfig;

        // Clear the static cache for clean tests
        ParadexTickerBuilder.cryptoTickers = new HashMap<>();
        ParadexTickerBuilder.systemConfig = mockSystemConfig;
    }

    @AfterEach
    void tearDown() {
        // Restore original static state
        ParadexTickerBuilder.cryptoTickers = originalCryptoTickers;
        ParadexTickerBuilder.systemConfig = originalSystemConfig;
    }

    @Test
    void testGetTicker_FirstCall_CreatesNewTicker() {
        // Given
        String localSymbol = "BTC-USD";
        String paradexSymbol = "BTC-USD-PARADEX";
        BigDecimal tickSize = new BigDecimal("0.01");
        BigDecimal orderSizeIncrement = new BigDecimal("0.001");

        when(mockSystemConfig.getParadexSymbol()).thenReturn(paradexSymbol);
        when(mockSystemConfig.getTickSize()).thenReturn(tickSize);
        when(mockSystemConfig.getOrderSizeIncrement()).thenReturn(orderSizeIncrement);

        // When
        CryptoTicker result = ParadexTickerBuilder.getTicker(localSymbol);

        // Then
        assertNotNull(result);
        assertEquals(paradexSymbol, result.getSymbol());
        assertEquals(Exchange.PARADEX, result.getExchange());
        assertEquals(tickSize, result.getMinimumTickSize());
        assertEquals(orderSizeIncrement, result.getOrderSizeIncrement());

        verify(mockSystemConfig).getParadexSymbol();
        verify(mockSystemConfig).getTickSize();
        verify(mockSystemConfig).getOrderSizeIncrement();
    }

    @Test
    void testGetTicker_SubsequentCall_ReturnsCachedTicker() {
        // Given
        String localSymbol = "ETH-USD";
        String paradexSymbol = "ETH-USD-PARADEX";
        BigDecimal tickSize = new BigDecimal("0.01");
        BigDecimal orderSizeIncrement = new BigDecimal("0.001");

        when(mockSystemConfig.getParadexSymbol()).thenReturn(paradexSymbol);
        when(mockSystemConfig.getTickSize()).thenReturn(tickSize);
        when(mockSystemConfig.getOrderSizeIncrement()).thenReturn(orderSizeIncrement);

        // When
        CryptoTicker firstCall = ParadexTickerBuilder.getTicker(localSymbol);
        CryptoTicker secondCall = ParadexTickerBuilder.getTicker(localSymbol);

        // Then
        assertSame(firstCall, secondCall);

        // Verify systemConfig methods called only once (during first call)
        verify(mockSystemConfig, times(1)).getParadexSymbol();
        verify(mockSystemConfig, times(1)).getTickSize();
        verify(mockSystemConfig, times(1)).getOrderSizeIncrement();
    }

    @Test
    void testGetTicker_DifferentLocalSymbols_CreatesSeparateTickers() {
        // Given
        String localSymbol1 = "BTC-USD";
        String localSymbol2 = "ETH-USD";
        String paradexSymbol = "CRYPTO-SYMBOL";
        BigDecimal tickSize = new BigDecimal("0.01");
        BigDecimal orderSizeIncrement = new BigDecimal("0.001");

        when(mockSystemConfig.getParadexSymbol()).thenReturn(paradexSymbol);
        when(mockSystemConfig.getTickSize()).thenReturn(tickSize);
        when(mockSystemConfig.getOrderSizeIncrement()).thenReturn(orderSizeIncrement);

        // When
        CryptoTicker ticker1 = ParadexTickerBuilder.getTicker(localSymbol1);
        CryptoTicker ticker2 = ParadexTickerBuilder.getTicker(localSymbol2);

        // Then
        assertNotNull(ticker1);
        assertNotNull(ticker2);
        assertEquals(ticker1.getClass(), ticker2.getClass());
        // Different instances for different local symbols
        // Note: Both will have same symbol from systemConfig, but are cached separately
        assertEquals(paradexSymbol, ticker1.getSymbol());
        assertEquals(paradexSymbol, ticker2.getSymbol());

        // Verify systemConfig methods called twice (once for each ticker creation)
        verify(mockSystemConfig, times(2)).getParadexSymbol();
        verify(mockSystemConfig, times(2)).getTickSize();
        verify(mockSystemConfig, times(2)).getOrderSizeIncrement();
    }

    @Test
    void testGetTicker_NullLocalSymbol_CreatesTickerWithNullKey() {
        // Given
        String localSymbol = null;
        String paradexSymbol = "DEFAULT-SYMBOL";
        BigDecimal tickSize = new BigDecimal("0.01");
        BigDecimal orderSizeIncrement = new BigDecimal("0.001");

        when(mockSystemConfig.getParadexSymbol()).thenReturn(paradexSymbol);
        when(mockSystemConfig.getTickSize()).thenReturn(tickSize);
        when(mockSystemConfig.getOrderSizeIncrement()).thenReturn(orderSizeIncrement);

        // When
        CryptoTicker firstCall = ParadexTickerBuilder.getTicker(localSymbol);
        CryptoTicker secondCall = ParadexTickerBuilder.getTicker(localSymbol);

        // Then
        assertNotNull(firstCall);
        assertSame(firstCall, secondCall); // Should be cached even with null key
        assertEquals(paradexSymbol, firstCall.getSymbol());
        assertEquals(Exchange.PARADEX, firstCall.getExchange());

        verify(mockSystemConfig, times(1)).getParadexSymbol();
        verify(mockSystemConfig, times(1)).getTickSize();
        verify(mockSystemConfig, times(1)).getOrderSizeIncrement();
    }

    @Test
    void testGetTicker_EmptyStringLocalSymbol_CreatesTickerWithEmptyKey() {
        // Given
        String localSymbol = "";
        String paradexSymbol = "EMPTY-SYMBOL";
        BigDecimal tickSize = new BigDecimal("0.05");
        BigDecimal orderSizeIncrement = new BigDecimal("0.1");

        when(mockSystemConfig.getParadexSymbol()).thenReturn(paradexSymbol);
        when(mockSystemConfig.getTickSize()).thenReturn(tickSize);
        when(mockSystemConfig.getOrderSizeIncrement()).thenReturn(orderSizeIncrement);

        // When
        CryptoTicker result = ParadexTickerBuilder.getTicker(localSymbol);

        // Then
        assertNotNull(result);
        assertEquals(paradexSymbol, result.getSymbol());
        assertEquals(Exchange.PARADEX, result.getExchange());
        assertEquals(tickSize, result.getMinimumTickSize());
        assertEquals(orderSizeIncrement, result.getOrderSizeIncrement());
    }

    @Test
    void testGetTicker_SystemConfigWithDifferentValues_CreatesTickerWithThoseValues() {
        // Given
        String localSymbol = "SOL-USD";
        String paradexSymbol = "SOLANA-USD";
        BigDecimal tickSize = new BigDecimal("0.001");
        BigDecimal orderSizeIncrement = new BigDecimal("0.01");

        when(mockSystemConfig.getParadexSymbol()).thenReturn(paradexSymbol);
        when(mockSystemConfig.getTickSize()).thenReturn(tickSize);
        when(mockSystemConfig.getOrderSizeIncrement()).thenReturn(orderSizeIncrement);

        // When
        CryptoTicker result = ParadexTickerBuilder.getTicker(localSymbol);

        // Then
        assertNotNull(result);
        assertEquals(paradexSymbol, result.getSymbol());
        assertEquals(Exchange.PARADEX, result.getExchange());
        assertEquals(tickSize, result.getMinimumTickSize());
        assertEquals(orderSizeIncrement, result.getOrderSizeIncrement());
        assertEquals("USD", result.getCurrency()); // Default currency
    }

    @Test
    void testGetTicker_MultipleCallsSequence_VerifiesCachingBehavior() {
        // Given
        String localSymbol1 = "BTC-USD";
        String localSymbol2 = "ETH-USD";
        String paradexSymbol = "CRYPTO";
        BigDecimal tickSize = new BigDecimal("0.01");
        BigDecimal orderSizeIncrement = new BigDecimal("0.001");

        when(mockSystemConfig.getParadexSymbol()).thenReturn(paradexSymbol);
        when(mockSystemConfig.getTickSize()).thenReturn(tickSize);
        when(mockSystemConfig.getOrderSizeIncrement()).thenReturn(orderSizeIncrement);

        // When - Complex sequence of calls
        CryptoTicker ticker1_first = ParadexTickerBuilder.getTicker(localSymbol1);
        CryptoTicker ticker2_first = ParadexTickerBuilder.getTicker(localSymbol2);
        CryptoTicker ticker1_second = ParadexTickerBuilder.getTicker(localSymbol1);
        CryptoTicker ticker2_second = ParadexTickerBuilder.getTicker(localSymbol2);

        // Then
        assertSame(ticker1_first, ticker1_second); // Same symbol returns cached instance
        assertSame(ticker2_first, ticker2_second); // Same symbol returns cached instance

        // Verify systemConfig called only when creating new tickers (2 times total)
        verify(mockSystemConfig, times(2)).getParadexSymbol();
        verify(mockSystemConfig, times(2)).getTickSize();
        verify(mockSystemConfig, times(2)).getOrderSizeIncrement();
    }
}
