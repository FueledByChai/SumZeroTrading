package com.sumzerotrading.paradex.common.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.broker.order.TradeOrder;
import com.sumzerotrading.data.BarData;
import com.sumzerotrading.paradex.common.api.historical.OHLCBar;
import com.sumzerotrading.paradex.common.api.order.OrderType;
import com.sumzerotrading.paradex.common.api.order.ParadexOrder;
import com.sumzerotrading.paradex.common.api.order.Side;

public class ParadexUtil {

    public static TradeOrder translateOrder(ParadexOrder order) {
        TradeOrder tradeOrder = new TradeOrder();

        return tradeOrder;
    }

    public static ParadexOrder translateOrder(TradeOrder order) {
        ParadexOrder paradoxOrder = new ParadexOrder();
        paradoxOrder.setClientId(order.getReference());
        paradoxOrder.setTicker(order.getTicker().getSymbol());

        if (order.getTradeDirection() == TradeDirection.BUY) {
            paradoxOrder.setSide(Side.BUY);
        } else {
            paradoxOrder.setSide(Side.SELL);
        }

        paradoxOrder.setSize(order.getSizeAsBigDecimal());

        if (order.getType() == TradeOrder.Type.MARKET) {
            paradoxOrder.setOrderType(OrderType.MARKET);
        } else if (order.getType() == TradeOrder.Type.LIMIT) {
            paradoxOrder.setOrderType(OrderType.LIMIT);
        } else if (order.getType() == TradeOrder.Type.STOP) {
            paradoxOrder.setOrderType(OrderType.STOP);
        } else {
            throw new UnsupportedOperationException("Order type " + order.getType() + " is not supported");
        }

        if (order.getType() == TradeOrder.Type.LIMIT) {
            paradoxOrder.setLimitPrice(order.getLimitPriceAsBigDecimal());
        }

        return paradoxOrder;
    }

    public static List<TradeOrder> translateOrders(List<ParadexOrder> orders) {
        return orders.stream().map(ParadexUtil::translateOrder).collect(Collectors.toList());
    }

    public static BarData translateBar(OHLCBar bar) {
        BarData barData = new BarData(java.time.Instant.ofEpochSecond(bar.getTime()).atZone(ZoneOffset.UTC),
                new BigDecimal(String.valueOf(bar.getOpen())), new BigDecimal(String.valueOf(bar.getHigh())),
                new BigDecimal(String.valueOf(bar.getLow())), new BigDecimal(String.valueOf(bar.getClose())),
                new BigDecimal(String.valueOf(bar.getVolume())));
        return barData;
    }

    public static List<BarData> translateBars(List<OHLCBar> bars) {
        return bars.stream().map(ParadexUtil::translateBar).collect(Collectors.toList());
    }

}
