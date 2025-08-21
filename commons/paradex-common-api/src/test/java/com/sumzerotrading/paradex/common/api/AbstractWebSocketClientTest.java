package com.sumzerotrading.paradex.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbstractWebSocketClientTest {

    @Mock
    private IWebSocketProcessor mockProcessor;

    @Mock
    private ServerHandshake mockHandshake;

    private TestAbstractWebSocketClient client;

    // Concrete implementation for testing the abstract class
    private static class TestAbstractWebSocketClient extends AbstractWebSocketClient {
        public TestAbstractWebSocketClient(String serverUri, String channel, IWebSocketProcessor processor)
                throws Exception {
            super(serverUri, channel, processor);
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            processor.connectionOpened();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        client = new TestAbstractWebSocketClient("ws://localhost:8080", "test-channel", mockProcessor);
    }

    @Test
    void testConstructorWithValidUri() throws Exception {
        // When - constructor is called in setUp()

        // Then
        assertNotNull(client);
        assertEquals(mockProcessor, client.processor);
        assertEquals("test-channel", client.channel);
        assertNotNull(client.messages);
        assertEquals(0, client.messages.size());
        assertEquals(new URI("ws://localhost:8080"), client.getURI());
    }

    @Test
    void testConstructorWithInvalidUri() {
        // When & Then
        assertThrows(URISyntaxException.class, () -> {
            new TestAbstractWebSocketClient("ht tp://invalid uri", "test-channel", mockProcessor);
        });
    }

    @Test
    void testOnMessage() {
        // Given
        String testMessage = "test message";

        // When
        client.onMessage(testMessage);

        // Then
        verify(mockProcessor).messageReceived(testMessage);
    }

    @Test
    void testOnClose() {
        // Given
        int code = 1000;
        String reason = "Normal closure";
        boolean remote = true;

        // When
        client.onClose(code, reason, remote);

        // Then
        verify(mockProcessor).connectionClosed(code, reason, remote);
    }

    @Test
    void testOnError() {
        // Given
        Exception testException = new RuntimeException("Test error");

        // When
        client.onError(testException);

        // Then - should not throw exception and should not interact with processor
        verifyNoInteractions(mockProcessor);
    }

    @Test
    void testOnOpenCallsProcessor() {
        // When
        client.onOpen(mockHandshake);

        // Then
        verify(mockProcessor).connectionOpened();
    }
}
