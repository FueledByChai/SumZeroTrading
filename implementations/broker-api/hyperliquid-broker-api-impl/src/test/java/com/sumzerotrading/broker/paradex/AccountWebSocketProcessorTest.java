package com.sumzerotrading.broker.paradex;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sumzerotrading.websocket.IWebSocketClosedListener;

public class AccountWebSocketProcessorTest {

    private AccountWebSocketProcessor processor;
    private TestWebSocketClosedListener closedListener;
    private TestAccountUpdateListener accountListener;

    @Before
    public void setUp() {
        closedListener = new TestWebSocketClosedListener();
        accountListener = new TestAccountUpdateListener();
        processor = new AccountWebSocketProcessor(closedListener);
        processor.addAccountUpdateListener(accountListener);
    }

    @After
    public void tearDown() {
        if (processor != null) {
            processor.removeAccountUpdateListener(accountListener);
        }
    }

    @Test
    public void testProcessClearinghouseStateMessage() throws InterruptedException {
        // JSON message from the user request
        String jsonMessage = """
                {
                    "channel": "clearinghouseState",
                    "data": {
                        "dex": "",
                        "user": "0x852b1bc8613a32aee1ce143032529b824fcb1e74",
                        "clearinghouseState": {
                            "marginSummary": {
                                "accountValue": "6867.907358",
                                "totalNtlPos": "15253.815742",
                                "totalRawUsd": "-420.9663",
                                "totalMarginUsed": "1066.314396"
                            },
                            "crossMarginSummary": {
                                "accountValue": "6867.907358",
                                "totalNtlPos": "15253.815742",
                                "totalRawUsd": "-420.9663",
                                "totalMarginUsed": "1066.314396"
                            },
                            "crossMaintenanceMarginUsed": "533.157198",
                            "withdrawable": "5125.31228",
                            "assetPositions": [
                                {
                                    "type": "oneWay",
                                    "position": {
                                        "coin": "ETH",
                                        "szi": "2.6994",
                                        "leverage": {
                                            "type": "cross",
                                            "value": 25
                                        },
                                        "entryPx": "2332.22",
                                        "positionValue": "11271.3447",
                                        "unrealizedPnl": "4975.72307",
                                        "returnOnEquity": "19.7586646817",
                                        "liquidationPx": "1780.8823903422",
                                        "marginUsed": "450.853788",
                                        "maxLeverage": 25,
                                        "cumFunding": {
                                            "allTime": "442.19151",
                                            "sinceOpen": "394.518362",
                                            "sinceChange": "167.564129"
                                        }
                                    }
                                },
                                {
                                    "type": "oneWay",
                                    "position": {
                                        "coin": "TRUMP",
                                        "szi": "-236.8",
                                        "leverage": {
                                            "type": "cross",
                                            "value": 10
                                        },
                                        "entryPx": "8.43982",
                                        "positionValue": "1810.336",
                                        "unrealizedPnl": "188.21472",
                                        "returnOnEquity": "0.9417560341",
                                        "liquidationPx": "33.1225987773",
                                        "marginUsed": "181.0336",
                                        "maxLeverage": 10,
                                        "cumFunding": {
                                            "allTime": "64.042232",
                                            "sinceOpen": "-7.444492",
                                            "sinceChange": "0.42693"
                                        }
                                    }
                                },
                                {
                                    "type": "oneWay",
                                    "position": {
                                        "coin": "MELANIA",
                                        "szi": "-12814.2",
                                        "leverage": {
                                            "type": "cross",
                                            "value": 5
                                        },
                                        "entryPx": "0.19509",
                                        "positionValue": "2172.135042",
                                        "unrealizedPnl": "327.859866",
                                        "returnOnEquity": "0.6557210676",
                                        "liquidationPx": "0.6189226658",
                                        "marginUsed": "434.427008",
                                        "maxLeverage": 5,
                                        "cumFunding": {
                                            "allTime": "3383.464033",
                                            "sinceOpen": "-20.932509",
                                            "sinceChange": "-4.613508"
                                        }
                                    }
                                }
                            ],
                            "time": 1758548816236
                        }
                    }
                }
                """;

        // Process the message
        processor.messageReceived(jsonMessage);

        // Wait for async processing to complete
        assertTrue("Account update should have been received", accountListener.waitForUpdate(5, TimeUnit.SECONDS));

        // Verify account level data
        IAccountUpdate accountUpdate = accountListener.getAccountUpdate();
        assertNotNull("Account update should not be null", accountUpdate);
        
        assertEquals("Account value should match", 6867.907358, accountUpdate.getAccountValue(), 0.000001);
        assertEquals("Maintenance margin should match", 533.157198, accountUpdate.getMaintenanceMargin(), 0.000001);

        // Verify positions
        List<IPositionUpdate> positions = accountUpdate.getPositions();
        assertNotNull("Positions should not be null", positions);
        assertEquals("Should have 3 positions", 3, positions.size());

        // Verify ETH position
        IPositionUpdate ethPosition = findPositionByTicker(positions, "ETH");
        assertNotNull("ETH position should exist", ethPosition);
        assertEquals("ETH ticker should match", "ETH", ethPosition.getTicker());
        assertEquals("ETH size should match", new BigDecimal("2.6994"), ethPosition.getSize());
        assertEquals("ETH entry price should match", new BigDecimal("2332.22"), ethPosition.getEntryPrice());
        assertEquals("ETH unrealized PnL should match", new BigDecimal("4975.72307"), ethPosition.getUnrealizedPnl());
        assertEquals("ETH liquidation price should match", new BigDecimal("1780.8823903422"), ethPosition.getLiquidationPrice());
        assertEquals("ETH funding since open should match", new BigDecimal("394.518362"), ethPosition.getFundingSinceOpen());

        // Verify TRUMP position (short)
        IPositionUpdate trumpPosition = findPositionByTicker(positions, "TRUMP");
        assertNotNull("TRUMP position should exist", trumpPosition);
        assertEquals("TRUMP ticker should match", "TRUMP", trumpPosition.getTicker());
        assertEquals("TRUMP size should match", new BigDecimal("-236.8"), trumpPosition.getSize());
        assertEquals("TRUMP entry price should match", new BigDecimal("8.43982"), trumpPosition.getEntryPrice());
        assertEquals("TRUMP unrealized PnL should match", new BigDecimal("188.21472"), trumpPosition.getUnrealizedPnl());
        assertEquals("TRUMP liquidation price should match", new BigDecimal("33.1225987773"), trumpPosition.getLiquidationPrice());
        assertEquals("TRUMP funding since open should match", new BigDecimal("-7.444492"), trumpPosition.getFundingSinceOpen());

        // Verify MELANIA position (short)
        IPositionUpdate melaniaPosition = findPositionByTicker(positions, "MELANIA");
        assertNotNull("MELANIA position should exist", melaniaPosition);
        assertEquals("MELANIA ticker should match", "MELANIA", melaniaPosition.getTicker());
        assertEquals("MELANIA size should match", new BigDecimal("-12814.2"), melaniaPosition.getSize());
        assertEquals("MELANIA entry price should match", new BigDecimal("0.19509"), melaniaPosition.getEntryPrice());
        assertEquals("MELANIA unrealized PnL should match", new BigDecimal("327.859866"), melaniaPosition.getUnrealizedPnl());
        assertEquals("MELANIA liquidation price should match", new BigDecimal("0.6189226658"), melaniaPosition.getLiquidationPrice());
        assertEquals("MELANIA funding since open should match", new BigDecimal("-20.932509"), melaniaPosition.getFundingSinceOpen());
    }

    @Test
    public void testIgnoreNonClearinghouseMessages() throws InterruptedException {
        String nonClearinghouseMessage = """
                {
                    "channel": "otherChannel",
                    "data": {
                        "someData": "value"
                    }
                }
                """;

        // Process the message
        processor.messageReceived(nonClearinghouseMessage);

        // Give time for any async processing
        Thread.sleep(100);

        // Verify no account update was received
        assertFalse("No account update should have been received", accountListener.wasUpdated());
    }

    @Test
    public void testLegacySubscriptionMessage() throws InterruptedException {
        String legacyMessage = """
                {
                    "method": "subscription",
                    "params": {
                        "data": {
                            "account_value": "5000.0",
                            "maintenance_margin_requirement": "250.0"
                        }
                    }
                }
                """;

        // Process the message
        processor.messageReceived(legacyMessage);

        // Wait for async processing to complete
        assertTrue("Account update should have been received", accountListener.waitForUpdate(5, TimeUnit.SECONDS));

        // Verify account data
        IAccountUpdate accountUpdate = accountListener.getAccountUpdate();
        assertNotNull("Account update should not be null", accountUpdate);
        assertEquals("Account value should match", 5000.0, accountUpdate.getAccountValue(), 0.000001);
        assertEquals("Maintenance margin should match", 250.0, accountUpdate.getMaintenanceMargin(), 0.000001);
        
        // Legacy format should have empty positions list
        List<IPositionUpdate> positions = accountUpdate.getPositions();
        assertNotNull("Positions should not be null", positions);
        assertEquals("Legacy format should have no positions", 0, positions.size());
    }

    @Test
    public void testMalformedJsonHandling() throws InterruptedException {
        String malformedJson = "{ invalid json }";

        // Should not throw exception, just log error
        processor.messageReceived(malformedJson);

        // Give time for any async processing
        Thread.sleep(100);

        // Verify no account update was received
        assertFalse("No account update should have been received", accountListener.wasUpdated());
    }

    @Test
    public void testEmptyPositionsArray() throws InterruptedException {
        String messageWithNoPositions = """
                {
                    "channel": "clearinghouseState",
                    "data": {
                        "clearinghouseState": {
                            "marginSummary": {
                                "accountValue": "1000.0",
                                "totalMarginUsed": "100.0"
                            },
                            "crossMaintenanceMarginUsed": "50.0",
                            "assetPositions": []
                        }
                    }
                }
                """;

        // Process the message
        processor.messageReceived(messageWithNoPositions);

        // Wait for async processing to complete
        assertTrue("Account update should have been received", accountListener.waitForUpdate(5, TimeUnit.SECONDS));

        // Verify account data
        IAccountUpdate accountUpdate = accountListener.getAccountUpdate();
        assertNotNull("Account update should not be null", accountUpdate);
        assertEquals("Account value should match", 1000.0, accountUpdate.getAccountValue(), 0.000001);
        assertEquals("Maintenance margin should match", 50.0, accountUpdate.getMaintenanceMargin(), 0.000001);
        
        // Should have empty positions list
        List<IPositionUpdate> positions = accountUpdate.getPositions();
        assertNotNull("Positions should not be null", positions);
        assertEquals("Should have no positions", 0, positions.size());
    }

    @Test
    public void testConnectionHandling() throws InterruptedException {
        // Test connection closed
        processor.connectionClosed(1000, "Normal closure", false);
        assertTrue("Connection closed listener should be notified", closedListener.wasClosed());

        // Reset for error test
        closedListener.reset();

        // Test connection error
        processor.connectionError(new RuntimeException("Test error"));
        assertTrue("Connection closed listener should be notified on error", closedListener.wasClosed());
    }

    // Helper method to find position by ticker
    private IPositionUpdate findPositionByTicker(List<IPositionUpdate> positions, String ticker) {
        return positions.stream()
                .filter(pos -> ticker.equals(pos.getTicker()))
                .findFirst()
                .orElse(null);
    }

    // Test helper classes
    private static class TestWebSocketClosedListener implements IWebSocketClosedListener {
        private volatile boolean closed = false;

        @Override
        public void connectionClosed() {
            closed = true;
        }

        public boolean wasClosed() {
            return closed;
        }

        public void reset() {
            closed = false;
        }
    }

    private static class TestAccountUpdateListener implements IAccountUpdateListener {
        private final CountDownLatch latch = new CountDownLatch(1);
        private final AtomicReference<IAccountUpdate> accountUpdate = new AtomicReference<>();
        private volatile boolean updated = false;

        @Override
        public void accountUpdated(IAccountUpdate accountInfo) {
            this.accountUpdate.set(accountInfo);
            this.updated = true;
            this.latch.countDown();
        }

        public boolean waitForUpdate(long timeout, TimeUnit unit) throws InterruptedException {
            return latch.await(timeout, unit);
        }

        public boolean wasUpdated() {
            return updated;
        }

        public IAccountUpdate getAccountUpdate() {
            return accountUpdate.get();
        }
    }
}