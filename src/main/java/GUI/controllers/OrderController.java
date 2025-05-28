package GUI.controllers;

import BE.Order;
import BE.Product;
import BE.Role;
import GUI.util.AlertHelper;
import GUI.util.Navigator;
import BLL.util.SessionManager;
import GUI.View;
import GUI.models.OrderModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

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
        enterKeyListeners();

    }
    public OrderController() {
        try {
            orderModel = new OrderModel();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlertError("Fatal error",
                    "Could not initialize components. Please restart the application.");
        }
    }

    private void enterKeyListeners() {
        txtOrderNumber.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String input = txtOrderNumber.getText();
                searchAndNavigate(input);
            }
        });
    }

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        SessionManager.getInstance().logout();
        try {
            Navigator.getInstance().goTo(View.LOGIN);
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlertError("Window error", "An error has occurred while trying to go to the login screen");
        }
    }

    @FXML
    public void handleAdmin(ActionEvent actionEvent) {
        try {
            Navigator.getInstance().goTo(View.ADMIN);
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlertError("Window error", "An error has occurred while trying to go the admin screen");
        }
    }


    //TODO rename this method, since it can also be used to find product only
    @FXML
    public void handleSearchOrder(ActionEvent actionEvent) {
        String input = txtOrderNumber.getText();
        searchAndNavigate(input);
    }

    private void searchAndNavigate(String input) {
        clearErrorStyles();
        if (input == null || input.isEmpty()) {
            setErrorStyles(txtOrderNumber);
            return;
        }
        String[] parts = parseInput(input);
        String orderNumber = parts[0];
        String productNumber = parts[1];

        Order order = findOrder(orderNumber);
        if (order == null) {
            setErrorStyles(txtOrderNumber);
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
        try {
            Navigator.getInstance().goTo(View.PHOTO_DOC, controller -> {
                if (controller instanceof PhotoDocController pdocController) {
                    pdocController.setOrderAndMaybeProduct(order, product);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlertError("Failed to open new window",
                    "An error occurred while opening the order window, please try again later.");
        }
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

        clearErrorStyles();

        try {
            return orderModel.findOrderByOrderNumber(orderNumber);

        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlertError("Database error",
                    "An error occurred while searching for the order. Please try again later.");
            return null;
        }
    }

    private void setErrorStyles(TextField textField) {
        if (!textField.getStyleClass().contains("error")) {
            textField.getStyleClass().add("error");
        }
    }

    private void clearErrorStyles() {
        txtOrderNumber.getStyleClass().remove("error");
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
}
