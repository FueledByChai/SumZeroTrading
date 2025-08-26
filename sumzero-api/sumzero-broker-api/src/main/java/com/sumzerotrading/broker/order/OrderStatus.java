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

package com.sumzerotrading.broker.order;

import com.sumzerotrading.data.Ticker;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 *
 * @author Rob Terpilowski
 */
public class OrderStatus implements Serializable {

    public static long serialVersionUID = 1L;

    public enum Status {
        NEW, PARTIAL_FILL, PENDING_CANCEL, REJECTED, FILLED, CANCELED, REPLACED, UNKNOWN
    };

    public enum CancelReason {
        NONE, POST_ONLY_WOULD_CROSS, USER_CANCELED;
    }

    protected Status status;
    protected String orderId;
    protected String oldOrderid;
    protected BigDecimal filled;
    protected BigDecimal remaining;
    protected BigDecimal fillPrice;
    protected Ticker ticker;
    protected ZonedDateTime timestamp;

    public OrderStatus(Status status, String oldOrderId, String orderId, Ticker ticker, ZonedDateTime timestamp) {
        this.status = status;
        this.oldOrderid = oldOrderId;
        this.orderId = orderId;
        this.ticker = ticker;
        this.timestamp = timestamp;
    }

    public OrderStatus(Status status, String orderId, BigDecimal filled, BigDecimal remaining, BigDecimal fillPrice,
            Ticker ticker, ZonedDateTime timestamp) {
        this.status = status;
        this.orderId = orderId;
        this.filled = filled;
        this.remaining = remaining;
        this.fillPrice = fillPrice;
        this.ticker = ticker;
        this.timestamp = timestamp;
    }

    public OrderStatus(Status status, String originalOrderId, String orderId, BigDecimal filled, BigDecimal remaining,
            BigDecimal fillPrice, Ticker ticker, ZonedDateTime timestamp) {
        this.status = status;
        this.orderId = orderId;
        this.oldOrderid = originalOrderId;
        this.filled = filled;
        this.remaining = remaining;
        this.fillPrice = fillPrice;
        this.ticker = ticker;
        this.timestamp = timestamp;
    }

    public BigDecimal getFilled() {
        return filled;
    }

    public BigDecimal getRemaining() {
        return remaining;
    }

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getFillPrice() {
        return fillPrice;
    }

    public Ticker getTicker() {
        return ticker;
    }

    public Status getStatus() {
        return status;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public String getOldOrderid() {
        return oldOrderid;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.status != null ? this.status.hashCode() : 0);
        hash = 97 * hash + (this.orderId != null ? this.orderId.hashCode() : 0);
        hash = 97 * hash + (this.oldOrderid != null ? this.oldOrderid.hashCode() : 0);
        hash = 97 * hash + (this.filled != null ? this.filled.hashCode() : 0);
        hash = 97 * hash + (this.remaining != null ? this.remaining.hashCode() : 0);
        hash = 97 * hash + (this.fillPrice != null ? this.fillPrice.hashCode() : 0);
        hash = 97 * hash + (this.ticker != null ? this.ticker.hashCode() : 0);
        hash = 97 * hash + (this.timestamp != null ? this.timestamp.hashCode() : 0);
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
        final OrderStatus other = (OrderStatus) obj;
        if (this.status != other.status) {
            return false;
        }
        if ((this.orderId == null) ? (other.orderId != null) : !this.orderId.equals(other.orderId)) {
            return false;
        }
        if ((this.oldOrderid == null) ? (other.oldOrderid != null) : !this.oldOrderid.equals(other.oldOrderid)) {
            return false;
        }
        if (this.filled != other.filled) {
            return false;
        }
        if (this.remaining != other.remaining) {
            return false;
        }
        if (this.fillPrice != other.fillPrice && (this.fillPrice == null || !this.fillPrice.equals(other.fillPrice))) {
            return false;
        }
        if (this.ticker != other.ticker && (this.ticker == null || !this.ticker.equals(other.ticker))) {
            return false;
        }
        if (this.timestamp != other.timestamp && (this.timestamp == null || !this.timestamp.equals(other.timestamp))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "OrderStatus{" + "status=" + status + ", orderId=" + orderId + ", oldOrderid=" + oldOrderid + ", filled="
                + filled + ", remaining=" + remaining + ", fillPrice=" + fillPrice + ", ticker=" + ticker
                + ", timestamp=" + timestamp + '}';
    }

}
