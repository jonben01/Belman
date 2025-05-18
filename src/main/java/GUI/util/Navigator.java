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

    public void init(Stage primaryStage) {
        this.stage = primaryStage;
        goTo(View.LOGIN);
        stage.show();
    }

    public void goTo(View view) {
        goTo(view, null);
    }

    public void goTo(View view, Consumer<Object> controllerConsumer) {
        boolean wasFullScreen = stage.isFullScreen();
        boolean wasMaximized = stage.isMaximized();

        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(view.getFXML())));
            Parent root = loader.load();

            Object controller = loader.getController();
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
            //TODO BUBBLE UP EXCEPTIO NADN THEN SHOW ALERT
        }
    }

    //likely to be redundant, but it's there in case it ever needs to be used
    public void showModal(View view) {
        showModal(view, null);
    }

    //Should arguably use a different enum for modals, but for now this works.
    public void showModal(View view, Consumer<Object> controllerConsumer) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(view.getFXML())));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setScene(new Scene(root));
            modalStage.centerOnScreen();
            modalStage.showAndWait();

            Object controller = loader.getController();
            if (controllerConsumer != null && controller != null) {
                controllerConsumer.accept(controller);
            }
        } catch (IOException e) {
            e.printStackTrace();
            //TODO BUBBLE UP EXCEPTION And THEN SHOW ALERT
        }

    }
}
