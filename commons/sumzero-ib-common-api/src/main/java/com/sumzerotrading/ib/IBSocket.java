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

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.ib.client.EReader;

/**
 *
 * @author Rob Terpilowski
 */
public class IBSocket extends BaseIBConnectionDelegate implements Runnable {

    protected static final Logger logger = LoggerFactory.getLogger(IBSocket.class);
    protected IBConnectionInterface connection;
    protected EClientSocket clientSocket;
    protected int clientId;
    protected boolean connected = false;
    protected EJavaSignal signal;
    protected EReader reader;
    protected Thread signalThread;

    protected volatile boolean shouldRun = false;
    protected Thread connectionMonitorThread;
    protected volatile boolean monitorRunning = false;
    protected final long MONITOR_INTERVAL_MS = 5000;
    protected CyclicBarrier barrier;

    public IBSocket(IBConnectionInterface connection) {
        barrier = new CyclicBarrier(2);
        this.connection = connection;
        connection.addIbConnectionDelegate(this);
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
                Thread.sleep(2000);
                logger.info("Disconnected.....Waiting for thread to reconnect...");
                cleanupConnections();

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                logger.error("Thread interrupted: " + e.getMessage());
            } // Sleep for a second before checking connection status again
        }
        logger.info("Signal thread stopped for " + connection.getHost() + ":" + connection.getPort()
                + " with clientId: " + connection.getClientId());
    }

    public synchronized void connect() {
        if (connected) {
            logger.info("Already connected to " + connection.getHost() + ":" + connection.getPort() + " with clientId: "
                    + connection.getClientId());
            return;
        }
        final CyclicBarrier cb = new CyclicBarrier(2);
        logger.info("Connecting to " + connection.getHost() + ":" + connection.getPort() + " with clientId: "
                + connection.getClientId());
        try {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    startConnection();
                    logger.info("Connected to " + connection.getHost() + ":" + connection.getPort() + " with clientId: "
                            + connection.getClientId());
                    try {
                        cb.await();
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
            cb.await();
            logger.info("Barrier passed, connection established.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // startConnectionMonitor();
    }

    public void disconnect() {
        if (clientSocket.isConnected()) {
            clientSocket.eDisconnect();
            reader.interrupt();
            connected = false;
            shouldRun = false;
        }
        // stopConnectionMonitor();
    }

    public boolean isConnected() {
        return connected;
    }

    protected void startConnection() {
        if (!connected) {

            shouldRun = true;
            // barrier.reset();
            logger.info("eConnect to " + connection.getHost() + ":" + connection.getPort() + " with clientId: "
                    + connection.getClientId());
            clientSocket.eConnect(connection.getHost(), connection.getPort(), connection.getClientId());
            // try {
            // logger.info("Waiting for connection to be acknowledged by TWS for " +
            // connection.getHost() + ":"
            // + connection.getPort() + " with clientId: " + connection.getClientId());
            // // barrier.await(2, TimeUnit.SECONDS);
            // logger.info("Connection acknowledged by TWS for " + connection.getHost() +
            // ":" + connection.getPort()
            // + " with clientId: " + connection.getClientId());
            // } catch (InterruptedException | BrokenBarrierException | TimeoutException e)
            // {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            // reader = new EReader(clientSocket, signal);
            // reader.start();
            // signalThread = new Thread(this, "IBSocket Signal Thread");
            // signalThread.start();
        }
    }

    protected void cleanupConnections() {
        // barrier.reset();
        if (reader != null) {
            reader.interrupt();
        }
        clientSocket.eDisconnect();
        reader = null;
        logger.info("Reconnecting to " + connection.getHost() + ":" + connection.getPort() + " with clientId: "
                + connection.getClientId());
        clientSocket.eConnect(connection.getHost(), connection.getPort(), connection.getClientId());
        // try {
        // barrier.await(2, TimeUnit.SECONDS);
        // reader = new EReader(clientSocket, signal);
        // reader.start();
        // } catch (InterruptedException | BrokenBarrierException | TimeoutException e)
        // {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

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

    @Override
    public void nextValidId(int orderId) {
        logger.info("Next valid order ID received: " + orderId);
        // new IllegalStateException("Trackin").printStackTrace();
        // logger.info("Next valid order ID received: " + orderId);
        // if (orderId == 0) {
        // logger.error("Received invalid order ID: " + orderId);
        // return;
        // }
        // try {
        // if (barrier != null) {
        // barrier.await(1, TimeUnit.SECONDS);
        // }
        // } catch (InterruptedException | BrokenBarrierException | TimeoutException e)
        // {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
    }

    @Override
    public void connectAck() {
        connected = true;
        logger.info("Connection acknowledged by TWS for " + connection.getHost() + ":" + connection.getPort()
                + " with clientId: " + connection.getClientId());
        // try {
        // barrier.await(2, TimeUnit.SECONDS);
        // } catch (InterruptedException | BrokenBarrierException | TimeoutException e)
        // {
        // // TODO Auto-generated catch block
        // logger.error(e.getMessage(), e);
        // }

        reader = new EReader(clientSocket, signal);
        reader.start();
        signalThread = new Thread(this, "IBSocket Signal Thread");
        signalThread.start();
    }

}
