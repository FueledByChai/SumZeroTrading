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

package com.sumzerotrading.broker.order;

import com.sumzerotrading.data.StockTicker;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Rob Terpilowski
 */
public class OrderSerializationTest {

    public OrderSerializationTest() {
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
    public void testOrderStatus() throws Exception {
        OrderStatus status = new OrderStatus(OrderStatus.Status.NEW, "123", BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), BigDecimal.ZERO, new StockTicker("ABC"), ZonedDateTime.now());
        test(status);
    }

    @Test
    public void testTradeOrder() throws Exception {
        TradeOrder order = new TradeOrder("123", new StockTicker("123"), 100, TradeDirection.BUY);
        test(order);
    }

    @Test
    public void testOrderEvent() throws Exception {
        TradeOrder order = new TradeOrder("123", new StockTicker("123"), 100, TradeDirection.BUY);
        OrderStatus status = new OrderStatus(OrderStatus.Status.NEW, "123", BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), BigDecimal.ZERO, new StockTicker("ABC"), ZonedDateTime.now());
        OrderEvent event = new OrderEvent(order, status);
        test(event);
    }

    @Test
    public void testOrderEventFilter() throws Exception {
        OrderEventFilter filter = new OrderEventFilter();
        test(filter);
    }

    public void test(Object object) throws Exception {
        byte[] serialized = serialize(object);
        assertTrue(serialized.length > 0);

        Object object2 = deserialize(object.getClass(), serialized);

        assertEquals(object, object2);
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
