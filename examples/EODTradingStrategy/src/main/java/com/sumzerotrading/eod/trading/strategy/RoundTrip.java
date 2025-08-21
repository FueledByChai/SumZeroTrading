/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.eod.trading.strategy;

import com.sumzerotrading.broker.order.TradeOrder;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 
 * @author RobTerpilowski
 */
public class RoundTrip implements IRoundTrip {

    public static final long serialVersionUID = 1l;
    protected String correlationId;
    protected TradeOrder longEntry;
    protected TradeOrder longExit;
    protected TradeOrder shortEntry;
    protected TradeOrder shortExit;

    @Override
    public void addTradeReference(TradeOrder order, TradeReferenceLine tradeReference) {
        correlationId = tradeReference.getCorrelationId();
        if (tradeReference.getDirection() == TradeReferenceLine.Direction.LONG) {
            if (tradeReference.getSide() == TradeReferenceLine.Side.ENTRY) {
                longEntry = order;
            } else {
                longExit = order;
            }
        } else {
            if (tradeReference.getSide() == TradeReferenceLine.Side.ENTRY) {
                shortEntry = order;
            } else {
                shortExit = order;
            }
        }
    }

    @Override
    public String getResults() {
        // Long-entry-date, longTicker, LongShares, Long Entry price, Long Entry
        // Commission,
        // LongExitDate, Long Exit Price, shortTicker, shortShares, shortEntryPrice,
        // shortEntryCommissions,
        // shortExitPrice, shortExitCommissions
        // 2016-03-20T12:40:00PST,QQQ,200,100.23,1.45,2016-03-21T12:40:00PST,
        StringBuilder sb = new StringBuilder();
        sb.append(longEntry.getOrderFilledTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append(",")
                .append("Long,").append(longEntry.getTicker().getSymbol()).append(",").append(longEntry.getSize())
                .append(",").append(longEntry.getFilledPrice()).append(",")
                // long entry commissions
                .append(0).append(",")
                .append(longExit.getOrderFilledTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append(",")
                .append(longExit.getFilledPrice()).append(",")
                // long exit commissions
                .append(0).append(",").append("Short,").append(shortEntry.getTicker().getSymbol()).append(",")
                .append(shortEntry.getSize()).append(",").append(shortEntry.getFilledPrice()).append(",")
                // short entry commissions
                .append(0).append(",").append(shortExit.getFilledPrice()).append(",")
                // short exit commissions.
                .append(0);

        return sb.toString();
    }

    @Override
    public boolean isComplete() {
        return longEntry != null && longExit != null && shortEntry != null && shortExit != null;
    }

    @Override
    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.correlationId);
        hash = 61 * hash + Objects.hashCode(this.longEntry);
        hash = 61 * hash + Objects.hashCode(this.longExit);
        hash = 61 * hash + Objects.hashCode(this.shortEntry);
        hash = 61 * hash + Objects.hashCode(this.shortExit);
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
        final RoundTrip other = (RoundTrip) obj;
        if (!Objects.equals(this.correlationId, other.correlationId)) {
            return false;
        }
        if (!Objects.equals(this.longEntry, other.longEntry)) {
            return false;
        }
        if (!Objects.equals(this.longExit, other.longExit)) {
            return false;
        }
        if (!Objects.equals(this.shortEntry, other.shortEntry)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RoundTrip{" + "correlationId=" + correlationId + ", longEntry=" + longEntry + ", longExit=" + longExit
                + ", shortEntry=" + shortEntry + ", shortExit=" + shortExit + '}';
    }

}
