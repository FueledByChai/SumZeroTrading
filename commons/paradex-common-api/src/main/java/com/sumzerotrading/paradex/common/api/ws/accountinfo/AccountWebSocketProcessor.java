package com.sumzerotrading.paradex.common.api.ws.accountinfo;

import org.json.JSONObject;

import com.sumzerotrading.websocket.AbstractWebSocketProcessor;
import com.sumzerotrading.websocket.IWebSocketClosedListener;

public class AccountWebSocketProcessor extends AbstractWebSocketProcessor<IAccountUpdate> {

    public AccountWebSocketProcessor(IWebSocketClosedListener listener) {
        super(listener);
    }

    @Override
    protected IAccountUpdate parseMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            if (!jsonObject.has("method")) {
                return null;
            }
            String method = jsonObject.getString("method");

            if ("subscription".equals(method)) {
                JSONObject params = jsonObject.getJSONObject("params");
                JSONObject data = params.getJSONObject("data");
                String accountValueString = data.getString("account_value");
                String maintMarginString = data.getString("maintenance_margin_requirement");

                IAccountUpdate accountInfo = new ParadexAccountInfoUpdate();
                accountInfo.setAccountValue(Double.parseDouble(accountValueString));
                accountInfo.setMaintenanceMargin(Double.parseDouble(maintMarginString));
                return accountInfo;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("Error parsing message: " + message, e);
            return null;
        }
    }

}
