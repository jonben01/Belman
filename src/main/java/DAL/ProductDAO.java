package DAL;

import BE.Photo;
import BE.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductDAO implements IProductDataAccess {

    private IPhotoDataAccess photoDataAccess;

    public ProductDAO() throws Exception{
        photoDataAccess = new PhotoDAO();
    }

    @Override
    public List<Product> getAllProductsForOrder(int orderId) throws Exception {

        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM Products WHERE order_id = ?";

        try (Connection conn = DBConnector.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);

            List<Integer> productIds = new ArrayList<>();

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    int productId = rs.getInt("id");

                    product.setId(productId);
                    product.setProductNumber(rs.getString("products_order_number"));

                    products.add(product);
                    productIds.add(productId);
                }
            }
            Map<Integer, List<Photo>> photoMap = photoDataAccess.getPhotosForProducts(productIds);

            for (Product product : products) {
                product.setPhotos(photoMap.getOrDefault(product.getId(), new ArrayList<>()));
            }

        } catch (SQLException e) {
            throw new Exception("Error while finding products for order:" + orderId, e);
        }
        return products;
    }

}
