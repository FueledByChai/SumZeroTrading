package com.sumzerotrading.broker.paper;

public class PaperBrokerCommission {
    public static final PaperBrokerCommission PARADEX_COMMISSION = new PaperBrokerCommission(-0.5, 3.0);
    public static final PaperBrokerCommission PARADEX_PROMO_COMMISSION = new PaperBrokerCommission(0.0, 3.0);
    public static final PaperBrokerCommission HYPERLIQUID_COMMISSION = new PaperBrokerCommission(1.5, 4.5);

    protected double makerFeeBps = 0.0;
    protected double takerFeeBps = 0.0;

    public PaperBrokerCommission(double makerFeeBps, double takerFeeBps) {
        this.makerFeeBps = makerFeeBps;
        this.takerFeeBps = takerFeeBps;
    }

    public double getMakerFeeBps() {
        return makerFeeBps;
    }

    public double getTakerFeeBps() {
        return takerFeeBps;
    }

    @Override
    public String toString() {
        return "PaperBrokerCommission{" + "makerFeeBps=" + makerFeeBps + ", takerFeeBps=" + takerFeeBps + '}';
    }

}
