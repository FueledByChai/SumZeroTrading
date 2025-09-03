package com.sumzerotrading.paradex;

import java.math.BigDecimal;

import com.sumzerotrading.data.ITickerBuilder;
import com.sumzerotrading.data.InstrumentDescriptor;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;

public class ParadexTickerBuilder implements ITickerBuilder {

    @Override
    public Ticker buildTicker(InstrumentDescriptor descriptor) {
        if (descriptor.getInstrumentType() == InstrumentType.PERPETUAL_FUTURES) {
            Ticker ticker = new Ticker(descriptor.getExchangeSymbol());
            ticker.setContractMultiplier(BigDecimal.ONE).setCurrency(descriptor.getQuoteCurrency())
                    .setExchange(descriptor.getExchange()).setInstrumentType(descriptor.getInstrumentType())
                    .setMinimumTickSize(descriptor.getPriceTickSize())
                    .setOrderSizeIncrement(descriptor.getOrderSizeIncrement())
                    .setPrimaryExchange(descriptor.getExchange()).setSymbol(descriptor.getExchangeSymbol())
                    .setFundingRateInterval(descriptor.getFundingPeriodHours());

            return ticker;
        } else {
            throw new IllegalArgumentException("Unsupported instrument type: " + descriptor.getInstrumentType());
        }
    }
}
