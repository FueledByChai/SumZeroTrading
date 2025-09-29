package com.sumzerotrading.hyperliquid.ws.listeners.accountinfo;

import java.math.BigDecimal;

public class HyperliquidPositionUpdate {

    private String ticker;
    private BigDecimal size;
    private BigDecimal entryPrice;
    private BigDecimal unrealizedPnl;
    private BigDecimal liquidationPrice;
    private BigDecimal fundingSinceOpen;

    public HyperliquidPositionUpdate() {
    }

    public HyperliquidPositionUpdate(String ticker, BigDecimal size, BigDecimal entryPrice, BigDecimal unrealizedPnl,
            BigDecimal liquidationPrice, BigDecimal fundingSinceOpen) {
        this.ticker = ticker;
        this.size = size;
        this.entryPrice = entryPrice;
        this.unrealizedPnl = unrealizedPnl;
        this.liquidationPrice = liquidationPrice;
        this.fundingSinceOpen = fundingSinceOpen;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public BigDecimal getSize() {
        return size;
    }

    public void setSize(BigDecimal size) {
        this.size = size;
    }

    public BigDecimal getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(BigDecimal entryPrice) {
        this.entryPrice = entryPrice;
    }

    public BigDecimal getUnrealizedPnl() {
        return unrealizedPnl;
    }

    public void setUnrealizedPnl(BigDecimal unrealizedPnl) {
        this.unrealizedPnl = unrealizedPnl;
    }

    public BigDecimal getLiquidationPrice() {
        return liquidationPrice;
    }

    public void setLiquidationPrice(BigDecimal liquidationPrice) {
        this.liquidationPrice = liquidationPrice;
    }

    public BigDecimal getFundingSinceOpen() {
        return fundingSinceOpen;
    }

    public void setFundingSinceOpen(BigDecimal fundingSinceOpen) {
        this.fundingSinceOpen = fundingSinceOpen;
    }

    @Override
    public String toString() {
        return "HyperliquidPositionUpdate{" + "ticker='" + ticker + '\'' + ", size=" + size + ", entryPrice="
                + entryPrice + ", unrealizedPnl=" + unrealizedPnl + ", liquidationPrice=" + liquidationPrice
                + ", fundingSinceOpen=" + fundingSinceOpen + '}';
    }
}