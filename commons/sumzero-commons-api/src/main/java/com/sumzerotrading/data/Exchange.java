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

import java.io.Serializable;

/**
 *
 * @author Rob Terpilowski
 */
public class Exchange implements Serializable {

    public static final long serialVersionUID = 1L;

    public static final Exchange ARCA = new Exchange("ARCA");
    public static final Exchange GLOBEX = new Exchange("GLOBEX");
    public static final Exchange NYMEX = new Exchange("NYMEX");
    public static final Exchange CBOE = new Exchange("CBOE");
    public static final Exchange ECBOT = new Exchange("ECBOT");
    public static final Exchange NYBOT = new Exchange("NYBOT");
    public static final Exchange CFE = new Exchange("CFE");
    public static final Exchange NYSE_LIFFE = new Exchange("NYSELIFFE");
    public static final Exchange IDEALPRO = new Exchange("IDEALPRO");
    public static final Exchange PSE = new Exchange("PSE");
    public static final Exchange INTERACTIVE_BROKERS_SMART = new Exchange("SMART");
    public static final Exchange NASDAQ = new Exchange("NASDAQ");
    public static final Exchange TSEJ = new Exchange("TSEJ");
    public static final Exchange SEHKNTL = new Exchange("SEHKNTL");
    public static final Exchange SEHK = new Exchange("SEHK");
    public static final Exchange HKFE = new Exchange("HKFE");
    public static final Exchange OSE = new Exchange("OSE.JPN");
    public static final Exchange SGX = new Exchange("SGX");
    public static final Exchange BOX = new Exchange("BOX");
    public static final Exchange DYDX = new Exchange("DYDX");
    public static final Exchange HYPERLIQUID = new Exchange("HYPERLIQUID");
    public static final Exchange PARADEX = new Exchange("PARADEX");

    public static final Exchange[] ALL_EXCHANGES = { ARCA, GLOBEX, NYMEX, CBOE, ECBOT, NYBOT, CFE, NYSE_LIFFE, IDEALPRO,
            PSE, INTERACTIVE_BROKERS_SMART, NASDAQ, TSEJ, SEHKNTL, SEHK, HKFE, OSE, SGX, BOX, DYDX, HYPERLIQUID,
            PARADEX };

    protected String exchangeName;

    protected Exchange(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public static Exchange getExchangeFromString(String exchangeString) {
        for (Exchange exchange : ALL_EXCHANGES) {
            if (exchangeString.equals(exchange.getExchangeName())) {
                return exchange;
            }
        }

        throw new SumZeroException("Unknown exchange " + exchangeString);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.exchangeName != null ? this.exchangeName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Exchange other = (Exchange) obj;
        if ((this.exchangeName == null) ? (other.exchangeName != null)
                : !this.exchangeName.equals(other.exchangeName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Exchange{" + "exchangeName=" + exchangeName + '}';
    }

}
