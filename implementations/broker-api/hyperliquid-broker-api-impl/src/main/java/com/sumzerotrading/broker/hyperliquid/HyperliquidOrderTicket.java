package com.sumzerotrading.broker.hyperliquid;

import com.sumzerotrading.BestBidOffer;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.hyperliquid.ws.json.Signature;

public class HyperliquidOrderTicket {

    protected BestBidOffer bestBidOffer;
    protected OrderTicket orderTicket;
    protected long nonce;
    protected Signature signature;

    public HyperliquidOrderTicket(BestBidOffer bestBidOffer, OrderTicket orderTicket) {
        this.bestBidOffer = bestBidOffer;
        this.orderTicket = orderTicket;
    }

    public BestBidOffer getBestBidOffer() {
        return bestBidOffer;
    }

    public OrderTicket getOrderTicket() {
        return orderTicket;
    }

    @Override
    public String toString() {
        return "HyperliquidOrderTicket [bestBidOffer=" + bestBidOffer + ", orderTicket=" + orderTicket + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bestBidOffer == null) ? 0 : bestBidOffer.hashCode());
        result = prime * result + ((orderTicket == null) ? 0 : orderTicket.hashCode());
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
        HyperliquidOrderTicket other = (HyperliquidOrderTicket) obj;
        if (bestBidOffer == null) {
            if (other.bestBidOffer != null)
                return false;
        } else if (!bestBidOffer.equals(other.bestBidOffer))
            return false;
        if (orderTicket == null) {
            if (other.orderTicket != null)
                return false;
        } else if (!orderTicket.equals(other.orderTicket))
            return false;
        return true;
    }

}
