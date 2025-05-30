package GUI.models;

import BE.User;
import BLL.UserManager;
import javafx.collections.ObservableList;

public class UserModel {

    private UserManager userManager;

    public UserModel() {
        userManager = new UserManager();
    }

    public User authenticateUser(String username, String rawPassword) throws Exception {
        return userManager.authenticateUser(username, rawPassword);
    }

    public User createUser(User user) throws Exception {
        return userManager.createUser(user);
    }

    public ObservableList<User> getAllUsers() throws Exception {
        return userManager.getAllUsers();
    }

    public void deleteUser(User user) throws Exception {
        userManager.deleteUser(user);
    }
}
