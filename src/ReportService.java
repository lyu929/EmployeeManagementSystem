import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReportService {

    public void totalPayrollByPayDate(String payDate) {
        String sql = """
            SELECT 
                pay_date,
                SUM(earnings) AS totalEarnings,
                SUM(fed_tax + fed_med + fed_SS + state_tax + retire_401k + health_care) AS totalDeductions
            FROM payroll
            WHERE pay_date = ?
            GROUP BY pay_date
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, payDate);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double earnings = rs.getDouble("totalEarnings");
                double deductions = rs.getDouble("totalDeductions");

                System.out.println("Pay Date: " + rs.getString("pay_date"));
                System.out.println("Total Earnings: $" + earnings);
                System.out.println("Total Deductions: $" + deductions);
                System.out.println("Estimated Net Total: $" + (earnings - deductions));
            } else {
                System.out.println("No payroll records found.");
            }

        } catch (Exception e) {
            System.out.println("Payroll summary error: " + e.getMessage());
        }
    }

    public void totalPayByJobTitle(String month) {
        String sql = """
            SELECT jt.job_title, SUM(p.earnings) AS totalPay
            FROM payroll p
            JOIN employees e ON p.empid = e.empid
            JOIN employee_job_titles ejt ON e.empid = ejt.empid
            JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id
            WHERE DATE_FORMAT(p.pay_date, '%Y-%m') = ?
            GROUP BY jt.job_title
            ORDER BY jt.job_title
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, month);
            ResultSet rs = ps.executeQuery();

            System.out.println("\nTotal Pay by Job Title for " + month);
            System.out.printf("%-30s %-12s%n", "Job Title", "Total Pay");

            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.printf("%-30s $%-12.2f%n",
                        rs.getString("job_title"),
                        rs.getDouble("totalPay"));
            }

            if (!found) {
                System.out.println("No records found.");
            }

        } catch (Exception e) {
            System.out.println("Job title report error: " + e.getMessage());
        }
    }

    public void totalPayByDivision(String month) {
        String sql = """
            SELECT d.Name AS divisionName, SUM(p.earnings) AS totalPay
            FROM payroll p
            JOIN employees e ON p.empid = e.empid
            JOIN employee_division ed ON e.empid = ed.empid
            JOIN division d ON ed.div_ID = d.ID
            WHERE DATE_FORMAT(p.pay_date, '%Y-%m') = ?
            GROUP BY d.Name
            ORDER BY d.Name
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, month);
            ResultSet rs = ps.executeQuery();

            System.out.println("\nTotal Pay by Division for " + month);
            System.out.printf("%-30s %-12s%n", "Division", "Total Pay");

            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.printf("%-30s $%-12.2f%n",
                        rs.getString("divisionName"),
                        rs.getDouble("totalPay"));
            }

            if (!found) {
                System.out.println("No records found.");
            }

        } catch (Exception e) {
            System.out.println("Division report error: " + e.getMessage());
        }
    }

    public void newHiresByDateRange(String startDate, String endDate) {
        String sql = """
            SELECT empid, Fname, Lname, HireDate
            FROM employees
            WHERE HireDate BETWEEN ? AND ?
            ORDER BY HireDate DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, startDate);
            ps.setString(2, endDate);

            ResultSet rs = ps.executeQuery();

            System.out.println("\nNew Hires from " + startDate + " to " + endDate);
            System.out.printf("%-10s %-15s %-15s %-12s%n",
                    "empID", "First Name", "Last Name", "Hire Date");

            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.printf("%-10d %-15s %-15s %-12s%n",
                        rs.getInt("empid"),
                        rs.getString("Fname"),
                        rs.getString("Lname"),
                        rs.getString("HireDate"));
            }

            if (!found) {
                System.out.println("No new hires found in this range.");
            }

        } catch (Exception e) {
            System.out.println("New hire report error: " + e.getMessage());
        }
    }
}