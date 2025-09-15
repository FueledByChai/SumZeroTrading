package com.sumzerotrading.marketdata.paradex;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sumzerotrading.websocket.IWebSocketClosedListener;

@ExtendWith(MockitoExtension.class)
class MarketBookWebSocketProcessorTest {

    @Mock
    private IParadexOrderBook mockOrderBook;

    @Mock
    private IWebSocketClosedListener mockListener;

    private MarketBookWebSocketProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new MarketBookWebSocketProcessor(mockOrderBook, mockListener);
    }

    @Test
    void testConnectionClosed() {
        // When
        processor.connectionClosed(1000, "Normal closure", false);

        // Then
        verify(mockListener).connectionClosed();
    }

    @Test
    void testConnectionError() {
        // Given
        Exception testException = new RuntimeException("Test error");

        // When
        processor.connectionError(testException);

        // Then
        verify(mockListener).connectionClosed();
    }

    @Test
    void testConnectionEstablished() {
        // When
        processor.connectionEstablished();

        // Then - no interactions expected (TODO method)
        verifyNoInteractions(mockOrderBook, mockListener);
    }

    @Test
    void testConnectionOpened() {
        // When
        processor.connectionOpened();

        // Then - no interactions expected (TODO method)
        verifyNoInteractions(mockOrderBook, mockListener);
    }

    @Test
    void testMessageReceivedWithNoMethod() {
        // Given
        String message = "{\"id\": 123, \"result\": \"success\"}";

        // When
        processor.messageReceived(message);

        // Then - should return early without calling orderBook
        verifyNoInteractions(mockOrderBook, mockListener);
    }

    @Test
    void testMessageReceivedWithSubscriptionSnapshot() {
        // Given
        String message = """
                {
                    "method": "subscription",
                    "params": {
                        "data": {
                            "update_type": "s",
                            "last_updated_at": 1625247600000,
                            "inserts": [
                                {"price": "100.50", "size": "10", "side": "BUY"}
                            ]
                        }
                    }
                }
                """;

        // When
        processor.messageReceived(message);

        // Then
        verify(mockOrderBook).handleSnapshot(anyMap(), any());
        verify(mockOrderBook, never()).applyDelta(anyMap(), any());
        verifyNoInteractions(mockListener);
    }

    @Test
    void testMessageReceivedWithSubscriptionDelta() {
        // Given
        String message = """
                {
                    "method": "subscription",
                    "params": {
                        "data": {
                            "update_type": "d",
                            "last_updated_at": 1625247600000,
                            "inserts": [
                                {"price": "101.00", "size": "5", "side": "SELL"}
                            ]
                        }
                    }
                }
                """;

        // When
        processor.messageReceived(message);

        // Then
        verify(mockOrderBook).applyDelta(anyMap(), any());
        verify(mockOrderBook, never()).handleSnapshot(anyMap(), any());
        verifyNoInteractions(mockListener);
    }

    @Test
    void testMessageReceivedWithUnknownMethod() {
        // Given
        String message = """
                {
                    "method": "unknown_method",
                    "params": {
                        "data": {}
                    }
                }
                """;

        // When
        processor.messageReceived(message);

        // Then - should log warning but not interact with orderBook
        verifyNoInteractions(mockOrderBook, mockListener);
    }

    @Test
    void testMessageReceivedWithMalformedJson() {
        // Given
        String message = "{invalid json}";

        // When
        processor.messageReceived(message);

        // Then - should catch exception and not interact with orderBook
        verifyNoInteractions(mockOrderBook, mockListener);
    }

    @Test
    void testMessageReceivedWithMissingParams() {
        // Given
        String message = """
                {
                    "method": "subscription"
                }
                """;

        // When
        processor.messageReceived(message);

        // Then - should catch exception and not interact with orderBook
        verifyNoInteractions(mockOrderBook, mockListener);
    }

    @Test
    void testMessageReceivedWithMissingData() {
        // Given
        String message = """
                {
                    "method": "subscription",
                    "params": {}
                }
                """;

        // When
        processor.messageReceived(message);

        // Then - should catch exception and not interact with orderBook
        verifyNoInteractions(mockOrderBook, mockListener);
    }

    @Test
    void testMessageReceivedWithMissingUpdateType() {
        // Given
        String message = """
                {
                    "method": "subscription",
                    "params": {
                        "data": {}
                    }
                }
                """;

        // When
        processor.messageReceived(message);

        // Then - should catch exception and not interact with orderBook
        verifyNoInteractions(mockOrderBook, mockListener);
    }

    @Test
    void testMessageReceivedWithEmptyMessage() {
        // Given
        String message = "";

        // When
        processor.messageReceived(message);

        // Then - should catch exception and not interact with orderBook
        verifyNoInteractions(mockOrderBook, mockListener);
    }

    @Test
    void testMessageReceivedWithNullMessage() {
        // Given
        String message = null;

        // When
        processor.messageReceived(message);

        // Then - should catch exception and not interact with orderBook
        verifyNoInteractions(mockOrderBook, mockListener);
    }
}
