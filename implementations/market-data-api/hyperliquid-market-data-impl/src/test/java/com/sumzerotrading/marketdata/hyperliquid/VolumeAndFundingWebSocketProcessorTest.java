package com.sumzerotrading.marketdata.hyperliquid;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.websocket.IWebSocketClosedListener;

public class VolumeAndFundingWebSocketProcessorTest {

    private VolumeAndFundingWebSocketProcessor processor;
    private TestWebSocketClosedListener closedListener;
    private TestVolumeAndFundingListener volumeListener;
    private Ticker ticker;

    @Before
    public void setUp() {
        ticker = new Ticker("BTC");
        closedListener = new TestWebSocketClosedListener();
        volumeListener = new TestVolumeAndFundingListener();
        processor = new VolumeAndFundingWebSocketProcessor(ticker, closedListener);
    }

    @After
    public void tearDown() throws Exception {
        if (processor != null) {
            processor.shutdown();
        }
    }

    @Test
    public void testAddAndRemoveListener() {
        // Test adding listener
        processor.add(volumeListener);

        // Test removing listener
        processor.remove(volumeListener);

        // No exceptions should be thrown
    }

    @Test
    public void testConnectionClosed() {
        processor.connectionClosed(1000, "Normal closure", false);
        assertTrue("Connection closed listener should be called", closedListener.wasClosed());
    }

    @Test
    public void testConnectionError() {
        Exception testException = new Exception("Test error");
        processor.connectionError(testException);
        assertTrue("Connection closed listener should be called on error", closedListener.wasClosed());
    }

    @Test
    public void testMessageReceivedWithValidActiveAssetCtxMessage() throws InterruptedException {
        // JSON message from the user request
        String jsonMessage = "{\n" + "    \"channel\": \"activeAssetCtx\",\n" + "    \"data\": {\n"
                + "        \"coin\": \"BTC\",\n" + "        \"ctx\": {\n" + "            \"funding\": \"0.0000125\",\n"
                + "            \"openInterest\": \"33380.0945799999\",\n" + "            \"prevDayPx\": \"116514.0\",\n"
                + "            \"dayNtlVlm\": \"4718133837.5883026123\",\n"
                + "            \"premium\": \"0.0000859454\",\n" + "            \"oraclePx\": \"116353.0\",\n"
                + "            \"markPx\": \"116364.0\",\n" + "            \"midPx\": \"116363.5\",\n"
                + "            \"impactPxs\": [\n" + "                \"116363.0\",\n"
                + "                \"116364.0\"\n" + "            ],\n"
                + "            \"dayBaseVlm\": \"40674.73547\"\n" + "        }\n" + "    }\n" + "}";

        // Add listener
        processor.add(volumeListener);

        // Process the message
        processor.messageReceived(jsonMessage);

        // Wait for async processing to complete
        assertTrue("Listener should have been called", volumeListener.waitForUpdate(5, TimeUnit.SECONDS));

        // Verify the values
        assertEquals("Volume should match", new BigDecimal("40674.73547"), volumeListener.getVolume());
        assertEquals("Volume notional should match", new BigDecimal("4718133837.5883026123"),
                volumeListener.getVolumeNotional());
        assertEquals("Funding rate should match", new BigDecimal("0.0000125"), volumeListener.getFundingRate());
        assertEquals("Mark price should match", new BigDecimal("116364.0"), volumeListener.getMarkPrice());
        assertEquals("Open interest should match", new BigDecimal("33380.0945799999"),
                volumeListener.getOpenInterest());
        assertNotNull("Timestamp should not be null", volumeListener.getTimestamp());
    }

    @Test
    public void testMessageReceivedWithNonActiveAssetCtxMessage() throws InterruptedException {
        String jsonMessage = "{\n" + "    \"channel\": \"otherChannel\",\n" + "    \"data\": {\n"
                + "        \"someData\": \"value\"\n" + "    }\n" + "}";

        // Add listener
        processor.add(volumeListener);

        // Process the message
        processor.messageReceived(jsonMessage);

        // Give time for any async processing
        Thread.sleep(100);

        // Verify the listener was NOT called
        assertFalse("Listener should not have been called", volumeListener.wasUpdated());
    }

    @Test
    public void testMessageReceivedWithMalformedJson() throws InterruptedException {
        String malformedJson = "{ malformed json }";

        // Add listener
        processor.add(volumeListener);

        // Process the message - should not throw exception
        processor.messageReceived(malformedJson);

        // Give time for any async processing
        Thread.sleep(100);

        // Verify the listener was NOT called
        assertFalse("Listener should not have been called", volumeListener.wasUpdated());
    }

    @Test
    public void testMessageReceivedWithMissingFields() throws InterruptedException {
        String jsonMessage = "{\n" + "    \"channel\": \"activeAssetCtx\",\n" + "    \"data\": {\n"
                + "        \"coin\": \"BTC\",\n" + "        \"ctx\": {\n" + "            \"funding\": \"0.0000125\"\n"
                + "        }\n" + "    }\n" + "}";

        // Add listener
        processor.add(volumeListener);

        // Process the message - should not throw exception
        processor.messageReceived(jsonMessage);

        // Give time for any async processing
        Thread.sleep(100);

        // Verify the listener was NOT called due to missing fields
        assertFalse("Listener should not have been called", volumeListener.wasUpdated());
    }

    @Test
    public void testMultipleListeners() throws InterruptedException {
        TestVolumeAndFundingListener listener2 = new TestVolumeAndFundingListener();

        String jsonMessage = "{\n" + "    \"channel\": \"activeAssetCtx\",\n" + "    \"data\": {\n"
                + "        \"coin\": \"BTC\",\n" + "        \"ctx\": {\n" + "            \"funding\": \"0.0000125\",\n"
                + "            \"openInterest\": \"33380.0945799999\",\n"
                + "            \"dayNtlVlm\": \"4718133837.5883026123\",\n" + "            \"markPx\": \"116364.0\",\n"
                + "            \"dayBaseVlm\": \"40674.73547\"\n" + "        }\n" + "    }\n" + "}";

        // Add both listeners
        processor.add(volumeListener);
        processor.add(listener2);

        // Process the message
        processor.messageReceived(jsonMessage);

        // Wait for async processing to complete
        assertTrue("First listener should have been called", volumeListener.waitForUpdate(5, TimeUnit.SECONDS));
        assertTrue("Second listener should have been called", listener2.waitForUpdate(5, TimeUnit.SECONDS));
    }

    @Test
    public void testShutdown() throws InterruptedException {
        // Add a listener and process a message to ensure executor is active
        processor.add(volumeListener);

        String jsonMessage = "{\n" + "    \"channel\": \"activeAssetCtx\",\n" + "    \"data\": {\n"
                + "        \"coin\": \"BTC\",\n" + "        \"ctx\": {\n" + "            \"funding\": \"0.0000125\",\n"
                + "            \"openInterest\": \"33380.0945799999\",\n"
                + "            \"dayNtlVlm\": \"4718133837.5883026123\",\n" + "            \"markPx\": \"116364.0\",\n"
                + "            \"dayBaseVlm\": \"40674.73547\"\n" + "        }\n" + "    }\n" + "}";

        processor.messageReceived(jsonMessage);

        // Shutdown should complete without hanging
        processor.shutdown();

        // Multiple shutdowns should be safe
        processor.shutdown();
    }

    // Test helper classes
    private static class TestWebSocketClosedListener implements IWebSocketClosedListener {
        private final AtomicBoolean closed = new AtomicBoolean(false);

        @Override
        public void connectionClosed() {
            closed.set(true);
        }

        public boolean wasClosed() {
            return closed.get();
        }
    }

    private static class TestVolumeAndFundingListener implements IVolumeAndFundingWebsocketListener {
        private final CountDownLatch latch = new CountDownLatch(1);
        private final AtomicBoolean updated = new AtomicBoolean(false);
        private final AtomicReference<BigDecimal> volume = new AtomicReference<>();
        private final AtomicReference<BigDecimal> volumeNotional = new AtomicReference<>();
        private final AtomicReference<BigDecimal> fundingRate = new AtomicReference<>();
        private final AtomicReference<BigDecimal> markPrice = new AtomicReference<>();
        private final AtomicReference<BigDecimal> openInterest = new AtomicReference<>();
        private final AtomicReference<ZonedDateTime> timestamp = new AtomicReference<>();

        @Override
        public void volumeAndFundingUpdate(Ticker ticker, BigDecimal volume, BigDecimal volumeNotional,
                BigDecimal fundingRate, BigDecimal markPrice, BigDecimal openInterest, ZonedDateTime timestamp) {
            this.volume.set(volume);
            this.volumeNotional.set(volumeNotional);
            this.fundingRate.set(fundingRate);
            this.markPrice.set(markPrice);
            this.openInterest.set(openInterest);
            this.timestamp.set(timestamp);
            this.updated.set(true);
            this.latch.countDown();
        }

        public boolean waitForUpdate(long timeout, TimeUnit unit) throws InterruptedException {
            return latch.await(timeout, unit);
        }

        public boolean wasUpdated() {
            return updated.get();
        }

        public BigDecimal getVolume() {
            return volume.get();
        }

        public BigDecimal getVolumeNotional() {
            return volumeNotional.get();
        }

        public BigDecimal getFundingRate() {
            return fundingRate.get();
        }

        public BigDecimal getMarkPrice() {
            return markPrice.get();
        }

        public BigDecimal getOpenInterest() {
            return openInterest.get();
        }

        public ZonedDateTime getTimestamp() {
            return timestamp.get();
        }
    }
}