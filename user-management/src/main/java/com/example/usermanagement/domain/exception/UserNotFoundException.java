package com.example.usermanagement.domain.exception;

/**
 * Exception thrown when a user is not found.
 *
 * Part of the DOMAIN LAYER - represents a business rule violation.
 */
public class UserNotFoundException extends DomainException {

    private final Long userId;
    private final String email;

    public UserNotFoundException(Long userId) {
        super(String.format("User not found with id: %d", userId));
        this.userId = userId;
        this.email = null;
    }

    public UserNotFoundException(String email) {
        super(String.format("User not found with email: %s", email));
        this.userId = null;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
