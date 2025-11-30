package com.example.usermanagement.domain.repository;

import com.example.usermanagement.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity.
 *
 * Part of the DOMAIN LAYER - defines the contract for data access.
 *
 * In strict Clean Architecture, this would be a plain interface without
 * JpaRepository. The implementation would be in the infrastructure layer.
 * For pragmatic Spring Boot development, we extend JpaRepository here.
 *
 * By extending JpaRepository<User, Long>, we automatically get:
 * - save(User entity): Create or update a user
 * - findById(Long id): Find user by ID
 * - findAll(): Get all users
 * - deleteById(Long id): Delete user by ID
 * - count(): Count total users
 * - existsById(Long id): Check if user exists
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by email.
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
