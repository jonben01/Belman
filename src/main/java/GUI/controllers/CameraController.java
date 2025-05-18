package GUI.controllers;

import BE.*;
import BLL.CameraStrategy;
import BLL.OpenCVStrategy;
import GUI.util.Navigator;
import GUI.util.SessionManager;
import GUI.View;
import GUI.models.PhotoModel;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;

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
    public StackPane rootPane;
    @FXML
    public StackPane cameraStackPane;
    @FXML
    public ImageView imgCamera;
    @FXML
    public ImageView imgFullPreview;
    @FXML
    public VBox cameraRightVbox;
    @FXML
    public Button btnReturn;
    @FXML
    public Button btnCapture;
    @FXML
    public ImageView imgPreview2;
    @FXML
    public ImageView imgPreview1;
    @FXML
    public Button btnFinish;
    @FXML
    public VBox previewControls;
    @FXML
    public Button btnDeletePreview;
    @FXML
    public Button btnClosePreview;
    public Button btnTag;

    private ScheduledExecutorService mainPreviewExecutor;
    private CameraStrategy strategy;

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
            //TODO alert
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
            //TODO alert
        }
        mainPreviewExecutor = Executors.newSingleThreadScheduledExecutor();
        mainPreviewExecutor.scheduleAtFixedRate(() -> {
            try {
                Image frame = strategy.takePhoto();
                Platform.runLater(() -> {
                    imgCamera.setImage(frame);
                    adjustImage(imgCamera, cameraStackPane);
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
        Navigator.getInstance().goTo(View.PHOTO_DOC, controller -> {
            if (controller instanceof PhotoDocController photoDocController) {
                photoDocController.setOrderAndMaybeProduct(selectedOrder, selectedProduct);
            }
        });
        //shut down the ExecutorService and stop the use of camera
        if (mainPreviewExecutor != null && !mainPreviewExecutor.isShutdown()) {
            mainPreviewExecutor.shutdownNow();
            mainPreviewExecutor = null;
            try {
                strategy.stop();
            } catch (Exception e) {
                //TODO exception
            }
        }
    }

    @FXML
    public void handleCaptureImage(ActionEvent actionEvent) {
        captureImage();
    }

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
            //TODO alert
        }
        //shut down the ExecutorService and stop the use of camera
        if (mainPreviewExecutor != null && !mainPreviewExecutor.isShutdown()) {
            mainPreviewExecutor.shutdownNow();
            mainPreviewExecutor = null;
            try {
                strategy.stop();
            } catch (Exception e) {
                //TODO exception
            }
        }
        Navigator.getInstance().goTo(View.PHOTO_DOC, controller -> {
            if (controller instanceof PhotoDocController photoDocController) {
                photoDocController.setOrderAndMaybeProduct(selectedOrder, selectedProduct);
            }
        });
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
            cameraRightVbox.setVisible(false);

            adjustImage(imgFullPreview, rootPane);
        }
    }

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
        cameraRightVbox.setVisible(true);
    }

    @FXML
    public void handleClosePreview(ActionEvent actionEvent) {
        closePreview();
    }

    private void bindPreviewToRoot() {
        rootPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            adjustImage(imgFullPreview, rootPane);
        });
        rootPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            adjustImage(imgFullPreview, rootPane);
        });
    }

    private void bindCameraViewToRoot() {
        cameraStackPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            adjustImage(imgCamera, cameraStackPane);
        });
        cameraStackPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            adjustImage(imgCamera, cameraStackPane);
        });
    }

    private void adjustImage(ImageView imageView, StackPane container) {
        Image frame = imageView.getImage();
        double paneHeight = container.getHeight();
        double paneWidth = container.getWidth();
        if (frame == null || paneHeight <= 0 || paneWidth <= 0) {
            return;
        }

        double scale = Math.max(paneWidth / frame.getWidth(), paneHeight / frame.getHeight());
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(frame.getWidth() * scale);
        imageView.setFitHeight(frame.getHeight() * scale);
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
            //TODO alert
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
        VBox tagPanel = new VBox(10);
        tagPanel.getStyleClass().add("tag-panel");

        Popup popup = new Popup();
        popup.setAutoHide(true);

        for (Tag tag : Tag.values()) {
            if (tag == Tag.APPROVED || tag == Tag.REJECTED) {
                //skip the QC tags
                continue;
            }
            String tagText = tag.toString().charAt(0) + tag.toString().substring(1).toLowerCase();
            Button tagButton = new Button(tagText);
            tagButton.getStyleClass().add("tag-button");

            tagButton.setOnAction(event -> {
                popup.hide();
                applyTagToPreview(tag);
            });
            tagPanel.getChildren().add(tagButton);
        }
        popup.getContent().add(tagPanel);

        Node source = previewControls;
        Bounds boundsInScene = source.localToScreen(source.getBoundsInLocal());

        if (boundsInScene != null) {
            double popupX = boundsInScene.getMinX() - tagPanel.getWidth() - 10;
            double popupY = boundsInScene.getMinY();
            popup.show(source.getScene().getWindow(), popupX, popupY);
        }



    }

    private void applyTagToPreview(Tag tag) {
        if (currentPreviewIndex >= 0 && currentPreviewIndex < photosToSave.size()) {
            photosToSave.get(currentPreviewIndex).setTag(tag);
        }
    }
}
