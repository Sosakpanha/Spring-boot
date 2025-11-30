package com.example.usermanagement.infrastructure.security.service;

import com.example.usermanagement.domain.enums.Role;
import com.example.usermanagement.domain.model.User;
import com.example.usermanagement.domain.exception.ValidationException;
import com.example.usermanagement.domain.repository.UserRepository;
import com.example.usermanagement.application.dto.auth.AuthResponse;
import com.example.usermanagement.application.dto.auth.LoginRequest;
import com.example.usermanagement.application.dto.auth.RegisterRequest;
import com.example.usermanagement.infrastructure.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service handling authentication operations.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user.
     */
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("email", "Email already registered: " + request.getEmail());
        }

        // Create new user
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        // Generate JWT token
        String jwtToken = jwtService.generateToken(user);

        return buildAuthResponse(savedUser, jwtToken);
    }

    /**
     * Register a new admin user.
     */
    public AuthResponse registerAdmin(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("email", "Email already registered: " + request.getEmail());
        }

        // Create new admin user
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .build();

        User savedUser = userRepository.save(user);

        // Generate JWT token
        String jwtToken = jwtService.generateToken(user);

        return buildAuthResponse(savedUser, jwtToken);
    }

    /**
     * Authenticate user and return JWT token.
     */
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new ValidationException("Invalid email or password");
        }

        // Get user from database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidationException("Invalid email or password"));

        // Generate JWT token
        String jwtToken = jwtService.generateToken(user);

        return buildAuthResponse(user, jwtToken);
    }

    /**
     * Build authentication response.
     */
    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }
}
