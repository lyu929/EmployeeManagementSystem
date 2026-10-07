
CREATE DATABASE employeeData;
USE employeeData;


DROP TABLE IF EXISTS employees;
CREATE TABLE employees (
  empid INT NOT NULL,
  Fname VARCHAR(65) NOT NULL,
  Lname VARCHAR(65) NOT NULL,
  email VARCHAR(65) NOT NULL,
  HireDate DATE,
  Salary DECIMAL(10,2) NOT NULL,
  SSN VARCHAR(12),
  addressID INT NOT NULL,
  PRIMARY KEY (empid)
);

/***********************************************************************/

DROP TABLE IF EXISTS payroll;
CREATE TABLE payroll (
  payID INT,
  pay_date DATE,
  earnings DECIMAL(8,2),
  fed_tax DECIMAL(7,2),
  fed_med DECIMAL(7,2),
  fed_SS DECIMAL(7,2),
  state_tax DECIMAL(7,2),
  retire_401k DECIMAL(7,2),
  health_care DECIMAL(7,2),
  empid INT
);

/***********************************************************************/ 

DROP TABLE IF EXISTS employee_job_titles ;
CREATE TABLE employee_job_titles (
  empid INT NOT NULL,
  job_title_id INT NOT NULL
);

/***********************************************************************/ 

DROP TABLE IF EXISTS job_titles;
CREATE TABLE job_titles (
  job_title_id INT,
  job_title VARCHAR(125) NOT NULL
);

/***********************************************************************/ 

DROP TABLE IF EXISTS employee_division;
CREATE TABLE employee_division (
  empid int NOT NULL,
  div_ID int NOT NULL,
  PRIMARY KEY (empid)
) COMMENT='links employee to a division';

/***********************************************************************/

DROP TABLE IF EXISTS division;
CREATE TABLE division (
  ID int NOT NULL,
  Name varchar(100) DEFAULT NULL,
  city varchar(50) NOT NULL,
  addressLine1 varchar(50) NOT NULL,
  addressLine2 varchar(50) DEFAULT NULL,
  state varchar(50) DEFAULT NULL,
  country varchar(50) NOT NULL,
  postalCode varchar(15) NOT NULL
) COMMENT='company divisions';

/***********************************************************************/ 
