package com.sumzerotrading.broker.order;

public interface FillEventListener {

    void fillReceived(Fill fill);
}
