package com.sumzerotrading.marketdata.paradex;

import java.time.ZonedDateTime;
import java.util.Map;

import com.sumzerotrading.marketdata.IOrderBook;

public interface IParadexOrderBook extends IOrderBook {

    void handleSnapshot(Map<String, Object> snapshot, ZonedDateTime timestamp);

    void applyDelta(Map<String, Object> delta, ZonedDateTime timestamp);

}