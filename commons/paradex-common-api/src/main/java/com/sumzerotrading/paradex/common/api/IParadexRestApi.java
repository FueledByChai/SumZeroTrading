package com.sumzerotrading.paradex.common.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.order.TradeOrder;
import com.sumzerotrading.data.InstrumentDescriptor;
import com.sumzerotrading.paradex.common.api.historical.OHLCBar;

public interface IParadexRestApi {

    List<Position> getPositionInfo(String jwtToken);

    /**
     * Supported resolutions
     * 
     * resolution in minutes: 1, 3, 5, 15, 30, 60
     * 
     * 
     * @param symbol
     * @param resolutionInMinutes
     * @param lookbackInMinutes
     * @param priceKind
     * @return
     */
    List<OHLCBar> getOHLCBars(String symbol, int resolutionInMinutes, int lookbackInMinutes,
            HistoricalPriceKind priceKind);

    List<TradeOrder> getOpenOrders(String jwtToken, String market);

    void cancelOrder(String jwtToken, String orderId);

    String placeOrder(String jwtToken, TradeOrder tradeOrder);

    String getJwtToken();

    String getJwtToken(Map<String, String> headers) throws IOException;

    String getOrderMessageSignature(String orderMessage) throws Exception;

    boolean isPublicApiOnly();

    InstrumentDescriptor getInstrumentDescriptor(String symbol);

}