package com.sumzerotrading.data;

public class CryptoTicker extends GenericTicker {

    public CryptoTicker(String symbol, Exchange exchange) {
        super(symbol);
        this.exchange = exchange;
        this.primaryExchange = exchange;
    }

    @Override
    public InstrumentType getInstrumentType() {
        return InstrumentType.CRYPTO_SPOT;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

}
