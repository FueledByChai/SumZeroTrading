package com.sumzerotrading.broker.hyperliquid.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;

public class OrderAction {
    private String type = "order";
    private List<OrderJson> orders;
    private String grouping;
    private Builder builder;

    public OrderAction(List<OrderJson> orders) {
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
