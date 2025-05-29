package GUI.controllers;

import BE.Role;
import BE.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class UserCreationController implements Initializable {
    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtPassword;
    @FXML
    private TextField txtFirstName;
    @FXML
    private TextField txtLastName;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextField txtEmail;
    @FXML
    private ComboBox<Role> cmbRole;
    @FXML
    private Button btnCloseWindow;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnCreate;

    private User newUser;

    public User getNewUser () {
        return newUser;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbRole.getItems().addAll(Role.values());
        //TODO escape key listener to close window.
    }

    @FXML
    public void handleCloseWindow(ActionEvent actionEvent) {
        newUser = null;
        close();
    }

    @FXML
    public void handleCancel(ActionEvent actionEvent) {
        newUser = null;
        close();
    }

    private void close() {
        Stage stage = (Stage) btnCloseWindow.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleCreate(ActionEvent actionEvent) {
        //TODO input validation

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        Role role = cmbRole.getValue();
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        newUser = new User(username, password, role, firstName, lastName, email, phone);
        close();
    }
}
