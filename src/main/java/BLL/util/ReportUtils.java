package BLL.util;

import BE.Order;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportUtils {

    //TODO move file.exist check and date formatting to this

    public static String generateReportFileName(Order order) {
        String targetDirectory = "QC_reports/";
        //make sure the directory exists
        new File(targetDirectory).mkdirs();
        //create a unique file name based on the order number and current time
        String date = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(new Date());

        return targetDirectory + order.getOrderNumber() + "_" + date + ".pdf";
    }
}
