package BLL.util;

import BE.Order;

import java.io.File;

public interface IReportGenerator {

    File generatePreview(Order order, String comment) throws Exception;

    void generateReport(Order order, String filePath, String comment) throws Exception;

    File getFileAndStoreEmail(Order order, String comment) throws Exception;
}
