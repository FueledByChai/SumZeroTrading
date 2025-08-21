package com.sumzerotrading.marketdata.paradex;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.OrderBook;

public class ParadexOrderBook extends OrderBook implements IParadexOrderBook {

    public ParadexOrderBook(Ticker ticker) {
        super(ticker);
    }

    public ParadexOrderBook(Ticker ticker, BigDecimal tickSize) {
        super(ticker, tickSize);
    }

    @Override
    public void handleSnapshot(Map<String, Object> snapshot) {
        // Clear existing order book
        buySide.clear();
        sellSide.clear();

        // Process snapshot data
        List<Map<String, Object>> inserts = (List<Map<String, Object>>) snapshot.get("inserts");

        for (Map<String, Object> orderData : inserts) {
            BigDecimal price = new BigDecimal((String) orderData.get("price"));
            Double size = Double.parseDouble((String) orderData.get("size"));
            String side = (String) orderData.get("side");

            if ("BUY".equals(side)) {
                buySide.insert(price, size);
            } else {
                sellSide.insert(price, size);
            }
        }
        initialized = true;
    }

    @Override
    public void applyDelta(Map<String, Object> delta) {
        // Process delta data
        List<Map<String, Object>> inserts = (List<Map<String, Object>>) delta.get("inserts");
        List<Map<String, Object>> updates = (List<Map<String, Object>>) delta.get("updates");
        List<Map<String, Object>> deletes = (List<Map<String, Object>>) delta.get("deletes");

        for (Map<String, Object> orderData : inserts) {
            BigDecimal price = new BigDecimal((String) orderData.get("price"));
            Double size = Double.parseDouble((String) orderData.get("size"));
            String side = (String) orderData.get("side");

            if ("BUY".equals(side)) {
                buySide.insert(price, size);
            } else {
                sellSide.insert(price, size);
            }
        }

        for (Map<String, Object> orderData : updates) {
            BigDecimal price = new BigDecimal((String) orderData.get("price"));
            Double size = Double.parseDouble((String) orderData.get("size"));
            String side = (String) orderData.get("side");

            if ("BUY".equals(side)) {
                buySide.update(price, size);
            } else {
                sellSide.update(price, size);
            }
        }

        for (Map<String, Object> orderData : deletes) {
            BigDecimal price = new BigDecimal((String) orderData.get("price"));
            String side = (String) orderData.get("side");

            if ("BUY".equals(side)) {
                buySide.remove(price);
            } else {
                sellSide.remove(price);
            }
        }
    }

}
