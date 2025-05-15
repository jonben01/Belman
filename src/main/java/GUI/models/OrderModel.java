package GUI.models;

import BE.Order;
import BLL.OrderManager;

public class OrderModel {

    private OrderManager orderManager;

    public OrderModel() throws Exception{
        orderManager = new OrderManager();
    }

    public Order findOrderByOrderNumber(String orderNumber) throws Exception {
        return orderManager.findOrderByOrderNumber(orderNumber);
    }
}
