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
package com.limituptrading.util;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author RobTerpilowski
 */
public class FuturesUtilTest {
    
    public FuturesUtilTest() {
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
    public void testGetContractMonthSymbol() {
        assertEquals( "F", FuturesUtil.getContractMonthSymbol(1) );
        assertEquals( "G", FuturesUtil.getContractMonthSymbol(2) );
        assertEquals( "H", FuturesUtil.getContractMonthSymbol(3) );
        assertEquals( "J", FuturesUtil.getContractMonthSymbol(4) );
        assertEquals( "K", FuturesUtil.getContractMonthSymbol(5) );
        assertEquals( "M", FuturesUtil.getContractMonthSymbol(6) );
        assertEquals( "N", FuturesUtil.getContractMonthSymbol(7) );
        assertEquals( "Q", FuturesUtil.getContractMonthSymbol(8) );
        assertEquals( "U", FuturesUtil.getContractMonthSymbol(9) );
        assertEquals( "V", FuturesUtil.getContractMonthSymbol(10) );
        assertEquals( "X", FuturesUtil.getContractMonthSymbol(11) );
        assertEquals( "Z", FuturesUtil.getContractMonthSymbol(12) );
        
        try {
            FuturesUtil.getContractMonthSymbol(0);
            fail();
        } catch( IllegalStateException ex ) {
            //this should happen
        }
    }
}
