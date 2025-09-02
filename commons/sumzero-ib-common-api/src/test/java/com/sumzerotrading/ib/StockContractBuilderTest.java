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

package com.sumzerotrading.ib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ib.client.Contract;
import com.sumzerotrading.data.Exchange;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;

/**
 *
 * @author Rob Terpilowski
 */
public class StockContractBuilderTest {

    public StockContractBuilderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testBuildContract() {
        Ticker ticker = getTicker();
        StockContractBuilder builder = new StockContractBuilder();
        Contract contract = builder.buildContract(ticker);

        assertEquals(ticker.getCurrency(), contract.currency());
        assertEquals(ticker.getExchange().getExchangeName(), contract.exchange());
        assertEquals(ticker.getSymbol(), contract.symbol());
        assertNull(contract.primaryExch());
    }

    @Test
    public void testBuildContract_primaryExchange() {
        Ticker ticker = getTicker();
        ticker.setPrimaryExchange(Exchange.NASDAQ);
        StockContractBuilder builder = new StockContractBuilder();
        Contract contract = builder.buildContract(ticker);

        assertEquals(ticker.getCurrency(), contract.currency());
        assertEquals(ticker.getExchange().getExchangeName(), contract.exchange());
        assertEquals(ticker.getSymbol(), contract.symbol());
        assertEquals(ticker.getPrimaryExchange().getExchangeName(), contract.primaryExch());
    }

    protected Ticker getTicker() {
        Ticker ticker = new Ticker("SBUX").setInstrumentType(InstrumentType.STOCK);
        ticker.setCurrency("USD");
        ticker.setExchange(Exchange.INTERACTIVE_BROKERS_SMART);
        return ticker;
    }
}
