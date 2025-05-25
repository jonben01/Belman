package GUI.models;

import BE.Order;
import BLL.ReportManager;
import javafx.scene.image.Image;

import java.util.List;

public class ReportModel {

    private ReportManager reportManager;

    public ReportModel() throws Exception{
        reportManager = new ReportManager();
    }

    public List<Image> generatePreview(Order order, String comment) throws Exception {
        System.out.println("HELLO?");
        return reportManager.generatePreview(order, comment);
    }

    public void sendEmail(String toEmail, String comment, Order order) throws Exception {
        reportManager.sendEmail(toEmail, comment, order);
    }
}
