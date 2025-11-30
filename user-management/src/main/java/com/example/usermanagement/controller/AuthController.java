package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ErrorResponse;
import com.example.usermanagement.security.dto.AuthResponse;
import com.example.usermanagement.security.dto.LoginRequest;
import com.example.usermanagement.security.dto.RegisterRequest;
import com.example.usermanagement.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for authentication endpoints.
 *
 * Endpoints:
 * - POST /api/auth/register - Register new user
 * - POST /api/auth/login - Login and get JWT token
 * - POST /api/auth/register-admin - Register admin (requires ADMIN role)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register new user", description = "Creates a new user account and returns JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-12-01T10:30:00",
                                      "status": 400,
                                      "error": "Validation Error",
                                      "message": "Email already registered: john@example.com",
                                      "path": "/api/auth/register",
                                      "fieldErrors": {
                                        "email": "Email already registered: john@example.com"
                                      }
                                    }
                                    """)))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Login", description = "Authenticates user and returns JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-12-01T10:30:00",
                                      "status": 400,
                                      "error": "Validation Error",
                                      "message": "Invalid email or password",
                                      "path": "/api/auth/login"
                                    }
                                    """)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Register admin user", description = "Creates a new admin account (requires ADMIN role)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Admin registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - requires authentication"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
            @ApiResponse(responseCode = "400", description = "Validation error or email already exists")
    })
    @PostMapping("/register-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.registerAdmin(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
