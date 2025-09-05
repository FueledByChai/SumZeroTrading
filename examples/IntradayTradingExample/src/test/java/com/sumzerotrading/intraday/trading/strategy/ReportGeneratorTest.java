/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.intraday.trading.strategy;

import static com.sumzerotrading.intraday.trading.strategy.TradeReferenceLine.Direction.LONG;
import static com.sumzerotrading.intraday.trading.strategy.TradeReferenceLine.Direction.SHORT;
import static com.sumzerotrading.intraday.trading.strategy.TradeReferenceLine.Side.ENTRY;
import static com.sumzerotrading.intraday.trading.strategy.TradeReferenceLine.Side.EXIT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sumzerotrading.broker.order.OrderEvent;
import com.sumzerotrading.broker.order.OrderStatus;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.intraday.trading.strategy.TradeReferenceLine.Direction;
import com.sumzerotrading.intraday.trading.strategy.TradeReferenceLine.Side;

/**
 *
 * @author RobTerpilowski
 */
@Ignore("Ignore until API is updated and fixed")
public class ReportGeneratorTest {

    protected ReportGenerator reportGenerator;
    protected OrderTicket order;
    protected String tmpDir;
    protected String partialDir;
    protected ZonedDateTime orderFilledTime;

    public ReportGeneratorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws Exception {
        orderFilledTime = ZonedDateTime.of(2015, 3, 15, 12, 30, 33, 0, ZoneId.systemDefault());
        reportGenerator = spy(ReportGenerator.class);
        order = new OrderTicket("123", new Ticker("QQQ").setInstrumentType(InstrumentType.STOCK),
                BigDecimal.valueOf(100), TradeDirection.BUY);
        order.setOrderFilledTime(orderFilledTime);
        String systemTmpDir = System.getProperty("java.io.tmpdir");
        if (!systemTmpDir.endsWith("/")) {
            systemTmpDir += "/";
        }

        System.out.println("System tmp dir is: " + systemTmpDir);
        tmpDir = systemTmpDir + "rg-test/";
        partialDir = tmpDir + "partial/";
        FileUtils.deleteDirectory(new File(tmpDir));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConstructor_NoSlashInPath() {
        MockGenerator generator = new MockGenerator(tmpDir);
        assertEquals(tmpDir + "report.csv", generator.outputFile);
        assertEquals(tmpDir, generator.outputDir);
        assertEquals(tmpDir + "partial/", generator.partialDir);
        assertTrue(Files.exists(Paths.get(generator.outputDir)));
        assertTrue(Files.exists(Paths.get(generator.partialDir)));
        assertTrue(generator.loadPartialCalled);

    }

    @Test
    public void testLoadSaveDeletePartial() throws Exception {
        ReportGenerator generator = new ReportGenerator(tmpDir);

        RoundTrip roundTrip = new RoundTrip();
        TradeReferenceLine tradeReferenceLine = buildReferenceLine("123", LONG, ENTRY);
        tradeReferenceLine.correlationId = "123";
        tradeReferenceLine.direction = TradeReferenceLine.Direction.LONG;
        tradeReferenceLine.side = TradeReferenceLine.Side.ENTRY;
        roundTrip.addTradeReference(order, tradeReferenceLine);

        order.setCurrentStatus(OrderStatus.Status.FILLED);
        OrderEvent orderEvent = new OrderEvent(order, new OrderStatus(OrderStatus.Status.NEW, "", "",
                new Ticker("QQQ").setInstrumentType(InstrumentType.STOCK), ZonedDateTime.now()));

        TradeReferenceLine longExitLine = buildReferenceLine("123", LONG, EXIT);
        TradeReferenceLine shortEntryLine = buildReferenceLine("123", SHORT, ENTRY);
        TradeReferenceLine shortExitLine = buildReferenceLine("123", SHORT, EXIT);

        roundTrip.addTradeReference(order, longExitLine);
        roundTrip.addTradeReference(order, shortEntryLine);

        File[] files = new File(partialDir).listFiles();
        assertEquals(0, files.length);

        generator.savePartial("123", roundTrip);

        files = new File(partialDir).listFiles();
        assertEquals(1, files.length);

        generator.roundTripMap.clear();
        assertTrue(generator.roundTripMap.isEmpty());

        generator.loadPartialRoundTrips();
        assertEquals(1, generator.roundTripMap.size());
        assertEquals(roundTrip, generator.roundTripMap.get("123"));

        generator.deletePartial("123");
        files = new File(partialDir).listFiles();
        assertEquals(0, files.length);

    }

    @Test
    public void testOrderEvent_NotFilled() {
        order.setCurrentStatus(OrderStatus.Status.NEW);
        OrderEvent orderEvent = new OrderEvent(order, new OrderStatus(OrderStatus.Status.NEW, "", "",
                new Ticker("QQQ").setInstrumentType(InstrumentType.STOCK), ZonedDateTime.now()));
        reportGenerator.orderEvent(orderEvent);

        verify(reportGenerator, never()).writeRoundTripToFile(any(RoundTrip.class));
    }

    @Test
    public void testOrderEvent_FirstRoundTrip() throws Exception {
        TradeReferenceLine tradeReferenceLine = new TradeReferenceLine();
        tradeReferenceLine.correlationId = "123";
        order.setCurrentStatus(OrderStatus.Status.FILLED);
        OrderEvent orderEvent = new OrderEvent(order, new OrderStatus(OrderStatus.Status.NEW, "", "",
                new Ticker("QQQ").setInstrumentType(InstrumentType.STOCK), ZonedDateTime.now()));

        doReturn(tradeReferenceLine).when(reportGenerator).getTradeReferenceLine(any(String.class));
        doNothing().when(reportGenerator).savePartial(any(String.class), any(RoundTrip.class));
        assertTrue(reportGenerator.roundTripMap.isEmpty());

        reportGenerator.orderEvent(orderEvent);

        verify(reportGenerator).savePartial(eq("123"), any(RoundTrip.class));
        verify(reportGenerator, never()).writeRoundTripToFile(any(RoundTrip.class));
        assertEquals(1, reportGenerator.roundTripMap.size());
    }

    @Test
    public void testOrderEvent_RoundTripExists_ButNotComplete() throws Exception {
        RoundTrip roundTrip = new RoundTrip();
        TradeReferenceLine tradeReferenceLine = new TradeReferenceLine();
        tradeReferenceLine.correlationId = "123";
        tradeReferenceLine.direction = TradeReferenceLine.Direction.LONG;
        tradeReferenceLine.side = TradeReferenceLine.Side.ENTRY;

        roundTrip.addTradeReference(order, tradeReferenceLine);

        order.setCurrentStatus(OrderStatus.Status.FILLED);
        OrderEvent orderEvent = new OrderEvent(order, new OrderStatus(OrderStatus.Status.NEW, "", "",
                new Ticker("QQQ").setInstrumentType(InstrumentType.STOCK), ZonedDateTime.now()));
        reportGenerator.roundTripMap.put("123", roundTrip);

        doReturn(tradeReferenceLine).when(reportGenerator).getTradeReferenceLine(order.getReference());
        doNothing().when(reportGenerator).savePartial(any(String.class), any(RoundTrip.class));
        assertEquals(1, reportGenerator.roundTripMap.size());

        reportGenerator.orderEvent(orderEvent);

        verify(reportGenerator).savePartial("123", roundTrip);
        verify(reportGenerator, never()).writeRoundTripToFile(any(RoundTrip.class));

        assertEquals(1, reportGenerator.roundTripMap.size());
    }

    @Test
    public void testOrderEvent_RoundTripComplete() throws Exception {
        RoundTrip roundTrip = new RoundTrip();
        TradeReferenceLine tradeReferenceLine = buildReferenceLine("123", LONG, ENTRY);
        tradeReferenceLine.correlationId = "123";
        tradeReferenceLine.direction = TradeReferenceLine.Direction.LONG;
        tradeReferenceLine.side = TradeReferenceLine.Side.ENTRY;
        roundTrip.addTradeReference(order, tradeReferenceLine);

        order.setCurrentStatus(OrderStatus.Status.FILLED);
        OrderEvent orderEvent = new OrderEvent(order, new OrderStatus(OrderStatus.Status.NEW, "", "",
                new Ticker("QQQ").setInstrumentType(InstrumentType.STOCK), ZonedDateTime.now()));

        TradeReferenceLine longExitLine = buildReferenceLine("123", LONG, EXIT);
        TradeReferenceLine shortEntryLine = buildReferenceLine("123", SHORT, ENTRY);
        TradeReferenceLine shortExitLine = buildReferenceLine("123", SHORT, EXIT);

        roundTrip.addTradeReference(order, longExitLine);
        roundTrip.addTradeReference(order, shortEntryLine);

        reportGenerator.roundTripMap.put("123", roundTrip);

        doReturn(shortExitLine).when(reportGenerator).getTradeReferenceLine(any(String.class));
        doNothing().when(reportGenerator).writeRoundTripToFile(any(RoundTrip.class));
        doNothing().when(reportGenerator).deletePartial("123");
        assertEquals(1, reportGenerator.roundTripMap.size());

        reportGenerator.orderEvent(orderEvent);

        verify(reportGenerator).deletePartial("123");
        verify(reportGenerator).writeRoundTripToFile(roundTrip);
        assertTrue(reportGenerator.roundTripMap.isEmpty());

    }

    @Test
    public void testWriteRoundTrip() throws Exception {
        Path path = Files.createTempFile("ReportGeneratorUnitTest", ".txt");
        reportGenerator.outputFile = path.toString();
        String expected = "2016-03-19T07:01:10,LONG,ABC,100,100.23,0,2016-03-20T06:01:10,101.23,0";

        Ticker longTicker = new Ticker("ABC").setInstrumentType(InstrumentType.STOCK);
        Ticker shortTicker = new Ticker("XYZ").setInstrumentType(InstrumentType.STOCK);
        BigDecimal longSize = BigDecimal.valueOf(100);
        BigDecimal shortSize = BigDecimal.valueOf(50);
        BigDecimal longEntryFillPrice = BigDecimal.valueOf(100.23);
        BigDecimal longExitFillPrice = BigDecimal.valueOf(101.23);
        ZonedDateTime entryTime = ZonedDateTime.of(2016, 3, 19, 7, 1, 10, 0, ZoneId.systemDefault());
        ZonedDateTime exitTime = ZonedDateTime.of(2016, 3, 20, 6, 1, 10, 0, ZoneId.systemDefault());

        OrderTicket longEntry = new OrderTicket("123", longTicker, longSize, TradeDirection.BUY);
        longEntry.setFilledPrice(longEntryFillPrice);
        longEntry.setOrderFilledTime(entryTime);
        TradeReferenceLine entryLine = new TradeReferenceLine();
        entryLine.correlationId = "123";
        entryLine.direction = Direction.LONG;
        entryLine.side = Side.ENTRY;

        OrderTicket longExit = new OrderTicket("123", longTicker, longSize, TradeDirection.SELL);
        longExit.setFilledPrice(longExitFillPrice);
        longExit.setOrderFilledTime(exitTime);
        TradeReferenceLine exitLine = new TradeReferenceLine();
        exitLine.correlationId = "123";
        exitLine.direction = LONG;
        exitLine.side = Side.EXIT;

        RoundTrip roundTrip = new RoundTrip();
        roundTrip.addTradeReference(longEntry, entryLine);
        roundTrip.addTradeReference(longExit, exitLine);

        System.out.println("Writing out to file: " + path);

        reportGenerator.writeRoundTripToFile(roundTrip);

        List<String> lines = Files.readAllLines(path);
        assertEquals(1, lines.size());
        assertEquals(expected, lines.get(0));

        Files.deleteIfExists(path);

    }

    @Test
    public void testReportGeneratorEndToEnd() throws Exception {
        Ticker longTicker = new Ticker("ABC").setInstrumentType(InstrumentType.STOCK);
        Ticker shortTicker = new Ticker("XYZ").setInstrumentType(InstrumentType.STOCK);

        ZonedDateTime entryOrderTime = ZonedDateTime.of(2016, 3, 25, 6, 18, 35, 0, ZoneId.systemDefault());
        ZonedDateTime exitOrderTime = ZonedDateTime.of(2016, 3, 25, 6, 19, 35, 0, ZoneId.systemDefault());

        String directory = System.getProperty("java.io.tmpdir");
        if (!directory.endsWith("/")) {
            directory += "/";
        }
        Path reportPath = Paths.get(directory + "report.csv");
        Files.deleteIfExists(reportPath);
        System.out.println("Creating directory at: " + directory);
        ReportGenerator generator = new ReportGenerator(directory);

        OrderTicket longEntryOrder = new OrderTicket("123", longTicker, BigDecimal.valueOf(100), TradeDirection.BUY);
        longEntryOrder.setFilledPrice(BigDecimal.valueOf(100.00));
        longEntryOrder.setReference("Intraday-Strategy:guid-123:Entry:Long*");
        longEntryOrder.setCurrentStatus(OrderStatus.Status.FILLED);
        longEntryOrder.setOrderFilledTime(entryOrderTime);

        generator.orderEvent(new OrderEvent(longEntryOrder, null));
        assertFalse(Files.exists(reportPath));

        OrderTicket longExitOrder = new OrderTicket("1234", longTicker, BigDecimal.valueOf(100), TradeDirection.SELL);
        longExitOrder.setFilledPrice(BigDecimal.valueOf(105.00));
        longExitOrder.setReference("Intraday-Strategy:guid-123:Exit:Long*");
        longExitOrder.setCurrentStatus(OrderStatus.Status.FILLED);
        longExitOrder.setOrderFilledTime(exitOrderTime);

        generator.orderEvent(new OrderEvent(longExitOrder, null));
        assertTrue(Files.exists(reportPath));

        List<String> lines = Files.readAllLines(reportPath);
        assertEquals(1, lines.size());

        String line = lines.get(0);
        String expected = "2016-03-25T06:18:35,LONG,ABC,100,100.0,0,2016-03-25T06:19:35,105.0,0";
        assertEquals(expected, line);

        generator.orderEvent(new OrderEvent(longEntryOrder, null));
        generator.orderEvent(new OrderEvent(longExitOrder, null));

        lines = Files.readAllLines(reportPath);
        assertEquals(2, lines.size());
        assertEquals(expected, lines.get(0));
        assertEquals(expected, lines.get(1));

    }

    protected TradeReferenceLine buildReferenceLine(String id, Direction direction, Side side) {
        TradeReferenceLine line = new TradeReferenceLine();
        line.correlationId = id;
        line.direction = direction;
        line.side = side;

        return line;
    }

    private static class MockGenerator extends ReportGenerator {

        boolean loadPartialCalled;

        public MockGenerator(String dir) {
            super(dir);
        }

        @Override
        public void loadPartialRoundTrips() throws IOException {
            loadPartialCalled = true;
        }

    }

}
