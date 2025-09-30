package com.sumzerotrading.paradex.example.trading;

import java.math.BigDecimal;

import com.sumzerotrading.broker.hyperliquid.HyperliquidBroker;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.broker.order.OrderTicket.Modifier;
import com.sumzerotrading.broker.order.OrderTicket.Type;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.ws.HyperliquidTickerRegistry;
import com.sumzerotrading.marketdata.QuoteEngine;
import com.sumzerotrading.marketdata.hyperliquid.HyperliquidQuoteEngine;

public class HyperliquidTradingExample {
    protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HyperliquidTradingExample.class);

    public void executeTrade() throws Exception {
        Ticker btcTicker = HyperliquidTickerRegistry.getInstance().lookupByBrokerSymbol("BTC");

        QuoteEngine engine = QuoteEngine.getInstance(HyperliquidQuoteEngine.class);
        engine.startEngine();

        engine.subscribeLevel1(btcTicker, (level1Quote) -> {
            // System.out.println("Level 1 Quote Updated : " + level1Quote);
        });

        HyperliquidBroker broker = new HyperliquidBroker();
        broker.connect();

        broker.addOrderEventListener((event) -> {
            log.info("Order Event Received : {}", event);
        });

        Thread.sleep(5000);

        OrderTicket order = new OrderTicket();
        order.setTicker(btcTicker).setSize(BigDecimal.valueOf(0.01)).setDirection(TradeDirection.BUY)
                .setType(Type.LIMIT).setLimitPrice(BigDecimal.valueOf(116000)).addModifier(Modifier.POST_ONLY);
        broker.placeOrder(order);

    }

    public static void main(String[] args) throws Exception {
        // config file
        System.setProperty("hyperliquid.config.file",
                "/Users/RobTerpilowski/Code/JavaProjects/SumZeroTrading/local-stuff/hl-trading-example.properties");
        HyperliquidTradingExample example = new HyperliquidTradingExample();
        example.executeTrade();
    }
}
