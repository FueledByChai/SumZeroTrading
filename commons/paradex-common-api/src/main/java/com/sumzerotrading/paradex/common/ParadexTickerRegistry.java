package com.sumzerotrading.paradex.common;

import java.util.HashMap;
import java.util.Map;

import com.sumzerotrading.data.ITickerBuilder;
import com.sumzerotrading.data.InstrumentDescriptor;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.paradex.ParadexTickerBuiler;
import com.sumzerotrading.paradex.common.api.IParadexRestApi;
import com.sumzerotrading.paradex.common.api.ParadexApiFactory;

public class ParadexTickerRegistry implements ITickerBuilder, IParadexTickerRegistry {

    protected static IParadexTickerRegistry instace;
    protected Map<String, Ticker> tickerMap = new HashMap<>();
    protected Map<String, Ticker> commonSymbolMap = new HashMap<>();
    protected Map<InstrumentDescriptor, Ticker> descriptorMap = new HashMap<>();
    protected IParadexRestApi restApi = ParadexApiFactory.getPublicApi();
    protected ITickerBuilder tickerBuilder = new ParadexTickerBuiler();

    public static IParadexTickerRegistry getInstance() {
        if (instace == null) {
            instace = new ParadexTickerRegistry();
        }
        return instace;
    }

    protected ParadexTickerRegistry() {
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
