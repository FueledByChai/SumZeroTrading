package com.sumzerotrading.paradex.common.api.order;

public enum Side {
    BUY("1"), SELL("2");

    private String chainSide;

    Side(String chainSide) {
        this.chainSide = chainSide;
    }

    public String getChainSide() {
        return chainSide;
    }
}
