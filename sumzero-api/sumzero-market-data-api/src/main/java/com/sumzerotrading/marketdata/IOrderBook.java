package com.sumzerotrading.marketdata;

import java.math.BigDecimal;

public interface IOrderBook {

    void clearOrderBook();

    boolean isInitialized();

    BigDecimal getBestBid();

    BigDecimal getBestBid(BigDecimal tickSize);

    BigDecimal getBestAsk();

    BigDecimal getBestAsk(BigDecimal tickSize);

    BigDecimal getMidpoint();

    BigDecimal getMidpoint(BigDecimal tickSize);

    double calculateWeightedOrderBookImbalance(double lambda);

    double calculateWeightedOrderBookImbalance(double lambda, BigDecimal tickSize);

    BigDecimal getCenterOfGravityMidpoint(int levels);

    /**
     * Returns the volume-weighted midpoint for the top N levels at the given tick
     * size.
     */
    BigDecimal getCenterOfGravityMidpoint(int levels, BigDecimal tickSize);

    BigDecimal getVWAPMidpoint(int levels);

    /**
     * Returns the volume-weighted midpoint for the top N levels at the given tick
     * size.
     */
    BigDecimal getVWAPMidpoint(int levels, BigDecimal tickSize);

    void printTopLevels(int levels);

    void printTopLevels(int levels, BigDecimal tickSize);

    void addOrderBookUpdateListener(OrderBookUpdateListener listener);

    void removeOrderBookUpdateListener(OrderBookUpdateListener listener);

}