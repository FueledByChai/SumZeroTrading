/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.intraday.trading.strategy;

import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.intraday.trading.strategy.TradeReferenceLine.Side;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 
 * @author RobTerpilowski
 */
public class RoundTrip implements Serializable {

    public static final long serialVersionUID = 1l;
    protected String correlationId;
    protected OrderTicket entry;
    protected OrderTicket exit;
    protected TradeReferenceLine.Direction direction;

    public void addTradeReference(OrderTicket order, TradeReferenceLine tradeReference) {
        correlationId = tradeReference.getCorrelationId();
        if (tradeReference.getSide() == Side.ENTRY) {
            direction = tradeReference.getDirection();
            entry = order;
        } else {
            exit = order;
        }
    }

    public String getResults() {
        // Long-entry-date, longTicker, LongShares, Long Entry price, Long Entry
        // Commission,
        // LongExitDate, Long Exit Price, shortTicker, shortShares, shortEntryPrice,
        // shortEntryCommissions,
        // shortExitPrice, shortExitCommissions
        // 2016-03-20T12:40:00PST,QQQ,200,100.23,1.45,2016-03-21T12:40:00PST,
        StringBuilder sb = new StringBuilder();
        sb.append(entry.getOrderFilledTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append(",")
                .append(direction).append(",").append(entry.getTicker().getSymbol()).append(",").append(entry.getSize())
                .append(",").append(entry.getFilledPrice()).append(",")
                // long entry commissions
                .append(0).append(",").append(exit.getOrderFilledTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .append(",").append(exit.getFilledPrice()).append(",")
                // long exit commissions
                .append(0);
        return sb.toString();
    }

    public boolean isComplete() {
        return entry != null && exit != null;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.correlationId);
        hash = 61 * hash + Objects.hashCode(this.entry);
        hash = 61 * hash + Objects.hashCode(this.exit);
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
        if (!Objects.equals(this.entry, other.entry)) {
            return false;
        }
        if (!Objects.equals(this.exit, other.exit)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RoundTrip{" + "correlationId=" + correlationId + ", Entry=" + entry + ", Exit=" + exit + '}';
    }

}
