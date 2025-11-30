package com.example.usermanagement.controller;

import com.example.usermanagement.dto.UserRequestDTO;
import com.example.usermanagement.dto.UserResponseDTO;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller handling HTTP requests for User operations.
 *
 * @RestController: Combines @Controller + @ResponseBody (returns JSON directly)
 * @RequestMapping: Base URL path for all endpoints in this controller
 * @RequiredArgsConstructor: Lombok generates constructor for dependency injection
 *
 * REST API Endpoints:
 * - POST   /api/users     -> Create a new user
 * - GET    /api/users     -> Get all users
 * - GET    /api/users/{id} -> Get user by ID
 * - PUT    /api/users/{id} -> Update user by ID
 * - DELETE /api/users/{id} -> Delete user by ID
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Create a new user.
     * POST /api/users
     *
     * @param userRequestDTO the user data from request body
     * @return created user with 201 CREATED status
     */
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        User user = mapToEntity(userRequestDTO);
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(mapToResponseDTO(createdUser), HttpStatus.CREATED);
    }

    /**
     * Get all users.
     * GET /api/users
     *
     * @return list of all users with 200 OK status
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponseDTO> userDTOs = users.stream()
                .map(this::mapToResponseDTO)
                .toList();
        return ResponseEntity.ok(userDTOs);
    }

    /**
     * Get a user by ID.
     * GET /api/users/{id}
     *
     * @param id the user ID from URL path
     * @return the user with 200 OK status
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(mapToResponseDTO(user));
    }

    /**
     * Update an existing user.
     * PUT /api/users/{id}
     *
     * @param id the user ID from URL path
     * @param userRequestDTO the updated user data from request body
     * @return updated user with 200 OK status
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO userRequestDTO) {
        User user = mapToEntity(userRequestDTO);
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(mapToResponseDTO(updatedUser));
    }

    /**
     * Delete a user by ID.
     * DELETE /api/users/{id}
     *
     * @param id the user ID from URL path
     * @return 204 NO CONTENT status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Map DTO to Entity.
     */
    private User mapToEntity(UserRequestDTO dto) {
        return User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .build();
    }

    /**
     * Map Entity to Response DTO.
     */
    private UserResponseDTO mapToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
