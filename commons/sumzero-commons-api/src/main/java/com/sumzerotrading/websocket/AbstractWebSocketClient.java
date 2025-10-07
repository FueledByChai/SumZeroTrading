package com.sumzerotrading.websocket;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractWebSocketClient extends WebSocketClient {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractWebSocketClient.class);
    protected IWebSocketProcessor processor;
    protected List<String> messages = new ArrayList<>();
    protected String channel;

    public AbstractWebSocketClient(String serverUri, String channel, IWebSocketProcessor processor) throws Exception {
        super(new URI(serverUri));
        setProxy(ProxyConfig.getInstance().getProxy());
        this.processor = processor;
        this.channel = channel;
    }

    @Override
    public void onMessage(String message) {
        processor.messageReceived(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        processor.connectionClosed(code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
        logger.error(ex.getMessage(), ex);
    }

}
