import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeService {

    private String baseQuery() {
        return """
            SELECT 
                e.empid,
                e.Fname,
                e.Lname,
                e.email,
                e.HireDate,
                e.Salary,
                e.SSN,
                e.addressID,
                d.Name AS divisionName,
                jt.job_title AS jobTitle
            FROM employees e
            LEFT JOIN employee_division ed ON e.empid = ed.empid
            LEFT JOIN division d ON ed.div_ID = d.ID
            LEFT JOIN employee_job_titles ejt ON e.empid = ejt.empid
            LEFT JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id
        """;
    }

    private Employee mapEmployee(ResultSet rs) throws SQLException {
        Employee e = new Employee();

        e.setEmpid(rs.getInt("empid"));
        e.setFname(rs.getString("Fname"));
        e.setLname(rs.getString("Lname"));
        e.setEmail(rs.getString("email"));
        e.setHireDate(rs.getString("HireDate"));
        e.setSalary(rs.getDouble("Salary"));
        e.setSsn(rs.getString("SSN"));
        e.setAddressID(rs.getInt("addressID"));
        e.setDivisionName(rs.getString("divisionName"));
        e.setJobTitle(rs.getString("jobTitle"));

        return e;
    }

    public Employee findEmployeeById(int empid) {
        String sql = baseQuery() + " WHERE e.empid = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, empid);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapEmployee(rs);
            }

        } catch (Exception e) {
            System.out.println("Find employee error: " + e.getMessage());
        }

        return null;
    }

    public List<Employee> searchEmployees(String searchType, String value) {
        List<Employee> list = new ArrayList<>();

        String whereClause;

        switch (searchType.toLowerCase()) {
            case "empid":
                whereClause = " WHERE e.empid = ?";
                break;
            case "name":
                whereClause = " WHERE e.Fname LIKE ? OR e.Lname LIKE ?";
                break;
            case "ssn":
                whereClause = " WHERE e.SSN = ?";
                break;
            default:
                System.out.println("Invalid search type. Use empid, name, or ssn.");
                return list;
        }

        String sql = baseQuery() + whereClause;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (searchType.equalsIgnoreCase("empid")) {
                ps.setInt(1, Integer.parseInt(value));
            } else if (searchType.equalsIgnoreCase("name")) {
                ps.setString(1, "%" + value + "%");
                ps.setString(2, "%" + value + "%");
            } else {
                ps.setString(1, value);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapEmployee(rs));
            }

        } catch (Exception e) {
            System.out.println("Search error: " + e.getMessage());
        }

        return list;
    }

    public void addEmployee(int empid, String fname, String lname, String email,
                            String hireDate, double salary, String ssn, int addressID,
                            int divID, int jobTitleID) {

        String insertEmp = """
            INSERT INTO employees(empid, Fname, Lname, email, HireDate, Salary, SSN, addressID)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        String insertDiv = "INSERT INTO employee_division(empid, div_ID) VALUES (?, ?)";
        String insertJob = "INSERT INTO employee_job_titles(empid, job_title_id) VALUES (?, ?)";

        String insertUser = """
            INSERT INTO users(username, password, role, empid)
            VALUES (?, ?, ?, ?)
        """;

        String username = fname.toLowerCase() + empid;
        String defaultPassword = "emp123";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(insertEmp)) {
                ps.setInt(1, empid);
                ps.setString(2, fname);
                ps.setString(3, lname);
                ps.setString(4, email);
                ps.setString(5, hireDate);
                ps.setDouble(6, salary);
                ps.setString(7, ssn);
                ps.setInt(8, addressID);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(insertDiv)) {
                ps.setInt(1, empid);
                ps.setInt(2, divID);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(insertJob)) {
                ps.setInt(1, empid);
                ps.setInt(2, jobTitleID);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(insertUser)) {
                ps.setString(1, username);
                ps.setString(2, defaultPassword);
                ps.setString(3, "EMPLOYEE");
                ps.setInt(4, empid);
                ps.executeUpdate();
            }

            conn.commit();

            System.out.println("New employee added successfully.");
            System.out.println("Employee login account created automatically.");
            System.out.println("Username: " + username);
            System.out.println("Default Password: " + defaultPassword);

        } catch (Exception e) {
            System.out.println("Add employee error: " + e.getMessage());
        }
    }

    public void updateEmployeeData(int empid, String email, double salary, String ssn, int addressID) {
        String sql = """
            UPDATE employees
            SET email = ?, Salary = ?, SSN = ?, addressID = ?
            WHERE empid = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setDouble(2, salary);
            ps.setString(3, ssn);
            ps.setInt(4, addressID);
            ps.setInt(5, empid);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Employee data updated successfully.");
            } else {
                System.out.println("Employee not found.");
            }

        } catch (Exception e) {
            System.out.println("Update employee error: " + e.getMessage());
        }
    }

    public void deleteEmployee(int empid) {
        String deletePayroll = "DELETE FROM payroll WHERE empid = ?";
        String deleteDiv = "DELETE FROM employee_division WHERE empid = ?";
        String deleteJob = "DELETE FROM employee_job_titles WHERE empid = ?";
        String deleteUser = "DELETE FROM users WHERE empid = ?";
        String deleteEmp = "DELETE FROM employees WHERE empid = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(deletePayroll)) {
                ps.setInt(1, empid);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(deleteDiv)) {
                ps.setInt(1, empid);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(deleteJob)) {
                ps.setInt(1, empid);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(deleteUser)) {
                ps.setInt(1, empid);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(deleteEmp)) {
                ps.setInt(1, empid);
                int rows = ps.executeUpdate();

                if (rows > 0) {
                    conn.commit();
                    System.out.println("Employee and login account deleted successfully.");
                } else {
                    conn.rollback();
                    System.out.println("Employee does not exist.");
                }
            }

        } catch (Exception e) {
            System.out.println("Delete employee error: " + e.getMessage());
        }
    }

    public void updateOneEmployeeSalary(int empid, double percent) {
        String sql = "UPDATE employees SET Salary = Salary + (Salary * ? / 100) WHERE empid = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, percent);
            ps.setInt(2, empid);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Salary updated successfully.");
            } else {
                System.out.println("Employee not found.");
            }

        } catch (Exception e) {
            System.out.println("Salary update error: " + e.getMessage());
        }
    }

    public void updateSalaryByRange(double minSalary, double maxSalary, double percent) {
        String sql = """
            UPDATE employees
            SET Salary = Salary + (Salary * ? / 100)
            WHERE Salary BETWEEN ? AND ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, percent);
            ps.setDouble(2, minSalary);
            ps.setDouble(3, maxSalary);

            int rows = ps.executeUpdate();
            System.out.println(rows + " salary record(s) updated.");

        } catch (Exception e) {
            System.out.println("Salary range update error: " + e.getMessage());
        }
    }

    public void updateSalaryByDivision(int divID, double percent) {
        String sql = """
            UPDATE employees e
            JOIN employee_division ed ON e.empid = ed.empid
            SET e.Salary = e.Salary + (e.Salary * ? / 100)
            WHERE ed.div_ID = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, percent);
            ps.setInt(2, divID);

            int rows = ps.executeUpdate();
            System.out.println(rows + " salary record(s) updated by division.");

        } catch (Exception e) {
            System.out.println("Division salary update error: " + e.getMessage());
        }
    }

    public void updateDivisionAndJobTitle(int empid, int divID, int jobTitleID) {
        String updateDiv = "UPDATE employee_division SET div_ID = ? WHERE empid = ?";
        String updateJob = "UPDATE employee_job_titles SET job_title_id = ? WHERE empid = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(updateDiv)) {
                ps.setInt(1, divID);
                ps.setInt(2, empid);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(updateJob)) {
                ps.setInt(1, jobTitleID);
                ps.setInt(2, empid);
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("Employee division/job title updated successfully.");

        } catch (Exception e) {
            System.out.println("Update division/job title error: " + e.getMessage());
        }
    }
}