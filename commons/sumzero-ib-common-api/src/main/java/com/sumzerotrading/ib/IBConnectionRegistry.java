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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.ib.client.EReader;

/**
 *
 * @author Rob Terpilowski
 */
public class IBConnectionRegistry {

    protected final static Logger logger = LoggerFactory.getLogger(IBConnectionRegistry.class);

    protected static final Map<IBConnectionInfo, IBSocket> connectionMap = new HashMap<IBConnectionInfo, IBSocket>();

    public static IBSocket getIBSocket(IBConnectionInfo info) {

        synchronized (connectionMap) {
            IBSocket savedSocket = connectionMap.get(info);
            if (savedSocket == null) {
                IBConnection connection = new IBConnection();
                connection.setClientId(info.getClientId());
                connection.setHost(info.getHost());
                connection.setPort(info.getPort());

                savedSocket = new IBSocket(connection);

                connectionMap.put(info, savedSocket);
            }
            return savedSocket;

        }

    }

    public static void setTestIBSocket(IBConnectionInfo connectionInfo, IBSocket ibSocket) {
        connectionMap.put(connectionInfo, ibSocket);
    }
}
