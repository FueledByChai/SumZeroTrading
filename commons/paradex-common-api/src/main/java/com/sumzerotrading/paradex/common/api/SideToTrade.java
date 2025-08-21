package com.sumzerotrading.paradex.common.api;

public enum SideToTrade {

    LONG("Long"), SHORT("Short"), BOTH("Both");

    private String side;

    SideToTrade(String side) {
        this.side = side;
    }

    public String getSide() {
        return side;
    }
}
