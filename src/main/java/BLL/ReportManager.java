package BLL;

import BE.Order;
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

    public ReportManager() throws Exception{
        reportGenerator = new ReportGenerator();
        reportDataAccess = new ReportDAO();
    }

    public List<Image> generatePreview(Order order, String comment) throws Exception {
        File file = reportGenerator.generatePreview(order, comment);
        System.out.println(file.getAbsolutePath());
        return PDFPreviewUtil.convertPDFToFXImage(file);
    }
}
