package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.AttendanceDTO;
import com.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attendance")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200"})
public class AttendanceController {
    
    @Autowired
    private AttendanceService attendanceService;
    
    @PostMapping("/mark")
    public ResponseEntity<ApiResponse<AttendanceDTO>> markAttendance(
            @RequestParam("image") MultipartFile image) throws IOException {
        
        if (image.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("No image provided"));
        }
        
        try {
            AttendanceDTO attendance = attendanceService.markAttendanceFromImage(image);
            String message = attendance.getCheckOutTime() != null 
                    ? "Check-out successful" 
                    : "Check-in successful";
            return ResponseEntity.ok(ApiResponse.success(message, attendance));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getTodayAttendance() {
        List<AttendanceDTO> attendance = attendanceService.getTodayAttendance();
        return ResponseEntity.ok(ApiResponse.success("Today's attendance retrieved", attendance));
    }
    
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getDailyAttendance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AttendanceDTO> attendance = attendanceService.getDailyAttendance(date);
        return ResponseEntity.ok(ApiResponse.success("Daily attendance retrieved", attendance));
    }
    
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getAttendanceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AttendanceDTO> report = attendanceService.getAttendanceReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Attendance report generated", report));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getUserAttendance(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AttendanceDTO> attendance = attendanceService.getUserAttendance(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("User attendance retrieved", attendance));
    }
    
    @PostMapping("/checkout/{userId}")
    public ResponseEntity<ApiResponse<AttendanceDTO>> checkOut(@PathVariable Long userId) {
        try {
            AttendanceDTO attendance = attendanceService.checkOut(userId);
            return ResponseEntity.ok(ApiResponse.success("Check-out successful", attendance));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/stats/today")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTodayStats() {
        List<AttendanceDTO> todayAttendance = attendanceService.getTodayAttendance();
        
        long present = todayAttendance.stream()
                .filter(a -> a.getStatus() != null)
                .filter(a -> a.getStatus().name().equals("PRESENT"))
                .count();
        
        long late = todayAttendance.stream()
                .filter(a -> a.getStatus() != null)
                .filter(a -> a.getStatus().name().equals("LATE"))
                .count();
        
        long halfDay = todayAttendance.stream()
                .filter(a -> a.getStatus() != null)
                .filter(a -> a.getStatus().name().equals("HALF_DAY"))
                .count();
        
        Map<String, Object> stats = Map.of(
                "date", LocalDate.now(),
                "totalCheckedIn", todayAttendance.size(),
                "present", present,
                "late", late,
                "halfDay", halfDay
        );
        
        return ResponseEntity.ok(ApiResponse.success("Today's statistics", stats));
    }
}
