package com.attendance.repository;

import com.attendance.entity.Attendance;
import com.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    List<Attendance> findByUser(User user);
    
    List<Attendance> findByAttendanceDate(LocalDate date);
    
    Optional<Attendance> findByUserAndAttendanceDate(User user, LocalDate date);
    
    @Query("SELECT a FROM Attendance a WHERE a.attendanceDate BETWEEN :startDate AND :endDate")
    List<Attendance> findByDateRange(@Param("startDate") LocalDate startDate, 
                                     @Param("endDate") LocalDate endDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.user.department = :department AND a.attendanceDate = :date")
    List<Attendance> findByDepartmentAndDate(@Param("department") String department, 
                                              @Param("date") LocalDate date);
    
    @Query("SELECT a FROM Attendance a WHERE a.user.userId = :userId AND a.attendanceDate BETWEEN :startDate AND :endDate")
    List<Attendance> findByUserIdAndDateRange(@Param("userId") Long userId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.attendanceDate = :date AND a.status = 'PRESENT'")
    long countPresentByDate(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.attendanceDate = :date AND a.status = 'LATE'")
    long countLateByDate(@Param("date") LocalDate date);
}
