package com.sumzerotrading.data;

import java.math.BigDecimal;

public class InstrumentDescriptor {

    protected Exchange exchange;
    protected String commonSymbol;
    protected String exchangeSymbol;
    protected String baseCurrency;
    protected String quoteCurrency;
    protected BigDecimal orderSizeIncrement = BigDecimal.ONE;
    protected BigDecimal priceTickSize;
    protected int minNotionalOrderSize = 1;
    protected BigDecimal minOrderSize = BigDecimal.ONE;
    protected int fundingPeriodHours = 8;
    protected InstrumentType instrumentType;
    protected BigDecimal contractMultiplier = BigDecimal.ONE;

    public InstrumentDescriptor(InstrumentType instrumentType, Exchange exchange, String commonSymbol,
            String exchangeSymbol, String baseCurrency, String quoteCurrency, BigDecimal orderSizeIncrement,
            BigDecimal priceTickSize, int minNotionalOrderSize, BigDecimal minOrderSize, int fundingPeriodHours,
            BigDecimal contractMultiplier) {
        this.instrumentType = instrumentType;
        this.exchange = exchange;
        this.commonSymbol = commonSymbol;
        this.exchangeSymbol = exchangeSymbol;
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
        this.orderSizeIncrement = orderSizeIncrement;
        this.priceTickSize = priceTickSize;
        this.minNotionalOrderSize = minNotionalOrderSize;
        this.minOrderSize = minOrderSize;
        this.fundingPeriodHours = fundingPeriodHours;
        this.contractMultiplier = contractMultiplier;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public String getCommonSymbol() {
        return commonSymbol;
    }

    public String getExchangeSymbol() {
        return exchangeSymbol;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public BigDecimal getOrderSizeIncrement() {
        return orderSizeIncrement;
    }

    public BigDecimal getPriceTickSize() {
        return priceTickSize;
    }

    public int getMinNotionalOrderSize() {
        return minNotionalOrderSize;
    }

    public int getFundingPeriodHours() {
        return fundingPeriodHours;
    }

    public InstrumentType getInstrumentType() {
        return instrumentType;
    }

    public BigDecimal getContractMultiplier() {
        return contractMultiplier;
    }

    public BigDecimal getMinOrderSize() {
        return minOrderSize;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((instrumentType == null) ? 0 : instrumentType.hashCode());
        result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
        result = prime * result + ((commonSymbol == null) ? 0 : commonSymbol.hashCode());
        result = prime * result + ((exchangeSymbol == null) ? 0 : exchangeSymbol.hashCode());
        result = prime * result + ((baseCurrency == null) ? 0 : baseCurrency.hashCode());
        result = prime * result + ((quoteCurrency == null) ? 0 : quoteCurrency.hashCode());
        result = prime * result + ((orderSizeIncrement == null) ? 0 : orderSizeIncrement.hashCode());
        result = prime * result + ((priceTickSize == null) ? 0 : priceTickSize.hashCode());
        result = prime * result + minNotionalOrderSize;
        result = prime * result + fundingPeriodHours;
        result = prime * result + ((contractMultiplier == null) ? 0 : contractMultiplier.hashCode());
        result = prime * result + ((minOrderSize == null) ? 0 : minOrderSize.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        InstrumentDescriptor other = (InstrumentDescriptor) obj;
        if (instrumentType != other.instrumentType)
            return false;
        if (exchange == null) {
            if (other.exchange != null)
                return false;
        } else if (!exchange.equals(other.exchange))
            return false;
        if (commonSymbol == null) {
            if (other.commonSymbol != null)
                return false;
        } else if (!commonSymbol.equals(other.commonSymbol))
            return false;
        if (exchangeSymbol == null) {
            if (other.exchangeSymbol != null)
                return false;
        } else if (!exchangeSymbol.equals(other.exchangeSymbol))
            return false;
        if (baseCurrency == null) {
            if (other.baseCurrency != null)
                return false;
        } else if (!baseCurrency.equals(other.baseCurrency))
            return false;
        if (quoteCurrency == null) {
            if (other.quoteCurrency != null)
                return false;
        } else if (!quoteCurrency.equals(other.quoteCurrency))
            return false;
        if (orderSizeIncrement == null) {
            if (other.orderSizeIncrement != null)
                return false;
        } else if (!orderSizeIncrement.equals(other.orderSizeIncrement))
            return false;
        if (priceTickSize == null) {
            if (other.priceTickSize != null)
                return false;
        } else if (!priceTickSize.equals(other.priceTickSize))
            return false;
        if (minNotionalOrderSize != other.minNotionalOrderSize)
            return false;
        if (fundingPeriodHours != other.fundingPeriodHours)
            return false;
        if (contractMultiplier == null) {
            if (other.contractMultiplier != null)
                return false;
        } else if (!contractMultiplier.equals(other.contractMultiplier))
            return false;
        if (minOrderSize == null) {
            if (other.minOrderSize != null)
                return false;
        } else if (!minOrderSize.equals(other.minOrderSize))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "InstrumentDescriptor [instrumentType=" + instrumentType + ", exchange=" + exchange + ", commonSymbol="
                + commonSymbol + ", exchangeSymbol=" + exchangeSymbol + ", baseCurrency=" + baseCurrency
                + ", quoteCurrency=" + quoteCurrency + ", orderSizeIncrement=" + orderSizeIncrement + ", priceTickSize="
                + priceTickSize + ", minNotionalOrderSize=" + minNotionalOrderSize + ", fundingPeriodHours="
                + fundingPeriodHours + ", contractMultiplier=" + contractMultiplier + ", minOrderSize=" + minOrderSize
                + "]";
    }

}
