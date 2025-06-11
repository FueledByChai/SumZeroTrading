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

import com.ib.client.TagValue;
import com.sumzerotrading.broker.order.OrderStatus;
import com.sumzerotrading.broker.order.TradeDirection;
import com.sumzerotrading.broker.order.TradeOrder;
import com.sumzerotrading.data.FuturesTicker;
import com.sumzerotrading.data.InstrumentType;
import com.sumzerotrading.data.OptionTicker;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;
import com.sumzerotrading.marketdata.QuoteType;
import java.awt.Image;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.imageio.ImageIO;

/**
 *
 * @author Rob Terpilowski
 */
public class IbUtils {

    public static void throwUnsupportedException() {
        if (true) {
            throw new UnsupportedOperationException("IB Changed implementation, this needs to be fixed");
        }
    }

    public static String translateToIbFuturesSymbol(String symbol) {
        switch (symbol) {
        case "6C":
            return "CAD";
        case "6E":
            return "EUR";
        case "6J":
            return "JPY";
        case "6S":
            return "CHF";
        case "6B":
            return "GBP";
        case "VX":
            return "VIX";
        default:
            return symbol;
        }
    }

    public static String getOrderType(TradeOrder.Type orderType) {
        if (null != orderType) {
            switch (orderType) {
            case LIMIT:
                return "LMT";
            case MARKET:
                return "MKT";
            case STOP:
                return "STP";
            case MARKET_ON_CLOSE:
                return "MOC";
            case MARKET_ON_OPEN:
                return "MKT";
            default:
                throw new IllegalStateException("Unknown order type: " + orderType);
            }
        } else {
            throw new IllegalStateException("Unknown order type: " + orderType);
        }
    }

    public static String getTif(TradeOrder.Duration duration) {
        if (duration == null) {
            return "DAY";
        }

        switch (duration) {
        case DAY:
            return "DAY";
        case GOOD_UNTIL_CANCELED:
            return "GTC";
        case GOOD_UTNIL_TIME:
            return "GTD";
        case FILL_OR_KILL:
            return "IOC";
        case MARKET_ON_OPEN:
            return "OPG";
        default:
            throw new IllegalStateException("Unknown duration: " + duration);
        }
    }

    public static String getSecurityType(InstrumentType instrumentType) {
        if (null != instrumentType)
            switch (instrumentType) {
            case STOCK:
                return "STK";
            case FOREX:
                return "CASH";
            case FUTURES:
                return "FUT";
            case OPTION:
                return "OPT";
            case INDEX:
                return "IND";
            case COMBO:
                return "BAG";
            case CFD:
                return "CFD";
            default:
                throw new IllegalStateException("Unknown instrument type: " + instrumentType);
            }
        else {
            throw new SumZeroException("Instrument type can't be null");
        }
    }

    public static String getAction(TradeDirection direction) {
        if (direction == TradeDirection.BUY) {
            return "BUY";
        } else if (direction == TradeDirection.SELL) {
            return "SELL";
        } else if (direction == TradeDirection.SELL_SHORT) {
            return "SELL";
        } else if (direction == TradeDirection.BUY_TO_COVER) {
            return "BUY";
        } else {
            throw new IllegalStateException("Unknown trade direction: " + direction);
        }
    }

    /**
     * Quote types are defined here....
     * https://interactivebrokers.github.io/tws-api/tick_types.html
     * 
     * @param field
     * @return
     */
    public static QuoteType getQuoteType(int field) {
        switch (field) {
        case 0:
        case 69:
            return QuoteType.BID_SIZE;
        case 1:
        case 66:
            return QuoteType.BID;
        case 2:
        case 67:
            return QuoteType.ASK;
        case 3:
        case 70:
            return QuoteType.ASK_SIZE;
        case 4:
        case 68:
            return QuoteType.LAST;
        case 5:
        case 71:
            return QuoteType.LAST_SIZE;
        case 8:
        case 74:
            return QuoteType.VOLUME;
        case 9:
        case 75:
            return QuoteType.CLOSE;
        case 14:
        case 76:
            return QuoteType.OPEN;
        default:
            return QuoteType.UNKNOWN;
        }
    }

    public static OrderStatus.Status getOrderStatus(String status) {
        if ("PendingSubmit".equalsIgnoreCase(status)) {
            return OrderStatus.Status.NEW;
        } else if ("PendingCancel".equalsIgnoreCase(status)) {
            return OrderStatus.Status.PENDING_CANCEL;
        } else if ("PreSubmitted".equalsIgnoreCase(status)) {
            return OrderStatus.Status.NEW;
        } else if ("Submitted".equalsIgnoreCase(status)) {
            return OrderStatus.Status.NEW;
        } else if ("Cancelled".equalsIgnoreCase(status)) {
            return OrderStatus.Status.CANCELED;
        } else if ("Filled".equalsIgnoreCase(status)) {
            return OrderStatus.Status.FILLED;
        } else if ("Inactive".equalsIgnoreCase(status)) {
            return OrderStatus.Status.CANCELED;
        } else {
            return OrderStatus.Status.UNKNOWN;
        }
    }

    public static String getExpiryString(int expiryMonth, int expiryYear) {
        StringBuilder sb = new StringBuilder();

        sb.append(expiryYear);

        if (expiryMonth < 10) {
            sb.append("0");
        }
        sb.append(expiryMonth);
        return sb.toString();
    }

    public static String getExpiryString(int expiryDay, int expiryMonth, int expiryYear) {
        String dayString = Integer.toString(expiryDay);
        if (expiryDay < 10) {
            dayString = "0" + dayString;
        }

        return getExpiryString(expiryMonth, expiryYear) + dayString;
    }

    public static String getOptionRight(OptionTicker.Right right) {
        switch (right) {
        case Call:
            return "C";
        case Put:
            return "P";
        default:
            throw new SumZeroException("Unknown option right: " + right);
        }
    }

    public static Image getIconImage() {
        try {
            return ImageIO.read(IbUtils.class.getResourceAsStream("/com/sumzerotrading/ib/ui/IB-Icon-sm.jpg"));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static BigDecimal getIbMultiplier(Ticker ticker) {
        if (ticker instanceof FuturesTicker) {
            FuturesTicker futuresTicker = (FuturesTicker) ticker;
            switch (futuresTicker.getSymbol()) {
            case "ZC":
            case "ZS":
            case "ZW":
                return new BigDecimal(5000);
            default:
                return ticker.getContractMultiplier();

            }
        }

        return ticker.getContractMultiplier();
    }

    public static Vector<TagValue> getDefaultTagVector() {
        Vector<TagValue> list = new Vector<>();
        list.add(new TagValue("XYZ", "XYZ"));

        return list;
    }

    public static List<TagValue> getDefaultTagList() {
        List<TagValue> list = new ArrayList<>();
        list.add(new TagValue("XYZ", "XYZ"));

        return list;
    }

}
