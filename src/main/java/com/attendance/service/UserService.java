package com.attendance.service;

import com.attendance.dto.UserDTO;
import com.attendance.entity.User;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FaceRecognitionService faceRecognitionService;
    
    @Autowired
    private ModelMapper modelMapper;
    
    public UserDTO createUser(UserDTO userDTO) {
        // Check if employee ID already exists
        if (userRepository.existsByEmployeeId(userDTO.getEmployeeId())) {
            throw new RuntimeException("Employee ID already exists: " + userDTO.getEmployeeId());
        }
        
        // Check if email already exists
        if (userDTO.getEmail() != null && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + userDTO.getEmail());
        }
        
        User user = modelMapper.map(userDTO, User.class);
        user.setIsActive(true);
        user.setIsFaceRegistered(false);
        
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDTO.class);
    }
    
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserDTO> getActiveUsers() {
        return userRepository.findByIsActiveTrue().stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return modelMapper.map(user, UserDTO.class);
    }
    
    @Transactional(readOnly = true)
    public UserDTO getUserByEmployeeId(String employeeId) {
        User user = userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "employeeId", employeeId));
        return modelMapper.map(user, UserDTO.class);
    }
    
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Update fields
        if (userDTO.getFirstName() != null) user.setFirstName(userDTO.getFirstName());
        if (userDTO.getLastName() != null) user.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) user.setEmail(userDTO.getEmail());
        if (userDTO.getPhone() != null) user.setPhone(userDTO.getPhone());
        if (userDTO.getDepartment() != null) user.setDepartment(userDTO.getDepartment());
        if (userDTO.getDesignation() != null) user.setDesignation(userDTO.getDesignation());
        if (userDTO.getIsActive() != null) user.setIsActive(userDTO.getIsActive());
        
        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserDTO.class);
    }
    
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        userRepository.delete(user);
    }
    
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setIsActive(false);
        userRepository.save(user);
    }
    
    public int registerFaces(Long userId, List<MultipartFile> images) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        int successCount = 0;
        for (MultipartFile image : images) {
            if (faceRecognitionService.registerFace(userId, image)) {
                successCount++;
            }
        }
        
        if (successCount > 0) {
            user.setIsFaceRegistered(true);
            userRepository.save(user);
        }
        
        return successCount;
    }
}
