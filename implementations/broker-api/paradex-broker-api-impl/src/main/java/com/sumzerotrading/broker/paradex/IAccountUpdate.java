package com.sumzerotrading.broker.paradex;

public interface IAccountUpdate {

    double getAccountValue();

    void setAccountValue(double accountValue);

    double getMaintenanceMargin();

    void setMaintenanceMargin(double maintenanceMargin);

    double getMarginRatioPercent();

}