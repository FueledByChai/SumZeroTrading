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

package com.sumzerotrading.broker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.ZonedDateTime;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;

/**
 *
 * @author Rob Terpilowski
 */
public class BrokerSerializationTest {

    public BrokerSerializationTest() {
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
    public void testBrokerError() throws Exception {
        BrokerError error = new BrokerError("123");

        test(error);
    }

    @Test
    public void testTransaction() throws Exception {
        Transaction t = new Transaction();
        t.setCommission(1.0);
        t.setPositionId("123");
        t.setTicker(new Ticker("123").setInstrumentType(InstrumentType.STOCK));
        t.setTradeDirection(TradeDirection.BUY);
        t.setTransactionDate(ZonedDateTime.now());
        t.setTransactionPrice(100.0);
        t.setTransactionSize(100);

        test(t);

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

}
