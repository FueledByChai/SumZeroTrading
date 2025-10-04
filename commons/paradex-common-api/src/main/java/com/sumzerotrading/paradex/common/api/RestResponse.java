package com.sumzerotrading.paradex.common.api;

public class RestResponse {
    int httpCode;
    String body;

    public RestResponse(int httpCode, String body) {
        this.httpCode = httpCode;
        this.body = body;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public String getBody() {
        return body;
    }

    public boolean isSuccessful() {
        return httpCode >= 200 && httpCode < 300;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + httpCode;
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RestResponse other = (RestResponse) obj;
        if (httpCode != other.httpCode)
            return false;
        if (body == null) {
            if (other.body != null)
                return false;
        } else if (!body.equals(other.body))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "RestResponse [httpCode=" + httpCode + ", body=" + body + "]";
    }

}
