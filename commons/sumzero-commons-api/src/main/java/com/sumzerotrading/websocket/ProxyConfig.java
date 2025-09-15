package com.sumzerotrading.websocket;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class ProxyConfig {

    public static boolean runningLocally = false;

    public static Proxy getProxy() {
        if (!runningLocally) {
            return Proxy.NO_PROXY;
        } else {
            return new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 1080));
        }
    }
}
