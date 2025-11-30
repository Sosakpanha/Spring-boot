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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                         AUTHENTICATION ENDPOINTS                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                              ║
 * ║  PUBLIC ENDPOINTS (No Authentication Required):                              ║
 * ║  • POST /api/auth/register - Register new user account                      ║
 * ║  • POST /api/auth/login    - Login and get JWT token                        ║
 * ║                                                                              ║
 * ║  PROTECTED ENDPOINTS (Requires Authentication):                              ║
 * ║  • POST /api/auth/register-admin - Register admin (ADMIN role required)     ║
 * ║                                                                              ║
 * ║  AUTHENTICATION FLOW:                                                        ║
 * ║  1. Register: POST /api/auth/register → Get JWT token                       ║
 * ║  2. Login: POST /api/auth/login → Get JWT token                             ║
 * ║  3. Use token: Add header "Authorization: Bearer <token>"                   ║
 * ║                                                                              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints - No authentication required for register/login")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register new user",
            description = """
                    Creates a new user account with USER role and returns JWT token.

                    **No authentication required.**

                    After registration, use the returned token for authenticated requests.
                    """,
            security = {} // Empty = no security required
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Successful Registration",
                                    value = """
                                            {
                                              "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNzAxNDIwMjAwLCJleHAiOjE3MDE1MDY2MDB9.abc123",
                                              "tokenType": "Bearer",
                                              "userId": 1,
                                              "email": "john@example.com",
                                              "firstName": "John",
                                              "lastName": "Doe",
                                              "role": "USER"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or email already exists",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Email Already Exists",
                                            value = """
                                                    {
                                                      "timestamp": "2025-12-01T10:30:00",
                                                      "status": 400,
                                                      "error": "Validation Error",
                                                      "message": "Email already registered: john@example.com",
                                                      "path": "/api/auth/register",
                                                      "fieldErrors": {
                                                        "email": "Email already registered"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Validation Errors",
                                            value = """
                                                    {
                                                      "timestamp": "2025-12-01T10:30:00",
                                                      "status": 400,
                                                      "error": "Validation Failed",
                                                      "message": "One or more fields have validation errors",
                                                      "path": "/api/auth/register",
                                                      "fieldErrors": {
                                                        "firstName": "First name must contain only letters",
                                                        "password": "Password must be at least 6 characters",
                                                        "email": "Please provide a valid email"
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Login",
            description = """
                    Authenticates user with email and password, returns JWT token.

                    **No authentication required.**

                    Use the returned token in the Authorization header for protected endpoints:
                    `Authorization: Bearer <token>`
                    """,
            security = {} // Empty = no security required
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Successful Login",
                                    value = """
                                            {
                                              "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNzAxNDIwMjAwLCJleHAiOjE3MDE1MDY2MDB9.abc123",
                                              "tokenType": "Bearer",
                                              "userId": 1,
                                              "email": "john@example.com",
                                              "firstName": "John",
                                              "lastName": "Doe",
                                              "role": "USER"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Invalid Credentials",
                                    value = """
                                            {
                                              "timestamp": "2025-12-01T10:30:00",
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "Invalid email or password",
                                              "path": "/api/auth/login"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Missing Fields",
                                    value = """
                                            {
                                              "timestamp": "2025-12-01T10:30:00",
                                              "status": 400,
                                              "error": "Validation Failed",
                                              "message": "One or more fields have validation errors",
                                              "path": "/api/auth/login",
                                              "fieldErrors": {
                                                "email": "Email is required",
                                                "password": "Password is required"
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Register admin user",
            description = """
                    Creates a new admin account with ADMIN role.

                    **Requires ADMIN role.**

                    Only existing admins can create new admin accounts.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Admin registered successfully",
                    content = @Content(
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Admin Created",
                                    value = """
                                            {
                                              "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "tokenType": "Bearer",
                                              "userId": 2,
                                              "email": "admin2@example.com",
                                              "firstName": "Jane",
                                              "lastName": "Admin",
                                              "role": "ADMIN"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - No token provided or token invalid",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Missing Token",
                                    value = """
                                            {
                                              "timestamp": "2025-12-01T10:30:00",
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "Full authentication is required to access this resource",
                                              "path": "/api/auth/register-admin"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have ADMIN role",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Insufficient Permissions",
                                    value = """
                                            {
                                              "timestamp": "2025-12-01T10:30:00",
                                              "status": 403,
                                              "error": "Forbidden",
                                              "message": "Access Denied: You don't have permission to access this resource",
                                              "path": "/api/auth/register-admin"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or email already exists",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Email Already Exists",
                                    value = """
                                            {
                                              "timestamp": "2025-12-01T10:30:00",
                                              "status": 400,
                                              "error": "Validation Error",
                                              "message": "Email already registered",
                                              "path": "/api/auth/register-admin"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/register-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.registerAdmin(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
