package com.sumzerotrading.marketdata;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import com.sumzerotrading.marketdata.OrderBook.PriceLevel;

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

    /**
     * Atomically updates the order book from a complete snapshot. This method
     * ensures readers never see inconsistent state during updates.
     * 
     * @param bids      List of bid entries (price, size pairs)
     * @param asks      List of ask entries (price, size pairs)
     * @param timestamp The timestamp for this update
     */
    void updateFromSnapshot(List<PriceLevel> bids, List<PriceLevel> asks, ZonedDateTime timestamp);

    /**
     * Convenience method for updating from raw price/size arrays.
     * 
     * @param bidPrices Array of bid prices
     * @param bidSizes  Array of bid sizes (must be same length as bidPrices)
     * @param askPrices Array of ask prices
     * @param askSizes  Array of ask sizes (must be same length as askPrices)
     * @param timestamp The timestamp for this update
     */
    void updateFromSnapshot(BigDecimal[] bidPrices, Double[] bidSizes, BigDecimal[] askPrices, Double[] askSizes,
            ZonedDateTime timestamp);

}