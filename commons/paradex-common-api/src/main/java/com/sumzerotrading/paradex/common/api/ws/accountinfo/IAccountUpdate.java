package com.sumzerotrading.paradex.common.api.ws.accountinfo;

public interface IAccountUpdate {

    double getAccountValue();

    void setAccountValue(double accountValue);

    double getMaintenanceMargin();

    void setMaintenanceMargin(double maintenanceMargin);

    double getMarginRatioPercent();

}