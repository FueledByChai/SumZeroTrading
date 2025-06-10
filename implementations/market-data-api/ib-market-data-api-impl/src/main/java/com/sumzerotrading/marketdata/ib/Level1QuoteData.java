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

package com.sumzerotrading.marketdata.ib;

import com.ib.client.Decimal;
import com.sumzerotrading.data.Ticker;

/**
 *
 * @author Rob Terpilowski
 */
public class Level1QuoteData {
    protected Ticker ticker;
    protected int field;
    protected double price = 0;
    protected boolean canAutoExecute = false;
    protected Decimal size = Decimal.ZERO;

    public Level1QuoteData(Ticker ticker, int field, double price, boolean canAutoExecute, Decimal size) {
        this.ticker = ticker;
        this.field = field;
        this.price = price;
        this.canAutoExecute = canAutoExecute;
        this.size = size;
    }

    public boolean getCanAutoExecute() {
        return canAutoExecute;
    }

    public int getField() {
        return field;
    }

    public double getPrice() {
        return price;
    }

    public Ticker getTicker() {
        return ticker;
    }

    public Decimal getSize() {
        return size;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ticker == null) ? 0 : ticker.hashCode());
        result = prime * result + field;
        long temp;
        temp = Double.doubleToLongBits(price);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + (canAutoExecute ? 1231 : 1237);
        result = prime * result + ((size == null) ? 0 : size.hashCode());
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
        Level1QuoteData other = (Level1QuoteData) obj;
        if (ticker == null) {
            if (other.ticker != null)
                return false;
        } else if (!ticker.equals(other.ticker))
            return false;
        if (field != other.field)
            return false;
        if (Double.doubleToLongBits(price) != Double.doubleToLongBits(other.price))
            return false;
        if (canAutoExecute != other.canAutoExecute)
            return false;
        if (size == null) {
            if (other.size != null)
                return false;
        } else if (!size.equals(other.size))
            return false;
        return true;
    }

}
