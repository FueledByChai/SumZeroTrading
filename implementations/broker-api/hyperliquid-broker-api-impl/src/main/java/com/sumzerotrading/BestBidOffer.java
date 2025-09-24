package com.sumzerotrading;

import java.math.BigDecimal;

public class BestBidOffer {

    private BigDecimal bid;
    private BigDecimal ask;

    public BestBidOffer(BigDecimal bid, BigDecimal ask) {
        this.bid = bid;
        this.ask = ask;
    }

    public BigDecimal getBid() {
        return bid;
    }

    public BigDecimal getAsk() {
        return ask;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bid == null) ? 0 : bid.hashCode());
        result = prime * result + ((ask == null) ? 0 : ask.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BestBidOffer other = (BestBidOffer) obj;
        if (bid == null) {
            if (other.bid != null)
                return false;
        } else if (!bid.equals(other.bid))
            return false;
        if (ask == null) {
            if (other.ask != null)
                return false;
        } else if (!ask.equals(other.ask))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "BBO [bid=" + bid + ", ask=" + ask + "]";
    }
}
