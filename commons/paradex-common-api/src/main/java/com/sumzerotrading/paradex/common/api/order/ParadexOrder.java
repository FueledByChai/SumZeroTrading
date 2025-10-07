package com.sumzerotrading.paradex.common.api.order;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;

import com.google.gson.annotations.SerializedName;

public class ParadexOrder implements Comparable<ParadexOrder> {

    @SerializedName("market")
    protected String ticker;
    protected Side side;
    protected BigDecimal size;
    @SerializedName("price")
    protected BigDecimal limitPrice;
    @SerializedName("type")
    protected OrderType orderType;
    protected ZonedDateTime submittedTime;
    protected ZonedDateTime filledAt;
    protected ZonedDateTime canceledAt;
    protected ZonedDateTime orderTTLExpiration;
    // This is used to track when the order was expired, not serialized in JSON

    protected long timeToLiveInMs = 0;

    @SerializedName("client_id")
    protected String clientId;

    protected BigDecimal stopPrice;

    @SerializedName("remaining_size")
    protected BigDecimal remainingSize;

    @SerializedName("id")
    protected String orderId;

    @SerializedName("instruction")
    protected OrderTIF timeInForce;

    @SerializedName("cancel_reason")
    protected String cancelReason;

    @SerializedName("last_updated_at")
    protected long lastUpdatedAt;

    public ParadexOrder() {
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public BigDecimal getSize() {
        return size;
    }

    public void setSize(BigDecimal size) {
        this.size = size;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public void setLimitPrice(BigDecimal limitPrice) {
        this.limitPrice = limitPrice;
    }

    public BigDecimal getStopPrice() {
        return stopPrice;
    }

    public void setStopPrice(BigDecimal stopPrice) {
        this.stopPrice = stopPrice;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public String getChainSide() {
        return side.getChainSide();
    }

    public OrderTIF getTimeInForce() {
        return timeInForce;
    }

    public void setTimeInForce(OrderTIF timeInForce) {
        this.timeInForce = timeInForce;
    }

    public BigInteger getChainLimitPrice() {
        if (limitPrice == null) {
            return BigInteger.ZERO;
        }
        return limitPrice.scaleByPowerOfTen(8).toBigInteger();
    }

    public BigInteger getChainStopPrice() {
        return stopPrice.scaleByPowerOfTen(8).toBigInteger();
    }

    public BigInteger getChainSize() {
        return size.scaleByPowerOfTen(8).toBigInteger();
    }

    public BigDecimal getRemainingSize() {
        return remainingSize;
    }

    public void setRemainingSize(BigDecimal remainingSize) {
        this.remainingSize = remainingSize;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public long getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(long lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public long getTimeToLiveInMs() {
        return timeToLiveInMs;
    }

    public void setTimeToLiveInMs(long timeToLiveInMs) {
        this.timeToLiveInMs = timeToLiveInMs;
        if (this.submittedTime != null && timeToLiveInMs > 0) {
            // Set the expiration time based on the TTL
            this.orderTTLExpiration = this.submittedTime.plus(timeToLiveInMs, java.time.temporal.ChronoUnit.MILLIS);
            this.orderTTLExpiration = this.orderTTLExpiration.withZoneSameInstant(java.time.ZoneId.of("UTC"));
        } else {
            this.orderTTLExpiration = null; // Clear it out if no TTL
        }
    }

    public ZonedDateTime getSubmittedTime() {
        return submittedTime;
    }

    public void setSubmittedTime(ZonedDateTime submittedTime) {
        this.submittedTime = submittedTime.withZoneSameInstant(java.time.ZoneId.of("UTC"));

        if (timeToLiveInMs > 0 && submittedTime != null) {
            // Set the expiration time based on the TTL
            this.orderTTLExpiration = submittedTime.plus(timeToLiveInMs, java.time.temporal.ChronoUnit.MILLIS);
            this.orderTTLExpiration = this.orderTTLExpiration.withZoneSameInstant(java.time.ZoneId.of("UTC"));
        } else {
            this.orderTTLExpiration = null; // Clear it out if no TTL
        }

    }

    public void setFilledTime(ZonedDateTime orderFilledTime) {

        filledAt = orderFilledTime.withZoneSameInstant(java.time.ZoneId.of("UTC"));
    }

    public ZonedDateTime getFilledTime() {
        return filledAt;
    }

    public ZonedDateTime getCancelledTime() {
        return canceledAt;
    }

    public void setCancelledTime(ZonedDateTime cancelledTime) {

        canceledAt = cancelledTime.withZoneSameInstant(java.time.ZoneId.of("UTC"));

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((orderType == null) ? 0 : orderType.hashCode());
        result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
        result = prime * result + ((ticker == null) ? 0 : ticker.hashCode());
        result = prime * result + ((size == null) ? 0 : size.hashCode());
        result = prime * result + ((limitPrice == null) ? 0 : limitPrice.hashCode());
        result = prime * result + ((stopPrice == null) ? 0 : stopPrice.hashCode());
        result = prime * result + ((remainingSize == null) ? 0 : remainingSize.hashCode());
        result = prime * result + ((orderId == null) ? 0 : orderId.hashCode());
        result = prime * result + ((side == null) ? 0 : side.hashCode());
        result = prime * result + ((timeInForce == null) ? 0 : timeInForce.hashCode());
        result = prime * result + ((cancelReason == null) ? 0 : cancelReason.hashCode());
        result = prime * result + (int) (lastUpdatedAt ^ (lastUpdatedAt >>> 32));
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
        ParadexOrder other = (ParadexOrder) obj;
        if (orderType != other.orderType)
            return false;
        if (clientId == null) {
            if (other.clientId != null)
                return false;
        } else if (!clientId.equals(other.clientId))
            return false;
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
        if (limitPrice == null) {
            if (other.limitPrice != null)
                return false;
        } else if (!limitPrice.equals(other.limitPrice))
            return false;
        if (stopPrice == null) {
            if (other.stopPrice != null)
                return false;
        } else if (!stopPrice.equals(other.stopPrice))
            return false;
        if (remainingSize == null) {
            if (other.remainingSize != null)
                return false;
        } else if (!remainingSize.equals(other.remainingSize))
            return false;
        if (orderId == null) {
            if (other.orderId != null)
                return false;
        } else if (!orderId.equals(other.orderId))
            return false;
        if (side != other.side)
            return false;
        if (timeInForce != other.timeInForce)
            return false;
        if (cancelReason == null) {
            if (other.cancelReason != null)
                return false;
        } else if (!cancelReason.equals(other.cancelReason))
            return false;
        if (lastUpdatedAt != other.lastUpdatedAt)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ParadexOrder [orderType=" + orderType + ", clientId=" + clientId + ", ticker=" + ticker + ", size="
                + size + ", limitPrice=" + limitPrice + ", stopPrice=" + stopPrice + ", remainingSize=" + remainingSize
                + ", orderId=" + orderId + ", side=" + side + ", timeInForce=" + timeInForce + ", cancelReason="
                + cancelReason + ", lastUpdatedAt=" + lastUpdatedAt + "]";
    }

    @Override
    public int compareTo(ParadexOrder o) {
        BigDecimal thisLimitPrice = this.getLimitPrice();
        BigDecimal otherLimitPrice = o.getLimitPrice();

        if (thisLimitPrice == null && otherLimitPrice == null) {
            // If both prices are null, compare by side, then orderId
            int sideCompare = this.side.compareTo(o.getSide());
            if (sideCompare != 0)
                return sideCompare;
            String thisOrderId = this.getOrderId();
            String otherOrderId = o.getOrderId();
            if (thisOrderId == null && otherOrderId == null)
                return 0;
            if (thisOrderId == null)
                return -1;
            if (otherOrderId == null)
                return 1;
            return thisOrderId.compareTo(otherOrderId);
        } else if (thisLimitPrice == null) {
            return this.side == Side.BUY ? -1 : 1;
        } else if (otherLimitPrice == null) {
            return this.side == Side.BUY ? 1 : -1;
        }

        int priceCompare;
        if (this.side == Side.BUY) {
            priceCompare = thisLimitPrice.compareTo(otherLimitPrice);
        } else {
            priceCompare = otherLimitPrice.compareTo(thisLimitPrice);
        }
        if (priceCompare != 0)
            return priceCompare;

        // If price is the same, compare by side
        int sideCompare = this.side.compareTo(o.getSide());
        if (sideCompare != 0)
            return sideCompare;

        // If price and side are the same, compare by orderId
        String thisOrderId = this.getOrderId();
        String otherOrderId = o.getOrderId();
        if (thisOrderId == null && otherOrderId == null)
            return 0;
        if (thisOrderId == null)
            return -1;
        if (otherOrderId == null)
            return 1;
        return thisOrderId.compareTo(otherOrderId);
    }

}
