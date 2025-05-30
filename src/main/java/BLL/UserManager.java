package BLL;

import BE.User;
import BLL.util.PBKDF2PasswordUtil;
import DAL.IUserDataAccess;
import DAL.UserDAO;
import javafx.collections.ObservableList;

public class UserManager {

    private IUserDataAccess userDataAcess;

    public UserManager() {
        userDataAcess = new UserDAO();
    }

    public User authenticateUser(String username, String rawPassword) throws Exception {
        User user = userDataAcess.findUserByUsername(username);
        //check if the user exists and if the password is correct.
        if (user == null || !PBKDF2PasswordUtil.verifyPassword(rawPassword, user.getPassword())) {
            throw new Exception("Invalid username or password");
            //TODO better message for bubbling up to user
        }
        //return the fully built user object from the database.
        return user;
    }

    public User createUser(User user) throws Exception {
        String rawPwd  = user.getPassword();
        String hashedPwd = PBKDF2PasswordUtil.hashPassword(rawPwd);
        user.setPassword(hashedPwd);
        return userDataAcess.createUser(user);
    }

    public ObservableList<User> getAllUsers() throws Exception {
        return userDataAcess.getAllUsers();
    }

    public void deleteUser(User user) throws Exception {
        userDataAcess.deleteUser(user);
    }
}
