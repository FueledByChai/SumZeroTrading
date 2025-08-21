/**
 * MIT License
 *
 * Copyright (c) 2015  Rob Terpilowski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.sumzerotrading.ib;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Bar;
import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.Decimal;
import com.ib.client.DeltaNeutralContract;
import com.ib.client.EJavaSignal;
import com.ib.client.EReader;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.TickAttrib;

/**
 *
 * @author Rob Terpilowski
 */
public class IBConnection extends AbstractIBConnection {

    protected static final Logger logger = LoggerFactory.getLogger(IBConnection.class);
    protected static IBConnection connection = null;

    protected List<IBConnectionInterface> ibConnectionDelegates = new ArrayList<>();
    protected int clientId;
    protected String host;
    protected int port;

    public IBConnection() {
    }

    @Override
    public void addIbConnectionDelegate(IBConnectionInterface delegate) {
        ibConnectionDelegates.add(delegate);
    }

    @Override
    public void removeIbConnectionDelegate(IBConnectionInterface delegate) {
        ibConnectionDelegates.remove(delegate);
    }

    @Override
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int getClientId() {
        return clientId;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void connectAck() {
        new NullPointerException("bogus for debug").printStackTrace();
        logger.info("connectAck called.");
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.connectAck();
            } catch (Exception ex) {
                logger.error("Exception in delegate.connectAck()", ex);
            }
        });
    }

    @Override
    public void tickGeneric(int tickerId, int tickType, double value) {
        logger.info("tickGeneric: " + tickerId + ", " + tickType + ", " + value);
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.tickGeneric(tickerId, tickType, value);
            } catch (Exception ex) {
                logger.error("Exception in delegate.tickGeneric()", ex);
            }
        });
    }

    @Override
    public void tickPrice(int tickerId, int field, double price, TickAttrib attrib) {
        logger.info("tickPrice: " + tickerId + ", " + field + ", " + price + ", " + attrib);
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.tickPrice(tickerId, field, price, attrib);
            } catch (Exception ex) {
                logger.error("Exception in delegate.tickPrice()", ex);
            }
        });
    }

    @Override
    public void tickSize(int tickerId, int field, Decimal size) {
        logger.info("tickSize: " + tickerId + ", " + field + ", " + size);
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.tickSize(tickerId, field, size);
            } catch (Exception ex) {
                logger.error("Exception in delegate.tickSize()", ex);
            }
        });
    }

    @Override
    public void tickOptionComputation(int tickerId, int field, int tickAttrib, double impliedVol, double delta,
            double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            delegate.tickOptionComputation(tickerId, field, tickAttrib, impliedVol, delta, optPrice, pvDividend, gamma,
                    vega, theta, undPrice);
        });
    }

    @Override
    public void tickString(int tickerId, int tickType, String value) {
        logger.info("tickString: " + tickerId + ", " + tickType + ", " + value);
        ibConnectionDelegates.stream().forEach((delegate) -> {
            delegate.tickString(tickerId, tickType, value);
        });
    }

    @Override
    public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints,
            double impliedFuture, int holdDays, String futureExpiry, double dividendImpact, double dividendsToExpiry) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.tickEFP(tickerId, tickType, basisPoints, formattedBasisPoints, impliedFuture, holdDays,
                        futureExpiry, dividendImpact, dividendsToExpiry);
            } catch (Exception ex) {
                logger.error("Exception in delegate.tickEFP()", ex);
            }
        });
    }

    @Override
    public void orderStatus(int orderId, String status, Decimal filled, Decimal remaining, double avgFillPrice,
            int permId, int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.orderStatus(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice,
                        clientId, whyHeld, mktCapPrice);
            } catch (Exception ex) {
                logger.error("Exception in delegate.orderStatus()", ex);
            }
        });
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.openOrder(orderId, contract, order, orderState);
            } catch (Exception ex) {
                logger.error("Exception in delegate.openOrder()", ex);
            }
        });
    }

    @Override
    public void openOrderEnd() {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.openOrderEnd();
            } catch (Exception ex) {
                logger.error("Exception in delegate.openOrderEnd()", ex);
            }
        });
    }

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.updateAccountValue(key, value, currency, accountName);
            } catch (Exception ex) {
                logger.error("Exception in delegate.updateAccountValue()", ex);
            }
        });
    }

    @Override
    public void updatePortfolio(Contract contract, Decimal position, double marketPrice, double marketValue,
            double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.updatePortfolio(contract, position, marketPrice, marketValue, averageCost, unrealizedPNL,
                        realizedPNL, accountName);
            } catch (Exception ex) {
                logger.error("Exception in delegate.updatePortfolio()", ex);
            }
        });
    }

    @Override
    public void updateAccountTime(String timeStamp) {
        logger.info("updateAccountTime: " + timeStamp);
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.updateAccountTime(timeStamp);
            } catch (Exception ex) {
                logger.error("Exception in delegate.updateAccountTime()", ex);
            }
        });
    }

    @Override
    public void accountDownloadEnd(String accountName) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.accountDownloadEnd(accountName);
            } catch (Exception ex) {
                logger.error("Exception in delegate.accountDownloadEnd()", ex);
            }
        });
    }

    @Override
    public void nextValidId(int orderId) {
        logger.info("nextValidId: " + orderId);
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.nextValidId(orderId);
            } catch (Exception ex) {
                logger.error("Exception in delegate.nextValidId()", ex);
            }
        });
    }

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.contractDetails(reqId, contractDetails);
            } catch (Exception ex) {
                logger.error("Exception in delegate.contractDetails()", ex);
            }
        });
    }

    @Override
    public void bondContractDetails(int reqId, ContractDetails contractDetails) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.bondContractDetails(reqId, contractDetails);
            } catch (Exception ex) {
                logger.error("Exception in delegate.bondContractDetails()", ex);
            }
        });
    }

    @Override
    public void contractDetailsEnd(int reqId) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.contractDetailsEnd(reqId);
            } catch (Exception ex) {
                logger.error("Exception in delegate.contractDetailsEnd()", ex);
            }
        });
    }

    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.execDetails(reqId, contract, execution);
            } catch (Exception ex) {
                logger.error("Exception in delegate.execDetails()", ex);
            }
        });
    }

    @Override
    public void execDetailsEnd(int reqId) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.execDetailsEnd(reqId);
            } catch (Exception ex) {
                logger.error("Exception in delegate.execDetailsEnd()", ex);
            }
        });
    }

    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.updateNewsBulletin(msgId, msgType, message, origExchange);
            } catch (Exception ex) {
                logger.error("Exception in delegate.updateNewsBulletin()", ex);
            }
        });
    }

    @Override
    public void updateMktDepth(int tickerId, int position, int operation, int side, double price, Decimal size) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.updateMktDepth(tickerId, position, operation, side, price, size);
            } catch (Exception ex) {
                logger.error("Exception in delegate.updateMktDepth()", ex);
            }
        });
    }

    @Override
    public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price,
            Decimal size, boolean isSmartDepth) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.updateMktDepthL2(tickerId, position, marketMaker, operation, side, price, size, isSmartDepth);
            } catch (Exception ex) {
                logger.error("Exception in delegate.updateMktDepthL2()", ex);
            }
        });
    }

    @Override
    public void managedAccounts(String accountsList) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.managedAccounts(accountsList);
            } catch (Exception ex) {
                logger.error("Exception in delegate.managedAccounts()", ex);
            }
        });
    }

    @Override
    public void receiveFA(int faDataType, String xml) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.receiveFA(faDataType, xml);
            } catch (Exception ex) {
                logger.error("Exception in delegate.receiveFA()", ex);
            }
        });
    }

    @Override
    public void scannerParameters(String xml) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.scannerParameters(xml);
            } catch (Exception ex) {
                logger.error("Exception in delegate.scannerParameters()", ex);
            }
        });
    }

    @Override
    public void historicalData(int reqId, Bar bar) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.historicalData(reqId, bar);
            } catch (Exception ex) {
                logger.error("Exception in delegate.historicalData()", ex);
            }
        });
    }

    @Override
    public void historicalDataEnd(int reqId, String startDateStr, String endDateStr) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.historicalDataEnd(reqId, startDateStr, endDateStr);
            } catch (Exception ex) {
                logger.error("Exception in delegate.historicalDataEnd()", ex);
            }
        });
    }

    @Override
    public void historicalDataUpdate(int reqId, Bar bar) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.historicalDataUpdate(reqId, bar);
            } catch (Exception ex) {
                logger.error("Exception in delegate.historicalDataUpdate()", ex);
            }
        });
    }

    @Override
    public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark,
            String projection, String legsStr) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.scannerData(reqId, rank, contractDetails, distance, benchmark, projection, legsStr);
            } catch (Exception ex) {
                logger.error("Exception in delegate.scannerData()", ex);
            }
        });
    }

    @Override
    public void scannerDataEnd(int reqId) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.scannerDataEnd(reqId);
            } catch (Exception ex) {
                logger.error("Exception in delegate.scannerDataEnd()", ex);
            }
        });
    }

    @Override
    public void currentTime(long time) {
        logger.info("currentTime: " + time);
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.currentTime(time);
            } catch (Exception ex) {
                logger.error("Exception in delegate.currentTime()", ex);
            }
        });
    }

    @Override
    public void realtimeBar(int reqId, long time, double open, double high, double low, double close, Decimal volume,
            Decimal wap, int count) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.realtimeBar(reqId, time, open, high, low, close, volume, wap, count);
            } catch (Exception ex) {
                logger.error("Exception in delegate.realtimeBar()", ex);
            }
        });
    }

    @Override
    public void fundamentalData(int reqId, String data) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.fundamentalData(reqId, data);
            } catch (Exception ex) {
                logger.error("Exception in delegate.fundamentalData()", ex);
            }
        });
    }

    @Override
    public void tickSnapshotEnd(int reqId) {
        logger.info("tickSnapshotEnd: " + reqId);
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.tickSnapshotEnd(reqId);
            } catch (Exception ex) {
                logger.error("Exception in delegate.tickSnapshotEnd()", ex);
            }
        });
    }

    @Override
    public void deltaNeutralValidation(int reqId, DeltaNeutralContract deltaNeutralContract) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.deltaNeutralValidation(reqId, deltaNeutralContract);
            } catch (Exception ex) {
                logger.error("Exception in delegate.deltaNeutralValidation()", ex);
            }
        });
    }

    @Override
    public void marketDataType(int reqId, int marketDataType) {
        logger.info("marketDataType: " + reqId + ", " + marketDataType);
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.marketDataType(reqId, marketDataType);
            } catch (Exception ex) {
                logger.error("Exception in delegate.marketDataType()", ex);
            }
        });
    }

    @Override
    public void positionEnd() {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.positionEnd();
            } catch (Exception ex) {
                logger.error("Exception in delegate.positionEnd()", ex);
            }
        });
    }

    @Override
    public void commissionReport(CommissionReport commissionReport) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.commissionReport(commissionReport);
            } catch (Exception ex) {
                logger.error("Exception in delegate.commissionReport()", ex);
            }
        });
    }

    @Override
    public void position(String account, Contract contract, Decimal pos, double avgCost) {
        logger.info("position: " + account + ", " + contract + ", " + pos + ", " + avgCost);
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.position(account, contract, pos, avgCost);
            } catch (Exception ex) {
                logger.error("Exception in delegate.position()", ex);
            }
        });
    }

    @Override
    public void positionMulti(int reqId, String account, String modelCode, Contract contract, Decimal pos,
            double avgCost) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.positionMulti(reqId, account, modelCode, contract, pos, avgCost);
            } catch (Exception ex) {
                logger.error("Exception in delegate.positionMulti()", ex);
            }
        });
    }

    @Override
    public void positionMultiEnd(int reqId) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.positionMultiEnd(reqId);
            } catch (Exception ex) {
                logger.error("Exception in delegate.positionMultiEnd()", ex);
            }
        });
    }

    @Override
    public void accountSummary(int reqId, String account, String tag, String value, String currency) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.accountSummary(reqId, account, tag, value, currency);
            } catch (Exception ex) {
                logger.error("Exception in delegate.accountSummary()", ex);
            }
        });
    }

    @Override
    public void accountSummaryEnd(int reqId) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.accountSummaryEnd(reqId);
            } catch (Exception ex) {
                logger.error("Exception in delegate.accountSummaryEnd()", ex);
            }
        });
    }

    @Override
    public void verifyMessageAPI(String apiData) {
        logger.info("verifyMessageAPI: " + apiData);
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.verifyMessageAPI(apiData);
            } catch (Exception ex) {
                logger.error("Exception in delegate.verifyMessageAPI()", ex);
            }
        });
    }

    @Override
    public void verifyCompleted(boolean isSuccessful, String errorText) {
        logger.info("verifyCompleted: isSuccessful=" + isSuccessful + ", errorText=" + errorText);
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.verifyCompleted(isSuccessful, errorText);
            } catch (Exception ex) {
                logger.error("Exception in delegate.verifyCompleted()", ex);
            }
        });
    }

    @Override
    public void displayGroupList(int reqId, String groups) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.displayGroupList(reqId, groups);
            } catch (Exception ex) {
                logger.error("Exception in delegate.displayGroupList()", ex);
            }
        });
    }

    @Override
    public void displayGroupUpdated(int reqId, String contractInfo) {
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.displayGroupUpdated(reqId, contractInfo);
            } catch (Exception ex) {
                logger.error("Exception in delegate.displayGroupUpdated()", ex);
            }
        });
    }

    @Override
    public void error(Exception e) {
        logger.error("Error occurred: ", e);
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.error(e);
            } catch (Exception ex) {
                logger.error("Exception in delegate.error(Exception)", ex);
            }
        });
    }

    @Override
    public void error(String str) {
        logger.error("Error occurred: " + str);
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.error(str);
            } catch (Exception ex) {
                logger.error("Exception in delegate.error(String)", ex);
            }
        });
    }

    @Override
    public void error(int id, int errorCode, String errorMsg, String advancedOrderRejectJson) {
        logger.error("Error occurred: id=" + id + ", errorCode=" + errorCode + ", errorMsg=" + errorMsg
                + ", advancedOrderRejectJson=" + advancedOrderRejectJson);
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.error(id, errorCode, errorMsg, advancedOrderRejectJson);
            } catch (Exception ex) {
                logger.error("Exception in delegate.error(int, int, String, String)", ex);
            }
        });
    }

    @Override
    public void connectionClosed() {
        logger.info("Connection closed.");
        ibConnectionDelegates.stream().forEach((delegate) -> {
            try {
                delegate.connectionClosed();
            } catch (Exception ex) {
                logger.error("Exception in delegate.connectionClosed()", ex);
            }
        });
    }

}
