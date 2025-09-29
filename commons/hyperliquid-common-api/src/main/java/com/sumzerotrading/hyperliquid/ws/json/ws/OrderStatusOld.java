package com.sumzerotrading.hyperliquid.ws.json.ws;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderStatusOld {

    /**
     * open
     * 
     * Placed successfully
     * 
     * filled
     * 
     * Filled
     * 
     * canceled
     * 
     * Canceled by user
     * 
     * triggered
     * 
     * Trigger order triggered
     * 
     * rejected
     * 
     * Rejected at time of placement
     * 
     * marginCanceled
     * 
     * Canceled because insufficient margin to fill
     * 
     * vaultWithdrawalCanceled
     * 
     * Vaults only. Canceled due to a user's withdrawal from vault
     * 
     * openInterestCapCanceled
     * 
     * Canceled due to order being too aggressive when open interest was at cap
     * 
     * selfTradeCanceled
     * 
     * Canceled due to self-trade prevention
     * 
     * reduceOnlyCanceled
     * 
     * Canceled reduced-only order that does not reduce position
     * 
     * siblingFilledCanceled
     * 
     * TP/SL only. Canceled due to sibling ordering being filled
     * 
     * delistedCanceled
     * 
     * Canceled due to asset delisting
     * 
     * liquidatedCanceled
     * 
     * Canceled due to liquidation
     * 
     * scheduledCancel
     * 
     * API only. Canceled due to exceeding scheduled cancel deadline (dead man's
     * switch)
     * 
     * tickRejected
     * 
     * Rejected due to invalid tick price
     * 
     * minTradeNtlRejected
     * 
     * Rejected due to order notional below minimum
     * 
     * perpMarginRejected
     * 
     * Rejected due to insufficient margin
     * 
     * reduceOnlyRejected
     * 
     * Rejected due to reduce only
     * 
     * badAloPxRejected
     * 
     * Rejected due to post-only immediate match
     * 
     * iocCancelRejected
     * 
     * Rejected due to IOC not able to match
     * 
     * badTriggerPxRejected
     * 
     * Rejected due to invalid TP/SL price
     * 
     * marketOrderNoLiquidityRejected
     * 
     * Rejected due to lack of liquidity for market order
     * 
     * positionIncreaseAtOpenInterestCapRejected
     * 
     * Rejected due to open interest cap
     * 
     * positionFlipAtOpenInterestCapRejected
     * 
     * Rejected due to open interest cap
     * 
     * tooAggressiveAtOpenInterestCapRejected
     * 
     * Rejected due to price too aggressive at open interest cap
     * 
     * openInterestIncreaseRejected
     * 
     * Rejected due to open interest cap
     * 
     * insufficientSpotBalanceRejected
     * 
     * Rejected due to insufficient spot balance
     * 
     * oracleRejected
     * 
     * Rejected due to price too far from oracle
     * 
     * perpMaxPositionRejected
     * 
     * Rejected due to exceeding margin tier limit at current leverage
     */

    public enum Status {
        BAD_ALO_PX_REJECTED("bad_alo_px_rejected"), OPEN("open"), FILLED("filled"), CANCELED("canceled"),
        TRIGGERED("triggered"), REJECTED("rejected"), MARGIN_CANCELED("margin_canceled"),
        VAULT_WITHDRAWAL_CANCELED("vault_withdrawal_canceled"),
        OPEN_INTEREST_CAP_CANCELED("open_interest_cap_canceled"), SELF_TRADE_CANCELED("self_trade_canceled"),
        REDUCE_ONLY_CANCELED("reduce_only_canceled"), SIBLING_FILLED_CANCELED("sibling_filled_canceled"),
        DELISTED_CANCELED("delisted_canceled"), LIQUIDATED_CANCELED("liquidated_canceled"),
        SCHEDULED_CANCEL("scheduled_cancel"), TICK_REJECTED("tick_rejected"),
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

        Status(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @JsonProperty("order")
    public OrderDetail order;

    @JsonProperty("status")
    public Status status;

    @JsonProperty("statusTimestamp")
    public long timestamp;
}
