/**
 * MIT License

Copyright (c) 2015  Rob Terpilowski

Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
and associated documentation files (the "Software"), to deal in the Software without restriction, 
including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.sumzerotrading.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Rob Terpilowski
 */
public class SerializationTest {

    public SerializationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testStockTicker() throws Exception {
        Ticker ticker = new StockTicker("SEA");
        test(ticker);
    }

    @Test
    public void testSerializeFuturesTicker() throws Exception {
        FuturesTicker ticker = new FuturesTicker();
        ticker.setSymbol("ES");
        ticker.setContractMultiplier(BigDecimal.ONE);
        ticker.setCurrency("USD");
        ticker.setExchange(Exchange.ARCA);
        ticker.setExpiryMonth(1);
        ticker.setExpiryYear(2015);
        ticker.setMinimumTickSize(BigDecimal.TEN);

        test(ticker);
    }

    @Test
    public void testSerializeBar() throws Exception {
        BarData bar = new BarData(ZonedDateTime.now(), new BigDecimal(1), new BigDecimal(2), new BigDecimal(3),
                new BigDecimal(4), new BigDecimal(5), 6);
        test(bar);
    }

    @Test
    public void testSerializeComboTicker() throws Exception {
        Ticker firstTicker = new StockTicker("ABC");
        Ticker secondTicker = new StockTicker("DEF");
        ComboTicker ticker = new ComboTicker(firstTicker, secondTicker, 1, 2);

        test(ticker);
    }

    @Test
    public void testSerializeCurrencyTicker() throws Exception {
        CurrencyTicker ticker = new CurrencyTicker();
        ticker.setContractMultiplier(BigDecimal.ZERO);
        ticker.setCurrency("USD");
        ticker.setExchange(Exchange.ARCA);
        ticker.setMinimumTickSize(BigDecimal.TEN);
        ticker.setSupporthalfTick(true);
        ticker.setSymbol("EUR");

        test(ticker);

    }

    @Test
    public void testSerializeExchange() throws Exception {
        test(Exchange.GLOBEX);
    }

    @Test
    public void testIndexTicker() throws Exception {
        IndexTicker ticker = new IndexTicker();
        ticker.setContractMultiplier(BigDecimal.ZERO);
        ticker.setCurrency("USD");
        ticker.setExchange(Exchange.ARCA);
        ticker.setMinimumTickSize(BigDecimal.TEN);
        ticker.setSymbol("SPX");

        test(ticker);
    }

    @Test
    public void testOptionTicker() throws Exception {
        OptionTicker ticker = new OptionTicker("ES");
        ticker.setContractMultiplier(BigDecimal.ZERO);
        ticker.setCurrency("USD");
        ticker.setExchange(Exchange.ARCA);
        ticker.setMinimumTickSize(BigDecimal.TEN);
        ticker.setSymbol("ES");

        test(ticker);
    }

    public void test(Object object) throws Exception {
        int hashBefore = object.hashCode();
        byte[] serialized = serialize(object);
        assertTrue(serialized.length > 0);

        Object object2 = deserialize(object.getClass(), serialized);

        assertEquals(object, object2);
        assertEquals(hashBefore, object2.hashCode());
    }

    protected byte[] serialize(Object object) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(output);

        objectOut.writeObject(object);
        return output.toByteArray();

    }

    protected <T> T deserialize(Class<T> clazz, byte[] bytes) throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        ObjectInputStream objectIn = new ObjectInputStream(input);
        return (T) objectIn.readObject();
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
