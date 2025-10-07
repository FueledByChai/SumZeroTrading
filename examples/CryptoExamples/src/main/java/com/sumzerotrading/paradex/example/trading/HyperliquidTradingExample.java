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
    protected static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HyperliquidTradingExample.class);

    public void executeTrade() throws Exception {
        String tickerString = "BTC";
        String size = "0.01";
        String price = "111000";
        Ticker ticker = HyperliquidTickerRegistry.getInstance().lookupByBrokerSymbol(tickerString);

        QuoteEngine engine = QuoteEngine.getInstance(HyperliquidQuoteEngine.class);
        engine.startEngine();

        engine.subscribeLevel1(ticker, (level1Quote) -> {
            // System.out.println("Level 1 Quote Updated : " + level1Quote);
        });

        HyperliquidBroker broker = new HyperliquidBroker();
        broker.connect();

        broker.addOrderEventListener((event) -> {
            logger.info("Order Event Received : {}", event);
        });

        broker.addFillEventListener(fill -> {
            if (fill.isSnapshot()) {
                logger.info("SNAPSHOT Fill Received : {}", fill);
            } else {
                logger.info("Fill Received : {}", fill);
            }
        });

        broker.addBrokerAccountInfoListener(new BrokerAccountInfoListener() {

            @Override
            public void accountEquityUpdated(double equity) {
                logger.info("Account equity updated: {}", equity);
            }

            @Override
            public void availableFundsUpdated(double availableFunds) {
                logger.info("Available funds updated: {}", availableFunds);
            }

        });

        Thread.sleep(5000);

        OrderTicket order = new OrderTicket();

        // double size = 0.001;
        // double price = 110000;
        TradeDirection direction = TradeDirection.BUY;
        order.setTicker(ticker).setSize(new BigDecimal(size)).setDirection(direction)
                // .setType(Type.LIMIT).setLimitPrice(new
                // BigDecimal(price)).addModifier(OrderTicket.Modifier.POST_ONLY);
                .setTicker(ticker).setSize(new BigDecimal(size)).setDirection(TradeDirection.BUY).setType(Type.MARKET);
        logger.info("placing order");
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
