/**
 * MIT License
 *
 * Copyright (c) 2015  Rob Terpilowski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.sumzerotrading.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.*;

/**
 * @author Rob Terpilowski
 *
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class Ticker implements Serializable {

    public static final long serialVersionUID = 1L;
    protected String symbol;
    protected Exchange exchange;
    protected Exchange primaryExchange;
    protected String currency = "USD";
    protected transient DecimalFormat decimalFormat = new DecimalFormat("00");
    protected BigDecimal minimumTickSize = new BigDecimal("0.01");
    protected BigDecimal contractMultiplier;
    protected BigDecimal orderSizeIncrement = new BigDecimal("1");

    public Ticker() {
    }

    public Ticker(String symbol) {
        this.symbol = symbol;
    }

    public String getCurrency() {
        return currency;
    }

    public Ticker setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public Ticker setExchange(Exchange exchange) {
        this.exchange = exchange;
        return this;
    }

    public abstract InstrumentType getInstrumentType();

    public String getSymbol() {
        return symbol;
    }

    public Ticker setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public BigDecimal getMinimumTickSize() {
        return minimumTickSize;
    }

    public Ticker setMinimumTickSize(BigDecimal minimumTickSize) {
        this.minimumTickSize = minimumTickSize;
        return this;
    }

    public BigDecimal getContractMultiplier() {
        return contractMultiplier;
    }

    public Ticker setContractMultiplier(BigDecimal contractMultiplier) {
        this.contractMultiplier = contractMultiplier;
        return this;
    }

    public boolean supportsHalfTick() {
        return false;
    }

    protected String padMonth(int month) {
        if (month < 10) {
            return "0" + month;
        } else {
            return Integer.toString(month);
        }
    }

    public DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }

    public Ticker setDecimalFormat(DecimalFormat decimalFormat) {
        this.decimalFormat = decimalFormat;
        return this;
    }

    public Exchange getPrimaryExchange() {
        return primaryExchange;
    }

    public Ticker setPrimaryExchange(Exchange primaryExchange) {
        this.primaryExchange = primaryExchange;
        return this;
    }

    public BigDecimal getOrderSizeIncrement() {
        return orderSizeIncrement;
    }

    public Ticker setOrderSizeIncrement(BigDecimal orderSizeIncrement) {
        this.orderSizeIncrement = orderSizeIncrement;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.symbol != null ? this.symbol.hashCode() : 0);
        hash = 29 * hash + (this.exchange != null ? this.exchange.hashCode() : 0);
        hash = 29 * hash + (this.primaryExchange != null ? this.primaryExchange.hashCode() : 0);
        hash = 29 * hash + (this.currency != null ? this.currency.hashCode() : 0);
        hash = 29 * hash + (this.minimumTickSize != null ? this.minimumTickSize.hashCode() : 0);
        hash = 29 * hash + (this.contractMultiplier != null ? this.contractMultiplier.hashCode() : 0);
        hash = 29 * hash + (this.orderSizeIncrement != null ? this.orderSizeIncrement.hashCode() : 0);
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
        final Ticker other = (Ticker) obj;
        if ((this.symbol == null) ? (other.symbol != null) : !this.symbol.equals(other.symbol)) {
            return false;
        }
        if (this.exchange != other.exchange && (this.exchange == null || !this.exchange.equals(other.exchange))) {
            return false;
        }
        if (this.primaryExchange != other.primaryExchange
                && (this.primaryExchange == null || !this.primaryExchange.equals(other.primaryExchange))) {
            return false;
        }
        if ((this.currency == null) ? (other.currency != null) : !this.currency.equals(other.currency)) {
            return false;
        }
        if (this.minimumTickSize != other.minimumTickSize
                && (this.minimumTickSize == null || !this.minimumTickSize.equals(other.minimumTickSize))) {
            return false;
        }
        if (this.contractMultiplier != other.contractMultiplier
                && (this.contractMultiplier == null || !this.contractMultiplier.equals(other.contractMultiplier))) {
            return false;
        }
        if (this.orderSizeIncrement != other.orderSizeIncrement
                && (this.orderSizeIncrement == null || !this.orderSizeIncrement.equals(other.orderSizeIncrement))) {
            return false;
        }
        return true;
    }

    /**
     * Custom deserialization method to reinitialize transient fields.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Reinitialize transient decimalFormat field
        decimalFormat = new DecimalFormat("00");
    }

    @Override
    public String toString() {
        String decimalFormatInfo = (decimalFormat != null) ? String.valueOf(decimalFormat.getGroupingSize()) : "null";
        return "Ticker{" + "symbol=" + symbol + ", exchange=" + exchange + ", primaryExchange=" + primaryExchange
                + ", currency=" + currency + ", decimalFormat=" + decimalFormatInfo + ", minimumTickSize="
                + minimumTickSize + ", contractMultiplier=" + contractMultiplier + ", orderSizeIncrement="
                + orderSizeIncrement + '}';
    }

}
