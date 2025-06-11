package DAL;

import BE.Order;

public interface IOrderDataAccess {

    Order findOrderByOrderNumber(String orderNumber) throws Exception;
}
