/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 * @author RobTerpilowski
 */
public class CFDContractBuilderTest {

    public CFDContractBuilderTest() {
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
    public void testBuildContract() {
        Ticker ticker = getTicker();
        CFDContractBuilder builder = new CFDContractBuilder();
        Contract contract = builder.buildContract(ticker);

        assertEquals(ticker.getCurrency(), contract.currency());
        assertEquals(ticker.getExchange().getExchangeName(), contract.exchange());
        assertEquals(ticker.getSymbol(), contract.symbol());
        assertEquals(InstrumentType.CFD, ticker.getInstrumentType());
        assertNull(contract.primaryExch());
    }

    @Test
    public void testBuildContract_primaryExchange() {
        Ticker ticker = getTicker();
        ticker.setPrimaryExchange(Exchange.NASDAQ);
        CFDContractBuilder builder = new CFDContractBuilder();
        Contract contract = builder.buildContract(ticker);

        assertEquals(ticker.getCurrency(), contract.currency());
        assertEquals(ticker.getExchange().getExchangeName(), contract.exchange());
        assertEquals(ticker.getSymbol(), contract.symbol());
        assertEquals(InstrumentType.CFD, ticker.getInstrumentType());
        assertEquals(ticker.getPrimaryExchange().getExchangeName(), contract.primaryExch());
    }

    protected Ticker getTicker() {
        Ticker ticker = new Ticker("SBUX").setInstrumentType(InstrumentType.CFD);
        ticker.setCurrency("USD");
        ticker.setExchange(Exchange.INTERACTIVE_BROKERS_SMART);
        return ticker;
    }
}
