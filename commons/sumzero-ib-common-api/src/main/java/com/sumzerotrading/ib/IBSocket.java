/**
 * MIT License

Copyright (c) 2015  Rob Terpilowski

Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
and associated documentation files (the "Software"), to deal in the Software without restriction, 
including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.sumzerotrading.ib;

import java.util.concurrent.CyclicBarrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.EClientSocket;

/**
 *
 * @author Rob Terpilowski
 */
public class IBSocket {

    protected static final Logger logger = LoggerFactory.getLogger(IBSocket.class);
    protected IBConnectionInterface connection;
    protected EClientSocket clientSocket;
    protected int clientId;
    protected boolean connected = false;

    public IBSocket(IBConnectionInterface connection, EClientSocket clientSocket) {
        this.connection = connection;
        this.clientSocket = clientSocket;
        clientId = connection.getClientId();
    }

    public IBConnectionInterface getConnection() {
        return connection;
    }

    public EClientSocket getClientSocket() {
        return clientSocket;
    }

    public void connect() {
        if (connected) {
            logger.info("Already connected to " + connection.getHost() + ":" + connection.getPort() + " with clientId: "
                    + connection.getClientId());
            return;
        }
        final CyclicBarrier barrier = new CyclicBarrier(2);
        logger.info("Connecting to " + connection.getHost() + ":" + connection.getPort() + " with clientId: "
                + connection.getClientId());
        try {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    startConnection();
                    logger.info("Connected to " + connection.getHost() + ":" + connection.getPort() + " with clientId: "
                            + connection.getClientId());
                    try {
                        barrier.await();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }, "Connect() thread: " + IBSocket.class.getName());

            thread.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            barrier.await();
            logger.info("Barrier passed, connection established.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void disconnect() {
        if (clientSocket.isConnected()) {
            clientSocket.eDisconnect();
            connected = false;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    protected void startConnection() {
        if (!connected) {
            connected = true;
            clientSocket.eConnect(connection.getHost(), connection.getPort(), connection.getClientId());
        }
    }

    public int getClientId() {
        return clientId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.connection != null ? this.connection.hashCode() : 0);
        hash = 59 * hash + (this.clientSocket != null ? this.clientSocket.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IBSocket other = (IBSocket) obj;
        if (this.connection != other.connection
                && (this.connection == null || !this.connection.equals(other.connection))) {
            return false;
        }
        if (this.clientSocket != other.clientSocket
                && (this.clientSocket == null || !this.clientSocket.equals(other.clientSocket))) {
            return false;
        }
        return true;
    }

}
