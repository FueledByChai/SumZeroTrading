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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents one bar of market data.
 *
 * @version $Revision: 1.3 $
 * @author Rob Terpilowski
 */
public class BarData implements Serializable {

    public static long serialVersionUID = 1L;

    public static final double NULL = -9D;
    public static final int OPEN = 1;
    public static final int HIGH = 2;
    public static final int LOW = 3;
    public static final int CLOSE = 4;

    public enum LengthUnit {

        TICK, SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR
    };

    protected Ticker ticker;
    protected BigDecimal open;
    protected BigDecimal formattedOpen;
    protected BigDecimal high;
    protected BigDecimal formattedHigh;
    protected BigDecimal low;
    protected BigDecimal formattedLow;
    protected BigDecimal close;
    protected BigDecimal formattedClose;
    protected BigDecimal volume = BigDecimal.ZERO;
    protected long openInterest = 0;
    protected int barLength = 1;
    protected LocalDateTime dateTime;
    protected LengthUnit lengthUnit;
    // protected Logger logger = Logger.getLogger( BarData.class );

    public BarData() {
    }

    public BarData(Ticker ticker, LocalDateTime dateTime, BigDecimal open, BigDecimal high, BigDecimal low,
            BigDecimal close, BigDecimal volume, int barLength, LengthUnit lengthUnit) {
        this.ticker = ticker;
        this.dateTime = dateTime;
        this.open = open;
        this.close = close;
        this.low = low;
        this.high = high;
        this.volume = volume;
        this.barLength = barLength;
        this.lengthUnit = lengthUnit;
    }

    /**
     * Creates a new instance of a Bar
     *
     * @param date   The date of this bar.
     * @param open   The open price.
     * @param high   The high price.
     * @param low    The low price.
     * @param close  The closing price.
     * @param volume The volume for the bar.
     */
    public BarData(LocalDateTime dateTime, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close,
            BigDecimal volume) {
        this(null, dateTime, open, high, low, close, volume, 1, LengthUnit.DAY);
    }// constructor()

    /**
     * Creates a new instance of a Bar
     *
     * @param date         The date of this bar.
     * @param open         The open price.
     * @param high         The high price.
     * @param low          The low price.
     * @param close        The closing price.
     * @param volume       The volume for the bar.
     * @param openInterest The open interest for the bar.
     */
    public BarData(LocalDateTime dateTime, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close,
            BigDecimal volume, long openInterest) {
        this(dateTime, open, high, low, close, volume);
        this.openInterest = openInterest;
    }// constructor()

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * @return the open price of this bar.
     */
    public BigDecimal getOpen() {
        return open;
    }

    /**
     * @return the High price of this bar.
     */
    public BigDecimal getHigh() {
        return high;
    }

    /*
     * @return the Low price of this BarData.
     */
    public BigDecimal getLow() {
        return low;
    }

    /**
     * @return the close price for this bar.
     */
    public BigDecimal getClose() {
        return close;
    }

    /**
     * @return the Volume for this bar.
     */
    public BigDecimal getVolume() {
        return volume;
    }

    /**
     * @return the open interest for this bar.
     */
    public long getOpenInterest() {
        return openInterest;
    }

    /**
     * Sets the open price for this bar.
     *
     * @param open The open price for this bar.
     */
    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    /**
     * Sets the high price for this bar.
     *
     * @param high The high price for this bar.
     */
    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    /**
     * Sets the low price for this bar.
     *
     * @param low The low price for this bar.
     */
    public void setLow(BigDecimal low) {
        this.low = low;
    }

    /**
     * Sets the closing price for this bar.
     *
     * @param close The closing price for this bar.
     */
    public void setClose(BigDecimal close) {
        this.close = close;
    }

    /**
     * Sets the volume for this bar.
     *
     * @param volume Sets the volume for this bar.
     */
    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    /**
     * Updates the last price, adjusting the high and low
     * 
     * @param close The last price
     */
    public void update(BigDecimal close) {
        if (close.doubleValue() > high.doubleValue()) {
            high = close;
        }

        if (close.doubleValue() < low.doubleValue()) {
            low = close;
        }
        this.close = close;
    }

    /**
     * Sets the open interest for this bar.
     *
     * @param openInterest The open interest for this bar.
     */
    public void setOpenInterest(long openInterest) {
        this.openInterest = openInterest;
    }

    public Ticker getTicker() {
        return ticker;
    }

    public int getBarLength() {
        return barLength;
    }

    public LengthUnit getLengthUnit() {
        return lengthUnit;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.ticker);
        hash = 59 * hash + Objects.hashCode(this.open);
        hash = 59 * hash + Objects.hashCode(this.formattedOpen);
        hash = 59 * hash + Objects.hashCode(this.high);
        hash = 59 * hash + Objects.hashCode(this.formattedHigh);
        hash = 59 * hash + Objects.hashCode(this.low);
        hash = 59 * hash + Objects.hashCode(this.formattedLow);
        hash = 59 * hash + Objects.hashCode(this.close);
        hash = 59 * hash + Objects.hashCode(this.formattedClose);
        hash = 59 * hash + Objects.hashCode(this.volume);
        hash = 59 * hash + (int) (this.openInterest ^ (this.openInterest >>> 32));
        hash = 59 * hash + this.barLength;
        hash = 59 * hash + Objects.hashCode(this.dateTime);
        hash = 59 * hash + Objects.hashCode(this.lengthUnit);
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
        final BarData other = (BarData) obj;
        if (this.openInterest != other.openInterest) {
            return false;
        }
        if (this.barLength != other.barLength) {
            return false;
        }
        if (!Objects.equals(this.ticker, other.ticker)) {
            return false;
        }
        if (!Objects.equals(this.open, other.open)) {
            return false;
        }
        if (!Objects.equals(this.formattedOpen, other.formattedOpen)) {
            return false;
        }
        if (!Objects.equals(this.high, other.high)) {
            return false;
        }
        if (!Objects.equals(this.formattedHigh, other.formattedHigh)) {
            return false;
        }
        if (!Objects.equals(this.low, other.low)) {
            return false;
        }
        if (!Objects.equals(this.formattedLow, other.formattedLow)) {
            return false;
        }
        if (!Objects.equals(this.close, other.close)) {
            return false;
        }
        if (!Objects.equals(this.formattedClose, other.formattedClose)) {
            return false;
        }
        if (!Objects.equals(this.volume, other.volume)) {
            return false;
        }
        if (!Objects.equals(this.dateTime, other.dateTime)) {
            return false;
        }
        if (this.lengthUnit != other.lengthUnit) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Bar{" + "ticker=" + ticker + ", open=" + open + ", formattedOpen=" + formattedOpen + ", high=" + high
                + ", formattedHigh=" + formattedHigh + ", low=" + low + ", formattedLow=" + formattedLow + ", close="
                + close + ", formattedClose=" + formattedClose + ", volume=" + volume + ", openInterest=" + openInterest
                + ", barLength=" + barLength + ", dateTime=" + dateTime + ", lengthUnit=" + lengthUnit + '}';
    }

}// class BarData
