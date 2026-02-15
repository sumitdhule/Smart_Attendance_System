package com.attendance.service;

import com.attendance.dto.AttendanceDTO;
import com.attendance.entity.Attendance;
import com.attendance.entity.Attendance.AttendanceStatus;
import com.attendance.entity.User;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceService {
    
    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);
    
    // Default check-in time threshold for "LATE" status
    private static final LocalTime LATE_THRESHOLD = LocalTime.of(9, 30);
    private static final LocalTime HALF_DAY_THRESHOLD = LocalTime.of(12, 0);
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FaceRecognitionService faceRecognitionService;
    
    /**
     * Mark attendance from face image
     */
    public AttendanceDTO markAttendanceFromImage(MultipartFile image) throws IOException {
        // Recognize face
        Optional<User> recognizedUser = faceRecognitionService.recognizeFace(image);
        
        if (recognizedUser.isEmpty()) {
            throw new RuntimeException("Face not recognized. Please register your face first.");
        }
        
        User user = recognizedUser.get();
        
        if (!user.getIsActive()) {
            throw new RuntimeException("User account is deactivated");
        }
        
        // Check if already checked in today
        Optional<Attendance> existingAttendance = attendanceRepository
                .findByUserAndAttendanceDate(user, LocalDate.now());
        
        if (existingAttendance.isPresent()) {
            // If already checked in, this is check-out
            Attendance attendance = existingAttendance.get();
            if (attendance.getCheckOutTime() == null) {
                attendance.setCheckOutTime(LocalDateTime.now());
                attendanceRepository.save(attendance);
                logger.info("Check-out recorded for user: {}", user.getUserId());
                return convertToDTO(attendance);
            } else {
                throw new RuntimeException("Already checked out for today");
            }
        }
        
        // Create new attendance record
        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setCheckInTime(LocalDateTime.now());
        
        // Determine status based on check-in time
        LocalTime checkInTime = LocalTime.now();
        if (checkInTime.isBefore(LATE_THRESHOLD)) {
            attendance.setStatus(AttendanceStatus.PRESENT);
        } else if (checkInTime.isBefore(HALF_DAY_THRESHOLD)) {
            attendance.setStatus(AttendanceStatus.LATE);
        } else {
            attendance.setStatus(AttendanceStatus.HALF_DAY);
        }
        
        // Get confidence score
        double confidence = faceRecognitionService.getConfidenceScore(image, user.getUserId());
        attendance.setConfidenceScore(confidence);
        
        Attendance savedAttendance = attendanceRepository.save(attendance);
        logger.info("Check-in recorded for user: {} with status: {}", user.getUserId(), attendance.getStatus());
        
        return convertToDTO(savedAttendance);
    }
    
    /**
     * Get daily attendance
     */
    @Transactional(readOnly = true)
    public List<AttendanceDTO> getDailyAttendance(LocalDate date) {
        List<Attendance> attendances = attendanceRepository.findByAttendanceDate(date);
        return attendances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get attendance report by date range
     */
    @Transactional(readOnly = true)
    public List<AttendanceDTO> getAttendanceReport(LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendances = attendanceRepository.findByDateRange(startDate, endDate);
        return attendances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get user's attendance history
     */
    @Transactional(readOnly = true)
    public List<AttendanceDTO> getUserAttendance(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Attendance> attendances;
        if (startDate != null && endDate != null) {
            attendances = attendanceRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        } else {
            attendances = attendanceRepository.findByUser(user);
        }
        
        return attendances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get today's attendance
     */
    @Transactional(readOnly = true)
    public List<AttendanceDTO> getTodayAttendance() {
        return getDailyAttendance(LocalDate.now());
    }
    
    /**
     * Manual check-out
     */
    public AttendanceDTO checkOut(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Attendance attendance = attendanceRepository
                .findByUserAndAttendanceDate(user, LocalDate.now())
                .orElseThrow(() -> new RuntimeException("No check-in record found for today"));
        
        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("Already checked out");
        }
        
        attendance.setCheckOutTime(LocalDateTime.now());
        Attendance savedAttendance = attendanceRepository.save(attendance);
        
        return convertToDTO(savedAttendance);
    }
    
    /**
     * Convert entity to DTO (Manual mapping to avoid ModelMapper issues)
     */
    private AttendanceDTO convertToDTO(Attendance attendance) {
        AttendanceDTO dto = new AttendanceDTO();
        
        dto.setAttendanceId(attendance.getAttendanceId());
        dto.setAttendanceDate(attendance.getAttendanceDate());
        dto.setCheckInTime(attendance.getCheckInTime());
        dto.setCheckOutTime(attendance.getCheckOutTime());
        dto.setStatus(attendance.getStatus());
        dto.setConfidenceScore(attendance.getConfidenceScore());
        
        // Map user details
        if (attendance.getUser() != null) {
            User user = attendance.getUser();
            dto.setUserId(user.getUserId());
            dto.setEmployeeId(user.getEmployeeId());
            dto.setFullName(user.getFullName());
            dto.setDepartment(user.getDepartment());
        }
        
        // Calculate working hours
        if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
            Duration duration = Duration.between(
                    attendance.getCheckInTime(), 
                    attendance.getCheckOutTime());
            dto.setWorkingHours(duration.toHours());
        }
        
        return dto;
    }
}
