package com.sumzerotrading.hyperliquid.ws;

import java.util.HashMap;
import java.util.Map;

import com.sumzerotrading.data.ITickerBuilder;
import com.sumzerotrading.data.InstrumentDescriptor;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.util.ITickerRegistry;

public class HyperliquidTickerRegistry implements ITickerBuilder, ITickerRegistry {

    protected static ITickerRegistry instace;
    protected Map<String, Ticker> tickerMap = new HashMap<>();
    protected Map<String, Ticker> commonSymbolMap = new HashMap<>();
    protected Map<InstrumentDescriptor, Ticker> descriptorMap = new HashMap<>();
    protected IHyperliquidRestApi restApi = HyperliquidApiFactory.getRestApi();
    protected ITickerBuilder tickerBuilder = new HyperliquidTickerBuilder();

    public static ITickerRegistry getInstance() {
        if (instace == null) {
            instace = new HyperliquidTickerRegistry();
        }
        return instace;
    }

    protected HyperliquidTickerRegistry() {
        try {
            for (InstrumentDescriptor descriptor : restApi.getAllInstrumentsForType(InstrumentType.PERPETUAL_FUTURES)) {
                buildTicker(descriptor);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ParadexTickerRegistry", e);
        }
    }

    @Override
    public Ticker lookupByBrokerSymbol(String tickerString) {
        return (Ticker) tickerMap.get(tickerString);
    }

    @Override
    public Ticker lookupByCommonSymbol(String commonSymbol) {
        return (Ticker) commonSymbolMap.get(commonSymbol);
    }

    @Override
    public Ticker buildTicker(InstrumentDescriptor descriptor) {
        if (descriptor.getInstrumentType() == InstrumentType.PERPETUAL_FUTURES) {

            Ticker ticker = tickerBuilder.buildTicker(descriptor);
            descriptorMap.put(descriptor, ticker);
            commonSymbolMap.put(descriptor.getCommonSymbol(), ticker);
            tickerMap.put(ticker.getSymbol(), ticker);

            return ticker;
        } else {
            throw new IllegalArgumentException("Unsupported instrument type: " + descriptor.getInstrumentType());
        }
    }
}
