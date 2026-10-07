import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    private static final AuthService authService = new AuthService();
    private static final EmployeeService employeeService = new EmployeeService();
    private static final PayrollService payrollService = new PayrollService();
    private static final ReportService reportService = new ReportService();

    public static void main(String[] args) {
        MenuPrinter.printWelcome();

        while (true) {
            System.out.println("\n1. Login");
            System.out.println("0. Exit");

            int choice = InputUtil.readInt(scanner, "Select option: ");

            if (choice == 1) {
                loginFlow();
            } else if (choice == 0) {
                System.out.println("Program closed.");
                return;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private static void loginFlow() {
        String username = InputUtil.readNonEmpty(scanner, "Username: ");
        String password = InputUtil.readNonEmpty(scanner, "Password: ");

        UserAccount user = authService.login(username, password);

        if (user == null) {
            System.out.println("Invalid username or password.");
            return;
        }

        System.out.println("Login successful. Role: " + user.getRole());

        if (user.getRole() == Role.HR_ADMIN) {
            adminMenu();
        } else {
            employeeMenu(user);
        }
    }

    private static void adminMenu() {
        while (true) {
            MenuPrinter.printAdminMenu();

            int choice = InputUtil.readInt(scanner, "Select option: ");

            switch (choice) {
                case 1:
                    searchEmployeeFlow();
                    break;
                case 2:
                    addEmployeeFlow();
                    break;
                case 3:
                    updateEmployeeFlow();
                    break;
                case 4:
                    deleteEmployeeFlow();
                    break;
                case 5:
                    updateOneSalaryFlow();
                    break;
                case 6:
                    updateSalaryRangeFlow();
                    break;
                case 7:
                    updateSalaryDivisionFlow();
                    break;
                case 8:
                    updateDivisionJobFlow();
                    break;
                case 9:
                    payrollSummaryFlow();
                    break;
                case 10:
                    payByJobTitleFlow();
                    break;
                case 11:
                    payByDivisionFlow();
                    break;
                case 12:
                    newHiresFlow();
                    break;
                case 0:
                    System.out.println("Logged out.");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void employeeMenu(UserAccount user) {
        while (true) {
            MenuPrinter.printEmployeeMenu();

            int choice = InputUtil.readInt(scanner, "Select option: ");

            switch (choice) {
                case 1:
                    if (user.getEmpid() == null) {
                        System.out.println("No employee record linked.");
                    } else {
                        Employee e = employeeService.findEmployeeById(user.getEmpid());
                        if (e != null) {
                            e.printFullInfo();
                        } else {
                            System.out.println("Employee not found.");
                        }
                    }
                    break;
                case 2:
                    if (user.getEmpid() == null) {
                        System.out.println("No employee record linked.");
                    } else {
                        payrollService.printPayHistory(user.getEmpid());
                    }
                    break;
                case 0:
                    System.out.println("Logged out.");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void searchEmployeeFlow() {
        System.out.println("Search types: empid, name, ssn");

        String type = InputUtil.readNonEmpty(scanner, "Search type: ");
        String value = InputUtil.readNonEmpty(scanner, "Search value: ");

        List<Employee> results = employeeService.searchEmployees(type, value);

        if (results.isEmpty()) {
            System.out.println("No matching employees found.");
        } else {
            for (Employee e : results) {
                e.printFullInfo();
            }
        }
    }

    private static void addEmployeeFlow() {
        int empid = InputUtil.readInt(scanner, "Employee ID: ");
        String fname = InputUtil.readNonEmpty(scanner, "First name: ");
        String lname = InputUtil.readNonEmpty(scanner, "Last name: ");
        String email = InputUtil.readNonEmpty(scanner, "Email: ");
        String hireDate = InputUtil.readNonEmpty(scanner, "Hire date (YYYY-MM-DD): ");
        double salary = InputUtil.readDouble(scanner, "Salary: ");
        String ssn = InputUtil.readNonEmpty(scanner, "SSN: ");
        int addressID = InputUtil.readInt(scanner, "Address ID: ");
        int divID = InputUtil.readInt(scanner, "Division ID: ");
        int jobTitleID = InputUtil.readInt(scanner, "Job Title ID: ");

        employeeService.addEmployee(
                empid, fname, lname, email, hireDate,
                salary, ssn, addressID, divID, jobTitleID
        );
    }

    private static void updateEmployeeFlow() {
        int empid = InputUtil.readInt(scanner, "Employee ID to update: ");

        Employee e = employeeService.findEmployeeById(empid);

        if (e == null) {
            System.out.println("Employee not found.");
            return;
        }

        e.printFullInfo();

        String email = InputUtil.readNonEmpty(scanner, "New email: ");
        double salary = InputUtil.readDouble(scanner, "New salary: ");
        String ssn = InputUtil.readNonEmpty(scanner, "New SSN: ");
        int addressID = InputUtil.readInt(scanner, "New address ID: ");

        employeeService.updateEmployeeData(empid, email, salary, ssn, addressID);
    }

    private static void deleteEmployeeFlow() {
        int empid = InputUtil.readInt(scanner, "Employee ID to delete: ");

        Employee e = employeeService.findEmployeeById(empid);

        if (e == null) {
            System.out.println("Employee " + empid + " does not exist.");
            return;
        }

        e.printFullInfo();

        String confirm = InputUtil.readNonEmpty(scanner, "Are you sure? Type YES to delete: ");

        if (confirm.equalsIgnoreCase("YES")) {
            employeeService.deleteEmployee(empid);
        } else {
            System.out.println("Delete cancelled.");
        }
    }

    private static void updateOneSalaryFlow() {
        int empid = InputUtil.readInt(scanner, "Employee ID: ");
        double percent = InputUtil.readDouble(scanner, "Increase percentage: ");

        employeeService.updateOneEmployeeSalary(empid, percent);
    }

    private static void updateSalaryRangeFlow() {
        double min = InputUtil.readDouble(scanner, "Minimum salary: ");
        double max = InputUtil.readDouble(scanner, "Maximum salary: ");
        double percent = InputUtil.readDouble(scanner, "Increase percentage: ");

        employeeService.updateSalaryByRange(min, max, percent);
    }

    private static void updateSalaryDivisionFlow() {
        int divID = InputUtil.readInt(scanner, "Division ID: ");
        double percent = InputUtil.readDouble(scanner, "Increase percentage: ");

        employeeService.updateSalaryByDivision(divID, percent);
    }

    private static void updateDivisionJobFlow() {
        int empid = InputUtil.readInt(scanner, "Employee ID: ");
        int divID = InputUtil.readInt(scanner, "New Division ID: ");
        int jobTitleID = InputUtil.readInt(scanner, "New Job Title ID: ");

        employeeService.updateDivisionAndJobTitle(empid, divID, jobTitleID);
    }

    private static void payrollSummaryFlow() {
        String payDate = InputUtil.readNonEmpty(scanner, "Pay date (YYYY-MM-DD): ");

        reportService.totalPayrollByPayDate(payDate);
    }

    private static void payByJobTitleFlow() {
        String month = InputUtil.readNonEmpty(scanner, "Month (YYYY-MM): ");

        reportService.totalPayByJobTitle(month);
    }

    private static void payByDivisionFlow() {
        String month = InputUtil.readNonEmpty(scanner, "Month (YYYY-MM): ");

        reportService.totalPayByDivision(month);
    }

    private static void newHiresFlow() {
        String start = InputUtil.readNonEmpty(scanner, "Start date (YYYY-MM-DD): ");
        String end = InputUtil.readNonEmpty(scanner, "End date (YYYY-MM-DD): ");

        reportService.newHiresByDateRange(start, end);
    }
}