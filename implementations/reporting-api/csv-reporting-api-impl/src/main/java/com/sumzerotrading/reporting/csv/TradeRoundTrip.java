/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.reporting.csv;

import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.reporting.IRoundTrip;
import com.sumzerotrading.reporting.TradeReferenceLine;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 *
 * @author RobTerpilowski
 */
public class TradeRoundTrip implements IRoundTrip {

    public static final long serialVersionUID = 1l;
    protected String correlationId;
    protected OrderTicket entry;
    protected OrderTicket exit;
    protected TradeReferenceLine.Direction direction;

    @Override
    public void addTradeReference(OrderTicket order, TradeReferenceLine tradeReference) {
        correlationId = tradeReference.getCorrelationId();
        if (tradeReference.getSide() == TradeReferenceLine.Side.ENTRY) {
            direction = tradeReference.getDirection();
            entry = order;
        } else {
            exit = order;
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

    @Override
    public boolean isComplete() {
        return entry != null && exit != null;
    }

    @Override
    public String getCorrelationId() {
        return correlationId;
    }

    public OrderTicket getEntryOrder() {
        return entry;
    }

    public OrderTicket getExitOrder() {
        return exit;
    }

}
