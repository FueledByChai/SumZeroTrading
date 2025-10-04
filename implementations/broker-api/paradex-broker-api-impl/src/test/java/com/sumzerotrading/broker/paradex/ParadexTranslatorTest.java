package com.sumzerotrading.broker.paradex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sumzerotrading.broker.order.Fill;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.paradex.common.api.ws.fills.ParadexFill;
import com.sumzerotrading.util.ITickerRegistry;

@ExtendWith(MockitoExtension.class)
class ParadexTranslatorTest {

    @Mock
    private ITickerRegistry mockTickerRegistry;

    @Mock
    private Ticker mockTicker;

    private ParadexTranslator translator;
    private ITickerRegistry originalTickerRegistry;

    @BeforeEach
    void setUp() {
        translator = new ParadexTranslator();
        // Save original tickerRegistry and inject mock
        originalTickerRegistry = ParadexTranslator.tickerRegistry;
        ParadexTranslator.tickerRegistry = mockTickerRegistry;
    }

    @AfterEach
    void tearDown() {
        // Restore original tickerRegistry
        ParadexTranslator.tickerRegistry = originalTickerRegistry;
    }

    @Test
    void testTranslateFill_BuyTakerFill() {
        // Given
        ParadexFill paradexFill = createParadexFill("BTC-USD-PERP", "50000.50", "0.1", ParadexFill.Side.BUY,
                ParadexFill.LiquidityType.TAKER, "fill-123", "order-456", "5.25", 1633024800000L // 2021-10-01 00:00:00
                                                                                                 // UTC
        );

        when(mockTickerRegistry.lookupByBrokerSymbol("BTC-USD-PERP")).thenReturn(mockTicker);

        // When
        Fill result = translator.translateFill(paradexFill);

        // Then
        assertNotNull(result);
        assertEquals(mockTicker, result.getTicker());
        assertEquals(new BigDecimal("50000.50"), result.getPrice());
        assertEquals("fill-123", result.getFillId());
        assertEquals(new BigDecimal("0.1"), result.getSize());
        assertEquals(TradeDirection.BUY, result.getSide());
        assertEquals("order-456", result.getOrderId());
        assertTrue(result.isTaker());
        assertEquals(new BigDecimal("5.25"), result.getCommission());
    }

    @Test
    void testTranslateFill_SellMakerFill() {
        // Given
        ParadexFill paradexFill = createParadexFill("ETH-USD-PERP", "3000.75", "2.5", ParadexFill.Side.SELL,
                ParadexFill.LiquidityType.MAKER, "fill-789", "order-101", "-1.50", // negative fee for maker rebate
                1633111200000L // 2021-10-02 00:00:00 UTC
        );

        when(mockTickerRegistry.lookupByBrokerSymbol("ETH-USD-PERP")).thenReturn(mockTicker);

        // When
        Fill result = translator.translateFill(paradexFill);

        // Then
        assertNotNull(result);
        assertEquals(mockTicker, result.getTicker());
        assertEquals(new BigDecimal("3000.75"), result.getPrice());
        assertEquals("fill-789", result.getFillId());
        assertEquals(new BigDecimal("2.5"), result.getSize());
        assertEquals(TradeDirection.SELL, result.getSide());
        assertEquals("order-101", result.getOrderId());
        assertFalse(result.isTaker()); // MAKER should be false for isTaker
        assertEquals(new BigDecimal("-1.50"), result.getCommission());
    }

    @Test
    void testTranslateFill_ZeroFee() {
        // Given
        ParadexFill paradexFill = createParadexFill("SOL-USD-PERP", "100.00", "10.0", ParadexFill.Side.BUY,
                ParadexFill.LiquidityType.MAKER, "fill-000", "order-999", "0.00", 1633197600000L // 2021-10-03 00:00:00
                                                                                                 // UTC
        );

        when(mockTickerRegistry.lookupByBrokerSymbol("SOL-USD-PERP")).thenReturn(mockTicker);

        // When
        Fill result = translator.translateFill(paradexFill);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("0.00"), result.getCommission());
        assertFalse(result.isTaker());
    }

    private ParadexFill createParadexFill(String market, String price, String size, ParadexFill.Side side,
            ParadexFill.LiquidityType liquidity, String fillId, String orderId, String fee, long createdAt) {

        ParadexFill fill = new ParadexFill();
        fill.setMarket(market);
        fill.setPrice(price);
        fill.setSize(size);
        fill.setSide(side);
        fill.setLiquidity(liquidity);
        fill.setId(fillId);
        fill.setOrderId(orderId);
        fill.setFee(fee);
        fill.setCreatedAt(createdAt);

        return fill;
    }
}