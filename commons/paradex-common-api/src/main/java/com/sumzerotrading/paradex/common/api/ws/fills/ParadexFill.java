package com.sumzerotrading.paradex.common.api.ws.fills;

import java.util.List;

public class ParadexFill {

    public enum FillType {
        LIQUIDATION, TRANSFER, FILL, SETTLE_MARKET, RPI
    }

    public enum LiquidityType {
        MAKER, TAKER
    }

    public enum Side {
        BUY, SELL
    }

    protected String account;
    protected String clientId;
    protected long createdAt;
    protected String fee;
    protected String feeCurrency;
    protected FillType fillType;
    protected List<String> flags;
    protected String id;
    protected LiquidityType liquidity;
    protected String market;
    protected String orderId;
    protected String price;
    protected String realizedFunding;
    protected String realizedPnl;
    protected String remainingSize;
    protected Side side;
    protected String size;
    protected String underlyingPrice;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getFeeCurrency() {
        return feeCurrency;
    }

    public void setFeeCurrency(String feeCurrency) {
        this.feeCurrency = feeCurrency;
    }

    public FillType getFillType() {
        return fillType;
    }

    public void setFillType(FillType fillType) {
        this.fillType = fillType;
    }

    public List<String> getFlags() {
        return flags;
    }

    public void setFlags(List<String> flags) {
        this.flags = flags;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LiquidityType getLiquidity() {
        return liquidity;
    }

    public void setLiquidity(LiquidityType liquidity) {
        this.liquidity = liquidity;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getRealizedFunding() {
        return realizedFunding;
    }

    public void setRealizedFunding(String realizedFunding) {
        this.realizedFunding = realizedFunding;
    }

    public String getRealizedPnl() {
        return realizedPnl;
    }

    public void setRealizedPnl(String realizedPnl) {
        this.realizedPnl = realizedPnl;
    }

    public String getRemainingSize() {
        return remainingSize;
    }

    public void setRemainingSize(String remainingSize) {
        this.remainingSize = remainingSize;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getUnderlyingPrice() {
        return underlyingPrice;
    }

    public void setUnderlyingPrice(String underlyingPrice) {
        this.underlyingPrice = underlyingPrice;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((account == null) ? 0 : account.hashCode());
        result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
        result = prime * result + (int) (createdAt ^ (createdAt >>> 32));
        result = prime * result + ((fee == null) ? 0 : fee.hashCode());
        result = prime * result + ((feeCurrency == null) ? 0 : feeCurrency.hashCode());
        result = prime * result + ((fillType == null) ? 0 : fillType.hashCode());
        result = prime * result + ((flags == null) ? 0 : flags.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((liquidity == null) ? 0 : liquidity.hashCode());
        result = prime * result + ((market == null) ? 0 : market.hashCode());
        result = prime * result + ((orderId == null) ? 0 : orderId.hashCode());
        result = prime * result + ((price == null) ? 0 : price.hashCode());
        result = prime * result + ((realizedFunding == null) ? 0 : realizedFunding.hashCode());
        result = prime * result + ((realizedPnl == null) ? 0 : realizedPnl.hashCode());
        result = prime * result + ((remainingSize == null) ? 0 : remainingSize.hashCode());
        result = prime * result + ((side == null) ? 0 : side.hashCode());
        result = prime * result + ((size == null) ? 0 : size.hashCode());
        result = prime * result + ((underlyingPrice == null) ? 0 : underlyingPrice.hashCode());
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
        ParadexFill other = (ParadexFill) obj;
        if (account == null) {
            if (other.account != null)
                return false;
        } else if (!account.equals(other.account))
            return false;
        if (clientId == null) {
            if (other.clientId != null)
                return false;
        } else if (!clientId.equals(other.clientId))
            return false;
        if (createdAt != other.createdAt)
            return false;
        if (fee == null) {
            if (other.fee != null)
                return false;
        } else if (!fee.equals(other.fee))
            return false;
        if (feeCurrency == null) {
            if (other.feeCurrency != null)
                return false;
        } else if (!feeCurrency.equals(other.feeCurrency))
            return false;
        if (fillType != other.fillType)
            return false;
        if (flags == null) {
            if (other.flags != null)
                return false;
        } else if (!flags.equals(other.flags))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (liquidity != other.liquidity)
            return false;
        if (market == null) {
            if (other.market != null)
                return false;
        } else if (!market.equals(other.market))
            return false;
        if (orderId == null) {
            if (other.orderId != null)
                return false;
        } else if (!orderId.equals(other.orderId))
            return false;
        if (price == null) {
            if (other.price != null)
                return false;
        } else if (!price.equals(other.price))
            return false;
        if (realizedFunding == null) {
            if (other.realizedFunding != null)
                return false;
        } else if (!realizedFunding.equals(other.realizedFunding))
            return false;
        if (realizedPnl == null) {
            if (other.realizedPnl != null)
                return false;
        } else if (!realizedPnl.equals(other.realizedPnl))
            return false;
        if (remainingSize == null) {
            if (other.remainingSize != null)
                return false;
        } else if (!remainingSize.equals(other.remainingSize))
            return false;
        if (side != other.side)
            return false;
        if (size == null) {
            if (other.size != null)
                return false;
        } else if (!size.equals(other.size))
            return false;
        if (underlyingPrice == null) {
            if (other.underlyingPrice != null)
                return false;
        } else if (!underlyingPrice.equals(other.underlyingPrice))
            return false;
        return true;
    }

}
