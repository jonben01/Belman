package BLL.util;

import BE.Order;

import java.io.File;
import java.io.IOException;

public interface IReportGenerator {

    File generatePreview(Order order, String comment) throws Exception;

    void generateReport(Order order, String filePath, String comment) throws Exception;

}
