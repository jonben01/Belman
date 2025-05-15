package DAL;

import BE.Order;
import BE.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


public class OrderDAO implements IOrderDataAccess {

    private IProductDataAccess productDataAccess;

    public OrderDAO() throws Exception{
        productDataAccess = new ProductDAO();
    }

    @Override
    public Order findOrderByOrderNumber(String orderNumber) throws Exception {
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Order number cannot be null or empty");
        }

        String sql = "SELECT * FROM Orders WHERE order_number = ?";

        try (Connection conn = DBConnector.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orderNumber);

            //autoclose
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setOrderNumber(orderNumber);
                    order.setCustomerEmail(rs.getString("customer_email"));

                    int orderId = rs.getInt("id");
                    order.setId(orderId);

                    List<Product> products = productDataAccess.getAllProductsForOrder(orderId);
                    order.setProducts(products);

                    System.out.println("Found order:" + order);
                    return order;
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error while finding order:" + orderNumber, e);
        }
        return null;
    }
}
