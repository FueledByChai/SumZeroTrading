/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.broker.ib;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.sumzerotrading.broker.order.OrderEvent;
import com.sumzerotrading.broker.order.OrderEventListener;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.broker.order.TradeOrder;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.ib.IBConnectionUtil;
import com.sumzerotrading.ib.IBSocket;
import com.sumzerotrading.marketdata.ILevel1Quote;
import com.sumzerotrading.marketdata.Level1QuoteListener;
import com.sumzerotrading.marketdata.ib.IBQuoteEngine;

/**
 *
 * @author RobTerpilowski
 */
public class MainClass implements OrderEventListener, Level1QuoteListener {

    @Override
    public void quoteRecieved(ILevel1Quote quote) {
        System.out.println("Quote: " + quote);
    }

    public void start() throws Exception {

        IBConnectionUtil util = new IBConnectionUtil("localhost", 7999, 1);
        IBSocket socket;
        InteractiveBrokersBroker broker;
        List<OrderEvent> eventList = new ArrayList<>();
        Ticker qqqTicker = new Ticker("QQQ").setInstrumentType(InstrumentType.STOCK);
        socket = util.getIBSocket();
        broker = new InteractiveBrokersBroker(socket);
        broker.addOrderEventListener(this);
        broker.connect();
        IBQuoteEngine ibQuoteEngine = new IBQuoteEngine(socket);
        ibQuoteEngine.startEngine();
        ibQuoteEngine.subscribeLevel1(qqqTicker, this);

        String orderId = broker.getNextOrderId();
        System.out.println("Order id: " + orderId);
        TradeOrder order = new TradeOrder(orderId, qqqTicker, BigDecimal.valueOf(100), TradeDirection.BUY);
        broker.placeOrder(order);

        Thread thread = new Thread(() -> {
            sleep(10000);
            String orderId2 = broker.getNextOrderId();
            System.out.println("Order id: " + orderId);
            TradeOrder order2 = new TradeOrder(orderId2, qqqTicker, BigDecimal.valueOf(200), TradeDirection.BUY);
            broker.placeOrder(order2);

            new Thread(() -> {
                sleep(20000);
                System.exit(0);

            }).start();

        });
        thread.start();

    }

    @Override
    public void orderEvent(OrderEvent event) {
        System.out.println("Event: " + event);
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ex) {
        }
    }

    public static void main(String[] args) throws Exception {
        new MainClass().start();
    }

}
