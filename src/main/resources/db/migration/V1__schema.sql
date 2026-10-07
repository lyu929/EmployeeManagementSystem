-- Normalised schema for the Employee Management System.
-- Portable between MySQL 8 and H2 (MySQL mode), which is used for local development and tests.

CREATE TABLE division (
    id            INT          NOT NULL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    city          VARCHAR(50)  NOT NULL,
    address_line1 VARCHAR(50)  NOT NULL,
    address_line2 VARCHAR(50),
    state         VARCHAR(50),
    country       VARCHAR(50)  NOT NULL,
    postal_code   VARCHAR(15)  NOT NULL
);

CREATE TABLE job_title (
    id    INT          NOT NULL PRIMARY KEY,
    title VARCHAR(125) NOT NULL,
    CONSTRAINT uk_job_title_title UNIQUE (title)
);

CREATE TABLE employee (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    first_name     VARCHAR(65)   NOT NULL,
    last_name      VARCHAR(65)   NOT NULL,
    email          VARCHAR(120)  NOT NULL,
    hire_date      DATE          NOT NULL,
    salary         DECIMAL(12,2) NOT NULL,
    ssn_ciphertext VARCHAR(255),          -- AES-256-GCM, base64(iv || ciphertext || tag)
    ssn_hash       VARCHAR(64),           -- HMAC-SHA256 blind index for exact-match search
    ssn_last4      VARCHAR(4),            -- for masked display only
    division_id    INT,
    job_title_id   INT,
    version        BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT uk_employee_email UNIQUE (email),
    CONSTRAINT uk_employee_ssn_hash UNIQUE (ssn_hash),
    CONSTRAINT ck_employee_salary CHECK (salary >= 0),
    CONSTRAINT fk_employee_division FOREIGN KEY (division_id) REFERENCES division (id),
    CONSTRAINT fk_employee_job_title FOREIGN KEY (job_title_id) REFERENCES job_title (id)
);
CREATE INDEX idx_employee_last_name ON employee (last_name);
CREATE INDEX idx_employee_hire_date ON employee (hire_date);

CREATE TABLE pay_statement (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT           NOT NULL,
    pay_date    DATE          NOT NULL,
    earnings    DECIMAL(10,2) NOT NULL,
    fed_tax     DECIMAL(10,2) NOT NULL,
    fed_med     DECIMAL(10,2) NOT NULL,
    fed_ss      DECIMAL(10,2) NOT NULL,
    state_tax   DECIMAL(10,2) NOT NULL,
    retire_401k DECIMAL(10,2) NOT NULL,
    health_care DECIMAL(10,2) NOT NULL,
    CONSTRAINT uk_pay_statement_employee_date UNIQUE (employee_id, pay_date),
    CONSTRAINT fk_pay_statement_employee FOREIGN KEY (employee_id) REFERENCES employee (id) ON DELETE CASCADE
);
CREATE INDEX idx_pay_statement_pay_date ON pay_statement (pay_date);

CREATE TABLE app_user (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,  -- BCrypt
    role          VARCHAR(20)  NOT NULL,
    employee_id   INT,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_app_user_username UNIQUE (username),
    CONSTRAINT uk_app_user_employee UNIQUE (employee_id),
    CONSTRAINT ck_app_user_role CHECK (role IN ('HR_ADMIN', 'EMPLOYEE')),
    CONSTRAINT fk_app_user_employee FOREIGN KEY (employee_id) REFERENCES employee (id) ON DELETE CASCADE
);

CREATE TABLE audit_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    occurred_at TIMESTAMP     NOT NULL,
    actor       VARCHAR(50)   NOT NULL,
    action      VARCHAR(50)   NOT NULL,
    entity_type VARCHAR(50)   NOT NULL,
    entity_id   VARCHAR(50),
    details     VARCHAR(1000)
);
CREATE INDEX idx_audit_log_entity ON audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_log_time ON audit_log (occurred_at);
