package GUI.controllers;

import BE.User;
import GUI.util.AlertHelper;
import GUI.util.Navigator;
import BLL.util.SessionManager;
import GUI.View;
import GUI.models.UserModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtPassword;
    @FXML
    private Button btnLogin;

    private UserModel userModel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        enterKeyListeners();
    }

    private void enterKeyListeners() {
        txtUsername.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                login();
            }
        });
        txtPassword.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                login();
            }
        });
    }

    public LoginController() {
        try {
            userModel = new UserModel();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlertError("FataL error",
                    "Could not initialize components. Please restart the application.");
        }
    }

    @FXML
    public void handleLogin(ActionEvent actionEvent) {
        login();
    }

    private void login() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            return;
        }

        try {
            User user = userModel.authenticateUser(username, password);
            SessionManager.getInstance().setCurrentUser(user);

            Navigator.getInstance().goTo(View.ORDER);

        } catch (Exception e) {
            e.printStackTrace();
            //TODO switch to styling
            AlertHelper.showAlertError("Login error", "incorrect username or password");

        }
    }
}
