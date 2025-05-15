package DAL;

import BE.User;
import javafx.collections.ObservableList;

public interface IUserDataAcess {

    User findUserByUsername(String username) throws Exception;

    User createUser(User user) throws Exception;

    void deleteUser(User user) throws Exception;

    ObservableList<User> getAllUsers() throws Exception;

}
