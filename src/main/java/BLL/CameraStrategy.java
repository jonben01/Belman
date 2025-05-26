package BLL;


import javafx.scene.image.Image;

public interface CameraStrategy {

    void start() throws Exception;

    void stop();

    Image takePhoto() throws Exception;
}
