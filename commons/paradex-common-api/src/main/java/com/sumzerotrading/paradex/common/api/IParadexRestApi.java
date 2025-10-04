package com.sumzerotrading.paradex.common.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.data.InstrumentDescriptor;
import com.sumzerotrading.data.InstrumentType;
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

    List<OrderTicket> getOpenOrders(String jwtToken, String market);

    RestResponse cancelOrder(String jwtToken, String orderId);

    String placeOrder(String jwtToken, OrderTicket tradeOrder);

    String getJwtToken();

    String getJwtToken(Map<String, String> headers) throws IOException;

    String getOrderMessageSignature(String orderMessage) throws Exception;

    boolean isPublicApiOnly();

    InstrumentDescriptor getInstrumentDescriptor(String symbol);

    InstrumentDescriptor[] getAllInstrumentsForType(InstrumentType instrumentType);

    boolean onboardAccount(String ethereumAddress, String starketAddress, boolean isTestnet) throws Exception;

}