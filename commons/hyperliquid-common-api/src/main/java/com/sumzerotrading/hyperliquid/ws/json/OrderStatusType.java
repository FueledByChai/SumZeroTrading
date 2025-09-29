package com.sumzerotrading.hyperliquid.ws.json;

public enum OrderStatusType {
    BAD_ALO_PX_REJECTED("bad_alo_px_rejected"), OPEN("open"), FILLED("filled"), CANCELED("canceled"),
    TRIGGERED("triggered"), REJECTED("rejected"), MARGIN_CANCELED("margin_canceled"),
    VAULT_WITHDRAWAL_CANCELED("vault_withdrawal_canceled"), OPEN_INTEREST_CAP_CANCELED("open_interest_cap_canceled"),
    SELF_TRADE_CANCELED("self_trade_canceled"), REDUCE_ONLY_CANCELED("reduce_only_canceled"),
    SIBLING_FILLED_CANCELED("sibling_filled_canceled"), DELISTED_CANCELED("delisted_canceled"),
    LIQUIDATED_CANCELED("liquidated_canceled"), SCHEDULED_CANCEL("scheduled_cancel"), TICK_REJECTED("tick_rejected"),
    MIN_TRADE_NTL_REJECTED("min_trade_ntl_rejected"), PERP_MARGIN_REJECTED("perp_margin_rejected"),
    REDUCE_ONLY_REJECTED("reduce_only_rejected"), BAD_TRIGGER_PX_REJECTED("bad_trigger_px_rejected"),
    MARKET_ORDER_NO_LIQUIDITY_REJECTED("market_order_no_liquidity_rejected"),
    POSITION_INCREASE_AT_OPEN_INTEREST_CAP_REJECTED("position_increase_at_open_interest_cap_rejected"),
    POSITION_FLIP_AT_OPEN_INTEREST_CAP_REJECTED("position_flip_at_open_interest_cap_rejected"),
    TOO_AGGRESSIVE_AT_OPEN_INTEREST_CAP_REJECTED("too_aggressive_at_open_interest_cap_rejected"),
    OPEN_INTEREST_INCREASE_REJECTED("open_interest_increase_rejected"),
    INSUFFICIENT_SPOT_BALANCE_REJECTED("insufficient_spot_balance_rejected"), ORACLE_REJECTED("oracle_rejected"),
    PERP_MAX_POSITION_REJECTED("perp_max_position_rejected");

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
