import BE.User;
import GUI.Navigator;
import GUI.SessionManager;
import GUI.View;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        Navigator.getInstance().init(stage);

    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        //implemented due to openCV keeping the JVM alive even when closing the application.
        System.exit(0);
    }
}