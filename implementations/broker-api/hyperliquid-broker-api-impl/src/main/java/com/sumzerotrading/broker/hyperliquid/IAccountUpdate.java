package com.sumzerotrading.broker.hyperliquid;

import java.util.List;

public interface IAccountUpdate {

    double getAccountValue();

    void setAccountValue(double accountValue);

    double getMaintenanceMargin();

    void setMaintenanceMargin(double maintenanceMargin);

    double getMarginRatioPercent();

    List<IPositionUpdate> getPositions();

    void setPositions(List<IPositionUpdate> positions);

}