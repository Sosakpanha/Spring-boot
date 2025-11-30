package com.example.usermanagement.repository;

import com.example.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity - Data Access Layer.
 *
 * By extending JpaRepository<User, Long>, we automatically get:
 * - save(User entity): Create or update a user
 * - findById(Long id): Find user by ID
 * - findAll(): Get all users
 * - deleteById(Long id): Delete user by ID
 * - count(): Count total users
 * - existsById(Long id): Check if user exists
 *
 * @Repository: Marks this as a Spring Data repository component
 *
 * The first generic parameter (User) is the entity type.
 * The second generic parameter (Long) is the primary key type.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Custom query method to find a user by email.
     * Spring Data JPA automatically implements this based on the method name.
     *
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email already exists.
     *
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);
}
