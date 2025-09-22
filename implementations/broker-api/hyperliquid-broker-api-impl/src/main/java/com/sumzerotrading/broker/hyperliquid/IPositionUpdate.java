package com.sumzerotrading.broker.hyperliquid;

import java.math.BigDecimal;

public interface IPositionUpdate {

    String getTicker();

    void setTicker(String ticker);

    BigDecimal getSize();

    void setSize(BigDecimal size);

    BigDecimal getEntryPrice();

    void setEntryPrice(BigDecimal entryPrice);

    BigDecimal getUnrealizedPnl();

    void setUnrealizedPnl(BigDecimal unrealizedPnl);

    BigDecimal getLiquidationPrice();

    void setLiquidationPrice(BigDecimal liquidationPrice);

    BigDecimal getFundingSinceOpen();

    void setFundingSinceOpen(BigDecimal fundingSinceOpen);
}