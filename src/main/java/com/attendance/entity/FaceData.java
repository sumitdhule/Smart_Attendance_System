package com.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "face_data")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FaceData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "face_id")
    private Long faceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "face_encoding", columnDefinition = "BLOB")
    private byte[] faceEncoding;

    @Column(name = "image_path", length = 255)
    private String imagePath;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    @PrePersist
    protected void onCreate() {
        capturedAt = LocalDateTime.now();
    }
}
