package BE;

import java.awt.image.BufferedImage;
import java.util.List;

public class Product {
    private int id;
    private String orderNumber;
    private String productNumber;
    private List<Photo> photos;

    public Product(int id, String orderNumber, String productNumber, List<Photo> photos) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.productNumber = productNumber;
        this.photos = photos;
    }

    public Product() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    @Override
    public String toString() {
        return productNumber;
    }
}
