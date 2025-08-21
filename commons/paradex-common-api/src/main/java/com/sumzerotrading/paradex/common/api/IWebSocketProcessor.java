package com.sumzerotrading.paradex.common.api;

public interface IWebSocketProcessor {

    public void messageReceived(String message);

    public void connectionClosed(int code, String reason, boolean remote);

    public void connectionOpened();

    public void connectionError(Exception error);

    public void connectionEstablished();

}
