package BLL;

import BE.Order;
import DAL.IOrderDataAccess;
import DAL.OrderDAO;

public class OrderManager {

    private IOrderDataAccess orderDataAccess;

    public OrderManager() throws Exception{
        orderDataAccess = new OrderDAO();
    }

    public Order findOrderByOrderNumber(String orderNumber) throws Exception {
        return orderDataAccess.findOrderByOrderNumber(orderNumber);
    }
}
