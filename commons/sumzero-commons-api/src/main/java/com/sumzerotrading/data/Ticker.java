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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * @author Rob Terpilowski
 *
 * 
 */
public class Ticker implements Serializable {

    public static final long serialVersionUID = 1L;

    public enum Right {
        CALL, PUT, NONE
    }

    protected String id;
    protected InstrumentType instrumentType;
    protected String symbol;
    protected Exchange exchange;
    protected Exchange primaryExchange;
    protected String currency = "USD";
    protected transient DecimalFormat decimalFormat = new DecimalFormat("00");
    protected BigDecimal minimumTickSize = BigDecimal.valueOf(0.01);
    protected BigDecimal contractMultiplier = BigDecimal.ONE;
    protected BigDecimal orderSizeIncrement = new BigDecimal("1");
    protected BigDecimal minimumOrderSize = null;
    protected int expiryMonth = 0;
    protected int expiryYear = 0;
    protected int expiryDay = 0;
    protected BigDecimal strike = null;
    protected Right right = Right.NONE;
    protected int fundingRateInterval = 0;

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

    /**
     * Formats a price according to the minimum tick size precision. For example, if
     * minimumTickSize is 0.001 and price is 1.25, returns 1.250
     */
    public BigDecimal formatPrice(BigDecimal price) {
        if (minimumTickSize == null || price == null) {
            return price;
        }

        // Calculate the scale (number of decimal places) from the minimum tick size
        int scale = minimumTickSize.scale();

        // Set the price to the same scale as the minimum tick size
        return price.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * Convenience method to format a price string according to the minimum tick
     * size precision.
     */
    public BigDecimal formatPrice(String priceString) {
        if (priceString == null) {
            return null;
        }
        BigDecimal price = new BigDecimal(priceString);
        return formatPrice(price);
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

    public Right getRight() {
        return right;
    }

    public Ticker setRight(Right right) {
        this.right = right;
        return this;
    }

    public InstrumentType getInstrumentType() {
        return instrumentType;
    }

    public Ticker setInstrumentType(InstrumentType instrumentType) {
        this.instrumentType = instrumentType;
        return this;
    }

    public int getExpiryMonth() {
        return expiryMonth;
    }

    public Ticker setExpiryMonth(int expiryMonth) {
        this.expiryMonth = expiryMonth;
        return this;
    }

    public int getExpiryYear() {
        return expiryYear;
    }

    public Ticker setExpiryYear(int expiryYear) {
        this.expiryYear = expiryYear;
        return this;
    }

    public int getExpiryDay() {
        return expiryDay;
    }

    public Ticker setExpiryDay(int expiryDay) {
        this.expiryDay = expiryDay;
        return this;
    }

    public BigDecimal getStrike() {
        return strike;
    }

    public Ticker setStrike(BigDecimal strike) {
        this.strike = strike;
        return this;
    }

    public int getFundingRateInterval() {
        return fundingRateInterval;
    }

    public Ticker setFundingRateInterval(int fundingRateInterval) {
        this.fundingRateInterval = fundingRateInterval;
        return this;
    }

    public String getId() {
        return id;
    }

    public int getIdAsInt() {
        if (id != null) {
            try {
                return Integer.parseInt(id);
            } catch (NumberFormatException nfe) {
                return 0;
            }
        }
        return 0;
    }

    public Ticker setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((instrumentType == null) ? 0 : instrumentType.hashCode());
        result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
        result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
        result = prime * result + ((primaryExchange == null) ? 0 : primaryExchange.hashCode());
        result = prime * result + ((currency == null) ? 0 : currency.hashCode());
        result = prime * result + ((minimumTickSize == null) ? 0 : minimumTickSize.hashCode());
        result = prime * result + ((contractMultiplier == null) ? 0 : contractMultiplier.hashCode());
        result = prime * result + ((orderSizeIncrement == null) ? 0 : orderSizeIncrement.hashCode());
        result = prime * result + ((minimumOrderSize == null) ? 0 : minimumOrderSize.hashCode());
        result = prime * result + expiryMonth;
        result = prime * result + expiryYear;
        result = prime * result + expiryDay;
        result = prime * result + ((strike == null) ? 0 : strike.hashCode());
        result = prime * result + ((right == null) ? 0 : right.hashCode());
        result = prime * result + fundingRateInterval;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Ticker other = (Ticker) obj;
        if (instrumentType != other.instrumentType)
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (exchange == null) {
            if (other.exchange != null)
                return false;
        } else if (!exchange.equals(other.exchange))
            return false;
        if (primaryExchange == null) {
            if (other.primaryExchange != null)
                return false;
        } else if (!primaryExchange.equals(other.primaryExchange))
            return false;
        if (currency == null) {
            if (other.currency != null)
                return false;
        } else if (!currency.equals(other.currency))
            return false;
        if (minimumTickSize == null) {
            if (other.minimumTickSize != null)
                return false;
        } else if (!minimumTickSize.equals(other.minimumTickSize))
            return false;
        if (contractMultiplier == null) {
            if (other.contractMultiplier != null)
                return false;
        } else if (!contractMultiplier.equals(other.contractMultiplier))
            return false;
        if (orderSizeIncrement == null) {
            if (other.orderSizeIncrement != null)
                return false;
        } else if (!orderSizeIncrement.equals(other.orderSizeIncrement))
            return false;
        if (minimumOrderSize == null) {
            if (other.minimumOrderSize != null)
                return false;
        } else if (!minimumOrderSize.equals(other.minimumOrderSize))
            return false;
        if (expiryMonth != other.expiryMonth)
            return false;
        if (expiryYear != other.expiryYear)
            return false;
        if (right != other.right)
            return false;
        if (expiryDay != other.expiryDay)
            return false;
        if (strike == null) {
            if (other.strike != null)
                return false;
        } else if (!strike.equals(other.strike))
            return false;
        if (fundingRateInterval != other.fundingRateInterval)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Ticker [id=" + id + ", instrumentType=" + instrumentType + ", symbol=" + symbol + ", exchange="
                + exchange + ", primaryExchange=" + primaryExchange + ", currency=" + currency + ", minimumTickSize="
                + minimumTickSize + ", contractMultiplier=" + contractMultiplier + ", orderSizeIncrement="
                + orderSizeIncrement + ", minimumOrderSize=" + minimumOrderSize + ", expiryMonth=" + expiryMonth
                + ", expiryYear=" + expiryYear + ", expiryDay=" + expiryDay + ", strike=" + strike + ", right=" + right
                + ", fundingRateInterval=" + fundingRateInterval + "]";
    }

}
