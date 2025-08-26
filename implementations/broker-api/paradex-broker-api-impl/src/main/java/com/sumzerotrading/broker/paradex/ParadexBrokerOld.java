package com.sumzerotrading.broker.paradex;

// import java.math.BigDecimal;
// import java.time.ZonedDateTime;
// import java.util.ArrayList;
// import java.util.List;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Primary;

// import com.fueledbychai.IHistoricalDataProvider;
// import com.fueledbychai.OrderStatusListener;
// import com.fueledbychai.broker.IBroker;
// import com.fueledbychai.broker.IOrder;
// import com.fueledbychai.broker.IOrderStatusUpdate;
// import com.fueledbychai.broker.IPositionInfo;
// import com.fueledbychai.broker.OrderType;
// import com.fueledbychai.broker.Side;
// import com.fueledbychai.paradex.config.ISystemConfig;
// import com.fueledbychai.paradex.ws.AccountWebSocketProcessor;
// import com.fueledbychai.paradex.ws.OrderStatusWebSocketProcessor;
// import com.sumzerotrading.data.CryptoTicker;
// import com.sumzerotrading.data.Exchange;

// import jakarta.annotation.PostConstruct;

public class ParadexBrokerOld {// implements OrderStatusListener, Runnable, IBroker, IHistoricalDataProvider {
    // protected static Logger logger =
    // LoggerFactory.getLogger(ParadexBroker.class);

    // @Autowired
    // protected ISystemConfig config;
    // protected ParadoxRestApi restApi;
    // protected String jwtToken = "";
    // protected Thread thread;
    // protected static ParadexBroker instance = null;
    // protected ParadexWebSocketClient accountInfoWSClient;
    // protected ParadexWebSocketClient orderStatusWSClient;
    // protected AccountWebSocketProcessor accountWebSocketProcessor;
    // protected OrderStatusWebSocketProcessor orderStatusProcessor;

    // @PostConstruct
    // protected void init() {
    // logger.info("Initializing ParadexBroker");
    // restApi = new ParadoxRestApi(config.getRestUrl(), config.getParadexAddress(),
    // config.getParadexKey());
    // accountWebSocketProcessor = new AccountWebSocketProcessor(() -> {
    // logger.info("Account WebSocket closed, trying to restart...");
    // startAccountInfoWSClient();
    // });
    // orderStatusProcessor = new OrderStatusWebSocketProcessor(this, () -> {
    // logger.info("Order status WebSocket closed, trying to restart...");
    // startOrderStatusWSClient();
    // });

    // }

    // public void connectToBroker() {
    // thread = new Thread(this);
    // thread.start();
    // startAccountInfoWSClient();
    // startOrderStatusWSClient();

    // }

    // // public static ParadexBroker getParadexBroker(ISystemConfig config) {
    // // if (instance == null) {
    // // instance = new ParadexBroker(config);
    // // }
    // // return instance;
    // // }

    // // public static IBroker getParadexBroker() {
    // // if (instance == null) {
    // // throw new IllegalStateException("ParadexBroker has not been initialized");
    // // }
    // // return instance;
    // // }

    // @Override
    // public void run() {
    // while (true) {
    // try {
    // logger.info("Refreshing JWT token");
    // jwtToken = authenticate();

    // Thread.sleep(60000);
    // } catch (Exception e) {
    // logger.error(e.getMessage(), e);
    // }
    // }
    // }

    // @Override
    // public void cancelOrder(String orderId) throws Exception {
    // if (jwtToken.isEmpty()) {
    // logger.info("JWT Token is empty. Authenticating...");
    // jwtToken = authenticate();
    // }

    // restApi.cancelOrder(jwtToken, orderId);
    // }

    // @Override
    // public String placeOrder(IOrder order) throws Exception {
    // if (jwtToken.isEmpty()) {
    // logger.info("JWT Token is empty. Authenticating...");
    // jwtToken = authenticate();
    // }

    // logger.info("Placing order: " + order);
    // order.setSubmittedTime(ZonedDateTime.now()); // Set the submitted time before
    // sending to the API
    // String orderId = restApi.placeOrder(jwtToken, order);
    // order.setOrderId(orderId);
    // return orderId;

    // }

    // @Override
    // public void cancelAllOrders(CryptoTicker ticker) {
    // if (jwtToken.isEmpty()) {
    // logger.info("JWT Token is empty. Authenticating...");
    // try {
    // jwtToken = authenticate();
    // } catch (Exception e) {
    // logger.error("Error authenticating: " + e.getMessage());
    // return;
    // }
    // }

    // restApi.cancelAllOrders(jwtToken, ticker.getSymbol());
    // }

    // @Override
    // public List<IOrder> getOpenOrders(CryptoTicker ticker) {
    // if (jwtToken.isEmpty()) {
    // logger.info("JWT Token is empty. Authenticating...");
    // try {
    // jwtToken = authenticate();
    // } catch (Exception e) {
    // logger.error("Error authenticating: " + e.getMessage());
    // return new ArrayList<>();
    // }
    // }

    // return restApi.getOpenOrders(jwtToken, ticker.getSymbol());
    // }

    // @Override
    // public List<IPositionInfo> getPositions() {
    // if (jwtToken.isEmpty()) {
    // logger.info("JWT Token is empty. Authenticating...");
    // try {
    // jwtToken = authenticate();
    // } catch (Exception e) {
    // logger.error("Error authenticating: " + e.getMessage());
    // return new ArrayList<>();
    // }
    // }

    // return restApi.getPositionInfo(jwtToken);
    // }

    // @Override
    // public IPositionInfo getOpenPosition(CryptoTicker ticker) {
    // if (jwtToken.isEmpty()) {
    // logger.info("JWT Token is empty. Authenticating...");
    // try {
    // jwtToken = authenticate();
    // } catch (Exception e) {
    // logger.error("Error authenticating: " + e.getMessage());
    // return null;
    // }
    // }

    // List<IPositionInfo> positions = getPositions();
    // for (IPositionInfo position : positions) {
    // if (position.getTicker().equals(ticker)) {
    // return position;
    // }
    // }

    // return new ParadexPositionInfo(ticker, 0, 0);

    // }

    // public List<OHLCBar> getHistoricalData(CryptoTicker ticker, int
    // resolutionInMinutes, int lookbackInMinutes) {
    // return restApi.getOHLCBars(ticker.getSymbol(), resolutionInMinutes,
    // lookbackInMinutes);
    // }

    // @Override
    // public void orderStatusUpdated(IOrderStatusUpdate orderStatus) {
    // logger.info("Order status updated: " + orderStatus);
    // }

    // protected String authenticate() throws Exception {
    // jwtToken = restApi.getJwtToken();
    // logger.info("Obtained JWT Token");
    // return jwtToken;
    // }

    // public synchronized String getNewJwtToken() {
    // try {
    // return restApi.getJwtToken();
    // } catch (Exception e) {
    // throw new IllegalStateException(e);
    // }
    // }

    // @Override
    // public void addAccountUpdateListener(IAccountUpdateListener
    // accountUpdateListener) {
    // accountWebSocketProcessor.addAccountUpdateListener(accountUpdateListener);
    // }

    // @Override
    // public void addOrderStatusListener(OrderStatusListener orderStatusListener) {
    // orderStatusProcessor.addListener(orderStatusListener);

    // }

    // public void startAccountInfoWSClient() {
    // try {
    // logger.info("Starting account WebSocket client");
    // String jwtToken = getNewJwtToken();
    // accountInfoWSClient = new ParadexWebSocketClient(config.getWsUrl(),
    // "account", accountWebSocketProcessor,
    // jwtToken);
    // accountInfoWSClient.connect();
    // } catch (Exception e) {
    // throw new IllegalStateException(e);
    // }

    // }

    // public void startOrderStatusWSClient() {
    // logger.info("Starting order status WebSocket client");
    // String jwtToken = getNewJwtToken();

    // try {
    // orderStatusWSClient = new ParadexWebSocketClient(config.getWsUrl(),
    // "orders.ALL", orderStatusProcessor,
    // jwtToken);

    // orderStatusWSClient.connect();
    // } catch (Exception e) {
    // throw new IllegalStateException(e);
    // }

    // }

    // @Override
    // public void markPriceUpdated(String symbol, BigDecimal markPrice, long
    // timestamp) {
    // logger.info("Mark Price Updated: {} - {} at {}", symbol, markPrice,
    // timestamp);
    // }

    // @Override
    // public void fundingRateUpdated(String symbol, BigDecimal fundingRate, long
    // timestamp) {
    // logger.info("Funding Rate Updated: {} - {} at {}", symbol, fundingRate,
    // timestamp);
    // }

    // // Add a conditional bean for IBroker
    // // @Bean
    // // @ConditionalOnProperty(name = "use.paradex.broker", havingValue = "true",
    // // matchIfMissing = false)
    // // public IBroker paradexBrokerAsIBroker() {
    // // return this;
    // // }

    // public static void main(String[] args) throws Exception {
    // String configFileLocation =
    // "/Users/RobTerpilowski/Code/JavaProjects/MarketMaker/stuff.txt";
    // IBroker broker = null; //
    // nParadexBroker.getParadexBroker(configFileLocation);
    // IOrder order = new ParadexOrder();
    // CryptoTicker ticker = new CryptoTicker("TRUMP-USD-PERP",
    // Exchange.HYPERLIQUID);
    // CryptoTicker ticker2 = new CryptoTicker("BTC-USD-PERP",
    // Exchange.HYPERLIQUID);
    // order.setClientId("MockClient2");

    // order.setSize(BigDecimal.valueOf(3.0));
    // order.setSide(Side.BUY);
    // // order.setOrderType(OrderType.LIMIT);
    // // order.setLimitPrice(BigDecimal.valueOf(10.00));
    // // order.setTimeInForce(TimeInForce.POST_ONLY);

    // order.setOrderType(OrderType.MARKET);

    // order.setTicker(ticker.getSymbol());
    // try {

    // // for (ParadexPositionInfo position : broker.getPositions()) {
    // // System.out.println(position.getTicker().getSymbol() + " " +
    // // position.getInventory() + " "
    // // + position.getLiquidationPrice());
    // // }

    // System.out.println("Open position: " + broker.getOpenPosition(ticker2));
    // // broker.placeOrder(order);
    // Thread.sleep(10000);
    // // broker.cancelAllOrders(ticker);
    // // broker.authenticate();

    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

}
