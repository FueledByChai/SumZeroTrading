package com.sumzerotrading.broker.paradex;

public interface ParadexOrderStatusListener {

    public void orderStatusUpdated(IParadexOrderStatusUpdate orderStatus);
}
