package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ErrorResponse;
import com.example.usermanagement.dto.UserRequestDTO;
import com.example.usermanagement.dto.UserResponseDTO;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.mapper.UserMapper;
import com.example.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller handling HTTP requests for User operations.
 * Uses MapStruct for Entity <-> DTO mapping.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "CRUD operations for managing users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Create a new user", description = "Creates a new user with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Validation Error",
                                            value = """
                                                    {
                                                      "timestamp": "2025-12-01T10:30:00",
                                                      "status": 400,
                                                      "error": "Validation Failed",
                                                      "message": "One or more fields have validation errors",
                                                      "path": "/api/users",
                                                      "fieldErrors": {
                                                        "firstName": "First name must contain only letters",
                                                        "email": "Please provide a valid email address"
                                                      }
                                                    }
                                                    """),
                                    @ExampleObject(name = "Duplicate Email",
                                            value = """
                                                    {
                                                      "timestamp": "2025-12-01T10:30:00",
                                                      "status": 400,
                                                      "error": "Validation Error",
                                                      "message": "Email already exists: john@example.com",
                                                      "path": "/api/users",
                                                      "fieldErrors": {
                                                        "email": "Email already exists: john@example.com"
                                                      }
                                                    }
                                                    """)
                            }))
    })
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        User user = userMapper.toEntity(userRequestDTO);
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(userMapper.toResponseDTO(createdUser), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all users", description = "Retrieves a list of all users")
    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class)))
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(userMapper.toResponseDTOList(users));
    }

    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-12-01T10:30:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "User not found with id: 999",
                                      "path": "/api/users/999"
                                    }
                                    """)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "User ID", required = true, example = "1") @PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userMapper.toResponseDTO(user));
    }

    @Operation(summary = "Update user", description = "Updates an existing user's details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-12-01T10:30:00",
                                      "status": 400,
                                      "error": "Validation Failed",
                                      "message": "One or more fields have validation errors",
                                      "path": "/api/users/1",
                                      "fieldErrors": {
                                        "email": "Please provide a valid email address"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-12-01T10:30:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "User not found with id: 999",
                                      "path": "/api/users/999"
                                    }
                                    """)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(description = "User ID", required = true, example = "1") @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO userRequestDTO) {
        User user = userMapper.toEntity(userRequestDTO);
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(userMapper.toResponseDTO(updatedUser));
    }

    @Operation(summary = "Delete user", description = "Deletes a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-12-01T10:30:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "User not found with id: 999",
                                      "path": "/api/users/999"
                                    }
                                    """)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", required = true, example = "1") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
