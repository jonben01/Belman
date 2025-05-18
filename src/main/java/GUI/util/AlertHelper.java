package GUI.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class AlertHelper {

    /**
     * Displays an error alert dialog with the specified title and message.
     *
     * @param title   the title of the alert dialog
     * @param message the message to be displayed in the alert dialog
     */
    public static void showAlertError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a confirmation alert dialog with the specified title and message.
     * The dialog contains "Yes" and "No" buttons. Returns true if the user
     * selects "Yes", and false if "No" or the dialog is dismissed.
     *
     * @param title   the title of the alert dialog
     * @param message the message to be displayed in the alert dialog
     * @return true if the user confirms by selecting "Yes"; false otherwise
     */
    public static boolean showConfirmationAlert(String title, String message) {
        //create the alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        //create buttons for the button bar
        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yes, no);

        //show and wait for the result
        Optional<ButtonType> result = alert.showAndWait();
        //return the result if present, otherwise return false.
        return result.orElse(no) == yes;
    }
}
