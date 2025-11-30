package com.example.usermanagement.application.service;

import com.example.usermanagement.application.port.input.UserServicePort;
import com.example.usermanagement.domain.exception.UserNotFoundException;
import com.example.usermanagement.domain.exception.ValidationException;
import com.example.usermanagement.domain.model.User;
import com.example.usermanagement.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User Service implementation.
 *
 * Part of the APPLICATION LAYER - contains business logic and use cases.
 *
 * This service:
 * - Implements the UserServicePort (input port)
 * - Uses UserRepository (domain repository)
 * - Throws domain exceptions
 * - Handles caching
 *
 * CACHING STRATEGY:
 * ==================
 * - @Cacheable: Check cache first, then execute if not found (READ)
 * - @CachePut: Always execute and update cache (UPDATE)
 * - @CacheEvict: Remove from cache (DELETE/CREATE)
 * - @Caching: Combine multiple cache operations
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService implements UserServicePort {

    private final UserRepository userRepository;

    /**
     * CREATE: Evict usersList cache because a new user is added.
     */
    @Override
    @CacheEvict(value = "usersList", allEntries = true)
    public User createUser(User user) {
        log.debug("Creating new user with email: {}", user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ValidationException("email", "Email already exists: " + user.getEmail());
        }

        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}. UsersList cache evicted.", savedUser.getId());
        return savedUser;
    }

    /**
     * READ by ID: Cache the result.
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        log.debug("Cache MISS for user ID: {}. Fetching from database...", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * READ all: Cache the entire list.
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "usersList")
    public List<User> getAllUsers() {
        log.debug("Cache MISS for usersList. Fetching all users from database...");
        return userRepository.findAll();
    }

    /**
     * UPDATE: Update cache and evict lists.
     */
    @Override
    @Caching(
            put = {@CachePut(value = "users", key = "#id")},
            evict = {
                    @CacheEvict(value = "usersList", allEntries = true),
                    @CacheEvict(value = "userByEmail", allEntries = true)
            }
    )
    public User updateUser(Long id, User userDetails) {
        log.debug("Updating user ID: {}. Cache will be updated.", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!existingUser.getEmail().equals(userDetails.getEmail())
                && userRepository.existsByEmail(userDetails.getEmail())) {
            throw new ValidationException("email", "Email already exists: " + userDetails.getEmail());
        }

        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setEmail(userDetails.getEmail());

        User updatedUser = userRepository.save(existingUser);
        log.info("User {} updated. Cache refreshed, usersList evicted.", id);
        return updatedUser;
    }

    /**
     * DELETE: Evict from all relevant caches.
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "usersList", allEntries = true),
            @CacheEvict(value = "userByEmail", allEntries = true)
    })
    public void deleteUser(Long id) {
        log.debug("Deleting user ID: {}. Evicting from all caches.", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(user);

        log.info("User {} deleted. Caches evicted.", id);
    }

    /**
     * Find by email with caching (for authentication).
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userByEmail", key = "#email")
    public User getUserByEmail(String email) {
        log.debug("Cache MISS for email: {}. Fetching from database...", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
}
