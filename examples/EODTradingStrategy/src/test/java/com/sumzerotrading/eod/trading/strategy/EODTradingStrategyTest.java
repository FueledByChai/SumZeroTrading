/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.eod.trading.strategy;

import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.broker.order.TradeOrder;
import com.sumzerotrading.data.StockTicker;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.interactive.brokers.client.InteractiveBrokersClient;
import com.sumzerotrading.interactive.brokers.client.InteractiveBrokersClientInterface;
import com.sumzerotrading.marketdata.ILevel1Quote;
import com.sumzerotrading.marketdata.Level1QuoteListener;
import com.sumzerotrading.marketdata.QuoteType;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 *
 * @author RobTerpilowski
 */
public class EODTradingStrategyTest {

    protected InteractiveBrokersClientInterface mockIbClient;
    protected String ibHost = "localhost";
    protected int ibPort = 7999;
    protected int ibClientId = 1;
    protected Ticker longTicker = new StockTicker("LONG");
    protected Ticker shortTicker = new StockTicker("SHORT");
    protected EODTradingStrategy strategy;
    protected String propFile;
    protected IReportGenerator mockReportGenerator;

    public EODTradingStrategyTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws Exception {
        mockIbClient = mock(InteractiveBrokersClientInterface.class);
        InteractiveBrokersClient.setMockInteractiveBrokersClient(mockIbClient, ibHost, ibPort, ibClientId);
        strategy = spy(EODTradingStrategy.class);
        strategy.timeToPlaceOrders = LocalTime.of(12, 40, 0);
        strategy.marketCloseTime = LocalTime.of(13, 0);
        strategy.ibHost = ibHost;
        strategy.ibPort = ibPort;
        strategy.ibClientId = ibClientId;
        strategy.entryOrderType = TradeOrder.Type.MARKET_ON_CLOSE;
        strategy.exitOrderType = TradeOrder.Type.MARKET_ON_OPEN;
        strategy.exitSeconds = 0;
        propFile = Paths.get(getClass().getResource("/eod.test.properties").toURI()).toString();
        doReturn(mockReportGenerator).when(strategy).getReportGenerator(any(String.class));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testStart() {

        TradeOrder order = new TradeOrder("123", longTicker, BigDecimal.valueOf(100), TradeDirection.SELL);
        List<TradeOrder> openOrders = new ArrayList<>();
        openOrders.add(order);
        when(mockIbClient.getOpenOrders()).thenReturn(openOrders);
        doReturn(true).when(strategy).checkOpenOrders(openOrders, strategy.longShortPairMap);

        strategy.start(propFile);

        verify(mockIbClient).addOrderStatusListener(mockReportGenerator);
        verify(mockIbClient).subscribeLevel1(eq(new StockTicker("QQQ")), any(Level1QuoteListener.class));
        verify(mockIbClient).subscribeLevel1(eq(new StockTicker("SPY")), any(Level1QuoteListener.class));
        verify(mockIbClient).subscribeLevel1(eq(new StockTicker("DIA")), any(Level1QuoteListener.class));
        verify(mockIbClient).subscribeLevel1(eq(new StockTicker("IWM")), any(Level1QuoteListener.class));

    }

    @Test
    public void testPlaceMOCOrders() {
        String corrId = "123";
        BigDecimal longSize = BigDecimal.valueOf(100);
        BigDecimal shortSize = BigDecimal.valueOf(200);
        ZonedDateTime currentDateTime = ZonedDateTime.of(2016, 3, 3, 5, 5, 1, 0, ZoneId.systemDefault());
        ZonedDateTime orderTime = ZonedDateTime.now();
        strategy.ibClient = mockIbClient;

        doReturn(corrId).when(strategy).getUUID();
        doReturn(longSize).when(strategy).getOrderSize(longTicker);
        doReturn(shortSize).when(strategy).getOrderSize(shortTicker);
        doReturn(orderTime).when(strategy).getNextBusinessDay(currentDateTime);
        when(mockIbClient.getNextOrderId()).thenReturn("100", "101", "102", "103", "104", "105");

        TradeOrder expectedLongOrder = new TradeOrder("100", longTicker, longSize, TradeDirection.BUY);
        expectedLongOrder.setType(TradeOrder.Type.MARKET_ON_CLOSE);
        expectedLongOrder.setReference("EOD-Pair-Strategy:123:Entry:Long*");

        TradeOrder expectedLongExitOrder = new TradeOrder("101", longTicker, longSize, TradeDirection.SELL);
        expectedLongExitOrder.setType(TradeOrder.Type.MARKET_ON_OPEN);
        expectedLongExitOrder.setGoodAfterTime(orderTime);
        expectedLongExitOrder.setReference("EOD-Pair-Strategy:123:Exit:Long*");

        expectedLongOrder.addChildOrder(expectedLongExitOrder);

        TradeOrder expectedShortOrder = new TradeOrder("102", shortTicker, shortSize, TradeDirection.SELL_SHORT);
        expectedShortOrder.setType(TradeOrder.Type.MARKET_ON_CLOSE);
        expectedShortOrder.setReference("EOD-Pair-Strategy:123:Entry:Short*");

        TradeOrder expectedShortExitOrder = new TradeOrder("103", shortTicker, shortSize, TradeDirection.BUY_TO_COVER);
        expectedShortExitOrder.setType(TradeOrder.Type.MARKET_ON_OPEN);
        expectedShortExitOrder.setGoodAfterTime(orderTime);
        expectedShortExitOrder.setReference("EOD-Pair-Strategy:123:Exit:Short*");

        expectedShortOrder.addChildOrder(expectedShortExitOrder);

        strategy.placeMOCOrders(longTicker, shortTicker, currentDateTime);

        verify(mockIbClient).placeOrder(expectedLongOrder);
        verify(mockIbClient).placeOrder(expectedShortOrder);
    }

    @Test
    public void testQuoteRecieved_NotLast() {
        ILevel1Quote mockQuote = mock(ILevel1Quote.class);
        when(mockQuote.containsType(QuoteType.VOLUME)).thenReturn(true);
        strategy.quoteRecieved(mockQuote);

        verify(strategy, never()).setAllPricesInitialized();
        verify(mockQuote, never()).getTimeStamp();
    }

    @Test
    public void testQuoteReceived_NotAllPricesInitialized_OrdersNotPlaced() {
        ZonedDateTime zdt = ZonedDateTime.of(2015, 6, 15, 15, 30, 0, 0, ZoneId.of("Z"));
        ILevel1Quote mockQuote = mock(ILevel1Quote.class);
        Ticker ticker = new StockTicker("ABC");
        when(mockQuote.containsType(QuoteType.LAST)).thenReturn(true);
        when(mockQuote.getTicker()).thenReturn(ticker);
        when(mockQuote.getValue(QuoteType.LAST)).thenReturn(BigDecimal.ONE);
        when(mockQuote.getTimeStamp()).thenReturn(zdt);
        doReturn(false).when(strategy).setAllPricesInitialized();
        strategy.ordersPlaced = false;

        strategy.quoteRecieved(mockQuote);

        assertFalse(strategy.ordersPlaced);
        verify(strategy).setAllPricesInitialized();
        verify(strategy, never()).placeMOCOrders(any(Ticker.class), any(Ticker.class), any(ZonedDateTime.class));
    }

    @Test
    public void testQuoteReceived_AllPricesInitialized_OrdersNotPlaced_IsBeforeTradeTime() {
        ZonedDateTime local = ZonedDateTime.of(2015, 6, 15, 0, 0, 0, 0, ZoneId.systemDefault());
        ZoneOffset offset = local.getOffset();
        ZonedDateTime zdt = ZonedDateTime.of(2015, 6, 15, 20, 30, 0, 0, ZoneId.of("Z"));

        System.out.println("Offset:  " + offset.getTotalSeconds() / 3600);
        int hour = strategy.timeToPlaceOrders.getHour() + (-offset.getTotalSeconds() / (3600));
        System.out.println("Offset hour: " + hour);
        zdt = zdt.withHour(hour);

        ILevel1Quote mockQuote = mock(ILevel1Quote.class);
        Ticker ticker = new StockTicker("ABC");
        when(mockQuote.containsType(QuoteType.LAST)).thenReturn(true);
        when(mockQuote.getTicker()).thenReturn(ticker);
        when(mockQuote.getValue(QuoteType.LAST)).thenReturn(BigDecimal.ONE);
        when(mockQuote.getTimeStamp()).thenReturn(zdt);
        doReturn(true).when(strategy).setAllPricesInitialized();
        strategy.ordersPlaced = false;

        strategy.quoteRecieved(mockQuote);

        assertFalse(strategy.ordersPlaced);
        verify(strategy).setAllPricesInitialized();
        verify(strategy, never()).placeMOCOrders(any(Ticker.class), any(Ticker.class), any(ZonedDateTime.class));
    }

    @Test
    public void testQuoteReceived_AllPricesInitialized_OrdersPacePlaced_BeforeTradeTime() {
        ZonedDateTime local = ZonedDateTime.of(2015, 6, 15, 0, 0, 0, 0, ZoneId.systemDefault());
        ZoneOffset offset = local.getOffset();
        ZonedDateTime zdt = ZonedDateTime.of(2015, 6, 15, 21, 30, 0, 0, ZoneId.of("Z"));
        int hour = strategy.timeToPlaceOrders.getHour() + (-offset.getTotalSeconds() / (3600));
        System.out.println("Offset hour: " + hour);
        zdt = zdt.withHour(hour);
        ILevel1Quote mockQuote = mock(ILevel1Quote.class);
        Ticker ticker = new StockTicker("ABC");
        when(mockQuote.containsType(QuoteType.LAST)).thenReturn(true);
        when(mockQuote.getTicker()).thenReturn(ticker);
        when(mockQuote.getValue(QuoteType.LAST)).thenReturn(BigDecimal.ONE);
        when(mockQuote.getTimeStamp()).thenReturn(zdt);
        doReturn(true).when(strategy).setAllPricesInitialized();
        strategy.ordersPlaced = true;

        strategy.quoteRecieved(mockQuote);

        assertFalse(strategy.ordersPlaced);
        verify(strategy).setAllPricesInitialized();
        verify(strategy, never()).placeMOCOrders(any(Ticker.class), any(Ticker.class), any(ZonedDateTime.class));
    }

    @Test
    public void testQuoteReceived_AllPricesInitialized_OrdersNotPlaced_AfterTradeTime_AfterMarketClose() {
        ZonedDateTime local = ZonedDateTime.of(2015, 6, 15, 0, 0, 0, 0, ZoneId.systemDefault());
        ZoneOffset offset = local.getOffset();
        ZonedDateTime zdt = ZonedDateTime.of(2015, 6, 15, 21, 50, 0, 0, ZoneId.of("Z"));
        System.out.println("Offset: " + offset.getTotalSeconds() / 3600);
        int hour = 1 + strategy.timeToPlaceOrders.getHour() + (-offset.getTotalSeconds() / (3600));
        System.out.println("Offset hour: " + hour);
        zdt = zdt.withHour(hour);
        ILevel1Quote mockQuote = mock(ILevel1Quote.class);
        strategy.longShortPairMap.put(longTicker, shortTicker);

        when(mockQuote.containsType(QuoteType.LAST)).thenReturn(true);
        when(mockQuote.getTicker()).thenReturn(longTicker);
        when(mockQuote.getValue(QuoteType.LAST)).thenReturn(BigDecimal.ONE);
        when(mockQuote.getTimeStamp()).thenReturn(zdt);
        doReturn(true).when(strategy).setAllPricesInitialized();
        doNothing().when(strategy).placeMOCOrders(any(Ticker.class), any(Ticker.class), any(ZonedDateTime.class));
        strategy.ordersPlaced = false;

        strategy.quoteRecieved(mockQuote);

        assertFalse(strategy.ordersPlaced);
        verify(strategy).setAllPricesInitialized();
        verify(strategy, never()).placeMOCOrders(any(Ticker.class), any(Ticker.class), any(ZonedDateTime.class));
    }

    @Test
    public void testQuoteReceived_AllPricesInitialized_OrdersNotPlaced_AfterTradeTime() {
        ZonedDateTime local = ZonedDateTime.of(2015, 6, 15, 0, 0, 0, 0, ZoneId.systemDefault());
        ZoneOffset offset = local.getOffset();
        ZonedDateTime zdt = ZonedDateTime.of(2015, 6, 15, 21, 50, 0, 0, ZoneId.of("Z"));
        System.out.println("Offset: " + offset.getTotalSeconds() / 3600);
        int hour = strategy.timeToPlaceOrders.getHour() + (-offset.getTotalSeconds() / (3600));
        zdt = zdt.withHour(hour);
        zdt = zdt.plusMinutes(1);
        ILevel1Quote mockQuote = mock(ILevel1Quote.class);
        strategy.longShortPairMap.put(longTicker, shortTicker);

        when(mockQuote.containsType(QuoteType.LAST)).thenReturn(true);
        when(mockQuote.getTicker()).thenReturn(longTicker);
        when(mockQuote.getValue(QuoteType.LAST)).thenReturn(BigDecimal.ONE);
        when(mockQuote.getTimeStamp()).thenReturn(zdt);
        doReturn(true).when(strategy).setAllPricesInitialized();
        doNothing().when(strategy).placeMOCOrders(any(Ticker.class), any(Ticker.class), any(ZonedDateTime.class));
        strategy.ordersPlaced = false;

        strategy.quoteRecieved(mockQuote);

        assertTrue(strategy.ordersPlaced);
        verify(strategy).setAllPricesInitialized();
        verify(strategy).placeMOCOrders(longTicker, shortTicker, zdt);
    }

    @Test
    public void testGetNextBusinessDay_ExitSecondsSet() {
        ZonedDateTime ldt = ZonedDateTime.of(2016, 2, 25, 6, 45, 55, 0, ZoneId.systemDefault());
        ZonedDateTime expectedDatetime = ZonedDateTime.of(2016, 2, 25, 6, 46, 55, 0, ZoneId.systemDefault());
        strategy.exitSeconds = 60;

        assertEquals(expectedDatetime, strategy.getNextBusinessDay(ldt));
    }

    @Test
    public void testGetNextBusinessDay_Thursday() {
        ZonedDateTime ldt = ZonedDateTime.of(2016, 2, 25, 6, 45, 55, 0, ZoneId.systemDefault());
        ZonedDateTime expectedDatetime = ZonedDateTime.of(2016, 2, 26, 6, 30, 2, 0, ZoneId.systemDefault());
        strategy.exitTime = LocalTime.of(6, 30, 2);

        assertEquals(expectedDatetime, strategy.getNextBusinessDay(ldt));
    }

    @Test
    public void testGetNextBusinessDay_Friday() {
        ZonedDateTime ldt = ZonedDateTime.of(2016, 2, 26, 6, 45, 55, 0, ZoneId.systemDefault());
        ZonedDateTime expectedDatetime = ZonedDateTime.of(2016, 2, 29, 6, 30, 2, 0, ZoneId.systemDefault());
        strategy.exitTime = LocalTime.of(6, 30, 2);

        assertEquals(expectedDatetime, strategy.getNextBusinessDay(ldt));
    }

    @Test
    public void testGetNextBusinessDay_Satruday() {
        ZonedDateTime ldt = ZonedDateTime.of(2016, 2, 27, 6, 45, 55, 0, ZoneId.systemDefault());
        ZonedDateTime expectedDatetime = ZonedDateTime.of(2016, 2, 29, 6, 30, 2, 0, ZoneId.systemDefault());
        strategy.exitTime = LocalTime.of(6, 30, 2);

        assertEquals(expectedDatetime, strategy.getNextBusinessDay(ldt));
    }

    @Test
    public void testInitProps() {
        strategy.initProps(propFile);
        assertEquals("localhost", strategy.ibHost);
        assertEquals(7999, strategy.ibPort);
        assertEquals(1, strategy.ibClientId);
        assertEquals(1000, strategy.orderSizeInDollars);
        assertEquals(LocalTime.of(12, 40), strategy.timeToPlaceOrders);
        assertEquals(LocalTime.of(13, 0), strategy.marketCloseTime);
        assertEquals(LocalTime.of(6, 30, 2), strategy.exitTime);
        assertEquals(2, strategy.longShortPairMap.size());
        assertEquals(new StockTicker("SPY"), strategy.longShortPairMap.get(new StockTicker("QQQ")));
        assertEquals(new StockTicker("IWM"), strategy.longShortPairMap.get(new StockTicker("DIA")));
        assertEquals("/some/test/dir/", strategy.strategyDirectory);
        assertEquals(TradeOrder.Type.MARKET_ON_CLOSE, strategy.entryOrderType);
        assertEquals(TradeOrder.Type.MARKET, strategy.exitOrderType);
        assertEquals(3, strategy.exitSeconds);
    }

    @Test
    public void testGetOrderSize() {
        strategy.start(propFile);
        strategy.lastPriceMap.put(longTicker, 50.00);
        BigDecimal expectedSize = BigDecimal.valueOf(20);

        assertEquals(expectedSize, strategy.getOrderSize(longTicker));
    }

    @Test
    public void testSetAllPricesInitialized() {
        Map mockLastPriceMap = mock(Map.class);
        strategy.lastPriceMap = mockLastPriceMap;
        strategy.longShortPairMap.put(longTicker, shortTicker);
        when(mockLastPriceMap.get(longTicker)).thenReturn(55.5);
        when(mockLastPriceMap.get(shortTicker)).thenReturn(20.5);

        assertFalse(strategy.allPricesInitialized);
        assertTrue(strategy.setAllPricesInitialized());
    }

    @Test
    public void testSetAllPricesInitialized_LongNotInitialized() {
        Map mockLastPriceMap = mock(Map.class);
        strategy.lastPriceMap = mockLastPriceMap;
        strategy.longShortPairMap.put(longTicker, shortTicker);
        when(mockLastPriceMap.get(longTicker)).thenReturn(null);
        when(mockLastPriceMap.get(shortTicker)).thenReturn(20.5);

        assertFalse(strategy.allPricesInitialized);
        assertFalse(strategy.setAllPricesInitialized());
    }

    @Test
    public void testSetAllPricesInitialized_ShortNotInitialized() {
        Map mockLastPriceMap = mock(Map.class);
        strategy.lastPriceMap = mockLastPriceMap;
        strategy.longShortPairMap.put(longTicker, shortTicker);
        when(mockLastPriceMap.get(longTicker)).thenReturn(50.0);
        when(mockLastPriceMap.get(shortTicker)).thenReturn(null);

        assertFalse(strategy.allPricesInitialized);
        assertFalse(strategy.setAllPricesInitialized());
    }

    @Test
    public void testSetAllPricesInitialized_AlreadyInitialized() {
        strategy.allPricesInitialized = true;
        Map mockLastPriceMap = mock(Map.class);
        strategy.lastPriceMap = mockLastPriceMap;

        assertTrue(strategy.setAllPricesInitialized());
        verify(mockLastPriceMap, never()).get(longTicker);
        verify(mockLastPriceMap, never()).get(shortTicker);
    }

    @Test
    public void testCheckOpenOrders_OrderMapEmpty() {
        assertFalse(strategy.checkOpenOrders(new ArrayList<>(), new HashMap<>()));
    }

    @Test
    public void testCheckOpenOrders_NoMocOrders() {
        List<TradeOrder> orders = new ArrayList<>();
        orders.add(new TradeOrder("123", new StockTicker("ABC"), BigDecimal.valueOf(10), TradeDirection.SELL));

        assertFalse(strategy.checkOpenOrders(orders, new HashMap<>()));
    }

    @Test
    public void testCheckOpenOrders_MocOrdersDontMatchTickers() {
        List<TradeOrder> orders = new ArrayList<>();
        TradeOrder order = new TradeOrder("123", new StockTicker("ABC"), BigDecimal.valueOf(10), TradeDirection.SELL);
        order.setType(TradeOrder.Type.MARKET_ON_CLOSE);
        orders.add(order);
        Map<Ticker, Ticker> tickerMap = new HashMap<>();
        tickerMap.put(new StockTicker("AAA"), new StockTicker("XYZ"));

        assertFalse(strategy.checkOpenOrders(orders, tickerMap));
    }

    @Test
    public void testCheckOpenOrders_HappyPath() {
        List<TradeOrder> orders = new ArrayList<>();
        TradeOrder order = new TradeOrder("123", new StockTicker("ABC"), BigDecimal.valueOf(10), TradeDirection.SELL);
        order.setType(TradeOrder.Type.MARKET_ON_CLOSE);
        orders.add(order);
        Map<Ticker, Ticker> tickerMap = new HashMap<>();
        tickerMap.put(new StockTicker("ABC"), new StockTicker("XYZ"));

        assertTrue(strategy.checkOpenOrders(orders, tickerMap));
    }

}
