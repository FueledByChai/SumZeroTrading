package com.sumzerotrading.broker.hyperliquid.translators;

import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.hyperliquid.HyperliquidPositionUpdate;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.websocket.HyperliquidTickerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TranslatorTest {

    @Mock
    HyperliquidTickerRegistry mockRegistry;
    @Mock
    Ticker mockTicker;
    @Mock
    HyperliquidPositionUpdate mockUpdate1;
    @Mock
    HyperliquidPositionUpdate mockUpdate2;

    private static org.mockito.MockedStatic<HyperliquidTickerRegistry> staticMock;

    @org.junit.jupiter.api.BeforeAll
    public static void setupAll() {
        staticMock = Mockito.mockStatic(HyperliquidTickerRegistry.class);
    }

    @BeforeEach
    public void setup() {
        staticMock.when(HyperliquidTickerRegistry::getInstance).thenReturn(mockRegistry);
    }

    @org.junit.jupiter.api.AfterAll
    public static void tearDownAll() {
        if (staticMock != null) {
            staticMock.close();
            staticMock = null;
        }
    }

    @Test
    public void testTranslatePositions_NullInput() {
        assertNull(Translator.translatePositions(null));
    }

    @Test
    public void testTranslatePositions_EmptyList() {
        List<Position> result = Translator.translatePositions(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testTranslatePositions_ValidList() {
        when(mockUpdate1.getTicker()).thenReturn("BTCUSD");
        when(mockUpdate1.getSize()).thenReturn(java.math.BigDecimal.valueOf(2.0));
        when(mockUpdate1.getEntryPrice()).thenReturn(java.math.BigDecimal.valueOf(50000.0));
        when(mockUpdate1.getLiquidationPrice()).thenReturn(java.math.BigDecimal.valueOf(45000.0));
        when(mockRegistry.lookupByBrokerSymbol("BTCUSD")).thenReturn(mockTicker);

        when(mockUpdate2.getTicker()).thenReturn("ETHUSD");
        when(mockUpdate2.getSize()).thenReturn(java.math.BigDecimal.valueOf(1.0));
        when(mockUpdate2.getEntryPrice()).thenReturn(java.math.BigDecimal.valueOf(3000.0));
        when(mockUpdate2.getLiquidationPrice()).thenReturn(java.math.BigDecimal.valueOf(2500.0));
        when(mockRegistry.lookupByBrokerSymbol("ETHUSD")).thenReturn(mockTicker);

        List<HyperliquidPositionUpdate> updates = Arrays.asList(mockUpdate1, mockUpdate2);
        List<Position> positions = Translator.translatePositions(updates);
        assertEquals(2, positions.size());
        for (Position pos : positions) {
            assertEquals(mockTicker, pos.getTicker());
        }
    }

    @Test
    public void testTranslatePosition_NullInput() {
        assertNull(Translator.translatePosition(null));
    }

    @Test
    public void testTranslatePosition_ValidInput() {
        when(mockUpdate1.getTicker()).thenReturn("BTCUSD");
        when(mockUpdate1.getSize()).thenReturn(java.math.BigDecimal.valueOf(2.0));
        when(mockUpdate1.getEntryPrice()).thenReturn(java.math.BigDecimal.valueOf(50000.0));
        when(mockUpdate1.getLiquidationPrice()).thenReturn(java.math.BigDecimal.valueOf(45000.0));
        when(mockRegistry.lookupByBrokerSymbol("BTCUSD")).thenReturn(mockTicker);

        Position pos = Translator.translatePosition(mockUpdate1);
        assertNotNull(pos);
        assertEquals(mockTicker, pos.getTicker());
        assertEquals(2.0, pos.getSize().doubleValue());
        assertEquals(50000.0, pos.getAverageCost().doubleValue());
        assertEquals(45000.0, pos.getLiquidationPrice().doubleValue());
    }
}
