package com.sumzerotrading.marketdata;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.sumzerotrading.data.Ticker;

public class OrderFlow {

    public enum Side {
        BUY, SELL
    }

    protected Ticker ticker;
    protected BigDecimal price;
    protected BigDecimal size;
    protected Side side;
    protected ZonedDateTime timestamp;

    public OrderFlow(Ticker ticker, BigDecimal price, BigDecimal size, Side side, ZonedDateTime timestamp) {
        this.ticker = ticker;
        this.price = price;
        this.size = size;
        this.side = side;
        this.timestamp = timestamp;
    }

    public Ticker getTicker() {
        return ticker;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getSize() {
        return size;
    }

    public Side getSide() {
        return side;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ticker == null) ? 0 : ticker.hashCode());
        result = prime * result + ((price == null) ? 0 : price.hashCode());
        result = prime * result + ((size == null) ? 0 : size.hashCode());
        result = prime * result + ((side == null) ? 0 : side.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
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
        OrderFlow other = (OrderFlow) obj;
        if (ticker == null) {
            if (other.ticker != null)
                return false;
        } else if (!ticker.equals(other.ticker))
            return false;
        if (price == null) {
            if (other.price != null)
                return false;
        } else if (!price.equals(other.price))
            return false;
        if (size == null) {
            if (other.size != null)
                return false;
        } else if (!size.equals(other.size))
            return false;
        if (side != other.side)
            return false;
        if (timestamp == null) {
            if (other.timestamp != null)
                return false;
        } else if (!timestamp.equals(other.timestamp))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TradeFlow [ticker=" + ticker + ", price=" + price + ", size=" + size + ", side=" + side + ", timestamp="
                + timestamp + "]";
    }

}
