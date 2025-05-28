import GUI.util.Navigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        try {
            Navigator.getInstance().init(stage);
        } catch (Exception e) {
            e.printStackTrace();
            //the program would have died
        }

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