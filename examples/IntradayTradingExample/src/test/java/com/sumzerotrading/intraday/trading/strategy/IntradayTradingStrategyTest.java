/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.intraday.trading.strategy;

import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.broker.order.TradeOrder;
import com.sumzerotrading.data.StockTicker;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.interactive.brokers.client.InteractiveBrokersClientInterface;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 *
 * @author RobTerpilowski
 */
public class IntradayTradingStrategyTest {

    protected IntradayTradingStrategy strategy;
    protected Ticker ticker;
    protected InteractiveBrokersClientInterface mockIbClient;

    public IntradayTradingStrategyTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        strategy = spy(IntradayTradingStrategy.class);
        ticker = new StockTicker("XYZ");
        mockIbClient = mock(InteractiveBrokersClientInterface.class);
        strategy.ibClient = mockIbClient;
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testInitProps() throws Exception {
        File file = new File(getClass().getResource("/intraday.test.properties").toURI());
        String path = file.getAbsolutePath();
        strategy.initProps(path);

        assertEquals("localhost", strategy.ibHost);
        assertEquals(10, strategy.ibClientId);
        assertEquals(9999, strategy.ibPort);
        assertEquals(1000, strategy.orderSizeInDollars);
        assertEquals(LocalTime.of(5, 37, 0), strategy.systemStartTime);
        assertEquals(LocalTime.of(5, 38, 0), strategy.longStartTime);
        assertEquals(LocalTime.of(5, 39, 0), strategy.longStopTime);
        assertEquals(LocalTime.of(5, 40, 0), strategy.longCloseTime);
        assertEquals(LocalTime.of(5, 41, 0), strategy.shortStartTime);
        assertEquals(LocalTime.of(5, 42, 0), strategy.shortStopTime);
        assertEquals(LocalTime.of(5, 43, 0), strategy.shortCloseTime);
        assertEquals("/my/dir", strategy.strategyDirectory);
        assertEquals(new StockTicker("XYZ"), strategy.mainTicker);

    }

    @Test
    public void testPlaceOrder_Buy() {
        LocalTime closeTime = LocalTime.of(13, 00, 00);
        String entryOrderId = "1";
        String exitOrderId = "2";
        String correlationId = "123";
        String entryOrderReference = "Intraday-Strategy-XYZ:123:Entry:LONG*";
        String exitOrderReference = "Intraday-Strategy-XYZ:123:Exit:LONG*";

        TradeOrder expectedEntryOrder = new TradeOrder(entryOrderId, ticker, BigDecimal.valueOf(100),
                TradeDirection.BUY);
        expectedEntryOrder.setReference(entryOrderReference);

        TradeOrder expectedExitOrder = new TradeOrder(exitOrderId, ticker, BigDecimal.valueOf(100),
                TradeDirection.SELL);
        expectedExitOrder.setReference(exitOrderReference);
        ZonedDateTime zdt = ZonedDateTime.of(LocalDate.now(ZoneId.systemDefault()), closeTime, ZoneId.of("GMT"));
        expectedExitOrder.setGoodAfterTime(zdt);

        expectedEntryOrder.addChildOrder(expectedExitOrder);

        doReturn(correlationId).when(strategy).getUUID();
        doReturn(entryOrderId).doReturn(exitOrderId).when(mockIbClient).getNextOrderId();

        strategy.placeOrder(ticker, TradeDirection.BUY, 100, closeTime);

        verify(mockIbClient).placeOrder(expectedEntryOrder);

    }
}
