package com.sumzerotrading.hyperliquid.ws;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumzerotrading.hyperliquid.ws.json.ws.SubmitPostResponse;
import com.sumzerotrading.websocket.AbstractWebSocketProcessor;
import com.sumzerotrading.websocket.IWebSocketClosedListener;

public class HyperliquidPostWebSocketProcessor extends AbstractWebSocketProcessor<SubmitPostResponse> {

    public HyperliquidPostWebSocketProcessor(IWebSocketClosedListener closeListener) {
        super(closeListener);
    }

    @Override
    protected SubmitPostResponse parseMessage(String message) {
        logger.info("POST Message received: " + message);

        try {
            JSONObject jsonObject = new JSONObject(message);
            if (!jsonObject.has("channel")) {
                return null;
            }
            String channel = jsonObject.getString("channel");

            if ("post".equals(channel)) {
                SubmitPostResponse postResponse = new SubmitPostResponse();

                JSONObject data = jsonObject.getJSONObject("data");
                int requestId = data.getInt("id");
                JSONObject response = data.getJSONObject("response");
                JSONObject payload = response.getJSONObject("payload");
                String requestStatus = payload.getString("status");
                if ("err".equalsIgnoreCase(requestStatus)) {
                    String errorResponseMessage = payload.getString("response");
                    postResponse.success = false;
                    postResponse.requestStatus = requestStatus;
                    postResponse.requestId = requestId;
                    postResponse.errorMessage = errorResponseMessage;
                } else {
                    JSONObject payloadResponse = payload.getJSONObject("response");
                    JSONObject payloadData = payloadResponse.getJSONObject("data");
                    JSONArray payloadStatuses = payloadData.getJSONArray("statuses");

                    postResponse.requestId = requestId;
                    postResponse.requestStatus = requestStatus;

                    for (int i = 0; i < payloadStatuses.length(); i++) {

                        JSONObject statusObj = payloadStatuses.getJSONObject(i);

                        int oid = -1;
                        String filledStatus = null;
                        String clientOid = null;

                        if (statusObj.has("filled")) {
                            JSONObject filledObj = statusObj.getJSONObject("filled");
                            oid = filledObj.getInt("oid");
                            filledStatus = "filled";
                            clientOid = filledObj.optString("cloid", null);
                            postResponse.addSubmitOrderResponse(oid, clientOid, filledStatus);
                        } else if (statusObj.has("error")) {
                            postResponse.addSubmitOrderResponse("error");
                        } else {
                            throw new JSONException("Unknown status format: " + statusObj.toString());
                        }
                    }
                }
                return postResponse;
            } else {
                logger.debug("Ignoring non-post message: {}", message);
                return null;
            }

        } catch (

        JSONException e) {
            logger.error("Error parsing POST message: " + message, e);
        }
        return null;
    }

}
