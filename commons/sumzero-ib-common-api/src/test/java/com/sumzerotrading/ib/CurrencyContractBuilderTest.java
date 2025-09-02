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

import java.math.BigDecimal;

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
public class CurrencyContractBuilderTest {

    public CurrencyContractBuilderTest() {
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
    public void testCurrencyContractBuilder() {
        CurrencyContractBuilder builder = new CurrencyContractBuilder();
        Ticker currencyTicker = new Ticker().setInstrumentType(InstrumentType.CURRENCY);

        currencyTicker.setContractMultiplier(BigDecimal.ONE);
        currencyTicker.setCurrency("USD");
        currencyTicker.setExchange(Exchange.IDEALPRO);
        currencyTicker.setMinimumTickSize(new BigDecimal("0.0001"));
        currencyTicker.setSymbol("EUR");

        Contract contract = builder.buildContract(currencyTicker);

        assertEquals("USD", contract.currency());
        assertEquals("IDEALPRO", contract.exchange());
        // assertEquals( "CASH", contract.secType() );
        // TODO: FIX UNIT TEST
        assertEquals("EUR", contract.symbol());

    }
}
