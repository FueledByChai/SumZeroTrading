package com.sumzerotrading.paradex.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.InetSocketAddress;
import java.net.Proxy;

import org.junit.jupiter.api.Test;

import com.sumzerotrading.websocket.ProxyConfig;

class ProxyConfigTest {

    @Test
    void testGetProxyWhenRunningLocally() {
        // Given
        ProxyConfig.runningLocally = true;

        // When
        Proxy proxy = ProxyConfig.getProxy();

        // Then
        assertNotNull(proxy);
        assertEquals(Proxy.Type.SOCKS, proxy.type());

        InetSocketAddress address = (InetSocketAddress) proxy.address();
        assertEquals("127.0.0.1", address.getHostString());
        assertEquals(1080, address.getPort());
    }

    @Test
    void testGetProxyWhenNotRunningLocally() {
        // Given
        ProxyConfig.runningLocally = false;

        // When
        Proxy proxy = ProxyConfig.getProxy();

        // Then
        assertNotNull(proxy);
        assertEquals(Proxy.NO_PROXY, proxy);
    }

    @Test
    void testRunningLocallyDefaultValue() {
        // The default value should be true
        assertEquals(true, ProxyConfig.runningLocally);
    }
}
