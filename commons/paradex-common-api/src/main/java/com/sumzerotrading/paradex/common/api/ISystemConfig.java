package com.sumzerotrading.paradex.common.api;

import java.math.BigDecimal;

public interface ISystemConfig {

    String getParadexKey();

    String getParadexAddress();

    String getRestUrl();

    String getWsUrl();

    String getSnapshotFileLocation();

    String getAccountMonitorFileLocation();

    double getReserveBalance();

    String getBinanceWsUrl();

    String getExtremeEventFileLocation();

    String getTradeReportFileLocation();

    String getTicker();

    int getServerPort();

    String getBrokerType();

    String getParadexSymbol();

    String getBinanceTicker();

    int getPriceScale();

    int getPositionScale();

    BigDecimal getTickSize();

    int getMaxLevels();

    int getPositionSizeInDollars();

    BigDecimal getOrderSizeIncrement();

    boolean isUseOrderBookImbalance();

    double getOrderBookImbalanceThreshold();

    SideToTrade getSideToTrade();

    int getTtlInSeconds();

}
