package com.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartAttendanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartAttendanceApplication.class, args);
        System.out.println("============================================");
        System.out.println("  Smart Attendance System Started!");
        System.out.println("  API: http://localhost:8080/api");
        System.out.println("============================================");
    }
}
