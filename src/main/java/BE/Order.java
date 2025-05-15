package BE;

import java.util.List;

public class Order {
    private int id;
    private String orderNumber;
    private String customerEmail;
    private List<Product> products;

    public Order(int id, String orderNumber, String customerEmail, List<Product> products) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerEmail = customerEmail;
        this.products = products;
    }

    public Order() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    @Override
    public String toString() {
        return orderNumber;
    }
}