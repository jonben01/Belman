package GUI.controllers;

import BE.Order;
import BE.Product;
import BE.Role;
import GUI.util.AlertHelper;
import GUI.util.Navigator;
import GUI.util.SessionManager;
import GUI.View;
import GUI.models.OrderModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class OrderController implements Initializable {
    @FXML
    private Button btnLogout;
    @FXML
    private Label lblOrderSelect;
    @FXML
    private TextField txtOrderNumber;
    @FXML
    private Button btnAdmin;
    @FXML
    private Button btnSearchOrder;

    private OrderModel orderModel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (SessionManager.getInstance().getCurrentUser().getRole() == Role.ADMIN) {
            btnAdmin.setVisible(true);
        }

    }
    public OrderController() {
        try {
            orderModel = new OrderModel();
        } catch (Exception e) {
            e.printStackTrace();
            //TODO alert
        }
    }

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        SessionManager.getInstance().logout();
        Navigator.getInstance().goTo(View.LOGIN);
    }

    @FXML
    public void handleAdmin(ActionEvent actionEvent) {
        Navigator.getInstance().goTo(View.ADMIN);
    }


    //TODO rename this method, since it can also be used to find product only
    @FXML
    public void handleSearchOrder(ActionEvent actionEvent) {
        String input = txtOrderNumber.getText();
        searchAndNavigate(input);
    }

    private void searchAndNavigate(String input) {
        if (input == null || input.isEmpty()) {
            return;
        }
        String[] parts = parseInput(input);
        String orderNumber = parts[0];
        String productNumber = parts[1];

        Order order = findOrder(orderNumber);
        if (order == null) {
            //TODO alert or css
            return;
        }

        Product product = findProductOnOrder(order, productNumber);
        if (productNumber != null && product == null) {
            boolean confirmed = AlertHelper.showConfirmationAlert("Product not found", "Product \"" + productNumber
                    + "\" was not found in order" + orderNumber + "\n Do you want to view the full order instead?");

            if (!confirmed) {
                return;
            }
        }
        Navigator.getInstance().goTo(View.PHOTO_DOC, controller -> {
            if (controller instanceof PhotoDocController pdocController) {
                pdocController.setOrderAndMaybeProduct(order, product);
            }
        });
    }

    private String[] parseInput(String input) {
        //mostly just a check to see if the user ONLY typed "-" and not a product number following it.
        if (input.contains("-")) {
            //split the input around the -, limit to 2 parts (order number and product number)
            String[] parts = input.split("-", 2);
            //return the order number, and if there is a product number, return that as well
            return new String[] {parts[0], parts.length > 1 ? parts[1] : null};
        }
        return new String[] {input, null};
    }

    private Order findOrder(String orderNumber) {
        try {
            Order order = orderModel.findOrderByOrderNumber(orderNumber);
            if (order == null) {
                //TODO alert
                // and remove lbl change below, wrong label lmao
                lblOrderSelect.setText("Order not found");
            }
            return order;

        } catch (Exception e) {
            e.printStackTrace();
            //TODO alert
            return null;
        }
    }

    private Product findProductOnOrder(Order order, String productNumber) {
        if (productNumber == null) {
            return null;
        }
        //iterate through the products in the order and find the one with the same product number, if none is found, return null
        return order.getProducts().stream()
                .filter(product -> product.getProductNumber().equals(productNumber))
                .findFirst()
                .orElse(null);
    }

    //TODO input validation, remember to reset style as well
}
