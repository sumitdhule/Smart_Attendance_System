package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.UserDTO;
import com.attendance.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200"})
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserDTO userDTO) {
        UserDTO createdUser = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", createdUser));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }
    
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getActiveUsers() {
        List<UserDTO> users = userService.getActiveUsers();
        return ResponseEntity.ok(ApiResponse.success("Active users retrieved successfully", users));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }
    
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByEmployeeId(@PathVariable String employeeId) {
        UserDTO user = userService.getUserByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id, 
            @Valid @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }
    
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully"));
    }
    
    @PostMapping("/{id}/face")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> registerFace(
            @PathVariable Long id,
            @RequestParam("images") List<MultipartFile> images) throws IOException {
        
        if (images == null || images.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("No images provided"));
        }
        
        int registeredCount = userService.registerFaces(id, images);
        
        if (registeredCount > 0) {
            return ResponseEntity.ok(ApiResponse.success(
                    "Face registration completed. " + registeredCount + " images registered.",
                    Map.of("registeredImages", registeredCount)));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("No faces detected in the provided images"));
        }
    }
}
