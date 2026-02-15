package com.attendance.repository;

import com.attendance.entity.FaceData;
import com.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FaceDataRepository extends JpaRepository<FaceData, Long> {
    
    List<FaceData> findByUser(User user);
    
    List<FaceData> findByUserUserId(Long userId);
    
    void deleteByUser(User user);
    
    long countByUser(User user);
}
