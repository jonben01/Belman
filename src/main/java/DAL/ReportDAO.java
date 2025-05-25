package DAL;

import BE.QCReport;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;

public class ReportDAO implements IReportDataAccess {
    @Override
    public void saveReportInfo(QCReport report) throws Exception {
        System.out.println("1");

        String sql = "INSERT INTO dbo.QCReports (order_id, created_at, created_by, mailed) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, report.getOrderId());
            stmt.setTimestamp(2, Timestamp.valueOf(report.getTime()));
            stmt.setInt(3, report.getUser().getId());
            //redundant, since the entry only exists through this method, so it will always be true.
            stmt.setBoolean(4, report.isSent());
            stmt.executeUpdate();

            System.out.println("2");

        } catch (SQLException | IOException e) {
            throw new Exception("Failed to save report info", e);
        }
    }

    @Override
    public QCReport getReportInfo(int orderId) throws Exception {
        //get only the most recent report
        String sql = "SELECT TOP 1 * FROM dbo.QCReports WHERE order_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

                QCReport report = new QCReport();
                report.setTime(createdAt);

                return report;
            }

            return null;

        } catch (SQLException e) {
            throw new Exception("Failed to get report info", e);
        }
    }
}
