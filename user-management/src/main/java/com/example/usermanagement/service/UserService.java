package com.example.usermanagement.service;

import com.example.usermanagement.entity.User;

import java.util.List;

/**
 * Service interface defining business operations for User management.
 *
 * Using an interface provides:
 * - Abstraction: Hides implementation details from the controller
 * - Loose coupling: Controller depends on interface, not implementation
 * - Testability: Easy to mock for unit testing
 * - Flexibility: Can have multiple implementations if needed
 */
public interface UserService {

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
     * @throws ResourceNotFoundException if user not found
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
     * @param user the updated user data
     * @return the updated user
     * @throws ResourceNotFoundException if user not found
     */
    User updateUser(Long id, User user);

    /**
     * Delete a user by ID.
     *
     * @param id the user ID to delete
     * @throws ResourceNotFoundException if user not found
     */
    void deleteUser(Long id);
}
