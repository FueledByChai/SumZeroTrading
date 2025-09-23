package com.sumzerotrading.broker.hyperliquid.translators;

import java.util.List;
import java.util.stream.Collectors;

import com.sumzerotrading.broker.Position;
import com.sumzerotrading.broker.hyperliquid.HyperliquidPositionUpdate;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.hyperliquid.websocket.HyperliquidTickerRegistry;

public class Translator {

    public static List<Position> translatePositions(List<HyperliquidPositionUpdate> positionUpdates) {
        if (positionUpdates == null)
            return null;
        return positionUpdates.stream().map(Translator::translatePosition).collect(Collectors.toList());
    }

    public static Position translatePosition(HyperliquidPositionUpdate positionUpdate) {
        if (positionUpdate == null)
            return null;
        Ticker ticker = HyperliquidTickerRegistry.getInstance().lookupByBrokerSymbol(positionUpdate.getTicker());

        return new Position(ticker).setSize(positionUpdate.getSize()).setAverageCost(positionUpdate.getEntryPrice())
                .setLiquidationPrice(positionUpdate.getLiquidationPrice());
    }
}
