package com.sumzerotrading.hyperliquid.websocket.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;

public class OrderAction {
    public String type = "order";
    public List<Order> orders;
    public String grouping = "na"; // "na" | "normalTpsl" | "positionTpsl"

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Map<String, Object> builder; // optional: {"b":"0x..","f":10}

    public OrderAction(List<Order> orders) {
        this.orders = orders;
        this.grouping = "na";
        this.builder = null;
    }

    public OrderAction(List<OrderJson> orders, Builder builder) {
        this.orders = orders;
        this.builder = builder;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", type);
        JsonArray ordersArray = new JsonArray();
        for (OrderJson order : orders) {
            ordersArray.add(order.toJson());
        }
        obj.add("orders", ordersArray);
        obj.addProperty("grouping", grouping);
        if (builder != null) {
            obj.add("builder", builder.toJson());
        }
        return obj;
    }

    public static class Builder {
        public String b; // address
        public Number f; // fee

        public Builder(String b, Number f) {
            this.b = b;
            this.f = f;
        }

        public JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("b", b);
            obj.addProperty("f", f);
            return obj;
        }
    }
}
