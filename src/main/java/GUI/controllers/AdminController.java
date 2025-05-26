package GUI.controllers;

import BE.User;
import GUI.util.AlertHelper;
import GUI.util.Navigator;
import BLL.util.SessionManager;
import GUI.View;
import GUI.models.UserModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

public class AdminController implements Initializable {
    @FXML
    public HBox belmanBar;
    @FXML
    public StackPane logoPane;
    @FXML
    public ImageView vboxLogo;
    @FXML
    public Label lblCurrentUser;
    @FXML
    public Button btnLogout;
    @FXML
    public ImageView logoutImage;
    @FXML
    public TextField txtSearch;
    @FXML
    public Button btnCreateUser;
    @FXML
    public ListView<User> lstUsers;
    @FXML
    public TextField txtUsername;
    @FXML
    public TextField txtPassword;
    @FXML
    public TextField txtFirstName;
    @FXML
    public TextField txtLastName;
    @FXML
    public TextField txtEmail;
    @FXML
    public TextField txtPhone;
    @FXML
    public Button btnDeleteUser;

    private User selectedUser;
    private UserModel userModel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateUserList();
        lblCurrentUser.setText(SessionManager.getInstance().getCurrentUser().getUsername());

        if (lstUsers.getItems() != null) {
            lstUsers.getSelectionModel().select(0);
            User user = lstUsers.getSelectionModel().getSelectedItem();
            setUserInfo(user);
        }
        lstUsers.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            setUserInfo(newValue);
        });
    }

    public AdminController() {
        try {
            userModel = new UserModel();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlertError("Fatal error",
                    "Could not initialize components. Please restart the application.");
        }
    }

    private void populateUserList() {
        ObservableList<User> users = FXCollections.observableArrayList();
        try {
            users = userModel.getAllUsers();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlertError("Loading error",
                    "An error occurred while loading users. Please try again later.");
        }

        if (users == null) {
            Label lblNoUsers = new Label("No users found");
            lstUsers.setPlaceholder(lblNoUsers);
        } else {
            users.sort(Comparator.comparing(user -> user.getFirstName().toLowerCase()));
            lstUsers.setItems(users);
            setupCellFactory();
        }
    }

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        Navigator.getInstance().goTo(View.ORDER);
    }

    @FXML
    public void handleCreateUser(ActionEvent actionEvent) {
        try {
            Object controller = Navigator.getInstance().showModalAndReturnController(View.USER_CREATION_MODAL);
            if (controller instanceof UserCreationController userCreationController) {
                User newUser = userCreationController.getNewUser();
                if (newUser != null) {
                    userModel.createUser(newUser);
                    populateUserList();
                    lstUsers.getSelectionModel().select(newUser);
                    User selectedUser = lstUsers.getSelectionModel().getSelectedItem();
                    setUserInfo(selectedUser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlertError("Creation error",
                    "An error occurred while creating the user. Please try again.");
        }
    }

    private void setUserInfo(User selectedUser) {
        if (selectedUser != null) {
            this.selectedUser = selectedUser;
            txtUsername.setText(selectedUser.getUsername());
            txtPassword.setText("*****");
            txtFirstName.setText(selectedUser.getFirstName());
            txtLastName.setText(selectedUser.getLastName());
            txtEmail.setText(selectedUser.getEmail());
            txtPhone.setText(selectedUser.getPhoneNumber());
        }
    }

    private void setupCellFactory() {
        lstUsers.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);

                setText(null);
                setGraphic(null);
                if (empty || item == null) {
                    return;
                }

                //TODO assign style classes in here

                Label nameLabel = new Label(item.getFirstName() + " " + item.getLastName());
                Label roleLabel;
                if (item.getRole() != null) {
                    roleLabel = new Label(item.getRole().toString().toLowerCase());
                } else {
                    roleLabel = new Label("null");
                }

                VBox vBox = new VBox(nameLabel, roleLabel);
                setGraphic(vBox);

            }
        });
    }

    @FXML
    public void handleDeleteUser(ActionEvent actionEvent) {
        User user = lstUsers.getSelectionModel().getSelectedItem();
        if (user == null) {
            return;
        }
        if (user.equals(SessionManager.getInstance().getCurrentUser())) {
            AlertHelper.showAlertWarning("Deletion error", "You cannot delete yourself.");
            return;
        }
        boolean verify = AlertHelper.showConfirmationAlert("Delete user",
                "Are you sure you want to delete user " + user.getUsername() + "?");
        if (verify) {
            try {
                userModel.deleteUser(user);
                lstUsers.getItems().remove(user);
                if (!lstUsers.getItems().isEmpty()) {
                    //select the first item if the list is not empty
                    lstUsers.getSelectionModel().select(0);
                    User selectedUser = lstUsers.getSelectionModel().getSelectedItem();
                    setUserInfo(selectedUser);
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertHelper.showAlertError("Deletion error",
                        "An error occurred while deleting the user. Please try again later.");
            }
        }
    }
}
