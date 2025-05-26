package GUI.controllers;

import BE.*;
import BLL.util.SessionManager;
import GUI.models.OrderModel;
import GUI.models.PhotoModel;
import GUI.util.*;
import GUI.View;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PhotoDocController implements Initializable {
    @FXML
    public VBox productContainer;
    @FXML
    public Button btnRefresh;
    @FXML
    public Button btnLogout;
    @FXML
    public Button btnOpenCamera;
    @FXML
    public Button btnGenerateReport;
    @FXML
    public Label lblOrderNumber;
    @FXML
    public ComboBox<Product> cmbProducts;
    @FXML
    public Label lblMetaDataTag;
    @FXML
    public Label lblMetaDataOrderAndProduct;
    @FXML
    public Label lblMetaDataTimestamp;
    @FXML
    public Label lblMetaDataUser;
    @FXML
    public StackPane previewControls;
    @FXML
    public Button btnPrev;
    @FXML
    public Button btnNext;
    @FXML
    public Label lblTotalImages;
    @FXML
    public Button btnApprove;
    @FXML
    public Button btnReject;
    @FXML
    public Button btnClosePreview;
    @FXML
    public VBox vboxGray;
    @FXML
    public VBox productOptionContainer;
    @FXML
    public StackPane previewContainer;
    @FXML
    public ImageView imgPreview;
    //not root, but root for images
    @FXML
    public StackPane rootPane;
    @FXML
    public ScrollPane scrollPane;
    @FXML
    public StackPane actualRootPane;
    @FXML
    public VBox previewMetaData;
    @FXML
    public ProgressIndicator loadingSpinner;

    private Order order;
    private Product selectedProduct;

    //could be local, but might need them later, who knows
    private final int PHOTO_WIDTH = 480;
    private final int PHOTO_HEIGHT = 270;
    private List<Product> productList = new ArrayList<>();

    private List<Photo> currentPreviewPhotos = new ArrayList<>();
    private PhotoCard currentPreviewCard;
    private int currentPreviewPhotoIndex = -1;
    private Product previewProduct;
    private Role userRole;

    private PhotoModel photoModel;
    private OrderModel orderModel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if (SessionManager.getInstance().getCurrentUser() != null) {
            userRole = SessionManager.getInstance().getCurrentUser().getRole();
        }

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(previewContainer.widthProperty());
        clip.heightProperty().bind(previewContainer.heightProperty());
        previewContainer.setClip(clip);

        imgPreview.setManaged(false);

        bindPreviewToRoot();
    }


    public PhotoDocController() {
        try {
            photoModel = new PhotoModel();
            orderModel = new OrderModel();
        } catch (Exception e) {
            e.printStackTrace();
            //todo alert
        }
    }

    @FXML
    public void handleRefresh(ActionEvent actionEvent) {
        productContainer.getChildren().clear();
        loadingSpinner.setVisible(true);
        loadingSpinner.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        Task<Order> refreshTask = new Task<>() {
            @Override
            protected Order call() throws Exception {
                //find the order again, in case changes were made
                return orderModel.findOrderByOrderNumber(order.getOrderNumber());
            }
        };

        refreshTask.setOnSucceeded(event -> {
            Order refreshedOrder = refreshTask.getValue();
            this.order = refreshedOrder;
            setOrderAndMaybeProduct(refreshedOrder, selectedProduct);
            loadingSpinner.setVisible(false);
        });

        refreshTask.setOnFailed(event -> {
            Throwable ex = refreshTask.getException();
            ex.printStackTrace();
            loadingSpinner.setVisible(false);
            AlertHelper.showAlertError("Refresh failed",
                    "An error occurred while refreshing the order, please try again.");
        });

        Thread thread = new Thread(refreshTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void handleOpenCamera(ActionEvent actionEvent) {
        //TODO if more than once product is being shown, ask the user to select from a drop down.

        System.out.println(productList.size() + "");
        if (productContainer.getChildren().size() > 1 && selectedProduct == null) {
            showProductsForCamera(productList);

        } else {
            if (selectedProduct != null) {
                openCamera(selectedProduct);
            }
        }
    }

    private void showProductsForCamera(List<Product> productList) {

        vboxGray.setVisible(true);
        productOptionContainer.getChildren().clear();

        Label lblSelectProduct = new Label("Select product to document");
        lblSelectProduct.getStyleClass().add("product-label");
        productOptionContainer.getChildren().add(lblSelectProduct);
        for (Product p : productList) {
            Button productButton = new Button(p.getProductNumber());
            productButton.setOnAction(event -> {
                vboxGray.setVisible(false);
                openCamera(p);
            });
            productButton.getStyleClass().add("product-button");
            productOptionContainer.getChildren().add(productButton);

        }

    }

    @FXML
    public void handleGenerateReport(ActionEvent actionEvent) {
        if (verifyDocumentation()) {
            try {
                Navigator.getInstance().showModal(View.REPORT_MODAL, controller -> {
                    if (controller instanceof ReportController reportController) {
                        try {
                            reportController.setOrder(order);
                        } catch (Exception e) {
                            e.printStackTrace();
                            //todo Alert
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                AlertHelper.showAlertError("Report generation failed",
                        "An error occurred while generating the report, please try again later.");
            }
        }
    }

    private boolean verifyDocumentation() {
        List<String> unapprovedPhotos = new ArrayList<>();
        List<String> productsWithLimitedPhotos = new ArrayList<>();
        List<String> productsWithNoPhotos = new ArrayList<>();
        for (Product p : productList) {
            if (p.getPhotos().size() < 5) {
                productsWithLimitedPhotos.add(p.getProductNumber());
            }
            if (p.getPhotos().isEmpty()) {
                productsWithNoPhotos.add(p.getProductNumber());
            }

            //check if a product contains unapproved photos.
            boolean unapproved = false;
            for (Photo photo : p.getPhotos()) {
                if (photo.getTag() != Tag.APPROVED) {
                    unapproved = true;
                    //quit early if any photo is unapproved
                    break;
                }
            }
            if (unapproved) {
                unapprovedPhotos.add(p.getProductNumber());
            }
        }
        //everything except for the limited photos should ideally not let the user generate a report.
        if (!productsWithNoPhotos.isEmpty()) {
            return AlertHelper.showConfirmationAlert("Missing documentation",
                    "The following products have no photos:\n\n"
                            + String.join("\n", productsWithNoPhotos)
                            + "\n\nGenerate a report anyway?");
        }
        if (!unapprovedPhotos.isEmpty()) {
            return AlertHelper.showConfirmationAlert("Unapproved photos",
                    "The following products have unapproved photos:\n\n"
                            + String.join("\n", unapprovedPhotos)
                            + "\n\nGenerate a report anyway?");
        }
        if (!productsWithLimitedPhotos.isEmpty()) {
            return AlertHelper.showConfirmationAlert("Limited photos",
                    "The following products have less than 5 photos:\n\n"
                            + String.join("\n", productsWithLimitedPhotos)
                            + "\n\nGenerate a report anyway?");
        }

        return true;
    }

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        Navigator.getInstance().goTo(View.ORDER);
    }

    public void setOrderAndMaybeProduct(Order order, Product product) {
        this.order = order;
        this.selectedProduct = product;


        productList = order.getProducts();

        lblOrderNumber.setText(order.getOrderNumber() + "-");

        if (cmbProducts.getItems().size() <= 1) {
            populateProductSwitcher(order);
        }

        productContainer.getChildren().clear();

        if (product != null) {
            cmbProducts.getSelectionModel().select(product);
            productContainer.getChildren().clear();
            displayProduct(product);
        } else {
            cmbProducts.getSelectionModel().clearSelection();
            productContainer.getChildren().clear();
            for (Product p : order.getProducts()) {
                displayProduct(p);
            }
        }
    }

    private void populateProductSwitcher(Order order) {

        //manually add an option to display all products (full order overview)
        Product product = new Product();
        product.setProductNumber("All products");
        cmbProducts.getItems().add(product);


        cmbProducts.getItems().addAll(order.getProducts());
        cmbProducts.setOnAction(event -> {
            Product selected = cmbProducts.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (selected.getProductNumber().equals("All products")) {
                    //move selection clearing off the main javafx thread, as it would otherwise cause issues,
                    // due to being inside its own event handler
                    Platform.runLater(() -> cmbProducts.getSelectionModel().clearSelection());
                    //close preview if a user switches to a different product
                    hidePreview();

                    productContainer.getChildren().clear();
                    selectedProduct = null;
                    for (Product p : order.getProducts()) {
                        displayProduct(p);
                    }
                } else {
                    //close preview if a user switches to a different product
                    hidePreview();

                    this.selectedProduct = selected;
                    productContainer.getChildren().clear();
                    displayProduct(selectedProduct);
                }
            }
        });
    }



    private void displayProduct(Product product) {

        VBox productBox = new VBox(10);
        productBox.getStyleClass().add("product-box");

        Label productText = new Label("Item: " + product.getProductNumber());
        productText.getStyleClass().add("product-label");
        productBox.getChildren().add(productText);

        FlowPane imageBox = new FlowPane(10,10);
        imageBox.prefWrapLengthProperty().bind(productBox.widthProperty().subtract(20));

        for (Photo p : product.getPhotos()) {
            PhotoCard card = new PhotoCard(p);

            card.getImageView().setOnMouseClicked(event -> {
                previewProduct = product;
                currentPreviewCard = card;
                openPreview(product.getPhotos(), product.getPhotos().indexOf(p));
            });

            imageBox.getChildren().add(card);
        }

        ImageView placeholder = new ImageView(new Image(getClass().getResource("/images/ImagePlaceholder.png").toExternalForm(),
                PHOTO_WIDTH, PHOTO_HEIGHT, true, true));
        placeholder.setFitWidth(PHOTO_WIDTH);
        placeholder.setFitHeight(PHOTO_HEIGHT);
        placeholder.setPreserveRatio(false);
        placeholder.getStyleClass().add("photo");
        VBox imageBoxPlaceholder = new VBox(placeholder);
        imageBoxPlaceholder.getStyleClass().add("image-card");
        imageBoxPlaceholder.getStyleClass().add("tag-null");

        imageBox.getChildren().add(imageBoxPlaceholder);

        placeholder.setOnMouseClicked(event -> {
            openCamera(product);
        });

        productBox.getChildren().add(imageBox);
        productContainer.getChildren().add(productBox);
    }

    private void openPreview(List<Photo> photos, int startIndex) {
        if (photos.isEmpty()) {
            return;
        }
        this.currentPreviewPhotos = photos;
        this.currentPreviewPhotoIndex = startIndex;

        updatePreviewDisplay();
        previewContainer.setVisible(true);
        previewControls.setVisible(true);
        previewMetaData.setVisible(true);
        //operator should not be able to approve or reject QC documentation.
        if (userRole == Role.OPERATOR) {
            btnApprove.setVisible(false);
            btnReject.setVisible(false);
        }

        Platform.runLater(() -> ImageScaler.adjustImageToContainer(imgPreview, actualRootPane));

    }

    private void hidePreview() {
        previewContainer.setVisible(false);
        previewControls.setVisible(false);
        previewMetaData.setVisible(false);
    }
    private void bindPreviewToRoot() {
        rootPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            ImageScaler.adjustImageToContainer(imgPreview, actualRootPane);
        });
        rootPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            ImageScaler.adjustImageToContainer(imgPreview, actualRootPane);
        });
    }

    private void updatePreviewDisplay() {
        if (currentPreviewPhotos.isEmpty() || currentPreviewPhotoIndex < 0) {
            return;
        }

        Photo photo = currentPreviewPhotos.get(currentPreviewPhotoIndex);
        File file = new File(photo.getFilePath());
        Image image = new Image(file.toURI().toString());
        imgPreview.setImage(image);

        //todo make first letter capital
        lblMetaDataTag.setText("Tag: " + (photo.getTag() != null ? photo.getTag().toString() : "None"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        lblMetaDataOrderAndProduct.setText("\uD83D\uDCE6 " + order.getOrderNumber() + "-" + previewProduct.getProductNumber());
        lblMetaDataTimestamp.setText("\uD83D\uDD52 " + formatter.format(photo.getTakenAt().toLocalDateTime()));
        lblMetaDataUser.setText("\uD83D\uDC64 " + photo.getCapturedBy());

        lblTotalImages.setText(currentPreviewPhotoIndex + 1 + "/" + currentPreviewPhotos.size() );

    }

    private void openCamera(Product product) {
        Navigator.getInstance().goTo(View.CAMERA, controller -> {
            if (controller instanceof CameraController cameraController) {
                cameraController.setOrderAndProduct(order, product);
            }
        });
    }

    @FXML
    public void handlePreviousPreview(ActionEvent actionEvent) {
        if (currentPreviewPhotoIndex > 0) {
            currentPreviewPhotoIndex--;
            updatePreviewDisplay();
        }
    }

    @FXML
    public void handleNextPreview(ActionEvent actionEvent) {
        if (currentPreviewPhotoIndex < currentPreviewPhotos.size() - 1) {
            currentPreviewPhotoIndex++;
            updatePreviewDisplay();
        }
    }

    @FXML
    public void handleApprovePreview(ActionEvent actionEvent) {
        approveOrRejectTag(Tag.APPROVED, "approve");
    }

    private void updateTag(Photo photo) {
        Task<Void> updateTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                photoModel.updateTag(photo);
                return null;
            }
        };
        updateTask.setOnSucceeded(event -> {
                updatePreviewDisplay();
                if (currentPreviewCard != null && currentPreviewCard.getPhoto().equals(photo)) {
                    currentPreviewCard.updateStyleClass();
                }
        });
        updateTask.setOnFailed(event -> {
            Throwable ex = updateTask.getException();
            ex.printStackTrace();
            AlertHelper.showAlertError("Approval failed",
                    "An error occurred while approving the photo, please try again later.");
            updatePreviewDisplay();

        });

        Thread thread = new Thread(updateTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void handleRejectPreview(ActionEvent actionEvent) {
        approveOrRejectTag(Tag.REJECTED, "reject");
    }

    private void approveOrRejectTag(Tag tag, String message) {
        Photo photo = currentPreviewPhotos.get(currentPreviewPhotoIndex);
        if (tag == Tag.APPROVED || tag == Tag.REJECTED) {
            if (photo.getTag() == null) {
                photo.setTag(tag);
                updateTag(photo);
                updatePreviewDisplay();
            } else if (photo.getTag() != null && photo.getTag() != tag) {

                boolean confirmed = AlertHelper.showConfirmationAlert(message + " image",
                        "Image has the tag: " + photo.getTag().toString()
                                + "\nAre you sure you want to " + message +  " it?");
                if (confirmed) {
                    photo.setTag(tag);
                    updateTag(photo);
                    updatePreviewDisplay();
                }
            }
        }
    }

    @FXML
    public void handleClosePreview(ActionEvent actionEvent) {
        hidePreview();
    }
}