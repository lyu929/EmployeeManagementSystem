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

SELECT * FROM users;