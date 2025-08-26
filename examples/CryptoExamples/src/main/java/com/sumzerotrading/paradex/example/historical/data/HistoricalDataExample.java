/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.paradex.example.historical.data;

import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sumzerotrading.data.BarData;
import com.sumzerotrading.data.BarData.LengthUnit;
import com.sumzerotrading.data.CryptoTicker;
import com.sumzerotrading.data.Exchange;
import com.sumzerotrading.historicaldata.IHistoricalDataProvider.ShowProperty;
import com.sumzerotrading.paradex.historical.ParadexHistoricalDataProvider;

/**
 * o
 * 
 * 
 */
public class HistoricalDataExample {

    protected Logger logger = LoggerFactory.getLogger(HistoricalDataExample.class);

    public void requestHistoricalData() {

        Properties props = new Properties();
        props.setProperty("paradex.rest.url", "https://api.prod.paradex.trade/v1");

        ParadexHistoricalDataProvider provider = new ParadexHistoricalDataProvider();
        provider.init(props);
        provider.connect();

        CryptoTicker ticker = new CryptoTicker("BTC-USD-PERP", Exchange.PARADEX);

        List<BarData> historicalData = provider.requestHistoricalData(ticker, 60, LengthUnit.MINUTE, 1,
                LengthUnit.MINUTE, ShowProperty.MARK_PRICE, false);

        // Process the retrieved historical data
        for (BarData bar : historicalData) {
            logger.info("Bar: {}", bar);
        }
    }

    public static void main(String[] args) {
        new HistoricalDataExample().requestHistoricalData();
    }
}
