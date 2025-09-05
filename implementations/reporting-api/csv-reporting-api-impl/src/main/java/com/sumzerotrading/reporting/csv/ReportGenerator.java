/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.reporting.csv;

import com.sumzerotrading.broker.order.OrderEvent;
import com.sumzerotrading.broker.order.OrderStatus.Status;
import com.sumzerotrading.broker.order.OrderTicket;
import com.sumzerotrading.reporting.IReportGenerator;
import com.sumzerotrading.reporting.IRoundTrip;
import com.sumzerotrading.reporting.TradeReferenceLine;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author RobTerpilowski
 */
public class ReportGenerator implements IReportGenerator {

    protected Logger logger = Logger.getLogger(ReportGenerator.class);
    protected Map<String, IRoundTrip> roundTripMap = new HashMap<>();
    protected String outputFile;
    protected String outputDir;
    protected String partialDir;
    protected IRoundTripBuilder roundTripBuilder;
    protected String strategyName;

    // For unit tests
    protected ReportGenerator() {
    }

    public ReportGenerator(String strategyName, String dir, IRoundTripBuilder roundTripBuilder) {
        this.roundTripBuilder = roundTripBuilder;
        this.strategyName = strategyName;
        StringBuilder sb = new StringBuilder();
        sb.append(dir);
        if (!dir.endsWith("/")) {
            sb.append("/");
        }

        this.outputDir = sb.toString();
        partialDir = sb.toString() + "partial/";

        try {
            Files.createDirectories(Paths.get(outputDir));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

        try {
            Files.createDirectories(Paths.get(partialDir));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

        sb.append("report.csv");
        outputFile = sb.toString();

        logger.info("Created new Report Generator with output dir: " + outputDir);
        logger.info("...and partialDir: " + partialDir);

        try {
            logger.info("Loading partial round trips...");
            loadPartialRoundTrips();
            logger.info("Round Trips loaded");
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

    }

    @Override
    public void loadPartialRoundTrips() throws IOException {
        File[] files = new File(partialDir).listFiles();
        for (File file : files) {
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
            IRoundTrip trip;
            try {
                trip = (IRoundTrip) input.readObject();
                logger.info("Loading round trip: " + trip);
                roundTripMap.put(trip.getCorrelationId(), trip);
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
            input.close();

        }
    }

    @Override
    public void savePartial(String correlationId, IRoundTrip trip) throws IOException {
        logger.info("Saving partial round trip: " + trip);
        String filename = partialDir + correlationId + ".ser";
        ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(filename));
        output.writeObject(trip);
        output.close();
    }

    @Override
    public void deletePartial(String correlationId) throws IOException {
        logger.info("Deleting partial round trip with correlation ID: " + correlationId);
        String filename = partialDir + correlationId + ".ser";
        Files.deleteIfExists(Paths.get(filename));
    }

    @Override
    public void orderEvent(OrderEvent event) {
        logger.info("Received order event: " + event);
        OrderTicket order = event.getOrder();
        if (order.getCurrentStatus() == Status.FILLED) {
            TradeReferenceLine line = getTradeReferenceLine(order.getReference());
            // we only care about orders for this strategy
            if (strategyName.equals(line.getStrategy())) {
                IRoundTrip roundTrip = roundTripMap.get(line.getCorrelationId());
                if (roundTrip == null) {
                    roundTrip = roundTripBuilder.buildRoundTrip();
                    roundTripMap.put(line.getCorrelationId(), roundTrip);
                }
                roundTrip.addTradeReference(order, line);
                if (roundTrip.isComplete()) {
                    try {
                        writeRoundTripToFile(roundTrip);
                        deletePartial(roundTrip.getCorrelationId());
                        roundTripMap.remove(line.getCorrelationId());
                    } catch (IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                } else {
                    try {
                        savePartial(line.getCorrelationId(), roundTrip);
                    } catch (IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
        }
    }

    protected synchronized void writeRoundTripToFile(IRoundTrip roundTrip) {
        logger.info("Writing round trip to result file: " + roundTrip);
        String resultString = roundTrip.getResults() + "\n";
        try {
            Files.write(Paths.get(outputFile), resultString.getBytes(), StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

    protected TradeReferenceLine getTradeReferenceLine(String line) {
        return TradeReferenceLine.parseLine(line);
    }

}
