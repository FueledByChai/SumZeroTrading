package com.sumzerotrading.paradex.example.trading;

import java.math.BigDecimal;

import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.broker.order.OrderTicket.Modifier;
import com.sumzerotrading.broker.order.OrderTicket.Type;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.broker.paradex.ParadexBroker;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.QuoteEngine;
import com.sumzerotrading.marketdata.paradex.ParadexQuoteEngine;
import com.sumzerotrading.paradex.common.ParadexTickerRegistry;
import com.sumzerotrading.paradex.common.api.IParadexRestApi;
import com.sumzerotrading.paradex.common.api.ParadexApiFactory;

public class ParadexTradingExample {
    protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ParadexTradingExample.class);

    public void onBoard() throws Exception {
        IParadexRestApi api = ParadexApiFactory.getPrivateApi();

        api.onboardAccount("0xA45DD69272de900714d780e28a8FAB6E1f19cfDA",
                "0x77f769fc86a4c82a93ac00e5e562d27d516e4de3193b0d7e46ad8773b74464d", true);
    }

    public void executeTrade() throws Exception {
        Ticker btcTicker = ParadexTickerRegistry.getInstance().lookupByBrokerSymbol("BTC-USD-PERP");

        QuoteEngine engine = QuoteEngine.getInstance(ParadexQuoteEngine.class);
        engine.startEngine();

        engine.subscribeLevel1(btcTicker, (level1Quote) -> {
            // System.out.println("Level 1 Quote Updated : " + level1Quote);
        });

        ParadexBroker broker = new ParadexBroker();
        broker.connect();

        broker.addOrderEventListener((event) -> {
            log.info("Order Event Received : {}", event);
        });

        broker.addFillEventListener(fill -> {
            log.info("Order Fill Received : {}", fill);
        });

        Thread.sleep(2000);

        OrderTicket order = new OrderTicket();
        order.setTicker(btcTicker).setSize(BigDecimal.valueOf(0.01)).setDirection(TradeDirection.BUY)
                .setType(Type.LIMIT).setLimitPrice(BigDecimal.valueOf(110000)).addModifier(Modifier.POST_ONLY);
        broker.placeOrder(order);

    }

    public static void main(String[] args) throws Exception {
        // config file
        System.setProperty("paradex.config.file",
                "/Users/RobTerpilowski/Code/JavaProjects/SumZeroTrading/local-stuff/paradex-trading-example.properties");
        ParadexTradingExample example = new ParadexTradingExample();
        example.executeTrade();
        // example.onBoard();
    }
}
