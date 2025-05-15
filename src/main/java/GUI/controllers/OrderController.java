package GUI.controllers;

import BE.Order;
import BE.Role;
import GUI.Navigator;
import GUI.SessionManager;
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


    @FXML
    public void handleSearchOrder(ActionEvent actionEvent) {
        String orderNumber = txtOrderNumber.getText();
        if (orderNumber.isEmpty()) {
            //do nothign atm
            return;
        }
        try {
            Order order = orderModel.findOrderByOrderNumber(orderNumber);
            if (order == null) {
                //TODO alert
                // and remove lbl change below, wrong label lmao
                lblOrderSelect.setText("Order not found");
                return;
            }
            Navigator.getInstance().goTo(View.PHOTO_DOC, controller -> {
                if (controller instanceof PhotoDocController pdocController) {
                    pdocController.setOrder(order);
                }
            });


            //should probably catch subclasses (invalid arg), but this is fine for now
        } catch (Exception e) {
            e.printStackTrace();
            //TODO alert

        }
    }

    //TODO input validation, remember to reset style as well
}
