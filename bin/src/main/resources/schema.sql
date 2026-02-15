-- Oracle Database Schema for Smart Attendance System
-- Run this script to create tables manually (optional - Hibernate will auto-create)

-- Users Table
CREATE TABLE users (
    user_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    employee_id VARCHAR2(20) UNIQUE NOT NULL,
    first_name VARCHAR2(50) NOT NULL,
    last_name VARCHAR2(50) NOT NULL,
    email VARCHAR2(100) UNIQUE,
    phone VARCHAR2(20),
    department VARCHAR2(50),
    designation VARCHAR2(100),
    is_active NUMBER(1) DEFAULT 1,
    is_face_registered NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    updated_at TIMESTAMP DEFAULT SYSTIMESTAMP
);

-- Face Data Table
CREATE TABLE face_data (
    face_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id NUMBER NOT NULL,
    face_encoding BLOB,
    image_path VARCHAR2(255),
    captured_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT fk_face_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Attendance Table
CREATE TABLE attendance (
    attendance_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id NUMBER NOT NULL,
    attendance_date DATE NOT NULL,
    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,
    status VARCHAR2(20),
    confidence_score NUMBER(5,2),
    CONSTRAINT fk_attendance_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Create indexes for better performance
CREATE INDEX idx_users_employee ON users(employee_id);
CREATE INDEX idx_users_department ON users(department);
CREATE INDEX idx_attendance_date ON attendance(attendance_date);
CREATE INDEX idx_attendance_user_date ON attendance(user_id, attendance_date);

-- Create sequence for IDs (if not using IDENTITY)
-- CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 1;
-- CREATE SEQUENCE face_data_seq START WITH 1 INCREMENT BY 1;
-- CREATE SEQUENCE attendance_seq START WITH 1 INCREMENT BY 1;
