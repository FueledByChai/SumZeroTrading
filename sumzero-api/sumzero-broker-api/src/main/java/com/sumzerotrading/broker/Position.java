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

package com.sumzerotrading.broker;

import java.io.Serializable;
import java.math.BigDecimal;

import com.sumzerotrading.data.Ticker;

/**
 * Defines a Position held at the broker.
 * 
 * @author RobTerpilowski
 */
public class Position implements Serializable {

    public static final long serialVersionUID = 1L;

    protected Ticker ticker;
    protected BigDecimal size;
    protected BigDecimal averageCost;
    protected BigDecimal liquidationPrice;

    /**
     * The ticker and the position size. Negative position sizes indicate a short
     * position
     * 
     * @param ticker      The ticker held.
     * @param size        The size of the position
     * @param averageCost the average price the position was acquired at.
     */
    public Position(Ticker ticker, BigDecimal size, BigDecimal averageCost) {
        this.ticker = ticker;
        this.size = size;
        this.averageCost = averageCost;
    }

    public Position(Ticker ticker) {
        this.ticker = ticker;
        this.size = BigDecimal.ZERO;
        this.averageCost = BigDecimal.ZERO;
    }

    public Position setSize(BigDecimal size) {
        this.size = size;
        return this;
    }

    public Position setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
        return this;
    }

    public Position setLiquidationPrice(BigDecimal liquidationPrice) {
        this.liquidationPrice = liquidationPrice;
        return this;
    }

    /**
     * Get the ticker for this position
     * 
     * @return The ticker for this position
     */
    public Ticker getTicker() {
        return ticker;
    }

    /**
     * Gets the size of the position. Negative sizes indicate short positions.
     * 
     * @return The position size.
     */
    public BigDecimal getSize() {
        return size;
    }

    /**
     * Gets the average price this position was acquired at.
     * 
     * @return The average price the position was acquired at.
     */
    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public BigDecimal getLiquidationPrice() {
        return liquidationPrice;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ticker == null) ? 0 : ticker.hashCode());
        result = prime * result + ((size == null) ? 0 : size.hashCode());
        result = prime * result + ((averageCost == null) ? 0 : averageCost.hashCode());
        result = prime * result + ((liquidationPrice == null) ? 0 : liquidationPrice.hashCode());
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
        Position other = (Position) obj;
        if (ticker == null) {
            if (other.ticker != null)
                return false;
        } else if (!ticker.equals(other.ticker))
            return false;
        if (size == null) {
            if (other.size != null)
                return false;
        } else if (!size.equals(other.size))
            return false;
        if (averageCost == null) {
            if (other.averageCost != null)
                return false;
        } else if (!averageCost.equals(other.averageCost))
            return false;
        if (liquidationPrice == null) {
            if (other.liquidationPrice != null)
                return false;
        } else if (!liquidationPrice.equals(other.liquidationPrice))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Position [ticker=" + ticker + ", size=" + size + ", averageCost=" + averageCost + ", liquidationPrice="
                + liquidationPrice + "]";
    }

}
