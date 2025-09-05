/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.eod.trading.strategy;

import com.sumzerotrading.broker.order.OrderTicket;
import java.io.Serializable;

/**
 *
 * @author RobTerpilowski
 */
public interface IRoundTrip extends Serializable {

    public void addTradeReference(OrderTicket order, TradeReferenceLine tradeReference);

    public String getCorrelationId();

    public String getResults();

    public boolean isComplete();

}
