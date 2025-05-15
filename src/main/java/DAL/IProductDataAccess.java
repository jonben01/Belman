package DAL;

import BE.Product;

import java.util.List;

public interface IProductDataAccess {

    List<Product> getAllProductsForOrder(int orderId) throws Exception;
}
