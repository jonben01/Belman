package BLL;

import javafx.scene.image.Image;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.ByteArrayInputStream;

public class OpenCVStrategy implements CameraStrategy {

    private VideoCapture camera;

    @Override
    public void start() throws Exception {
        OpenCV.loadLocally();

        camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            throw new Exception("Could not open camera," +
                    " either in use or not found");
        }
        camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 1280);
        camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 720);
    }

    @Override
    public void stop() {
        if (camera.isOpened()) {
            camera.release();
        }
    }

    @Override
    public Image takePhoto() throws Exception {
        Mat frame = new Mat();
        camera.read(frame);
        if (frame.empty()) {
            throw new Exception("Could not take photo, camera is not responding");
        }

        MatOfByte buffer =new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
}
