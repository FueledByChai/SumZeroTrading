/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.eod.trading.strategy;

import com.sumzerotrading.broker.order.OrderTicket;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.spy;

/**
 *
 * @author RobTerpilowski
 */
public class EODSystemPropertiesTest {

    protected EODSystemProperties systemProps;

    public EODSystemPropertiesTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        systemProps = spy(EODSystemProperties.class);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetLongShortTickers() {
        Map<String, String> expected = new HashMap<>();
        expected.put("QQQ", "SPY");
        expected.put("IWM", "DIA");

        Properties props = new Properties();
        props.setProperty("pair.1", "QQQ:SPY");
        props.setProperty("foo", "bar");
        props.setProperty("pair.2", "IWM:DIA");

        assertEquals(expected, systemProps.getLongShortTickers(props));

    }

    @Test
    public void testGetOrderType() {
        assertEquals(OrderTicket.Type.MARKET, systemProps.getOrderType("MKT"));
        assertEquals(OrderTicket.Type.MARKET_ON_CLOSE, systemProps.getOrderType("MOC"));
        assertEquals(OrderTicket.Type.MARKET_ON_OPEN, systemProps.getOrderType("MOO"));

        try {
            systemProps.getOrderType("foo");
            fail();
        } catch (IllegalStateException ex) {
            // this should happen
        }
    }

    @Test
    public void testParseProps() {
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("QQQ", "SPY");
        expectedMap.put("IWM", "DIA");

        Properties props = new Properties();
        props.setProperty("tws.host", "myHost");
        props.setProperty("tws.port", "123");
        props.setProperty("tws.client.id", "13");
        props.setProperty("trade.size.dollars", "999");
        props.setProperty("start.time", "05:32:44");
        props.setProperty("exit.time", "06:30:02");
        props.setProperty("market.close.time", "13:00:00");
        props.setProperty("pair.1", "QQQ:SPY");
        props.setProperty("foo", "bar");
        props.setProperty("pair.2", "IWM:DIA");
        props.setProperty("strategy.directory", "/my/dir");
        props.setProperty("entry.order.type", "MOC");
        props.setProperty("exit.order.type", "MOO");
        props.setProperty("exit.seconds", "10");

        systemProps.parseProps(props);

        assertEquals("myHost", systemProps.getTwsHost());
        assertEquals(123, systemProps.getTwsPort());
        assertEquals(13, systemProps.getTwsClientId());
        assertEquals(999, systemProps.getTradeSizeDollars());
        assertEquals("/my/dir", systemProps.getStrategyDirectory());
        assertEquals(LocalTime.of(5, 32, 44), systemProps.getStartTime());
        assertEquals(LocalTime.of(6, 30, 02), systemProps.getExitTime());
        assertEquals(LocalTime.of(13, 0), systemProps.getMarketCloseTime());
        assertEquals(OrderTicket.Type.MARKET_ON_CLOSE, systemProps.getEntryOrderType());
        assertEquals(expectedMap, systemProps.getLongShortTickerMap());
        assertEquals(OrderTicket.Type.MARKET_ON_OPEN, systemProps.getExitOrderType());
        assertEquals(10, systemProps.getExitSeconds());
    }

    @Test
    public void testGetFilenameConstructor() throws Exception {
        Path path = Paths.get(getClass().getResource("/eod.test.properties").toURI());
        EODSystemProperties props = new EODSystemProperties(path.toString());
        assertEquals(getExpected(), props);
    }

    @Test
    public void testInputStreamConstructor() throws Exception {
        InputStream input = getClass().getResourceAsStream("/eod.test.properties");
        EODSystemProperties props = new EODSystemProperties(input);

        assertEquals(getExpected(), props);
    }

    protected EODSystemProperties getExpected() {
        EODSystemProperties props = new EODSystemProperties();
        props.twsHost = "localhost";
        props.twsPort = 7999;
        props.twsClientId = 1;
        props.tradeSizeDollars = 1000;
        props.startTime = LocalTime.of(12, 40);
        props.exitTime = LocalTime.of(6, 30, 2);
        props.marketCloseTime = LocalTime.of(13, 0);
        props.longShortTickerMap.put("QQQ", "SPY");
        props.longShortTickerMap.put("DIA", "IWM");
        props.strategyDirectory = "/some/test/dir/";
        props.entryOrderType = OrderTicket.Type.MARKET_ON_CLOSE;
        props.exitOrderType = OrderTicket.Type.MARKET;
        props.exitSeconds = 3;

        return props;

    }
}
