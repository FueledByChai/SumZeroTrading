package com.sumzerotrading.paradex.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParadexWebSocketClientTest {

    @Mock
    private IWebSocketProcessor mockProcessor;

    @Mock
    private ServerHandshake mockHandshake;

    private TestParadexWebSocketClient client;

    @BeforeEach
    void setUp() throws Exception {
        client = new TestParadexWebSocketClient("ws://localhost:8080", "test-channel", mockProcessor);
    }

    @Test
    void testConstructorWithNullToken() throws Exception {
        // When
        TestParadexWebSocketClient clientWithoutToken = new TestParadexWebSocketClient("ws://localhost:8080",
                "test-channel", mockProcessor);

        // Then
        assertEquals("test-channel", clientWithoutToken.getChannel());
    }

    @Test
    void testConstructorWithToken() throws Exception {
        // Given
        String token = "test-token";

        // When
        TestParadexWebSocketClient clientWithToken = new TestParadexWebSocketClient("ws://localhost:8080",
                "test-channel", mockProcessor, token);

        // Then
        assertEquals("test-channel", clientWithToken.getChannel());
        assertEquals(token, clientWithToken.getJwtToken());
    }

    @Test
    void testOnOpenWithoutToken() {
        // When
        client.onOpen(mockHandshake);

        // Then
        assertEquals(1, client.sentMessages.size());
        assertTrue(client.sentMessages.get(0).contains("subscribe"));
        assertTrue(client.sentMessages.get(0).contains("test-channel"));
        // Parent class calls processor.connectionOpened() which we don't override
    }

    @Test
    void testOnOpenWithToken() {
        // Given
        String token = "test-token";
        client.setJwtToken(token);

        // When
        client.onOpen(mockHandshake);

        // Then
        assertEquals(2, client.sentMessages.size()); // Auth message + subscribe message
        assertTrue(client.sentMessages.get(0).contains("auth"));
        assertTrue(client.sentMessages.get(0).contains(token));
        assertTrue(client.sentMessages.get(1).contains("subscribe"));
        assertTrue(client.sentMessages.get(1).contains("test-channel"));
    }

    @Test
    void testOnOpenAuthMessageFormat() {
        // Given
        String token = "test-token";
        client.setJwtToken(token);

        // When
        client.onOpen(mockHandshake);

        // Then
        String authMessage = client.sentMessages.get(0);
        assertTrue(authMessage.contains("auth"));
        assertTrue(authMessage.contains("test-token"));
        assertTrue(authMessage.contains("\"id\":0"));
        assertTrue(authMessage.contains("\"method\":\"auth\""));
    }

    @Test
    void testOnOpenSubscriptionMessageFormat() {
        // When
        client.onOpen(mockHandshake);

        // Then
        String subscribeMessage = client.sentMessages.get(0);
        assertTrue(subscribeMessage.contains("subscribe"));
        assertTrue(subscribeMessage.contains("test-channel"));
        assertTrue(subscribeMessage.contains("\"id\":1"));
        assertTrue(subscribeMessage.contains("\"method\":\"subscribe\""));
    }

    @Test
    void testInheritedMethods() {
        // Test that inherited methods work correctly
        String testMessage = "test message";

        // When
        client.onMessage(testMessage);
        client.onClose(1000, "Normal closure", false);
        // Note: onError only logs, doesn't call processor

        // Then
        verify(mockProcessor).messageReceived(testMessage);
        verify(mockProcessor).connectionClosed(1000, "Normal closure", false);
    }

    // Test double class that tracks sent messages
    private static class TestParadexWebSocketClient extends ParadexWebSocketClient {
        public List<String> sentMessages = new ArrayList<>();

        public TestParadexWebSocketClient(String serverUrl, String channelName, IWebSocketProcessor processor)
                throws Exception {
            super(serverUrl, channelName, processor);
        }

        public TestParadexWebSocketClient(String serverUrl, String channelName, IWebSocketProcessor processor,
                String token) throws Exception {
            super(serverUrl, channelName, processor, token);
        }

        @Override
        public void send(String text) {
            sentMessages.add(text);
        }

        public void setJwtToken(String token) {
            this.jwtToken = token;
        }

        public String getJwtToken() {
            return this.jwtToken;
        }

        public String getChannel() {
            return this.channel;
        }
    }
}
