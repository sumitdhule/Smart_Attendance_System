package com.attendance.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/")
public class WelcomeController {
    
    @GetMapping
    public Map<String, Object> welcome() {
        return Map.of(
            "status", "running",
            "message", "Welcome to Smart Attendance System API",
            "version", "1.0.0",
            "endpoints", Map.of(
                "users", "/api/users",
                "attendance", "/api/attendance",
                "markAttendance", "/api/attendance/mark (POST with image)",
                "todayAttendance", "/api/attendance/today",
                "dailyReport", "/api/attendance/daily?date=YYYY-MM-DD"
            )
        );
    }
}
