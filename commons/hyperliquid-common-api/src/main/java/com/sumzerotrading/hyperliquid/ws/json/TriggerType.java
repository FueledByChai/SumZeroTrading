package com.sumzerotrading.hyperliquid.ws.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public class TriggerType implements OrderType {

    public enum TpSl {
        TP("tp"), SL("sl");

        private final String wire;

        TpSl(String wire) {
            this.wire = wire;
        }

        @JsonValue
        public String getWire() {
            return wire;
        }
    }

    @JsonProperty("isMarket")
    public boolean marketAfterTrigger; // true = market-like child

    @JsonProperty("triggerPx")
    public String triggerPx; // mark price threshold

    @JsonProperty("tpsl")
    public TpSl tpsl; // enum, not free string

    public TriggerType() {
    }

    public TriggerType(boolean marketAfterTrigger, String triggerPx, TpSl tpsl) {
        this.marketAfterTrigger = marketAfterTrigger;
        this.triggerPx = triggerPx;
        this.tpsl = tpsl;
    }
}