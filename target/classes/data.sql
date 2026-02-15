-- Sample Data for Smart Attendance System

-- Insert sample users
INSERT INTO users (employee_id, first_name, last_name, email, phone, department, designation, is_active, is_face_registered)
VALUES ('EMP001', 'John', 'Smith', 'john.smith@company.com', '9876543210', 'IT', 'Software Engineer', 1, 0);

INSERT INTO users (employee_id, first_name, last_name, email, phone, department, designation, is_active, is_face_registered)
VALUES ('EMP002', 'Jane', 'Doe', 'jane.doe@company.com', '9876543211', 'HR', 'HR Manager', 1, 0);

INSERT INTO users (employee_id, first_name, last_name, email, phone, department, designation, is_active, is_face_registered)
VALUES ('EMP003', 'Robert', 'Johnson', 'robert.j@company.com', '9876543212', 'Finance', 'Accountant', 1, 0);

INSERT INTO users (employee_id, first_name, last_name, email, phone, department, designation, is_active, is_face_registered)
VALUES ('EMP004', 'Emily', 'Williams', 'emily.w@company.com', '9876543213', 'IT', 'Senior Developer', 1, 0);

INSERT INTO users (employee_id, first_name, last_name, email, phone, department, designation, is_active, is_face_registered)
VALUES ('EMP005', 'Michael', 'Brown', 'michael.b@company.com', '9876543214', 'Operations', 'Operations Lead', 1, 0);

COMMIT;
