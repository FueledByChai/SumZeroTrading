package com.sumzerotrading.hyperliquid.websocket.json;

import org.msgpack.jackson.dataformat.MessagePackFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public final class Mappers {
    public static final ObjectMapper JSON = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);

    public static final ObjectMapper MSGPACK = new ObjectMapper(new MessagePackFactory())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
}
