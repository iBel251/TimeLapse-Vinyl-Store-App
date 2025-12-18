package org.yearup.data;

import org.yearup.models.Order;
import org.yearup.models.OrderLineItem;

public interface OrderDao {
    Order create(Order order);
    void addLineItem(OrderLineItem lineItem);
}
