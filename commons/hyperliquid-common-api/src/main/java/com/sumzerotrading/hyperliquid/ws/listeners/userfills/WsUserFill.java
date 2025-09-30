package com.sumzerotrading.hyperliquid.ws.listeners.userfills;

import java.util.List;

public class WsUserFill {

    protected List<WsFill> fills = new java.util.ArrayList<>();
    protected String user;
    protected boolean isSnapshot = false;

    public void addFill(WsFill fill) {
        fills.add(fill);
    }

    public List<WsFill> getFills() {
        return fills;
    }

    public void setFills(List<WsFill> fills) {
        this.fills = fills;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isSnapshot() {
        return isSnapshot;
    }

    public void setSnapshot(boolean isSnapshot) {
        this.isSnapshot = isSnapshot;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fills == null) ? 0 : fills.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        result = prime * result + (isSnapshot ? 1231 : 1237);
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
        WsUserFill other = (WsUserFill) obj;
        if (fills == null) {
            if (other.fills != null)
                return false;
        } else if (!fills.equals(other.fills))
            return false;
        if (user == null) {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        if (isSnapshot != other.isSnapshot)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "WsUserFill [fills=" + fills + ", user=" + user + ", isSnapshot=" + isSnapshot + "]";
    }

}
