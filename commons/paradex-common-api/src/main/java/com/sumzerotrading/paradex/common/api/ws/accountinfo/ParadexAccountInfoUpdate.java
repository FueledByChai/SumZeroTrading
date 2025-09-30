package com.sumzerotrading.paradex.common.api.ws.accountinfo;

public class ParadexAccountInfoUpdate implements IAccountUpdate {

    protected double accountValue;
    protected double maintenanceMargin;

    public ParadexAccountInfoUpdate() {

    }

    @Override
    public double getAccountValue() {
        return accountValue;
    }

    @Override
    public void setAccountValue(double accountValue) {
        this.accountValue = accountValue;
    }

    @Override
    public double getMaintenanceMargin() {
        return maintenanceMargin;
    }

    @Override
    public void setMaintenanceMargin(double maintenanceMargin) {
        this.maintenanceMargin = maintenanceMargin;
    }

    @Override
    public double getMarginRatioPercent() {
        if (accountValue == 0 || maintenanceMargin == 0) {
            return 0;
        } else {
            return (maintenanceMargin / accountValue) * 100.0;
        }

    }

    @Override
    public String toString() {
        return "ParadexAccountInfo [accountValue=" + accountValue + ", maintenanceMargin=" + maintenanceMargin + "]";
    }

}
