import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PayrollService {
    public void printPayHistory(int empid) {
        String sql = """
            SELECT payID, pay_date, earnings, fed_tax, fed_med, fed_SS, 
                   state_tax, retire_401k, health_care
            FROM payroll
            WHERE empid = ?
            ORDER BY pay_date DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, empid);
            ResultSet rs = ps.executeQuery();

            System.out.println("\nPay Statement History");
            System.out.printf(
                    "%-6s %-12s %-10s %-10s %-10s %-10s %-10s %-10s %-10s%n",
                    "PayID", "PayDate", "Earnings", "FedTax", "FedMed", "FedSS",
                    "StateTax", "401K", "Health"
            );

            boolean found = false;

            while (rs.next()) {
                found = true;

                PayrollRecord record = new PayrollRecord(
                        rs.getInt("payID"),
                        rs.getString("pay_date"),
                        rs.getDouble("earnings"),
                        rs.getDouble("fed_tax"),
                        rs.getDouble("fed_med"),
                        rs.getDouble("fed_SS"),
                        rs.getDouble("state_tax"),
                        rs.getDouble("retire_401k"),
                        rs.getDouble("health_care")
                );

                record.printRecord();
            }

            if (!found) {
                System.out.println("No payroll records found.");
            }

        } catch (Exception e) {
            System.out.println("Payroll history error: " + e.getMessage());
        }
    }
}