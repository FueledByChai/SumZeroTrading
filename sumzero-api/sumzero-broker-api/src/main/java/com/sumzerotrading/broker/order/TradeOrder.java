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

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.sumzerotrading.broker.order.OrderStatus.Status;
import com.sumzerotrading.data.Ticker;

public class TradeOrder implements Serializable {

    public final static long serialVersionUID = 1L;

    public enum Type {

        MARKET, STOP, LIMIT, MARKET_ON_OPEN, MARKET_ON_CLOSE
    };

    public enum Duration {

        DAY, GOOD_UNTIL_CANCELED, GOOD_UTNIL_TIME, FILL_OR_KILL, MARKET_ON_OPEN
    };

    protected Ticker ticker;
    protected TradeDirection direction;
    protected Type type;
    protected ZonedDateTime goodAfterTime = null;
    protected ZonedDateTime goodUntilTime = null;
    protected BigDecimal limitPrice = null;
    protected BigDecimal stopPrice = null;
    protected Duration duration;
    protected BigDecimal size;
    protected String orderId;
    protected String parentOrderId = "";
    protected String ocaGroup;
    protected String positionId;
    protected List<TradeOrder> childOrders = new ArrayList<TradeOrder>();
    protected TradeOrder comboOrder;
    protected String reference;
    protected boolean submitted = false;
    protected boolean submitChildOrdersFirst = false;
    protected ZonedDateTime orderEntryTime;
    protected ZonedDateTime orderFilledTime;
    protected double orderTimeInForceMinutes = 0;

    protected BigDecimal filledSize = BigDecimal.ZERO;
    protected BigDecimal filledPrice = BigDecimal.ZERO;
    protected BigDecimal commission = BigDecimal.ZERO;
    protected Status currentStatus = Status.NEW;

    public TradeOrder() {
    }

    public TradeOrder(String orderId, Ticker ticker, BigDecimal size, TradeDirection tradeDirection) {
        type = Type.MARKET;
        duration = Duration.DAY;
        direction = TradeDirection.BUY;
        this.ticker = ticker;
        this.orderId = orderId;
        this.size = size;
        this.direction = tradeDirection;
    }

    public TradeOrder(TradeOrder original) {
        this(original.getOrderId(), original.getTicker(), original.getSize(), original.getTradeDirection());
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public boolean isSubmitChildOrdersFirst() {
        return submitChildOrdersFirst;
    }

    public void setSubmitChildOrdersFirst(boolean submitChildOrdersFirst) {
        this.submitChildOrdersFirst = submitChildOrdersFirst;
    }

    public Ticker getTicker() {
        return ticker;
    }

    public void setTicker(Ticker ticker) {
        this.ticker = ticker;
    }

    public TradeDirection getTradeDirection() {
        return direction;
    }

    public void setTradeDirection(TradeDirection direction) {
        this.direction = direction;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ZonedDateTime getGoodAfterTime() {
        return goodAfterTime;
    }

    public void setGoodAfterTime(ZonedDateTime goodAfterTime) {
        this.goodAfterTime = goodAfterTime;
    }

    public ZonedDateTime getGoodUntilTime() {
        return goodUntilTime;
    }

    public void setGoodUntilTime(ZonedDateTime goodUntilTime) {
        this.goodUntilTime = goodUntilTime;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public void setLimitPrice(BigDecimal price) {
        this.limitPrice = price;
    }

    public BigDecimal getStopPrice() {
        return stopPrice;
    }

    public BigDecimal getStopPriceAsBigDecimal() {
        return stopPrice;
    }

    public void setStopPrice(BigDecimal stopPrice) {
        this.stopPrice = stopPrice;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public BigDecimal getSize() {
        return size;
    }

    public void setSize(BigDecimal size) {
        this.size = size;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getParentOrderId() {
        return parentOrderId;
    }

    public void setParentOrderId(String parentOrderId) {
        this.parentOrderId = parentOrderId;
    }

    public String getOcaGroup() {
        return ocaGroup;
    }

    public void setOcaGroup(String ocaGroup) {
        this.ocaGroup = ocaGroup;
    }

    public boolean isBuyOrder() {
        return (direction == TradeDirection.BUY) || (direction == TradeDirection.BUY_TO_COVER);
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public List<TradeOrder> getChildOrders() {
        return childOrders;
    }

    public void setChildOrders(List<TradeOrder> orders) {
        this.childOrders = orders;
    }

    public void addChildOrder(TradeOrder childOrder) {
        childOrder.setParentOrderId(orderId);
        childOrders.add(childOrder);
    }

    public void removeChildOrder(TradeOrder childOrder) {
        childOrders.remove(childOrder);
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public TradeOrder getComboOrder() {
        return comboOrder;
    }

    public void setComboOrder(TradeOrder comboOrder) {
        this.comboOrder = comboOrder;
    }

    public TradeDirection getDirection() {
        return direction;
    }

    public void setDirection(TradeDirection direction) {
        this.direction = direction;
    }

    public ZonedDateTime getOrderEntryTime() {
        return orderEntryTime;
    }

    public void setOrderEntryTime(ZonedDateTime orderEntryTime) {
        this.orderEntryTime = orderEntryTime;
    }

    public double getOrderTimeInForceMinutes() {
        return orderTimeInForceMinutes;
    }

    public void setOrderTimeInForceMinutes(double orderTimeInForceMinutes) {
        this.orderTimeInForceMinutes = orderTimeInForceMinutes;
    }

    public BigDecimal getFilledSize() {
        return filledSize;
    }

    public void setFilledSize(BigDecimal filledSize) {
        this.filledSize = filledSize;
    }

    public BigDecimal getFilledPrice() {
        return filledPrice;
    }

    public void setFilledPrice(BigDecimal filledPrice) {
        this.filledPrice = filledPrice;
    }

    public Status getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(Status currentStatus) {
        this.currentStatus = currentStatus;
    }

    public ZonedDateTime getOrderFilledTime() {
        return orderFilledTime;
    }

    public void setOrderFilledTime(ZonedDateTime orderFilledTime) {
        this.orderFilledTime = orderFilledTime;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.ticker);
        hash = 71 * hash + Objects.hashCode(this.direction);
        hash = 71 * hash + Objects.hashCode(this.type);
        hash = 71 * hash + Objects.hashCode(this.goodAfterTime);
        hash = 71 * hash + Objects.hashCode(this.goodUntilTime);
        hash = 71 * hash + Objects.hashCode(this.limitPrice);
        hash = 71 * hash + Objects.hashCode(this.stopPrice);
        hash = 71 * hash + Objects.hashCode(this.duration);
        hash = 71 * hash + (this.size != null ? this.size.hashCode() : 0);
        hash = 71 * hash + Objects.hashCode(this.orderId);
        hash = 71 * hash + Objects.hashCode(this.parentOrderId);
        hash = 71 * hash + Objects.hashCode(this.ocaGroup);
        hash = 71 * hash + Objects.hashCode(this.positionId);
        hash = 71 * hash + Objects.hashCode(this.childOrders);
        hash = 71 * hash + Objects.hashCode(this.comboOrder);
        hash = 71 * hash + Objects.hashCode(this.reference);
        hash = 71 * hash + (this.submitted ? 1 : 0);
        hash = 71 * hash + (this.submitChildOrdersFirst ? 1 : 0);
        hash = 71 * hash + Objects.hashCode(this.orderEntryTime);
        hash = 71 * hash + Objects.hashCode(this.orderFilledTime);
        hash = 71 * hash + (int) (Double.doubleToLongBits(this.orderTimeInForceMinutes)
                ^ (Double.doubleToLongBits(this.orderTimeInForceMinutes) >>> 32));
        hash = 71 * hash + Objects.hashCode(this.filledSize);
        hash = 71 * hash + Objects.hashCode(this.filledPrice);
        hash = 71 * hash + Objects.hashCode(this.commission);
        hash = 71 * hash + Objects.hashCode(this.currentStatus);
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
        final TradeOrder other = (TradeOrder) obj;
        if (!Objects.equals(this.size, other.size)) {
            return false;
        }
        if (this.submitted != other.submitted) {
            return false;
        }
        if (this.submitChildOrdersFirst != other.submitChildOrdersFirst) {
            return false;
        }
        if (Double.doubleToLongBits(this.orderTimeInForceMinutes) != Double
                .doubleToLongBits(other.orderTimeInForceMinutes)) {
            return false;
        }
        if (!Objects.equals(this.filledSize, other.filledSize)) {
            return false;
        }
        if (!Objects.equals(this.filledPrice, other.filledPrice)) {
            return false;
        }
        if (!Objects.equals(this.commission, other.commission)) {
            return false;
        }
        if (!Objects.equals(this.orderId, other.orderId)) {
            return false;
        }
        if (!Objects.equals(this.parentOrderId, other.parentOrderId)) {
            return false;
        }
        if (!Objects.equals(this.ocaGroup, other.ocaGroup)) {
            return false;
        }
        if (!Objects.equals(this.positionId, other.positionId)) {
            return false;
        }
        if (!Objects.equals(this.reference, other.reference)) {
            return false;
        }
        if (!Objects.equals(this.ticker, other.ticker)) {
            return false;
        }
        if (this.direction != other.direction) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.goodAfterTime, other.goodAfterTime)) {
            return false;
        }
        if (!Objects.equals(this.goodUntilTime, other.goodUntilTime)) {
            return false;
        }
        if (!Objects.equals(this.limitPrice, other.limitPrice)) {
            return false;
        }
        if (!Objects.equals(this.stopPrice, other.stopPrice)) {
            return false;
        }
        if (this.duration != other.duration) {
            return false;
        }
        if (!Objects.equals(this.childOrders, other.childOrders)) {
            return false;
        }
        if (!Objects.equals(this.comboOrder, other.comboOrder)) {
            return false;
        }
        if (!Objects.equals(this.orderEntryTime, other.orderEntryTime)) {
            return false;
        }
        if (!Objects.equals(this.orderFilledTime, other.orderFilledTime)) {
            return false;
        }
        if (this.currentStatus != other.currentStatus) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TradeOrder{" + "ticker=" + ticker + ", direction=" + direction + ", type=" + type + ", goodAfterTime="
                + goodAfterTime + ", goodUntilTime=" + goodUntilTime + ", limitPrice=" + limitPrice + ", stopPrice="
                + stopPrice + ", duration=" + duration + ", size=" + size + ", orderId=" + orderId + ", parentOrderId="
                + parentOrderId + ", ocaGroup=" + ocaGroup + ", positionId=" + positionId + ", childOrders="
                + childOrders + ", comboOrder=" + comboOrder + ", reference=" + reference + ", submitted=" + submitted
                + ", submitChildOrdersFirst=" + submitChildOrdersFirst + ", orderEntryTime=" + orderEntryTime
                + ", orderFilledTime=" + orderFilledTime + ", orderTimeInForceMinutes=" + orderTimeInForceMinutes
                + ", filledSize=" + filledSize + ", filledPrice=" + filledPrice + ", commission=" + commission
                + ", currentStatus=" + currentStatus + '}';
    }

}
