package com.sumzerotrading.hyperliquid.websocket.json;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonPropertyOrder({ "tif" })
public class LimitType implements OrderType {

    public enum TimeInForce {
        ALO("Alo"), IOC("Ioc"), GTC("Gtc");

        private final String wireName;

        TimeInForce(String wireName) {
            this.wireName = wireName;
        }

        @JsonValue
        public String getWireName() {
            return wireName;
        }

        @Override
        public String toString() {
            return wireName;
        }
    }

    public TimeInForce tif = TimeInForce.GTC;

    public LimitType() {
    }

    public LimitType(TimeInForce tif) {
        this.tif = tif;
    }

}