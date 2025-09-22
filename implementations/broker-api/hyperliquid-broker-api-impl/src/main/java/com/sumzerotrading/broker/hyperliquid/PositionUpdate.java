package com.sumzerotrading.broker.hyperliquid;

import java.math.BigDecimal;

public class PositionUpdate implements IPositionUpdate {

    private String ticker;
    private BigDecimal size;
    private BigDecimal entryPrice;
    private BigDecimal unrealizedPnl;
    private BigDecimal liquidationPrice;
    private BigDecimal fundingSinceOpen;

    public PositionUpdate() {
    }

    public PositionUpdate(String ticker, BigDecimal size, BigDecimal entryPrice, BigDecimal unrealizedPnl,
            BigDecimal liquidationPrice, BigDecimal fundingSinceOpen) {
        this.ticker = ticker;
        this.size = size;
        this.entryPrice = entryPrice;
        this.unrealizedPnl = unrealizedPnl;
        this.liquidationPrice = liquidationPrice;
        this.fundingSinceOpen = fundingSinceOpen;
    }

    @Override
    public String getTicker() {
        return ticker;
    }

    @Override
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    @Override
    public BigDecimal getSize() {
        return size;
    }

    @Override
    public void setSize(BigDecimal size) {
        this.size = size;
    }

    @Override
    public BigDecimal getEntryPrice() {
        return entryPrice;
    }

    @Override
    public void setEntryPrice(BigDecimal entryPrice) {
        this.entryPrice = entryPrice;
    }

    @Override
    public BigDecimal getUnrealizedPnl() {
        return unrealizedPnl;
    }

    @Override
    public void setUnrealizedPnl(BigDecimal unrealizedPnl) {
        this.unrealizedPnl = unrealizedPnl;
    }

    @Override
    public BigDecimal getLiquidationPrice() {
        return liquidationPrice;
    }

    @Override
    public void setLiquidationPrice(BigDecimal liquidationPrice) {
        this.liquidationPrice = liquidationPrice;
    }

    @Override
    public BigDecimal getFundingSinceOpen() {
        return fundingSinceOpen;
    }

    @Override
    public void setFundingSinceOpen(BigDecimal fundingSinceOpen) {
        this.fundingSinceOpen = fundingSinceOpen;
    }

    @Override
    public String toString() {
        return "ParadexPositionUpdate [ticker=" + ticker + ", size=" + size + ", entryPrice=" + entryPrice
                + ", unrealizedPnl=" + unrealizedPnl + ", liquidationPrice=" + liquidationPrice + ", fundingSinceOpen="
                + fundingSinceOpen + "]";
    }
}