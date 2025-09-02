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

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sumzerotrading.data.ComboTicker;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;

/**
 *
 * @author Rob Terpilowski
 */
public class ContractBuilderFactoryTest {

    public ContractBuilderFactoryTest() {
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
    public void testGetContractBuilder() {
        Ticker stockTicker = new Ticker("ABC").setInstrumentType(InstrumentType.STOCK);
        Ticker currencyTicker = new Ticker().setInstrumentType(InstrumentType.CURRENCY);
        Ticker futurTicker = new Ticker().setInstrumentType(InstrumentType.FUTURES);
        Ticker optionsTicker = new Ticker("QQQ").setInstrumentType(InstrumentType.OPTION);
        Ticker combTicker = new ComboTicker(futurTicker, stockTicker).setInstrumentType(InstrumentType.COMBO);
        Ticker indexTicker = new Ticker().setInstrumentType(InstrumentType.INDEX);
        Ticker cfdTicker = new Ticker("XYZ").setInstrumentType(InstrumentType.CFD);

        assertTrue(ContractBuilderFactory.getContractBuilder(stockTicker) instanceof StockContractBuilder);
        assertTrue(ContractBuilderFactory.getContractBuilder(currencyTicker) instanceof CurrencyContractBuilder);
        assertTrue(ContractBuilderFactory.getContractBuilder(futurTicker) instanceof FuturesContractBuilder);
        assertTrue(ContractBuilderFactory.getContractBuilder(combTicker) instanceof ComboContractBuilder);
        assertTrue(ContractBuilderFactory.getContractBuilder(indexTicker) instanceof IndexContractBuilder);
        assertTrue(ContractBuilderFactory.getContractBuilder(cfdTicker) instanceof CFDContractBuilder);
        assertTrue(ContractBuilderFactory.getContractBuilder(optionsTicker) instanceof OptionContractBuilder);

    }

}
