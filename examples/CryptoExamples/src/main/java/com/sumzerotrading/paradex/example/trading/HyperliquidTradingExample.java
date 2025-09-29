package com.sumzerotrading.paradex.example.trading;

import java.math.BigDecimal;

import com.sumzerotrading.broker.hyperliquid.HyperliquidBroker;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.broker.order.OrderTicket.Type;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.ws.HyperliquidTickerRegistry;

public class HyperliquidTradingExample {

    public void executeTrade() {
        HyperliquidBroker broker = new HyperliquidBroker();
        broker.connect();

        broker.addOrderEventListener((event) -> {
            System.out.println("Order Event Received : " + event);
        });

        Ticker btcTicker = HyperliquidTickerRegistry.getInstance().lookupByBrokerSymbol("BTC");
        OrderTicket order = new OrderTicket();
        order.setTicker(btcTicker).setSize(BigDecimal.valueOf(0.01)).setDirection(TradeDirection.BUY)
                .setType(Type.MARKET);

        broker.placeOrder(order);

    }

    public static void main(String[] args) {
        HyperliquidTradingExample example = new HyperliquidTradingExample();
        example.executeTrade();
    }
}
