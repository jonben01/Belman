package GUI.controllers;

import BE.*;
import BLL.CameraStrategy;
import BLL.OpenCVStrategy;
import GUI.util.AlertHelper;
import GUI.util.ImageScaler;
import GUI.util.Navigator;
import BLL.util.SessionManager;
import GUI.View;
import GUI.models.PhotoModel;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CameraController implements Initializable {

    @FXML
    private StackPane rootPane;
    @FXML
    private StackPane cameraStackPane;
    @FXML
    private ImageView imgCamera;
    @FXML
    private ImageView imgFullPreview;
    @FXML
    private StackPane controlsContainer;
    @FXML
    private Button btnReturn;
    @FXML
    private Button btnCapture;
    @FXML
    private ImageView imgPreview2;
    @FXML
    private ImageView imgPreview1;
    @FXML
    private Button btnFinish;
    @FXML
    private VBox previewControls;
    @FXML
    private Button btnDeletePreview;
    @FXML
    private Button btnClosePreview;
    @FXML
    private Button btnTag;
    @FXML
    private VBox vboxGray;
    @FXML
    private VBox tagOptionsContainer;
    @FXML
    private Label lblTag;

    private ScheduledExecutorService mainPreviewExecutor;
    private CameraStrategy strategy;


    /**
     *ArrayDeque is used to mimic FIFO behavior for the gallery, but achieves it in reverse order
     *using addFirst and removeLast instead of addLast and removeFirst.
     *the only benefit to this is avoiding index flipping for the UI logic.
     *easier to write and read images[0] than images[images.length-1]
     *LinkedList is more code and slower.
     */
    private final ArrayDeque<Image> gallery = new ArrayDeque<>();
    private PhotoModel photoModel;
    private List<Photo> photosToSave = new ArrayList<>();
    private int currentPreviewIndex = -1;

    private Order selectedOrder;
    private Product selectedProduct;
    private Photo selectedPhoto;


    public CameraController() {
        try {
            photoModel = new PhotoModel();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlertError("Fatal error",
                    "Could not initialize components. Please restart the application and try again.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(cameraStackPane.widthProperty());
        clip.heightProperty().bind(cameraStackPane.heightProperty());
        cameraStackPane.setClip(clip);


        imgCamera.setManaged(false);

        bindPreviewToRoot();
        bindCameraViewToRoot();

        imgPreview1.setOnMouseClicked(e -> openOverlayPreview(0));
        imgPreview2.setOnMouseClicked(e -> openOverlayPreview(1));

        strategy = new OpenCVStrategy();
        try {
            strategy.start();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlertError("Camera not found", "No valid camera was found." +
                    "Please ensure it is not being used and that it is connected to the device");
        }
        mainPreviewExecutor = Executors.newSingleThreadScheduledExecutor();
        mainPreviewExecutor.scheduleAtFixedRate(() -> {
            try {
                Image frame = strategy.takePhoto();
                Platform.runLater(() -> {
                    imgCamera.setImage(frame);
                    ImageScaler.adjustImageToContainer(imgCamera, cameraStackPane);
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //30 fps
        }, 0, 33, TimeUnit.MILLISECONDS);

        btnFinish.setDisable(true);
    }

    public void setOrderAndProduct(Order order, Product product) {
        selectedOrder = order;
        selectedProduct = product;
    }

    @FXML
    public void handleReturn(ActionEvent actionEvent) {
        try {
            Navigator.getInstance().goTo(View.PHOTO_DOC, controller -> {
                if (controller instanceof PhotoDocController photoDocController) {
                    photoDocController.setOrderAndMaybeProduct(selectedOrder, selectedProduct);
                }
            });
            //shut down the ExecutorService and stop the use of camera
            if (mainPreviewExecutor != null && !mainPreviewExecutor.isShutdown()) {
                mainPreviewExecutor.shutdownNow();
                mainPreviewExecutor = null;

                strategy.stop();
            }
        } catch (Exception e) {
            AlertHelper.showAlertError("Failed to open new window",
                    "An error occurred while returning to previous window");
        }
    }

    @FXML
    public void handleCaptureImage(ActionEvent actionEvent) {
        captureImage();
    }


    //TODO do the db calling in a new thread, and lock the buttons and show a spinny animation with UPLOADING IMAGES

    @FXML
    public void handleFinishCamera(ActionEvent actionEvent) {
        if (photosToSave.isEmpty()) {
            return;
        }

        List<String> fileNames = new ArrayList<>();

        for (int i = 0; i < photosToSave.size(); i++) {
            fileNames.add(i +"");
        }
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String orderNumber = selectedOrder.getOrderNumber();
        try {
            photoModel.saveImageAndPath(photosToSave, fileNames, currentUser, selectedProduct, orderNumber);
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlertError("Data error",
                    "An error occurred while saving the images, please try again.");
        }
        //shut down the ExecutorService and stop the use of camera
        if (mainPreviewExecutor != null && !mainPreviewExecutor.isShutdown()) {
            mainPreviewExecutor.shutdownNow();
            mainPreviewExecutor = null;

            strategy.stop();
        }
        selectedProduct.getPhotos().addAll(photosToSave);
        try {
            Navigator.getInstance().goTo(View.PHOTO_DOC, controller -> {
                if (controller instanceof PhotoDocController photoDocController) {
                    photoDocController.setOrderAndMaybeProduct(selectedOrder, selectedProduct);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlertError("Failed to return",
                    "An error occurred while returning to previous window");
        }
    }

    private void openOverlayPreview(int i) {
        //convert array deque to array to make use of indexing.
        Image[] images = gallery.toArray(new Image[0]);
        if (i < images.length) {
            imgFullPreview.setImage(images[i]);
            imgFullPreview.setVisible(true);
            previewControls.setVisible(true);
            btnFinish.setVisible(false);
            btnReturn.setVisible(false);
            btnCapture.setVisible(false);
            currentPreviewIndex = i;
            controlsContainer.setVisible(false);

            ImageScaler.adjustImageToContainer(imgFullPreview, rootPane);
        }
    }


    //TODO swap from using ArrayDeque<Image> to ArrayDeque<Photo>, the below method sets the lbl for that index only, oops
    /*
    private void showTag(int i) {
        if (i < photosToSave.size()) {
            Tag tag = photosToSave.get(i).getTag();
            if (tag != null) {
                String tagText = tag.toString().toUpperCase().charAt(0) + tag.toString().substring(1).toLowerCase();
                lblTag.setText(tagText);
                lblTag.setVisible(true);
            } else {
                lblTag.setText("");
                lblTag.setVisible(false);
            }
        } else {
            lblTag.setText("");
            lblTag.setVisible(false);
        }
    }

     */

    @FXML
    public void handleDeletePreview(ActionEvent actionEvent) {
        deletePreview();
    }

    public void deletePreview() {
        if (currentPreviewIndex < 0) {
            return;
        }
        Image[] images = gallery.toArray(new Image[0]);
        Image imageToDelete = images[currentPreviewIndex];

        gallery.remove(imageToDelete);
        if (currentPreviewIndex < photosToSave.size()) {
            photosToSave.remove(currentPreviewIndex);
        }
        updatePreviews();
        closePreview();
    }

    private void updatePreviews() {
        Image[] images = gallery.toArray(new Image[0]);
        imgPreview1.setImage((images.length > 0 ? images[0] : null));
        imgPreview2.setImage((images.length > 1 ? images[1] : null));
    }

    public void closePreview() {
        imgFullPreview.setVisible(false);
        previewControls.setVisible(false);
        btnFinish.setVisible(true);
        btnReturn.setVisible(true);
        btnCapture.setVisible(true);
        currentPreviewIndex = -1;
        controlsContainer.setVisible(true);
    }

    @FXML
    public void handleClosePreview(ActionEvent actionEvent) {
        closePreview();
    }

    private void bindPreviewToRoot() {
        rootPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            ImageScaler.adjustImageToContainer(imgFullPreview, rootPane);
        });
        rootPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            ImageScaler.adjustImageToContainer(imgFullPreview, rootPane);
        });
    }

    /** This method binds the camera ImageView's width and height properties to its parent, cameraStackPane.
     */
    private void bindCameraViewToRoot() {
        cameraStackPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            ImageScaler.adjustImageToContainer(imgCamera, cameraStackPane);
        });
        cameraStackPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            ImageScaler.adjustImageToContainer(imgCamera, cameraStackPane);
        });
    }

    private void captureImage() {
        try {
            Image image = strategy.takePhoto();
            sendToGallery(image);
            BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);

            Photo photo = new Photo();
            photo.setImage(bImage);
            photo.setTag(null);

            photosToSave.add(photo);
            btnFinish.setDisable(false);

        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlertError("Camera error",
                    "An issue occurred while capturing the image, please try again.");
        }
    }

    /**
     * Adds an image to the gallery. Removes the oldest image if the gallery size exceeds the limit (2 images).
     * Updates the preview images to display the most recent images in the gallery.
     *
     * @param image The image to be added to the gallery.
     */
    private void sendToGallery(Image image) {
        if (gallery.size() == 2) {
            gallery.removeLast();
        }
        gallery.addFirst(image);

        imgPreview1.setImage((gallery.size() > 0 ? gallery.toArray(new Image[0])[0] : null));
        imgPreview2.setImage((gallery.size() > 1 ? gallery.toArray(new Image[0])[1] : null));
    }

    public void handleTagImage(ActionEvent actionEvent) {
        vboxGray.setVisible(true);
        tagOptionsContainer.getChildren().clear();

        Label lblSelectTag = new Label("Select tag to apply");
        lblSelectTag.getStyleClass().add("product-label");

        tagOptionsContainer.getChildren().add(lblSelectTag);
        for (Tag tag : Tag.values()) {
            if (tag == Tag.APPROVED || tag == Tag.REJECTED) {
                //skip the QC tags
                continue;
            }
            String tagText = tag.toString().toUpperCase().charAt(0) +
                    tag.toString().substring(1).toLowerCase();

            Button tagButton = new Button(tagText);
            tagButton.getStyleClass().add("product-button");
            tagButton.setOnAction(event -> {
                applyTagToPreview(tag);
                vboxGray.setVisible(false);
                lblTag.setText(tagText);
            });
            tagOptionsContainer.getChildren().add(tagButton);
        }
    }

    private void applyTagToPreview(Tag tag) {
        if (currentPreviewIndex >= 0 && currentPreviewIndex < photosToSave.size()) {
            photosToSave.get(currentPreviewIndex).setTag(tag);
        }
    }
}
