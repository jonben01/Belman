package DAL;


import BE.Order;
import BE.Product;

import java.util.List;

public interface IOrderDataAccess {

    Order findOrderByOrderNumber(String orderNumber) throws Exception;
}
