package com.sumzerotrading.websocket;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class ProxyConfig {

    protected boolean runningLocally = false;
    protected static ProxyConfig instance;

    public static ProxyConfig getInstance() {
        if (instance == null) {
            instance = new ProxyConfig();
        }
        return instance;
    }

    public void setRunningLocally(boolean runningLocally) {
        this.runningLocally = runningLocally;
        if (runningLocally) {
            System.setProperty("socksProxyHost", "localhost");
            System.setProperty("socksProxyPort", "1080");
        } else {
            System.clearProperty("socksProxyHost");
            System.clearProperty("socksProxyPort");
        }
    }

    public boolean isRunningLocally() {
        return runningLocally;
    }

    public Proxy getProxy() {
        if (!runningLocally) {
            return Proxy.NO_PROXY;
        } else {
            return new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 1080));
        }
    }
}
