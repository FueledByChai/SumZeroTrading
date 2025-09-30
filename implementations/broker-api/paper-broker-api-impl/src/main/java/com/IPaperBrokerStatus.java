package com;

import java.util.Collection;
import java.util.Set;

import com.sumzerotrading.broker.order.OrderTicket;

public interface IPaperBrokerStatus {

    double getCurrentPosition();

    void setCurrentPosition(double currentPosition);

    double getAccountValue();

    void setAccountValue(double accountValue);

    double getDollarVolume();

    void setDollarVolume(double dollarVolume);

    double getFeesCollectedOrPaid();

    void setFeesCollectedOrPaid(double feesPaid);

    int getTotalTrades();

    void setTotalTrades(int totalTrades);

    Collection<OrderTicket> getOpenOrders();

    void setOpenOrders(Collection<OrderTicket> openOrders);

    Set<OrderTicket> getExecutedOrders();

    void setExecutedOrders(Set<OrderTicket> executedOrders);

    double getBestBid();

    void setBestBid(double bestBid);

    double getBestAsk();

    void setBestAsk(double bestAsk);

    double getMidPoint();

    void setMidPoint(double midPoint);

    double getVWAPMidpoint();

    void setVWAPMidpoint(double vwapMidpoint);

    double getCOGMidpoint();

    void setCOGMidpoint(double cogMidpoint);

    double getRealizedPnL();

    void setRealizedPnL(double realizedPnL);

    double getUnrealizedPnL();

    void setUnrealizedPnL(double unrealizedPnL);

    double getUnrealizedPnLPercent();

    int getOpenOrdersCount();

    void setOpenOrdersCount(int openOrdersCount);

    double getTotalPnL();

    void setTotalPnL(double totalPnL);

    double getPnlWithFees();

    void setPnlWithFees(double pnlWithFees);

    double getPnlWithFeesAndFunding();

    void setPnlWithFeesAndFunding(double pnlWithFeesAndFunding);

    String getAsset();

    void setAsset(String asset);

    double getFundingAccruedOrPaid();

    void setFundingAccruedOrPaid(double fundingAccruedOrPaid);

    double getFundingRateAnnualized();

    void setFundingRateAnnualized(double fundingRateAnnualized);

}