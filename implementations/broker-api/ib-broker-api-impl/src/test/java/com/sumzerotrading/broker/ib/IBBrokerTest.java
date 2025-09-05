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

package com.sumzerotrading.broker.ib;

import java.math.BigDecimal;

import org.jmock.Mockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.ib.client.EClientSocket;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.ib.IBConnectionInterface;
import com.sumzerotrading.ib.IBSocket;

/**
 *
 * @author Rob Terpilowski
 */
@Ignore("Ignore until IB implementation is fixed")
public class IBBrokerTest {

    Mockery mockery;
    EClientSocket mockClientSocket;
    IBConnectionInterface mockConnection;
    IBSocket testIbSocket;
    InteractiveBrokersBroker testBroker;

    public IBBrokerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        mockery = new Mockery();
        mockClientSocket = Mockito.mock(EClientSocket.class);
        mockConnection = Mockito.mock(IBConnectionInterface.class);

        testIbSocket = new IBSocket(mockConnection);
        testBroker = new InteractiveBrokersBroker(testIbSocket);
    }

    @After
    public void tearDown() {
    }

    @Test
    @Ignore
    public void testConnect() {
        // fail();
    }

    @Test
    @Ignore
    public void testDisconnect() {
        // fail();
    }

    @Test
    @Ignore
    public void testIsConnected() {
        // fail();
    }

    @Test
    @Ignore
    public void testExecDetails() {
        // fail();
    }

    @Test
    @Ignore
    public void testNextValidId() {
        // fail();
    }

    @Test
    public void testCancelOrder() {
        final String id = "99";
        final int intId = Integer.parseInt(id);

        testBroker.cancelOrder(id);

        // verify(mockClientSocket).cancelOrder(intId);

    }

    @Test
    public void testCancelTradeOrder() {
        final String id = "99";
        final int intId = Integer.parseInt(id);
        final OrderTicket tradeOrder = new OrderTicket(id, null, BigDecimal.valueOf(1), TradeDirection.BUY);

        testBroker.cancelOrder(tradeOrder);

        // verify(mockClientSocket).cancelOrder(intId);

    }

}
