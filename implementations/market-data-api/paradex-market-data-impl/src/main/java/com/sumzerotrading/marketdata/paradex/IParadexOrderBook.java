package com.sumzerotrading.marketdata.paradex;

import java.util.Map;

import com.sumzerotrading.marketdata.IOrderBook;

public interface IParadexOrderBook extends IOrderBook {

    void handleSnapshot(Map<String, Object> snapshot);

    void applyDelta(Map<String, Object> delta);

}