package com.example.usermanagement.application.port.input;

import com.example.usermanagement.application.dto.auth.AuthResponse;
import com.example.usermanagement.application.dto.auth.LoginRequest;
import com.example.usermanagement.application.dto.auth.RegisterRequest;

/**
 * Input port for Authentication operations.
 *
 * Part of the APPLICATION LAYER - defines authentication use cases.
 */
public interface AuthServicePort {

    /**
     * Register a new user.
     *
     * @param request the registration request
     * @return authentication response with JWT token
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate a user and return JWT token.
     *
     * @param request the login request
     * @return authentication response with JWT token
     */
    AuthResponse login(LoginRequest request);

    /**
     * Register a new admin user (requires existing admin).
     *
     * @param request the registration request
     * @return authentication response with JWT token
     */
    AuthResponse registerAdmin(RegisterRequest request);
}
