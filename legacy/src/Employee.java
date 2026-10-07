public class Employee {
    private int empid;
    private String fname;
    private String lname;
    private String email;
    private String hireDate;
    private double salary;
    private String ssn;
    private int addressID;
    private String divisionName;
    private String jobTitle;

    public int getEmpid() {
        return empid;
    }

    public void setEmpid(int empid) {
        this.empid = empid;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getHireDate() {
        return hireDate;
    }

    public void setHireDate(String hireDate) {
        this.hireDate = hireDate;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }
    
    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public int getAddressID() {
        return addressID;
    }

    public void setAddressID(int addressID) {
        this.addressID = addressID;
    }

    public String getDivisionName() {
        return divisionName;
    }

    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void printFullInfo() {
        System.out.println("----------------------------------------");
        System.out.println("Employee ID: " + empid);
        System.out.println("Name: " + fname + " " + lname);
        System.out.println("Email: " + email);
        System.out.println("Hire Date: " + hireDate);
        System.out.println("Salary: $" + salary);
        System.out.println("SSN: " + ssn);
        System.out.println("Address ID: " + addressID);
        System.out.println("Division: " + divisionName);
        System.out.println("Job Title: " + jobTitle);
        System.out.println("----------------------------------------");
    }
}