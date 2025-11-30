package com.example.usermanagement.exception;

/**
 * Exception thrown when a user is not found in the database.
 * Results in HTTP 404 Not Found response.
 */
public class UserNotFoundException extends RuntimeException {

    private final Long userId;

    public UserNotFoundException(Long userId) {
        super(String.format("User not found with id: %d", userId));
        this.userId = userId;
    }

    public UserNotFoundException(String email) {
        super(String.format("User not found with email: %s", email));
        this.userId = null;
    }

    public Long getUserId() {
        return userId;
    }
}
