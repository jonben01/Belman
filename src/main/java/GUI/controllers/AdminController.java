package GUI.controllers;

import BE.User;
import GUI.Navigator;
import GUI.SessionManager;
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
            //TODO ALERT
        }
    }

    private void populateUserList() {
        ObservableList<User> users = FXCollections.observableArrayList();
        try {
            users = userModel.getAllUsers();
        } catch (Exception e) {
            e.printStackTrace();
            //TODO alert
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
        Navigator.getInstance().showModal(View.USER_CREATION_MODAL, controller -> {
            if (controller instanceof UserCreationController userCreationController) {
                User newUser = userCreationController.getNewUser();
                if (newUser != null) {
                    try {
                        User user = userModel.createUser(newUser);
                        lstUsers.getItems().add(user);
                        lstUsers.getSelectionModel().select(user);
                    } catch(Exception e) {
                        e.printStackTrace();
                        //TODO alerts
                    }
                }
            }
        });
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
    }
}
