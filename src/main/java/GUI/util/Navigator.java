package GUI.util;

import GUI.View;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public class Navigator {

    //TODO comments and javadoc

    private static Navigator instance;
    private Stage stage;

    private Navigator() {
    }

    public static Navigator getInstance() {
        //no need for double-checked locking due to only being accessed by javafx application thread
        if (instance == null) {
            instance = new Navigator();
        }
        return instance;
    }

    public void init(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        goTo(View.LOGIN);
        stage.show();
    }

    public void goTo(View view) throws Exception {
        goTo(view, null);
    }

    public void goTo(View view, Consumer<Object> controllerConsumer) throws Exception {
        boolean wasFullScreen = stage.isFullScreen();
        boolean wasMaximized = stage.isMaximized();

        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(view.getFXML())));
            Parent root = loader.load();

            Object controller = loader.getController();
            //if the controllerConsumer and the controller is not null do what was defined in the lambda expression
            if (controllerConsumer != null && controller != null) {
                controllerConsumer.accept(controller);
            }
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();

            if (wasFullScreen) {
                stage.setFullScreen(true);
            } else if (wasMaximized) {
                //Unfortunately, this is very janky, but it is required due to javaFX being annoying
                stage.setMaximized(false);
                stage.setMaximized(true);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Failed to open new window", e);
        }
    }

    //have to use this to create new user, there are other workarounds too, but this is straightforward.
    public Object showModalAndReturnController(View view) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader((Objects.requireNonNull(getClass().getResource(view.getFXML()))));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setScene(new Scene(root));
            modalStage.centerOnScreen();


            Object controller = loader.getController();
            //dont return controller till the window is closed.
            modalStage.showAndWait();
            return controller;

        } catch (IOException e) {
            throw new Exception("Error showing modal or returning the controller when doing so", e);
        }
    }

    //Should arguably use a different enum for modals, but for now this works.
    public void showModal(View view, Consumer<Object> controllerConsumer) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(view.getFXML())));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setScene(new Scene(root));
            modalStage.centerOnScreen();

            Object controller = loader.getController();
            if (controllerConsumer != null && controller != null) {
                controllerConsumer.accept(controller);
            }
            modalStage.showAndWait();


        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Failed to open modal", e);
        }

    }
}
