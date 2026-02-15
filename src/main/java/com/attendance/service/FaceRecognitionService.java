package com.attendance.service;

import com.attendance.entity.FaceData;
import com.attendance.entity.User;
import com.attendance.repository.FaceDataRepository;
import com.attendance.repository.UserRepository;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class FaceRecognitionService {
    
    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionService.class);
    
    @Autowired
    private FaceDataRepository faceDataRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${face.recognition.threshold:0.6}")
    private double recognitionThreshold;
    
    private CascadeClassifier faceDetector;
    private boolean initialized = false;
    
    // Standard size for face encoding
    private static final int ENCODING_WIDTH = 100;
    private static final int ENCODING_HEIGHT = 100;
    
    // Load OpenCV library
    static {
        try {
            OpenCV.loadShared();
            logger.info("OpenCV library loaded successfully. Version: {}", Core.VERSION);
        } catch (Exception e) {
            logger.error("Failed to load OpenCV library", e);
        }
    }
    
    @PostConstruct
    public void init() {
        try {
            // Load Haar cascade for face detection
            ClassPathResource resource = new ClassPathResource("haarcascade_frontalface_default.xml");
            
            if (!resource.exists()) {
                logger.error("Haar cascade file not found in resources!");
                return;
            }
            
            // Copy to temp file (OpenCV needs a file path, not stream)
            Path tempFile = Files.createTempFile("haarcascade", ".xml");
            try (InputStream is = resource.getInputStream()) {
                Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            
            faceDetector = new CascadeClassifier(tempFile.toString());
            
            if (faceDetector.empty()) {
                logger.error("Failed to load face detection cascade!");
            } else {
                initialized = true;
                logger.info("Face detection cascade loaded successfully!");
            }
            
        } catch (Exception e) {
            logger.error("Error initializing face detector", e);
        }
    }
    
    private void checkInitialized() {
        if (!initialized || faceDetector == null || faceDetector.empty()) {
            throw new RuntimeException("Face recognition not initialized. Please check Haar cascade file.");
        }
    }
    
    /**
     * Register face for a user
     */
    public boolean registerFace(Long userId, MultipartFile imageFile) throws IOException {
        checkInitialized();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Convert MultipartFile to Mat
        Mat image = bytesToMat(imageFile.getBytes());
        
        if (image.empty()) {
            throw new RuntimeException("Could not read image file");
        }
        
        logger.info("Processing image for user: {}, image size: {}x{}", userId, image.width(), image.height());
        
        // Detect faces
        MatOfRect faces = new MatOfRect();
        faceDetector.detectMultiScale(image, faces, 1.1, 3, 0, new Size(30, 30), new Size(500, 500));
        
        if (faces.empty()) {
            logger.warn("No face detected in the image for user: {}", userId);
            return false;
        }
        
        // Get the first (largest) face
        Rect[] facesArray = faces.toArray();
        logger.info("Detected {} face(s), using the first one", facesArray.length);
        
        Rect faceRect = facesArray[0];
        Mat face = new Mat(image, faceRect);
        
        // Extract face encoding
        double[] encoding = extractFaceEncoding(face);
        
        // Save to database
        FaceData faceData = new FaceData();
        faceData.setUser(user);
        faceData.setFaceEncoding(doubleArrayToBytes(encoding));
        faceDataRepository.save(faceData);
        
        // Update user face registration status
        user.setIsFaceRegistered(true);
        userRepository.save(user);
        
        logger.info("Face registered successfully for user: {}", userId);
        return true;
    }
    
    /**
     * Recognize face from image and return matched user
     */
    public Optional<User> recognizeFace(MultipartFile imageFile) throws IOException {
        checkInitialized();
        
        Mat image = bytesToMat(imageFile.getBytes());
        
        if (image.empty()) {
            throw new RuntimeException("Could not read image file");
        }
        
        // Detect faces
        MatOfRect faces = new MatOfRect();
        faceDetector.detectMultiScale(image, faces, 1.1, 3, 0, new Size(30, 30), new Size(500, 500));
        
        if (faces.empty()) {
            logger.warn("No face detected in the image");
            return Optional.empty();
        }
        
        // Get the first face
        Rect faceRect = faces.toArray()[0];
        Mat face = new Mat(image, faceRect);
        
        // Extract encoding for comparison
        double[] testEncoding = extractFaceEncoding(face);
        
        // Compare with all stored encodings
        List<FaceData> allFaceData = faceDataRepository.findAll();
        
        if (allFaceData.isEmpty()) {
            logger.warn("No registered faces found in database");
            return Optional.empty();
        }
        
        User bestMatch = null;
        double bestSimilarity = 0;
        
        for (FaceData storedFace : allFaceData) {
            if (storedFace.getFaceEncoding() == null) continue;
            
            double[] storedEncoding = bytesToDoubleArray(storedFace.getFaceEncoding());
            double distance = calculateEuclideanDistance(testEncoding, storedEncoding);
            
            // Convert distance to similarity (0-1, higher is better)
            double maxPossibleDistance = Math.sqrt(testEncoding.length) * 255.0;
            double similarity = 1.0 - (distance / maxPossibleDistance);
            
            logger.debug("Comparing with user {}: similarity={:.2f}%", 
                    storedFace.getUser().getUserId(), similarity * 100);
            
            if (similarity > recognitionThreshold && similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestMatch = storedFace.getUser();
            }
        }
        
        if (bestMatch != null) {
            logger.info("Face recognized as user: {} with similarity: {:.2f}%", 
                    bestMatch.getUserId(), bestSimilarity * 100);
        } else {
            logger.info("No matching face found (best similarity: {:.2f}%)", bestSimilarity * 100);
        }
        
        return Optional.ofNullable(bestMatch);
    }
    
    /**
     * Get confidence score for face recognition
     */
    public double getConfidenceScore(MultipartFile imageFile, Long userId) throws IOException {
        checkInitialized();
        
        Mat image = bytesToMat(imageFile.getBytes());
        
        if (image.empty()) {
            return 0.0;
        }
        
        MatOfRect faces = new MatOfRect();
        faceDetector.detectMultiScale(image, faces, 1.1, 3, 0, new Size(30, 30), new Size(500, 500));
        
        if (faces.empty()) {
            return 0.0;
        }
        
        Rect faceRect = faces.toArray()[0];
        Mat face = new Mat(image, faceRect);
        double[] testEncoding = extractFaceEncoding(face);
        
        List<FaceData> userFaceData = faceDataRepository.findByUserUserId(userId);
        if (userFaceData.isEmpty()) {
            return 0.0;
        }
        
        double maxSimilarity = 0;
        for (FaceData fd : userFaceData) {
            if (fd.getFaceEncoding() == null) continue;
            double[] storedEncoding = bytesToDoubleArray(fd.getFaceEncoding());
            double distance = calculateEuclideanDistance(testEncoding, storedEncoding);
            double maxPossibleDistance = Math.sqrt(testEncoding.length) * 255.0;
            double similarity = 1.0 - (distance / maxPossibleDistance);
            maxSimilarity = Math.max(maxSimilarity, similarity);
        }
        
        return Math.round(maxSimilarity * 10000.0) / 100.0;
    }
    
    /**
     * Extract face encoding (fixed version)
     */
    private double[] extractFaceEncoding(Mat face) {
        try {
            // Resize to standard size
            Mat resized = new Mat();
            Size standardSize = new Size(ENCODING_WIDTH, ENCODING_HEIGHT);
            Imgproc.resize(face, resized, standardSize);
            
            // Convert to grayscale
            Mat gray = new Mat();
            if (resized.channels() > 1) {
                Imgproc.cvtColor(resized, gray, Imgproc.COLOR_BGR2GRAY);
            } else {
                resized.copyTo(gray);
            }
            
            // Apply histogram equalization for better lighting normalization
            Imgproc.equalizeHist(gray, gray);
            
            // Convert to double precision (CV_64F)
            Mat grayDouble = new Mat();
            gray.convertTo(grayDouble, CvType.CV_64F);
            
            // Get the total number of elements
            int totalElements = (int) (grayDouble.total() * grayDouble.channels());
            
            // Create array and get data
            double[] encoding = new double[totalElements];
            grayDouble.get(0, 0, encoding);
            
            // Normalize the encoding
            double sum = 0;
            for (double val : encoding) {
                sum += val * val;
            }
            double norm = Math.sqrt(sum);
            if (norm > 0) {
                for (int i = 0; i < encoding.length; i++) {
                    encoding[i] = encoding[i] / norm;
                }
            }
            
            // Cleanup
            resized.release();
            gray.release();
            grayDouble.release();
            
            return encoding;
            
        } catch (Exception e) {
            logger.error("Error extracting face encoding", e);
            throw new RuntimeException("Failed to extract face encoding: " + e.getMessage());
        }
    }
    
    /**
     * Calculate Euclidean distance between two encoding arrays
     */
    private double calculateEuclideanDistance(double[] arr1, double[] arr2) {
        if (arr1 == null || arr2 == null || arr1.length != arr2.length) {
            return Double.MAX_VALUE;
        }
        
        double sum = 0.0;
        for (int i = 0; i < arr1.length; i++) {
            double diff = arr1[i] - arr2[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
    
    /**
     * Convert byte array to Mat
     */
    private Mat bytesToMat(byte[] bytes) {
        MatOfByte matOfByte = new MatOfByte(bytes);
        Mat image = Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_COLOR);
        matOfByte.release();
        return image;
    }
    
    /**
     * Convert double array to byte array for storage
     */
    private byte[] doubleArrayToBytes(double[] array) {
        if (array == null) return new byte[0];
        
        byte[] bytes = new byte[array.length * 8];
        for (int i = 0; i < array.length; i++) {
            long bits = Double.doubleToLongBits(array[i]);
            for (int j = 0; j < 8; j++) {
                bytes[i * 8 + j] = (byte) ((bits >> (j * 8)) & 0xff);
            }
        }
        return bytes;
    }
    
    /**
     * Convert byte array back to double array
     */
    private double[] bytesToDoubleArray(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return new double[0];
        
        double[] array = new double[bytes.length / 8];
        for (int i = 0; i < array.length; i++) {
            long bits = 0;
            for (int j = 0; j < 8; j++) {
                bits |= ((long) (bytes[i * 8 + j] & 0xff)) << (j * 8);
            }
            array[i] = Double.longBitsToDouble(bits);
        }
        return array;
    }
}
