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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;

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
                BigDecimal.valueOf(10), BigDecimal.ZERO, new Ticker("ABC").setInstrumentType(InstrumentType.STOCK),
                ZonedDateTime.now());
        test(status);
    }

    @Test
    public void testTradeOrder() throws Exception {
        OrderTicket order = new OrderTicket("123", new Ticker("123").setInstrumentType(InstrumentType.STOCK),
                BigDecimal.valueOf(100), TradeDirection.BUY);
        test(order);
    }

    @Test
    public void testOrderEvent() throws Exception {
        OrderTicket order = new OrderTicket("123", new Ticker("123").setInstrumentType(InstrumentType.STOCK),
                BigDecimal.valueOf(100), TradeDirection.BUY);
        OrderStatus status = new OrderStatus(OrderStatus.Status.NEW, "123", BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), BigDecimal.ZERO, new Ticker("ABC").setInstrumentType(InstrumentType.STOCK),
                ZonedDateTime.now());
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

        // Compare objects based on their type for better assertions
        if (object instanceof OrderTicket) {
            assertTradeOrderEquals((OrderTicket) object, (OrderTicket) object2);
        } else if (object instanceof OrderEvent) {
            assertOrderEventEquals((OrderEvent) object, (OrderEvent) object2);
        } else if (object instanceof OrderStatus) {
            assertOrderStatusEquals((OrderStatus) object, (OrderStatus) object2);
        } else {
            assertEquals(object, object2);
        }
    }

    private void assertTradeOrderEquals(OrderTicket expected, OrderTicket actual) {
        assertNotNull(actual);
        assertEquals(expected.getTicker().getSymbol(), actual.getTicker().getSymbol());
        assertEquals(expected.getDirection(), actual.getDirection());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getSize(), actual.getSize());
        assertEquals(expected.getOrderId(), actual.getOrderId());
        assertEquals(expected.getCurrentStatus(), actual.getCurrentStatus());
        assertEquals(expected.isSubmitted(), actual.isSubmitted());
        assertEquals(expected.getDuration(), actual.getDuration());
        assertEquals(expected.getFilledSize(), actual.getFilledSize());
        assertEquals(expected.getFilledPrice(), actual.getFilledPrice());
        assertEquals(expected.getCommission(), actual.getCommission());
        // Compare other key fields as needed
    }

    private void assertOrderEventEquals(OrderEvent expected, OrderEvent actual) {
        assertNotNull(actual);
        assertTradeOrderEquals(expected.getOrder(), actual.getOrder());
        assertOrderStatusEquals(expected.getOrderStatus(), actual.getOrderStatus());
    }

    private void assertOrderStatusEquals(OrderStatus expected, OrderStatus actual) {
        assertNotNull(actual);
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getOrderId(), actual.getOrderId());
        assertEquals(expected.getFilled(), actual.getFilled());
        assertEquals(expected.getRemaining(), actual.getRemaining());
        assertEquals(expected.getFillPrice(), actual.getFillPrice());
        assertEquals(expected.getTicker().getSymbol(), actual.getTicker().getSymbol());
        // Compare timestamp with tolerance for serialization precision issues
        if (expected.getTimestamp() != null && actual.getTimestamp() != null) {
            // Compare as strings to avoid precision issues with ZonedDateTime serialization
            assertEquals(expected.getTimestamp().toString(), actual.getTimestamp().toString());
        } else {
            assertEquals(expected.getTimestamp(), actual.getTimestamp());
        }
    }

    protected byte[] serialize(Object object) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(output);

        objectOut.writeObject(object);
        return output.toByteArray();

    }

    @SuppressWarnings("unchecked")
    protected <T> T deserialize(Class<T> clazz, byte[] bytes) throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        ObjectInputStream objectIn = new ObjectInputStream(input);
        return (T) objectIn.readObject();
    }

}
