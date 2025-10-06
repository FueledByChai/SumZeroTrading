package com.sumzerotrading.paradex.example.trading;

import java.math.BigDecimal;

import com.sumzerotrading.broker.BrokerAccountInfoListener;
import com.sumzerotrading.broker.hyperliquid.HyperliquidBroker;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.broker.order.OrderTicket.Type;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.ws.HyperliquidTickerRegistry;
import com.sumzerotrading.marketdata.QuoteEngine;
import com.sumzerotrading.marketdata.hyperliquid.HyperliquidQuoteEngine;

public class HyperliquidTradingExample {
    protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HyperliquidTradingExample.class);

    public void executeTrade() throws Exception {
        String tickerString = "ASTER";
        Ticker ticker = HyperliquidTickerRegistry.getInstance().lookupByBrokerSymbol(tickerString);

        QuoteEngine engine = QuoteEngine.getInstance(HyperliquidQuoteEngine.class);
        engine.startEngine();

        engine.subscribeLevel1(ticker, (level1Quote) -> {
            // System.out.println("Level 1 Quote Updated : " + level1Quote);
        });

        HyperliquidBroker broker = new HyperliquidBroker();
        broker.connect();

        broker.addOrderEventListener((event) -> {
            log.info("Order Event Received : {}", event);
        });

        broker.addFillEventListener(fill -> {
            if (fill.isSnapshot()) {
                log.info("SNAPSHOT Fill Received : {}", fill);
            } else {
                log.info("Fill Received : {}", fill);
            }
        });

        broker.addBrokerAccountInfoListener(new BrokerAccountInfoListener() {

            @Override
            public void accountEquityUpdated(double equity) {
                log.info("Account equity updated: {}", equity);
            }

            @Override
            public void availableFundsUpdated(double availableFunds) {
                log.info("Available funds updated: {}", availableFunds);
            }

        });

        Thread.sleep(5000);

        OrderTicket order = new OrderTicket();
        double size = 10;
        double price = 1.75123;
        // double size = 0.001;
        // double price = 110000;
        TradeDirection direction = TradeDirection.BUY;
        order.setTicker(ticker).setSize(new BigDecimal("10")).setDirection(direction).setType(Type.LIMIT)
                .setLimitPrice(BigDecimal.valueOf(price)).addModifier(OrderTicket.Modifier.POST_ONLY);
        // order.setTicker(ticker).setSize(BigDecimal.valueOf(size)).setDirection(TradeDirection.BUY).setType(Type.MARKET);
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
