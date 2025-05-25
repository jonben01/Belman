package DAL;

import BE.QCReport;

public interface IReportDataAccess {
    void saveReportInfo(QCReport report) throws Exception;

    QCReport getReportInfo(int orderId) throws Exception;
}
