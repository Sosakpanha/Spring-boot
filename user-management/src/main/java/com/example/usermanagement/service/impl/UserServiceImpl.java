package com.example.usermanagement.service.impl;

import com.example.usermanagement.entity.User;
import com.example.usermanagement.exception.UserNotFoundException;
import com.example.usermanagement.exception.ValidationException;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.service.UserService;
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
 * Implementation of UserService containing business logic with Redis caching.
 *
 * CACHING STRATEGY EXPLAINED:
 * ===========================
 *
 * @Cacheable - "Check cache first, then execute if not found"
 *   - Used on READ operations (getUserById, getAllUsers)
 *   - If cache HIT: returns cached value, method NOT executed
 *   - If cache MISS: executes method, stores result in cache
 *
 * @CachePut - "Always execute and update cache"
 *   - Used on UPDATE operations (updateUser)
 *   - ALWAYS executes the method
 *   - Updates cache with new value
 *
 * @CacheEvict - "Remove from cache"
 *   - Used on DELETE operations (deleteUser)
 *   - Also used on CREATE (to invalidate lists)
 *   - allEntries=true: Clears the entire cache
 *
 * @Caching - "Combine multiple cache operations"
 *   - Used when you need multiple cache operations at once
 *
 * WHEN TO CLEAR CACHE:
 * ====================
 * 1. CREATE: Evict usersList (new user added, list is stale)
 * 2. UPDATE: Update specific user cache, evict usersList
 * 3. DELETE: Evict specific user AND usersList
 *
 * Cache Names:
 * - "users": Individual user by ID (TTL: 30 min)
 * - "usersList": All users list (TTL: 5 min - changes frequently)
 * - "userByEmail": User by email lookup (TTL: 30 min)
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * CREATE: Evict usersList cache because a new user is added.
     * The list is now stale and needs to be refreshed on next read.
     */
    @Override
    @CacheEvict(value = "usersList", allEntries = true)
    public User createUser(User user) {
        log.debug("Creating new user with email: {}", user.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ValidationException("email", "Email already exists: " + user.getEmail());
        }

        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}. UsersList cache evicted.", savedUser.getId());
        return savedUser;
    }

    /**
     * READ by ID: Cache the result.
     * - First call: Cache MISS → query database → store in cache
     * - Subsequent calls: Cache HIT → return from cache (no DB query)
     *
     * key = "#id" means use the method parameter 'id' as cache key
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
     * Lists change frequently, so we use shorter TTL (5 min in RedisConfig).
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "usersList")
    public List<User> getAllUsers() {
        log.debug("Cache MISS for usersList. Fetching all users from database...");
        return userRepository.findAll();
    }

    /**
     * UPDATE: Use @Caching to combine multiple cache operations:
     * 1. @CachePut: Update the specific user in "users" cache
     * 2. @CacheEvict: Invalidate usersList (list content changed)
     * 3. @CacheEvict: Invalidate userByEmail (email might have changed)
     *
     * Note: @CachePut ALWAYS executes the method (unlike @Cacheable)
     */
    @Override
    @Caching(
            put = {
                    @CachePut(value = "users", key = "#id")
            },
            evict = {
                    @CacheEvict(value = "usersList", allEntries = true),
                    @CacheEvict(value = "userByEmail", allEntries = true)
            }
    )
    public User updateUser(Long id, User userDetails) {
        log.debug("Updating user ID: {}. Cache will be updated.", id);

        // Need to bypass cache to get fresh data for comparison
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // Check if new email conflicts with another user
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
     * - Evict specific user from "users" cache
     * - Evict entire "usersList" (list content changed)
     * - Evict from "userByEmail" (user no longer exists)
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "usersList", allEntries = true),
            @CacheEvict(value = "userByEmail", allEntries = true)
    })
    public void deleteUser(Long id) {
        log.debug("Deleting user ID: {}. Evicting from all caches.", id);

        // Bypass cache to get fresh user data
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(user);

        log.info("User {} deleted. Caches evicted.", id);
    }

    /**
     * Optional: Find by email with caching.
     * Useful for authentication/login lookups.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userByEmail", key = "#email")
    public User getUserByEmail(String email) {
        log.debug("Cache MISS for email: {}. Fetching from database...", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }
}
