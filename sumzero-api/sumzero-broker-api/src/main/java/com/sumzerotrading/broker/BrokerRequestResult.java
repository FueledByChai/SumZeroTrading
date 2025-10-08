package com.sumzerotrading.broker;

public class BrokerRequestResult {

    protected boolean success;
    protected String message = "";

    public BrokerRequestResult() {
        this.success = true;
    }

    public BrokerRequestResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (success ? 1231 : 1237);
        result = prime * result + ((message == null) ? 0 : message.hashCode());
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
        BrokerRequestResult other = (BrokerRequestResult) obj;
        if (success != other.success)
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "BrokerRequestResult [success=" + success + ", message=" + message + "]";
    }

}
