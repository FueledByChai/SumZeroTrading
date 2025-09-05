package com.sumzerotrading.paradex.example.trading;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.broker.order.OrderTicket.Type;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.broker.paper.PaperBroker;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.paradex.IParadexOrderBook;
import com.sumzerotrading.marketdata.paradex.OrderBookRegistry;
import com.sumzerotrading.marketdata.paradex.ParadexQuoteEngine;
import com.sumzerotrading.paradex.common.ParadexTickerRegistry;

public class TradingPaperBrokerWithParadexData {

    protected Logger logger = LoggerFactory.getLogger(TradingPaperBrokerWithParadexData.class);

    public void runExample() throws Exception {
        Ticker ticker = ParadexTickerRegistry.getInstance().lookupByBrokerSymbol("BTC-USD-PERP");

        ParadexQuoteEngine quoteEngine = new ParadexQuoteEngine();
        quoteEngine.startEngine();

        IParadexOrderBook paradexOrderBook = OrderBookRegistry.getInstance().getOrderBook(ticker);
        PaperBroker broker = new PaperBroker(paradexOrderBook, ticker);
        quoteEngine.subscribeLevel1(ticker, broker);
        broker.connect();

        String orderId = broker.getNextOrderId();

        OrderTicket order = new OrderTicket(orderId, ticker, BigDecimal.valueOf(1.0), TradeDirection.BUY);
        order.setType(Type.MARKET);

        broker.placeOrder(order);

        broker.addOrderEventListener((event) -> {
            logger.info("Order Event: {}", event);
        });

        Thread.sleep(2000);
        broker.getAllPositions().forEach(position -> {
            logger.info("Position: {}", position);
        });

    }

    public static void main(String[] args) throws Exception {
        TradingPaperBrokerWithParadexData example = new TradingPaperBrokerWithParadexData();
        example.runExample();
    }
}
