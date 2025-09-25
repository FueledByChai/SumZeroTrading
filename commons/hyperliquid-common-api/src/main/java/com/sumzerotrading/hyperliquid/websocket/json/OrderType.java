package com.sumzerotrading.hyperliquid.websocket.json;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({ @JsonSubTypes.Type(value = LimitType.class, name = "limit"),
        @JsonSubTypes.Type(value = TriggerType.class, name = "trigger") })
public interface OrderType {

}