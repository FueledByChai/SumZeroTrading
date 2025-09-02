/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.ib.example.historical.data;

import java.util.List;

import com.sumzerotrading.data.BarData;
import com.sumzerotrading.data.BarData.LengthUnit;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.historicaldata.IHistoricalDataProvider.ShowProperty;
import com.sumzerotrading.interactive.brokers.client.InteractiveBrokersClient;
import com.sumzerotrading.interactive.brokers.client.InteractiveBrokersClientInterface;

/**
 *
 * @author RobTerpilowski
 */
public class HistoricalDataExample {

    public void requestHistoricalData() {
        InteractiveBrokersClientInterface client = InteractiveBrokersClient.getInstance("localhost", 6468, 1);
        client.connect();

        Ticker ticker = new Ticker("AMZN").setInstrumentType(InstrumentType.STOCK);
        int duration = 1;
        LengthUnit durationUnit = LengthUnit.DAY;
        int barSize = 1;
        LengthUnit barSizeUnit = LengthUnit.MINUTE;
        ShowProperty dataToRequest = ShowProperty.TRADES;

        List<BarData> historicalData = client.requestHistoricalData(ticker, duration, durationUnit, barSize,
                barSizeUnit, dataToRequest);

        System.out.println("Retrieved " + historicalData.size() + " bars");
        historicalData.stream().forEach((bar) -> {
            System.out.println("Retrieved Bar: " + bar);
        });

        client.disconnect();
    }

    public static void main(String[] args) {
        new HistoricalDataExample().requestHistoricalData();
    }
}
