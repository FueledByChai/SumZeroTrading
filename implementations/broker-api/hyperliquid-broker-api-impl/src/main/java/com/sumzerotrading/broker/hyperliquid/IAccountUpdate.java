package com.sumzerotrading.broker.hyperliquid;

import java.util.List;

public interface IAccountUpdate {

    double getAccountValue();

    void setAccountValue(double accountValue);

    double getMaintenanceMargin();

    void setMaintenanceMargin(double maintenanceMargin);

    double getMarginRatioPercent();

    List<HyperliquidPositionUpdate> getPositions();

    void setPositions(List<HyperliquidPositionUpdate> positions);

}