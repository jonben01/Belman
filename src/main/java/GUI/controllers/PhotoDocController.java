package GUI.controllers;

import BE.Order;
import GUI.Navigator;
import GUI.View;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class PhotoDocController implements Initializable {
    @FXML
    public VBox productContainer;
    @FXML
    public TextField txtProductNumber;
    @FXML
    public Button btnRefresh;
    @FXML
    public Button btnLogout;
    @FXML
    public Button btnOpenCamera;
    @FXML
    public Button btnSendReport;
    @FXML
    public Label lblOrderNumber;

    private Order order;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblOrderNumber.setText("");


    }

    @FXML
    public void handleRefresh(ActionEvent actionEvent) {
    }

    @FXML
    public void handleOpenCamera(ActionEvent actionEvent) {
        Navigator.getInstance().goTo(View.CAMERA);
    }

    //TODO rename this button
    @FXML
    public void handleSendReport(ActionEvent actionEvent) {
        //TODO GO TO REPORT PREVIEW PAGE
    }

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
    }

    public void setOrder(Order order) {
        this.order = order;
        if (order != null) {
            lblOrderNumber.setText(order.getOrderNumber());
        }
    }
}
