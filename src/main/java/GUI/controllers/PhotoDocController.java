package GUI.controllers;

import BE.Order;
import BE.Photo;
import BE.Product;
import BE.Tag;
import GUI.util.Navigator;
import GUI.View;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.File;
import java.net.URL;
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
    public Button btnSendReport;
    @FXML
    public Label lblOrderNumber;
    public ComboBox<Product> cmbProducts;

    private Order order;
    private Product selectedProduct;

    private final int PHOTO_WIDTH = 480;
    private final int PHOTO_HEIGHT = 270;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblOrderNumber.setText("");

    }

    @FXML
    public void handleRefresh(ActionEvent actionEvent) {
    }

    @FXML
    public void handleOpenCamera(ActionEvent actionEvent) {
        //TODO if more than once product is being shown, ask the user to select from a drop down.

        Navigator.getInstance().goTo(View.CAMERA, controller -> {
            if (controller instanceof CameraController cameraController) {
                cameraController.setOrderAndProduct(order, selectedProduct);
            }
        });
    }

    //TODO rename this button
    @FXML
    public void handleSendReport(ActionEvent actionEvent) {
        //TODO GO TO REPORT PREVIEW PAGE
    }

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        Navigator.getInstance().goTo(View.ORDER);
    }

    public void setOrder(Order order) {
        this.order = order;
        if (order != null) {
            lblOrderNumber.setText(order.getOrderNumber());
        }
    }

    public void setOrderAndMaybeProduct(Order order, Product product) {
        this.order = order;
        this.selectedProduct = product;

        populateProductSwitcher(order);
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
                    productContainer.getChildren().clear();
                    selectedProduct = null;
                    for (Product p : order.getProducts()) {
                        displayProduct(p);
                    }
                } else {
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
            //TODO test what looks best here
            File file = new File(p.getFilePath());
            Image image = new Image(file.toURI().toString(), PHOTO_WIDTH, PHOTO_HEIGHT, true, true);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(PHOTO_WIDTH);
            imageView.setFitHeight(PHOTO_HEIGHT);
            imageView.setPreserveRatio(true);

            imageView.getStyleClass().add("photo");


            VBox imageCard = new VBox();
            imageCard.getStyleClass().add("image-card");
            imageCard.getChildren().add(imageView);

            if (p.getTag() != null) {
                String tagClass = p.getTag() == Tag.APPROVED ? "tag-approved"
                        : p.getTag() == Tag.REJECTED ? "tag-rejected"
                        : "tag-info";
                imageCard.getStyleClass().add(tagClass);
            } else {
                imageCard.getStyleClass().add("tag-null");
            }

            imageBox.getChildren().add(imageCard);
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
            //TODO make this instantly open the right product and order
            //openCamera(product);
        });

        productBox.getChildren().add(imageBox);
        productContainer.getChildren().add(productBox);
    }
}