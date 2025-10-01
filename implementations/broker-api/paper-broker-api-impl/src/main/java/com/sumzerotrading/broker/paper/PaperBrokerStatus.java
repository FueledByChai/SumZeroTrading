package com.sumzerotrading.broker.paper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import com.sumzerotrading.broker.order.OrderTicket;

public class PaperBrokerStatus implements IPaperBrokerStatus {

    protected String asset;
    protected double bestAsk;
    protected double bestBid;
    protected double midPoint;
    protected double vwapMidpoint;
    protected double cogMidpoint;
    protected double currentPosition;
    protected double accountValue;
    protected double realizedPnL;
    protected double unrealizedPnL;
    protected double totalPnL;
    protected double feesCollectedOrPaid;
    protected double pnlWithFees;
    protected double dollarVolume;
    protected double fundingAccruedOrPaid = 0.0; // Total funding accrued or paid
    protected double pnlWithFeesAndFunding = 0.0; // PnL including fees and funding
    protected double fundingRateAnnualized = 0.0; // Annualized funding rate for the asset

    protected int openOrdersCount = 0; // Number of open orders
    protected int totalTrades;

    protected Collection<OrderTicket> openOrders;

    protected Set<OrderTicket> executedOrders = new TreeSet<>((o1, o2) -> {
        if (o1.getOrderFilledTime().equals(o2.getOrderFilledTime())) {
            return 0;
        }
        return o1.getOrderFilledTime().isAfter(o2.getOrderFilledTime()) ? -1 : 1;
    });

    public PaperBrokerStatus() {
    }

    @Override
    public double getCurrentPosition() {
        return currentPosition;
    }

    @Override
    public void setCurrentPosition(double currentPosition) {
        this.currentPosition = currentPosition;
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
    public double getDollarVolume() {
        return dollarVolume;
    }

    @Override
    public void setDollarVolume(double dollarVolume) {
        this.dollarVolume = dollarVolume;
    }

    @Override
    public double getFeesCollectedOrPaid() {
        return feesCollectedOrPaid;
    }

    @Override
    public void setFeesCollectedOrPaid(double feesPaid) {
        this.feesCollectedOrPaid = feesPaid;
    }

    @Override
    public int getTotalTrades() {
        return totalTrades;
    }

    @Override
    public void setTotalTrades(int totalTrades) {
        this.totalTrades = totalTrades;
    }

    @Override
    public Collection<OrderTicket> getOpenOrders() {
        return openOrders;
    }

    @Override
    public void setOpenOrders(Collection<OrderTicket> openOrders) {
        this.openOrders = openOrders;
    }

    @Override
    public Set<OrderTicket> getExecutedOrders() {
        return executedOrders;
    }

    @Override
    public void setExecutedOrders(Set<OrderTicket> executedOrders) {
        this.executedOrders = executedOrders;
    }

    @Override
    public double getBestBid() {
        return bestBid;
    }

    @Override
    public void setBestBid(double bestBid) {
        this.bestBid = bestBid;
    }

    @Override
    public double getBestAsk() {
        return bestAsk;
    }

    @Override
    public void setBestAsk(double bestAsk) {
        this.bestAsk = bestAsk;
    }

    @Override
    public double getMidPoint() {
        return midPoint;
    }

    @Override
    public void setMidPoint(double midPoint) {
        this.midPoint = midPoint;
    }

    @Override
    public double getRealizedPnL() {
        return realizedPnL;
    }

    @Override
    public void setRealizedPnL(double realizedPnL) {
        this.realizedPnL = realizedPnL;
    }

    @Override
    public double getUnrealizedPnL() {
        return unrealizedPnL;
    }

    @Override
    public void setUnrealizedPnL(double unrealizedPnL) {
        this.unrealizedPnL = unrealizedPnL;
    }

    @Override
    public double getUnrealizedPnLPercent() {
        return unrealizedPnL;
    }

    @Override
    public int getOpenOrdersCount() {
        return openOrdersCount;
    }

    @Override
    public void setOpenOrdersCount(int openOrdersCount) {
        this.openOrdersCount = openOrdersCount;
    }

    @Override
    public double getTotalPnL() {
        return totalPnL;
    }

    @Override
    public void setTotalPnL(double totalPnL) {
        this.totalPnL = totalPnL;
    }

    @Override
    public double getPnlWithFees() {
        return pnlWithFees;
    }

    @Override
    public void setPnlWithFees(double pnlWithFees) {
        this.pnlWithFees = pnlWithFees;
    }

    @Override
    public String getAsset() {
        return asset;
    }

    @Override
    public void setAsset(String asset) {
        this.asset = asset;
    }

    @Override
    public double getFundingAccruedOrPaid() {
        return fundingAccruedOrPaid;
    }

    @Override
    public void setFundingAccruedOrPaid(double fundingAccruedOrPaid) {
        this.fundingAccruedOrPaid = fundingAccruedOrPaid;
    }

    @Override
    public double getPnlWithFeesAndFunding() {
        return pnlWithFeesAndFunding;
    }

    @Override
    public void setPnlWithFeesAndFunding(double pnlWithFeesAndFunding) {
        this.pnlWithFeesAndFunding = pnlWithFeesAndFunding;
    }

    @Override
    public double getFundingRateAnnualized() {
        return fundingRateAnnualized;
    }

    @Override
    public void setFundingRateAnnualized(double fundingRateAnnualized) {
        this.fundingRateAnnualized = fundingRateAnnualized;
    }

    @Override
    public double getVWAPMidpoint() {
        return vwapMidpoint;
    }

    @Override
    public void setVWAPMidpoint(double vwapMidpoint) {
        this.vwapMidpoint = vwapMidpoint;
    }

    @Override
    public double getCOGMidpoint() {
        return cogMidpoint;
    }

    @Override
    public void setCOGMidpoint(double cogMidpoint) {
        this.cogMidpoint = cogMidpoint;
    }

    @Override
    public String toString() {
        synchronized (openOrders) { // Ensure thread-safe access to openOrders
            return "PaperBrokerStatus [asset=" + asset + ", bestAsk=" + bestAsk + ", bestBid=" + bestBid + ", midPoint="
                    + midPoint + ", vwapMidpoint=" + vwapMidpoint + ", cogMidpoint=" + cogMidpoint
                    + ", currentPosition=" + currentPosition + ", accountValue=" + accountValue + ", realizedPnL="
                    + realizedPnL + ", unrealizedPnL=" + unrealizedPnL + ", totalPnL=" + totalPnL
                    + ", feesCollectedOrPaid=" + feesCollectedOrPaid + ", pnlWithFees=" + pnlWithFees
                    + ", dollarVolume=" + dollarVolume + ", openOrdersCount=" + openOrdersCount + ", totalTrades="
                    + totalTrades + ", openOrders=" + new ArrayList<>(openOrders) + "]";
        }
    }
}
