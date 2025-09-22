package com.sumzerotrading.broker.hyperliquid;

import java.util.ArrayList;
import java.util.List;

public class ParadexAccountInfoUpdate implements IAccountUpdate {

    protected double accountValue;
    protected double maintenanceMargin;
    protected List<IPositionUpdate> positions = new ArrayList<>();

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
    public List<IPositionUpdate> getPositions() {
        return positions;
    }

    @Override
    public void setPositions(List<IPositionUpdate> positions) {
        this.positions = positions != null ? positions : new ArrayList<>();
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
        return "ParadexAccountInfo [accountValue=" + accountValue + ", maintenanceMargin=" + maintenanceMargin
                + ", positions=" + positions.size() + "]";
    }

}
