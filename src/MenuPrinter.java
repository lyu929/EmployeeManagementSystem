public class MenuPrinter {
    public static void printWelcome() {
        System.out.println("==========================================");
        System.out.println(" Company Z Employee Management System");
        System.out.println("==========================================");
    }

    public static void printAdminMenu() {
        System.out.println("\n========== HR ADMIN MENU ==========");
        System.out.println("1. Search Employee");
        System.out.println("2. Add New Employee");
        System.out.println("3. Update Employee Data");
        System.out.println("4. Delete Employee");
        System.out.println("5. Update One Employee Salary");
        System.out.println("6. Increase Salary by Salary Range");
        System.out.println("7. Increase Salary by Division");
        System.out.println("8. Update Employee Division / Job Title");
        System.out.println("9. Total Payroll Summary by Pay Date");
        System.out.println("10. Total Pay by Job Title");
        System.out.println("11. Total Pay by Division");
        System.out.println("12. New Employee Hires by Date Range");
        System.out.println("0. Logout");
    }

    public static void printEmployeeMenu() {
        System.out.println("\n========== EMPLOYEE MENU ==========");
        System.out.println("1. View My Personal Information");
        System.out.println("2. View My Pay Statement History");
        System.out.println("0. Logout");
    }
}