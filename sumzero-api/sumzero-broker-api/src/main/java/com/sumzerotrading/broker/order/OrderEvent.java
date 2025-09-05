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

package com.sumzerotrading.broker.order;

import java.io.Serializable;

/**
 *
 * @author Rob Terpilowski
 */
public class OrderEvent implements Serializable {

    public static long serialVersionUID = 1L;
    protected OrderTicket order;
    protected OrderStatus orderStatus;

    public OrderEvent(OrderTicket order, OrderStatus orderStatus) {
        this.order = order;
        this.orderStatus = orderStatus;
    }

    public OrderTicket getOrder() {
        return order;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OrderEvent other = (OrderEvent) obj;
        if (this.order != other.order && (this.order == null || !this.order.equals(other.order))) {
            return false;
        }
        if (this.orderStatus != other.orderStatus
                && (this.orderStatus == null || !this.orderStatus.equals(other.orderStatus))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.order != null ? this.order.hashCode() : 0);
        hash = 67 * hash + (this.orderStatus != null ? this.orderStatus.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "OrderEvent{" + "order=" + order + ", orderStatus=" + orderStatus + '}';
    }

}
