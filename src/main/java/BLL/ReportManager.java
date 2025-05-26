package BLL;

import BE.Order;
import BE.QCReport;
import BE.User;
import BLL.util.Emailer;
import BLL.util.IReportGenerator;
import BLL.util.PDFPreviewUtil;
import BLL.util.ReportGenerator;
import DAL.IReportDataAccess;
import DAL.ReportDAO;
import BLL.util.SessionManager;
import javafx.scene.image.Image;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class ReportManager {

    private IReportGenerator reportGenerator;
    private IReportDataAccess reportDataAccess;
    private Emailer emailer;

    public ReportManager(){
        reportGenerator = new ReportGenerator();
        reportDataAccess = new ReportDAO();
        emailer = new Emailer();

    }

    public void sendEmail(String toEmail, String comment, Order order) throws Exception {
        File emailPdf = reportGenerator.getFileAndStoreEmail(order, comment);
        emailer.sendEmail(toEmail, emailPdf, order);


        LocalDateTime now = LocalDateTime.now();
        int orderId = order.getId();
        User sender = SessionManager.getInstance().getCurrentUser();
        boolean isSent = true;

        QCReport report = new QCReport(orderId, now, sender, isSent);
        reportDataAccess.saveReportInfo(report);

    }

    public List<Image> generatePreview(Order order, String comment) throws Exception {
        File file = reportGenerator.generatePreview(order, comment);
        return PDFPreviewUtil.convertPDFToFXImage(file);

    }

    public QCReport getReportInfo(int orderId) throws Exception {
        return reportDataAccess.getReportInfo(orderId);
    }
}
