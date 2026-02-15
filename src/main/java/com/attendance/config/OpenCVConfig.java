package com.attendance.config;

import nu.pattern.OpenCV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class OpenCVConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenCVConfig.class);
    
    @PostConstruct
    public void init() {
        try {
            OpenCV.loadShared();
            logger.info("=========================================");
            logger.info("  OpenCV Library Initialized Successfully");
            logger.info("  Version: {}", org.opencv.core.Core.VERSION);
            logger.info("=========================================");
        } catch (Exception e) {
            logger.error("Failed to initialize OpenCV library", e);
            throw new RuntimeException("Could not initialize OpenCV", e);
        }
    }
}
