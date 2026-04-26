# Employee Management System

## CSC3350 Software Development Final Project  
### Group Project – Spring 2026

This project is a Java + MySQL based Employee Management System developed for Company Z.

The system provides secure login and role-based authorization for both HR Admin users and General Employees.

---

# Technologies Used

- Java (Console-based UI)
- MySQL Database
- JDBC (MySQL Connector J)
- DBeaver
- VS Code

---

# Project Purpose

Company Z currently manages employee data manually using MySQL scripts and DBeaver with only one HR Admin using the MySQL root account.

This project improves that workflow by providing:

- Secure login and authorization
- Employee search, insert, update, and delete
- Salary management
- Payroll reports
- Employee self-service access

---

# System Features

## HR Admin Functions

- Login and authorization
- Search employee by empID, name, or SSN
- Add new employee
- Update employee information
- Delete employee
- Update one employee salary
- Increase salary by salary range
- Increase salary by division
- Update employee division / job title
- Total payroll summary by pay date
- Total pay by job title
- Total pay by division
- New employee hires by date range

## General Employee Functions

- Login
- View personal information
- View personal pay statement history

---

# Project Structure

```text
EmployeeManagementSystem
│
├── src
│   ├── AuthService.java
│   ├── DBConnection.java
│   ├── Employee.java
│   ├── EmployeeService.java
│   ├── InputUtil.java
│   ├── Main.java
│   ├── MenuPrinter.java
│   ├── PayrollRecord.java
│   ├── PayrollService.java
│   ├── ReportService.java
│   ├── Role.java
│   └── UserAccount.java
│
├── sql
│   ├── EmployeeData_MySQL_create.sql
│   ├── EmployeeData_INSERT_datum.sql
│   └── users.sql
│
├── lib
│   └── mysql-connector-j-9.7.0.jar
│
└── README.md
```

---

# Database Setup

## Step 1: Open DBeaver

- Open DBeaver
- Connect to your local MySQL server

## Step 2: Run Instructor SQL Files

Run the following SQL files in order:

1. `sql/EmployeeData_MySQL_create.sql`
2. `sql/EmployeeData_INSERT_datum.sql`

These files will:

- Create the `employeeData` database
- Create all required tables
- Insert the original 15 employees
- Insert payroll, division, and job title data

## Step 3: Create users Table

Create a new SQL file:

```text
sql/users.sql
```

Paste the following SQL code:

```sql
USE employeeData;

DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    empid INT NULL
);

INSERT INTO users (username, password, role, empid)
VALUES
('admin', 'admin123', 'HR_ADMIN', NULL),
('snoopy', 'emp123', 'EMPLOYEE', 1),
('charlie', 'emp123', 'EMPLOYEE', 2),
('lucy', 'emp123', 'EMPLOYEE', 3),
('pepermint', 'emp123', 'EMPLOYEE', 4),
('linus', 'emp123', 'EMPLOYEE', 5),
('pigpin', 'emp123', 'EMPLOYEE', 6),
('scooby', 'emp123', 'EMPLOYEE', 7),
('shaggy', 'emp123', 'EMPLOYEE', 8),
('velma', 'emp123', 'EMPLOYEE', 9),
('daphne', 'emp123', 'EMPLOYEE', 10),
('bugs', 'emp123', 'EMPLOYEE', 11),
('daffy', 'emp123', 'EMPLOYEE', 12),
('porky', 'emp123', 'EMPLOYEE', 13),
('elmer', 'emp123', 'EMPLOYEE', 14),
('marvin', 'emp123', 'EMPLOYEE', 15);
```

Run this file in DBeaver.

This table is used for:

- Login authentication
- Role-based authorization
- Employee system access

---

# How to Run the Project

## Step 1: Open Terminal

Navigate to the project folder:

```bash
cd ~/Desktop/EmployeeManagementSystem
```

## Step 2: Compile Java Files

Run:

```bash
javac -cp ".:lib/mysql-connector-j-9.7.0.jar" src/*.java
```

If no errors appear, compilation is successful.

## Step 3: Run the Program

Run:

```bash
java -cp ".:src:lib/mysql-connector-j-9.7.0.jar" Main
```

You should see:

```text
==========================================
Company Z Employee Management System
==========================================

1. Login
0. Exit
```

This means the system started successfully.

---

# Default Login Accounts

## HR Admin Account

```text
Username: admin
Password: admin123
```

Used for:

- CRUD operations
- Salary updates
- Reports
- Employee management

## Employee Account Example

```text
Username: snoopy
Password: emp123
```

Used for:

- Viewing personal information
- Viewing pay statement history

---

# Automatic Login Generation for New Employees

When HR Admin adds a new employee using:

```text
Add New Employee
```

The system automatically creates:

- Employee record
- Division relationship
- Job title relationship
- Login account in `users` table

Example:

```text
Employee ID: 16
First Name: Haolin
```

System automatically creates:

```text
Username: haolin16
Password: emp123
Role: EMPLOYEE
```

This is administrator-controlled account provisioning.

This is NOT self-registration.

---

# Recommended Demo Flow

Use this order for final video presentation:

## Admin Side

1. Login as Admin
2. Search Employee
3. Add New Employee
4. Search New Employee
5. Update Employee Data
6. Update Employee Salary
7. Increase Salary by Range
8. Increase Salary by Division
9. Update Division / Job Title
10. Payroll Summary Report
11. Total Pay by Job Title
12. Total Pay by Division
13. New Hires by Date Range

## Employee Side

14. Logout Admin
15. Login as Employee
16. View Personal Information
17. View Pay Statement History

## Final Step

18. Delete New Employee
19. Exit Program

This demonstrates all major required project functionalities.

---

# Important Notes

## Employee IDs

Do NOT use:

```text
1 ~ 15
```

These are instructor-provided employees.

Always start new employees from:

```text
16+
```

Example:

```text
16
17
18
20
30
```

## Before Final Demo

Always reset database by re-running:

```text
EmployeeData_MySQL_create.sql
EmployeeData_INSERT_datum.sql
users.sql
```

This ensures:

- Clean database
- No duplicate employees
- Correct demo results

---

# Submission Requirements

Submit:

- Java source files only (`.java`)
- ZIP compressed source code
- Final SDD PDF
- Final demonstration video (10–20 minutes)

Do NOT submit:

- `.class` files
- JDBC jar files
- Database export files
- Temporary logs

---

# End

CSC3350 Software Development  
Spring 2026  
Final Group Project