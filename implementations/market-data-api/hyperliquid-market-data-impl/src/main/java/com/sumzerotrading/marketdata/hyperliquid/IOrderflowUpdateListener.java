package com.sumzerotrading.marketdata.hyperliquid;

import com.sumzerotrading.marketdata.OrderFlow;

public interface IOrderflowUpdateListener {

    void onOrderflowUpdate(OrderFlow orderFlow);
}
