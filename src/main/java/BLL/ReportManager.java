package BLL;

import BE.Order;
import BLL.util.Emailer;
import BLL.util.IReportGenerator;
import BLL.util.PDFPreviewUtil;
import BLL.util.ReportGenerator;
import DAL.IReportDataAccess;
import DAL.ReportDAO;
import javafx.scene.image.Image;

import java.io.File;
import java.util.List;

public class ReportManager {

    private IReportGenerator reportGenerator;
    private IReportDataAccess reportDataAccess;
    private Emailer emailer;

    public ReportManager() throws Exception{
        reportGenerator = new ReportGenerator();
        reportDataAccess = new ReportDAO();
        emailer = new Emailer();

    }

    public void sendEmail(String toEmail, String comment, Order order) throws Exception {
        File emailPdf = reportGenerator.getFileAndStoreEmail(order, comment);
        emailer.sendEmail(toEmail, emailPdf, order);

    }

    public List<Image> generatePreview(Order order, String comment) throws Exception {
        File file = reportGenerator.generatePreview(order, comment);
        System.out.println(file.getAbsolutePath());
        return PDFPreviewUtil.convertPDFToFXImage(file);
    }
}
