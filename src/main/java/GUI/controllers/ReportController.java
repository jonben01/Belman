package GUI.controllers;

import BE.Order;
import GUI.View;
import GUI.models.ReportModel;
import GUI.util.Navigator;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ReportController implements Initializable {
    @FXML
    public VBox pdfContainer;
    @FXML
    public Button btnSendReport;
    @FXML
    public Button btnLogout;
    @FXML
    public ScrollPane scrollPane;
    @FXML
    public ProgressIndicator loadingSpinner;
    @FXML
    public Button btnComment;
    @FXML
    public TextArea txtComment;
    @FXML
    public TextField txtEmail;


    private String comment = null;
    private List<Image> previewImages;
    private Order order;
    private ReportModel reportModel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            reportModel = new ReportModel();
        } catch (Exception e) {
            e.printStackTrace();
            //todo alert
        }
    }


    public void setOrder(Order order) {
        this.order = order;
        generateAndDisplayPreview();
        txtEmail.setText(order.getCustomerEmail() != null ? order.getCustomerEmail() : "No email found");
    }

    public void generateAndDisplayPreview() {

        pdfContainer.getChildren().clear();
        loadingSpinner.setVisible(true);
        loadingSpinner.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        Task<List<Image>> previewTask = new Task<>() {
            @Override
            protected List<Image> call() throws Exception {
                return reportModel.generatePreview(order, comment);
            }
        };

        previewTask.setOnSucceeded(event -> {
            loadingSpinner.setVisible(false);
            previewImages = previewTask.getValue();
            pdfContainer.getChildren().clear();
            for (Image i : previewImages) {
                ImageView page = new ImageView(i);
                page.setPreserveRatio(true);
                page.setFitWidth(800);
                pdfContainer.getChildren().add(page);

                Separator separator = new Separator();
                separator.setPrefWidth(800);
                pdfContainer.getChildren().add(separator);

            }
        });
        previewTask.setOnFailed(event -> {
            previewTask.getException().printStackTrace();
            loadingSpinner.setVisible(false);
            //todo alert
        });

        Thread thread = new Thread(previewTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void handleSendReport(ActionEvent actionEvent) {
    }

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        Stage stage = (Stage) btnLogout.getScene().getWindow();
        stage.close();
    }


    @FXML
    public void handleAddComment(ActionEvent actionEvent) {
        this.comment = txtComment.getText();
        generateAndDisplayPreview();
    }
}
