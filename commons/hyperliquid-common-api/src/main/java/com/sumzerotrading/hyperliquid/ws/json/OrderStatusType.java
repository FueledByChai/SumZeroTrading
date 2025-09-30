package com.sumzerotrading.hyperliquid.ws.json;

public enum OrderStatusType {
    BAD_ALO_PX_REJECTED("badAloPxRejected"), OPEN("open"), FILLED("filled"), CANCELED("canceled"),
    TRIGGERED("triggered"), REJECTED("rejected"), MARGIN_CANCELED("marginCanceled"),
    VAULT_WITHDRAWAL_CANCELED("vaultWithdrawalCanceled"), OPEN_INTEREST_CAP_CANCELED("openInterestCapCanceled"),
    SELF_TRADE_CANCELED("selfTradeCanceled"), REDUCE_ONLY_CANCELED("reduceOnlyCanceled"),
    SIBLING_FILLED_CANCELED("siblingFilledCanceled"), DELISTED_CANCELED("delistedCanceled"),
    LIQUIDATED_CANCELED("liquidatedCanceled"), SCHEDULED_CANCEL("scheduledCancel"), TICK_REJECTED("tickRejected"),
    MIN_TRADE_NTL_REJECTED("minTradeNtlRejected"), PERP_MARGIN_REJECTED("perpMarginRejected"),
    REDUCE_ONLY_REJECTED("reduceOnlyRejected"), BAD_TRIGGER_PX_REJECTED("badTriggerPxRejected"),
    MARKET_ORDER_NO_LIQUIDITY_REJECTED("marketOrderNoLiquidityRejected"),
    POSITION_INCREASE_AT_OPEN_INTEREST_CAP_REJECTED("positionIncreaseAtOpenInterestCapRejected"),
    POSITION_FLIP_AT_OPEN_INTEREST_CAP_REJECTED("positionFlipAtOpenInterestCapRejected"),
    TOO_AGGRESSIVE_AT_OPEN_INTEREST_CAP_REJECTED("tooAggressiveAtOpenInterestCapRejected"),
    OPEN_INTEREST_INCREASE_REJECTED("openInterestIncreaseRejected"),
    INSUFFICIENT_SPOT_BALANCE_REJECTED("insufficientSpotBalanceRejected"), ORACLE_REJECTED("oracleRejected"),
    PERP_MAX_POSITION_REJECTED("perpMaxPositionRejected");

    private final String value;

    OrderStatusType(String value) {
        this.value = value;
    }

    public static OrderStatusType fromString(String text) {
        for (OrderStatusType b : OrderStatusType.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }

    @Override
    public String toString() {
        return value;
    }
}
