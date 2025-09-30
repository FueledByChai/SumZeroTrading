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

import com.sumzerotrading.broker.order.OrderStatus.Status;
import com.sumzerotrading.data.Ticker;

public class OrderTicket implements Serializable {

    public final static long serialVersionUID = 1L;

    public enum Type {
        MARKET, STOP, LIMIT, MARKET_ON_OPEN, MARKET_ON_CLOSE, STOP_LIMIT, TRAILING_STOP, TRAILING_STOP_LIMIT
    };

    public enum Duration {
        DAY, GOOD_UNTIL_CANCELED, GOOD_UNTIL_TIME, FILL_OR_KILL, IMMEDIATE_OR_CANCEL
    };

    public enum Modifier {
        ALL_OR_NONE, POST_ONLY, REDUCE_ONLY
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
    protected String orderId = "";
    protected String parentOrderId = "";
    protected String ocaGroup;
    protected String positionId;
    protected List<OrderTicket> childOrders = new ArrayList<OrderTicket>();
    protected OrderTicket comboOrder;
    protected String reference;
    protected boolean submitted = false;
    protected boolean submitChildOrdersFirst = false;
    protected ZonedDateTime orderEntryTime;
    protected ZonedDateTime orderFilledTime;
    protected double orderTimeInForceMinutes = 0;
    protected String clientOrderId = "";

    protected BigDecimal filledSize = BigDecimal.ZERO;
    protected BigDecimal filledPrice = BigDecimal.ZERO;
    protected BigDecimal commission = BigDecimal.ZERO;
    protected Status currentStatus = Status.NEW;
    protected List<Modifier> modifiers = new ArrayList<>();
    protected List<Fill> fills = new ArrayList<>();

    public OrderTicket() {
    }

    public OrderTicket(String orderId, Ticker ticker, BigDecimal size, TradeDirection tradeDirection) {
        type = Type.MARKET;
        duration = Duration.DAY;
        direction = TradeDirection.BUY;
        this.ticker = ticker;
        this.orderId = orderId;
        this.size = size;
        this.direction = tradeDirection;
    }

    public OrderTicket(OrderTicket original) {
        this(original.getOrderId(), original.getTicker(), original.getSize(), original.getTradeDirection());
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public OrderTicket setSubmitted(boolean submitted) {
        this.submitted = submitted;
        return this;
    }

    public boolean isSubmitChildOrdersFirst() {
        return submitChildOrdersFirst;
    }

    public OrderTicket setSubmitChildOrdersFirst(boolean submitChildOrdersFirst) {
        this.submitChildOrdersFirst = submitChildOrdersFirst;
        return this;
    }

    public Ticker getTicker() {
        return ticker;
    }

    public OrderTicket setTicker(Ticker ticker) {
        this.ticker = ticker;
        return this;
    }

    public TradeDirection getTradeDirection() {
        return direction;
    }

    public OrderTicket setTradeDirection(TradeDirection direction) {
        this.direction = direction;
        return this;
    }

    public Type getType() {
        return type;
    }

    public OrderTicket setType(Type type) {
        this.type = type;
        return this;
    }

    public ZonedDateTime getGoodAfterTime() {
        return goodAfterTime;
    }

    public OrderTicket setGoodAfterTime(ZonedDateTime goodAfterTime) {
        this.goodAfterTime = goodAfterTime;
        return this;
    }

    public ZonedDateTime getGoodUntilTime() {
        return goodUntilTime;
    }

    public OrderTicket setGoodUntilTime(ZonedDateTime goodUntilTime) {
        this.goodUntilTime = goodUntilTime;
        return this;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public OrderTicket setLimitPrice(BigDecimal price) {
        this.limitPrice = price;
        return this;
    }

    public BigDecimal getStopPrice() {
        return stopPrice;
    }

    public BigDecimal getStopPriceAsBigDecimal() {
        return stopPrice;
    }

    public OrderTicket setStopPrice(BigDecimal stopPrice) {
        this.stopPrice = stopPrice;
        return this;
    }

    public Duration getDuration() {
        return duration;
    }

    public OrderTicket setDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public BigDecimal getSize() {
        return size;
    }

    public OrderTicket setSize(BigDecimal size) {
        this.size = size;
        return this;
    }

    public String getOrderId() {
        return orderId;
    }

    public OrderTicket setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public String getParentOrderId() {
        return parentOrderId;
    }

    public OrderTicket setParentOrderId(String parentOrderId) {
        this.parentOrderId = parentOrderId;
        return this;
    }

    public String getOcaGroup() {
        return ocaGroup;
    }

    public OrderTicket setOcaGroup(String ocaGroup) {
        this.ocaGroup = ocaGroup;
        return this;
    }

    public boolean isBuyOrder() {
        return (direction == TradeDirection.BUY) || (direction == TradeDirection.BUY_TO_COVER);
    }

    public String getPositionId() {
        return positionId;
    }

    public OrderTicket setPositionId(String positionId) {
        this.positionId = positionId;
        return this;
    }

    public List<OrderTicket> getChildOrders() {
        return childOrders;
    }

    public OrderTicket setChildOrders(List<OrderTicket> orders) {
        this.childOrders = orders;
        return this;
    }

    public OrderTicket addChildOrder(OrderTicket childOrder) {
        childOrder.setParentOrderId(orderId);
        childOrders.add(childOrder);
        return this;
    }

    public void removeChildOrder(OrderTicket childOrder) {
        childOrders.remove(childOrder);
    }

    public String getReference() {
        return reference;
    }

    public OrderTicket setReference(String reference) {
        this.reference = reference;
        return this;
    }

    public OrderTicket getComboOrder() {
        return comboOrder;
    }

    public OrderTicket setComboOrder(OrderTicket comboOrder) {
        this.comboOrder = comboOrder;
        return this;
    }

    public TradeDirection getDirection() {
        return direction;
    }

    public OrderTicket setDirection(TradeDirection direction) {
        this.direction = direction;
        return this;
    }

    public ZonedDateTime getOrderEntryTime() {
        return orderEntryTime;
    }

    public OrderTicket setOrderEntryTime(ZonedDateTime orderEntryTime) {
        this.orderEntryTime = orderEntryTime;
        return this;
    }

    public double getOrderTimeInForceMinutes() {
        return orderTimeInForceMinutes;
    }

    public OrderTicket setOrderTimeInForceMinutes(double orderTimeInForceMinutes) {
        this.orderTimeInForceMinutes = orderTimeInForceMinutes;
        return this;
    }

    public BigDecimal getFilledSize() {
        return filledSize;
    }

    public OrderTicket setFilledSize(BigDecimal filledSize) {
        this.filledSize = filledSize;
        return this;
    }

    public BigDecimal getFilledPrice() {
        return filledPrice;
    }

    public OrderTicket setFilledPrice(BigDecimal filledPrice) {
        this.filledPrice = filledPrice;
        return this;
    }

    public Status getCurrentStatus() {
        return currentStatus;
    }

    public OrderTicket setCurrentStatus(Status currentStatus) {
        this.currentStatus = currentStatus;
        return this;
    }

    public ZonedDateTime getOrderFilledTime() {
        return orderFilledTime;
    }

    public OrderTicket setOrderFilledTime(ZonedDateTime orderFilledTime) {
        this.orderFilledTime = orderFilledTime;
        return this;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public OrderTicket setCommission(BigDecimal commission) {
        this.commission = commission;
        return this;
    }

    public BigDecimal getRemainingSize() {
        if (size == null) {
            return BigDecimal.ZERO;
        }
        if (filledSize == null) {
            return size;
        }
        return size.subtract(filledSize);
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public OrderTicket setModifiers(List<Modifier> modifiers) {
        this.modifiers = modifiers;
        return this;
    }

    public OrderTicket addModifier(Modifier modifier) {
        if (modifier != null && !modifiers.contains(modifier)) {
            modifiers.add(modifier);
        }
        return this;
    }

    public boolean containsModifier(Modifier modifier) {
        return modifiers.contains(modifier);
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public OrderTicket setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
        return this;
    }

    public List<Fill> getFills() {
        return fills;
    }

    public OrderTicket setFills(List<Fill> fills) {
        this.fills = fills;
        return this;
    }

    public OrderTicket addFill(Fill fill) {
        if (fill != null) {
            fills.add(fill);
        }
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ticker == null) ? 0 : ticker.hashCode());
        result = prime * result + ((direction == null) ? 0 : direction.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((goodAfterTime == null) ? 0 : goodAfterTime.hashCode());
        result = prime * result + ((goodUntilTime == null) ? 0 : goodUntilTime.hashCode());
        result = prime * result + ((limitPrice == null) ? 0 : limitPrice.hashCode());
        result = prime * result + ((stopPrice == null) ? 0 : stopPrice.hashCode());
        result = prime * result + ((duration == null) ? 0 : duration.hashCode());
        result = prime * result + ((size == null) ? 0 : size.hashCode());
        result = prime * result + ((orderId == null) ? 0 : orderId.hashCode());
        result = prime * result + ((parentOrderId == null) ? 0 : parentOrderId.hashCode());
        result = prime * result + ((ocaGroup == null) ? 0 : ocaGroup.hashCode());
        result = prime * result + ((positionId == null) ? 0 : positionId.hashCode());
        result = prime * result + ((childOrders == null) ? 0 : childOrders.hashCode());
        result = prime * result + ((comboOrder == null) ? 0 : comboOrder.hashCode());
        result = prime * result + ((reference == null) ? 0 : reference.hashCode());
        result = prime * result + (submitted ? 1231 : 1237);
        result = prime * result + (submitChildOrdersFirst ? 1231 : 1237);
        result = prime * result + ((orderEntryTime == null) ? 0 : orderEntryTime.hashCode());
        result = prime * result + ((orderFilledTime == null) ? 0 : orderFilledTime.hashCode());
        long temp;
        temp = Double.doubleToLongBits(orderTimeInForceMinutes);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((filledSize == null) ? 0 : filledSize.hashCode());
        result = prime * result + ((filledPrice == null) ? 0 : filledPrice.hashCode());
        result = prime * result + ((commission == null) ? 0 : commission.hashCode());
        result = prime * result + ((currentStatus == null) ? 0 : currentStatus.hashCode());
        result = prime * result + ((modifiers == null) ? 0 : modifiers.hashCode());
        result = prime * result + ((clientOrderId == null) ? 0 : clientOrderId.hashCode());
        result = prime * result + ((fills == null) ? 0 : fills.hashCode());
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
        OrderTicket other = (OrderTicket) obj;
        if (ticker == null) {
            if (other.ticker != null)
                return false;
        } else if (!ticker.equals(other.ticker))
            return false;
        if (direction != other.direction)
            return false;
        if (type != other.type)
            return false;
        if (goodAfterTime == null) {
            if (other.goodAfterTime != null)
                return false;
        } else if (!goodAfterTime.equals(other.goodAfterTime))
            return false;
        if (goodUntilTime == null) {
            if (other.goodUntilTime != null)
                return false;
        } else if (!goodUntilTime.equals(other.goodUntilTime))
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
        if (duration != other.duration)
            return false;
        if (size == null) {
            if (other.size != null)
                return false;
        } else if (!size.equals(other.size))
            return false;
        if (orderId == null) {
            if (other.orderId != null)
                return false;
        } else if (!orderId.equals(other.orderId))
            return false;
        if (parentOrderId == null) {
            if (other.parentOrderId != null)
                return false;
        } else if (!parentOrderId.equals(other.parentOrderId))
            return false;
        if (ocaGroup == null) {
            if (other.ocaGroup != null)
                return false;
        } else if (!ocaGroup.equals(other.ocaGroup))
            return false;
        if (positionId == null) {
            if (other.positionId != null)
                return false;
        } else if (!positionId.equals(other.positionId))
            return false;
        if (childOrders == null) {
            if (other.childOrders != null)
                return false;
        } else if (!childOrders.equals(other.childOrders))
            return false;
        if (comboOrder == null) {
            if (other.comboOrder != null)
                return false;
        } else if (!comboOrder.equals(other.comboOrder))
            return false;
        if (reference == null) {
            if (other.reference != null)
                return false;
        } else if (!reference.equals(other.reference))
            return false;
        if (submitted != other.submitted)
            return false;
        if (submitChildOrdersFirst != other.submitChildOrdersFirst)
            return false;
        if (orderEntryTime == null) {
            if (other.orderEntryTime != null)
                return false;
        } else if (!orderEntryTime.equals(other.orderEntryTime))
            return false;
        if (orderFilledTime == null) {
            if (other.orderFilledTime != null)
                return false;
        } else if (!orderFilledTime.equals(other.orderFilledTime))
            return false;
        if (Double.doubleToLongBits(orderTimeInForceMinutes) != Double.doubleToLongBits(other.orderTimeInForceMinutes))
            return false;
        if (filledSize == null) {
            if (other.filledSize != null)
                return false;
        } else if (!filledSize.equals(other.filledSize))
            return false;
        if (filledPrice == null) {
            if (other.filledPrice != null)
                return false;
        } else if (!filledPrice.equals(other.filledPrice))
            return false;
        if (commission == null) {
            if (other.commission != null)
                return false;
        } else if (!commission.equals(other.commission))
            return false;
        if (currentStatus != other.currentStatus)
            return false;
        if (modifiers == null) {
            if (other.modifiers != null)
                return false;
        } else if (!modifiers.equals(other.modifiers))
            return false;
        if (clientOrderId == null) {
            if (other.clientOrderId != null)
                return false;
        } else if (!clientOrderId.equals(other.clientOrderId))
            return false;
        if (fills == null) {
            if (other.fills != null)
                return false;
        } else if (!fills.equals(other.fills))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TradeOrder [ticker=" + ticker + ", direction=" + direction + ", type=" + type + ", goodAfterTime="
                + goodAfterTime + ", goodUntilTime=" + goodUntilTime + ", limitPrice=" + limitPrice + ", stopPrice="
                + stopPrice + ", duration=" + duration + ", size=" + size + ", orderId=" + orderId + ", parentOrderId="
                + parentOrderId + ", ocaGroup=" + ocaGroup + ", positionId=" + positionId + ", childOrders="
                + childOrders + ", comboOrder=" + comboOrder + ", reference=" + reference + ", submitted=" + submitted
                + ", submitChildOrdersFirst=" + submitChildOrdersFirst + ", orderEntryTime=" + orderEntryTime
                + ", orderFilledTime=" + orderFilledTime + ", orderTimeInForceMinutes=" + orderTimeInForceMinutes
                + ", filledSize=" + filledSize + ", filledPrice=" + filledPrice + ", commission=" + commission
                + ", currentStatus=" + currentStatus + ", modifiers=" + modifiers + ", clientOrderId=" + clientOrderId
                + ", fills=" + fills + "]";
    }

}
