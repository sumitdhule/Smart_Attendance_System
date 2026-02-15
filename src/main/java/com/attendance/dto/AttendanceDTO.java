package com.attendance.dto;

import com.attendance.entity.Attendance;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDTO {
    
    private Long attendanceId;
    private Long userId;
    private String employeeId;
    private String fullName;
    private String department;
    private LocalDate attendanceDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Attendance.AttendanceStatus status;
    private Double confidenceScore;
    
    // Calculated field
    private Long workingHours;
}
