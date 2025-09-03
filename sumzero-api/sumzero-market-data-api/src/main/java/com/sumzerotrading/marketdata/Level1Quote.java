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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.sumzerotrading.marketdata;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;

import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;

/**
 * Implementation of an ILevel1 quote
 *
 * @author RobTerpilowski
 */
public class Level1Quote extends AbstractQuote implements ILevel1Quote {

    public Level1Quote(Ticker ticker) {
        super(ticker, ZonedDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Builds a new Level1Quote
     *
     * @param ticker    The ticker this quote is for
     * @param type      The type of quote (bid/ask/last/open/etc)
     * @param timeStamp The time of the quote
     * @param value     The price (or volume) of this quote
     */
    public Level1Quote(Ticker ticker, ZonedDateTime timestamp, Map<QuoteType, BigDecimal> quoteValues) {
        super(ticker, timestamp);
        this.quoteMap = quoteValues;
    }

    @Override
    public QuoteType[] getTypes() {
        return quoteMap.keySet().toArray(new QuoteType[] {});
    }

    @Override
    public boolean containsType(QuoteType type) {
        return quoteMap.containsKey(type);
    }

    @Override
    public BigDecimal getValue(QuoteType type) {
        if (containsType(type)) {
            return quoteMap.get(type);
        } else {
            throw new SumZeroException("Quote does not contain type: " + type);
        }

    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.quoteMap);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Level1Quote other = (Level1Quote) obj;
        if (!Objects.equals(this.quoteMap, other.quoteMap)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Level1Quote [ticker=" + ticker + ", timeStamp=" + timeStamp + ", quoteMap=" + quoteMap + "]";
    }

}
