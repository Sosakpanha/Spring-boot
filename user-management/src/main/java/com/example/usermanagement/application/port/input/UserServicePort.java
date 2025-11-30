package com.example.usermanagement.application.port.input;

import com.example.usermanagement.domain.model.User;

import java.util.List;

/**
 * Input port for User operations.
 *
 * Part of the APPLICATION LAYER - defines the use cases (input port).
 *
 * In Clean Architecture, input ports define what the application can do.
 * Controllers (presentation layer) use this port to interact with the application.
 */
public interface UserServicePort {

    /**
     * Create a new user.
     *
     * @param user the user to create
     * @return the created user with generated ID
     */
    User createUser(User user);

    /**
     * Get a user by ID.
     *
     * @param id the user ID
     * @return the user
     * @throws com.example.usermanagement.domain.exception.UserNotFoundException if not found
     */
    User getUserById(Long id);

    /**
     * Get all users.
     *
     * @return list of all users
     */
    List<User> getAllUsers();

    /**
     * Update an existing user.
     *
     * @param id the user ID to update
     * @param userDetails the new user details
     * @return the updated user
     */
    User updateUser(Long id, User userDetails);

    /**
     * Delete a user by ID.
     *
     * @param id the user ID to delete
     */
    void deleteUser(Long id);
}
