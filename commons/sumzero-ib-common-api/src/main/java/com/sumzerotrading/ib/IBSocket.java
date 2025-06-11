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
import com.ib.client.EJavaSignal;
import com.ib.client.EReader;

/**
 *
 * @author Rob Terpilowski
 */
public class IBSocket implements Runnable {

    protected static final Logger logger = LoggerFactory.getLogger(IBSocket.class);
    protected IBConnectionInterface connection;
    protected EClientSocket clientSocket;
    protected int clientId;
    protected boolean connected = false;
    protected EJavaSignal signal;
    protected EReader reader;
    protected Thread signalThread = new Thread(this, "IBSocket Signal Thread");

    protected volatile boolean shouldRun = false;

    public IBSocket(IBConnectionInterface connection) {
        this.connection = connection;
        clientId = connection.getClientId();
        signal = new EJavaSignal();
        clientSocket = new EClientSocket(connection, signal);

    }

    public IBConnectionInterface getConnection() {
        return connection;
    }

    public EClientSocket getClientSocket() {
        return clientSocket;
    }

    @Override
    public void run() {
        logger.info("Signal thread started for " + connection.getHost() + ":" + connection.getPort()
                + " with clientId: " + connection.getClientId());
        while (shouldRun) {
            while (clientSocket.isConnected()) {
                logger.info("Waiting for signal for " + connection.getHost() + ":" + connection.getPort()
                        + " with clientId: " + connection.getClientId());
                signal.waitForSignal();
                try {
                    reader.processMsgs();
                    logger.info("Processed messages for " + connection.getHost() + ":" + connection.getPort()
                            + " with clientId: " + connection.getClientId());
                } catch (Exception e) {
                    logger.error("Exception: " + e.getMessage());
                }
            }
            try {
                logger.info("Waiting for thread to reconnect...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                logger.error("Thread interrupted: " + e.getMessage());
            } // Sleep for a second before checking connection status again
        }
        logger.info("Signal thread stopped for " + connection.getHost() + ":" + connection.getPort()
                + " with clientId: " + connection.getClientId());
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
            shouldRun = false;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    protected void startConnection() {
        if (!connected) {
            connected = true;
            shouldRun = true;

            clientSocket.eConnect(connection.getHost(), connection.getPort(), connection.getClientId());
            reader = new EReader(clientSocket, signal);
            reader.start();
            signalThread.start();
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
