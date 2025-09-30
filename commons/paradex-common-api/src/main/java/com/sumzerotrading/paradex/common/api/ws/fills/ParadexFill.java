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

    public String account;
    public String clientId;
    public long createdAt;
    public String fee;
    public String feeCurrency;
    public FillType fillType;
    public List<String> flags;
    public String id;
    public LiquidityType liquidity;
    public String market;
    public String orderId;
    public String price;
    public String realizedFunding;
    public String realizedPnl;
    public String remainingSize;
    public Side side;
    public String size;
    public String underlyingPrice;

}
