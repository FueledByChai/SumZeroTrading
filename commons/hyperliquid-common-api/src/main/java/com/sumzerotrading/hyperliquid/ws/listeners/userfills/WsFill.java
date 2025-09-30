package com.sumzerotrading.hyperliquid.ws.listeners.userfills;

public class WsFill {

    protected String coin;
    protected String price;
    protected String size;
    protected String side;
    protected long time;
    protected String startPosition;
    protected String dir;
    protected String closedPnl;
    protected String hash;
    protected long orderId;
    protected boolean taker;
    protected String fee;
    protected long tradeId;
    protected String feeToken;
    protected String builderFee;

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(String startPosition) {
        this.startPosition = startPosition;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getClosedPnl() {
        return closedPnl;
    }

    public void setClosedPnl(String closedPnl) {
        this.closedPnl = closedPnl;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public boolean isTaker() {
        return taker;
    }

    public void setTaker(boolean taker) {
        this.taker = taker;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public long getTradeId() {
        return tradeId;
    }

    public void setTradeId(long tradeId) {
        this.tradeId = tradeId;
    }

    public String getFeeToken() {
        return feeToken;
    }

    public void setFeeToken(String feeToken) {
        this.feeToken = feeToken;
    }

    public String getBuilderFee() {
        return builderFee;
    }

    public void setBuilderFee(String builderFee) {
        this.builderFee = builderFee;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((coin == null) ? 0 : coin.hashCode());
        result = prime * result + ((price == null) ? 0 : price.hashCode());
        result = prime * result + ((size == null) ? 0 : size.hashCode());
        result = prime * result + ((side == null) ? 0 : side.hashCode());
        result = prime * result + (int) (time ^ (time >>> 32));
        result = prime * result + ((startPosition == null) ? 0 : startPosition.hashCode());
        result = prime * result + ((dir == null) ? 0 : dir.hashCode());
        result = prime * result + ((closedPnl == null) ? 0 : closedPnl.hashCode());
        result = prime * result + ((hash == null) ? 0 : hash.hashCode());
        result = prime * result + (int) (orderId ^ (orderId >>> 32));
        result = prime * result + (taker ? 1231 : 1237);
        result = prime * result + ((fee == null) ? 0 : fee.hashCode());
        result = prime * result + (int) (tradeId ^ (tradeId >>> 32));
        result = prime * result + ((feeToken == null) ? 0 : feeToken.hashCode());
        result = prime * result + ((builderFee == null) ? 0 : builderFee.hashCode());
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
        WsFill other = (WsFill) obj;
        if (coin == null) {
            if (other.coin != null)
                return false;
        } else if (!coin.equals(other.coin))
            return false;
        if (price == null) {
            if (other.price != null)
                return false;
        } else if (!price.equals(other.price))
            return false;
        if (size == null) {
            if (other.size != null)
                return false;
        } else if (!size.equals(other.size))
            return false;
        if (side == null) {
            if (other.side != null)
                return false;
        } else if (!side.equals(other.side))
            return false;
        if (time != other.time)
            return false;
        if (startPosition == null) {
            if (other.startPosition != null)
                return false;
        } else if (!startPosition.equals(other.startPosition))
            return false;
        if (dir == null) {
            if (other.dir != null)
                return false;
        } else if (!dir.equals(other.dir))
            return false;
        if (closedPnl == null) {
            if (other.closedPnl != null)
                return false;
        } else if (!closedPnl.equals(other.closedPnl))
            return false;
        if (hash == null) {
            if (other.hash != null)
                return false;
        } else if (!hash.equals(other.hash))
            return false;
        if (orderId != other.orderId)
            return false;
        if (taker != other.taker)
            return false;
        if (fee == null) {
            if (other.fee != null)
                return false;
        } else if (!fee.equals(other.fee))
            return false;
        if (tradeId != other.tradeId)
            return false;
        if (feeToken == null) {
            if (other.feeToken != null)
                return false;
        } else if (!feeToken.equals(other.feeToken))
            return false;
        if (builderFee == null) {
            if (other.builderFee != null)
                return false;
        } else if (!builderFee.equals(other.builderFee))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "WsFill [coin=" + coin + ", price=" + price + ", size=" + size + ", side=" + side + ", time=" + time
                + ", startPosition=" + startPosition + ", dir=" + dir + ", closedPnl=" + closedPnl + ", hash=" + hash
                + ", orderId=" + orderId + ", taker=" + taker + ", fee=" + fee + ", tradeId=" + tradeId + ", feeToken="
                + feeToken + ", builderFee=" + builderFee + "]";
    }

}
