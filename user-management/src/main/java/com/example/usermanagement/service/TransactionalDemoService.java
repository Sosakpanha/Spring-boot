package com.example.usermanagement.service;

import com.example.usermanagement.entity.AuditLog;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.exception.UserNotFoundException;
import com.example.usermanagement.repository.AuditLogRepository;
import com.example.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service demonstrating @Transactional usage with multiple tables.
 *
 * ============================================================
 * @TRANSACTIONAL ATTRIBUTES EXPLAINED:
 * ============================================================
 *
 * 1. PROPAGATION (How transactions relate to each other):
 *    - REQUIRED (default): Use existing transaction or create new one
 *    - REQUIRES_NEW: Always create a new transaction (suspend existing)
 *    - SUPPORTS: Use transaction if exists, otherwise run without
 *    - NOT_SUPPORTED: Run without transaction (suspend if exists)
 *    - MANDATORY: Must run within existing transaction (error if none)
 *    - NEVER: Must NOT run within transaction (error if exists)
 *    - NESTED: Create nested transaction within existing one
 *
 * 2. ISOLATION (How transactions see each other's changes):
 *    - READ_UNCOMMITTED: Can see uncommitted changes (dirty reads)
 *    - READ_COMMITTED: Only see committed changes
 *    - REPEATABLE_READ: Same read returns same data within transaction
 *    - SERIALIZABLE: Highest isolation, transactions run sequentially
 *
 * 3. ROLLBACK:
 *    - rollbackFor: Exceptions that trigger rollback
 *    - noRollbackFor: Exceptions that should NOT trigger rollback
 *    - By default: RuntimeException rolls back, checked Exception doesn't
 *
 * 4. readOnly: Optimization hint for read-only operations
 *
 * 5. timeout: Maximum time (seconds) before transaction timeout
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionalDemoService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    // ============================================================
    // EXAMPLE 1: Basic Transaction - Updates Two Tables
    // ============================================================
    /**
     * Updates user email and creates audit log in ONE transaction.
     * If either operation fails, BOTH are rolled back.
     *
     * SCENARIO:
     * 1. User table is updated
     * 2. AuditLog is created
     * 3. If step 2 fails, step 1 is ROLLED BACK
     */
    @Transactional
    public User updateUserEmailWithAudit(Long userId, String newEmail) {
        log.info("Starting transaction: updateUserEmailWithAudit");

        // Step 1: Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String oldEmail = user.getEmail();

        // Step 2: Update user email
        user.setEmail(newEmail);
        User updatedUser = userRepository.save(user);
        log.info("User email updated from {} to {}", oldEmail, newEmail);

        // Step 3: Create audit log (same transaction)
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action("EMAIL_UPDATED")
                .details("User email changed")
                .oldValue(oldEmail)
                .newValue(newEmail)
                .build();
        auditLogRepository.save(auditLog);
        log.info("Audit log created");

        // If we reach here, both operations are committed together
        return updatedUser;
    }

    // ============================================================
    // EXAMPLE 2: Demonstrating Rollback
    // ============================================================
    /**
     * Demonstrates rollback when exception occurs.
     * User update is ROLLED BACK because exception happens after save.
     *
     * @param simulateError if true, throws exception after user update
     */
    @Transactional(rollbackFor = Exception.class)
    public User updateUserWithPossibleRollback(Long userId, String newEmail, boolean simulateError) {
        log.info("Starting transaction: updateUserWithPossibleRollback");

        // Step 1: Update user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String oldEmail = user.getEmail();
        user.setEmail(newEmail);
        User updatedUser = userRepository.save(user);
        log.info("User saved (but not committed yet!)");

        // Step 2: Create audit log
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action("EMAIL_UPDATED")
                .oldValue(oldEmail)
                .newValue(newEmail)
                .build();
        auditLogRepository.save(auditLog);

        // Step 3: Simulate error (if requested)
        if (simulateError) {
            log.error("Simulating error - transaction will ROLLBACK!");
            throw new RuntimeException("Simulated error - all changes rolled back!");
            // At this point:
            // - User update is ROLLED BACK
            // - Audit log is ROLLED BACK
            // - Database state is unchanged
        }

        return updatedUser;
    }

    // ============================================================
    // EXAMPLE 3: Read-Only Transaction
    // ============================================================
    /**
     * Read-only transaction for SELECT operations.
     *
     * Benefits of readOnly=true:
     * - Performance optimization (no dirty checking)
     * - Database can optimize for read operations
     * - Prevents accidental modifications
     */
    @Transactional(readOnly = true)
    public User getUserWithAuditHistory(Long userId) {
        log.info("Read-only transaction: getUserWithAuditHistory");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // This would throw exception if we tried to modify:
        // user.setEmail("new@email.com");
        // userRepository.save(user); // Would fail!

        return user;
    }

    // ============================================================
    // EXAMPLE 4: Transaction with Isolation Level
    // ============================================================
    /**
     * Transaction with REPEATABLE_READ isolation.
     *
     * ISOLATION LEVELS EXPLAINED:
     *
     * READ_UNCOMMITTED:
     * - Lowest isolation, highest concurrency
     * - Can read uncommitted changes from other transactions
     * - Problem: Dirty reads
     *
     * READ_COMMITTED (Most common):
     * - Only reads committed data
     * - Same query may return different results if other transaction commits
     * - Problem: Non-repeatable reads
     *
     * REPEATABLE_READ:
     * - Same query returns same results within transaction
     * - Problem: Phantom reads (new rows from other transactions)
     *
     * SERIALIZABLE:
     * - Highest isolation, lowest concurrency
     * - Transactions executed sequentially
     * - No concurrency problems, but slowest
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public User updateUserWithRepeatableRead(Long userId, String newEmail) {
        log.info("Transaction with REPEATABLE_READ isolation");

        // First read
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Even if another transaction modifies this user,
        // our reads will see the same data

        user.setEmail(newEmail);
        return userRepository.save(user);
    }

    // ============================================================
    // EXAMPLE 5: Propagation REQUIRES_NEW
    // ============================================================
    /**
     * Creates audit log in a NEW transaction.
     *
     * REQUIRES_NEW means:
     * - Suspends current transaction (if any)
     * - Creates brand new transaction
     * - Even if outer transaction rolls back, this commits
     *
     * USE CASE: Logging errors even when main transaction fails
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createAuditLogInNewTransaction(Long userId, String action, String details) {
        log.info("Creating audit log in NEW transaction");

        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .details(details)
                .build();

        auditLogRepository.save(auditLog);
        // This commits independently of the calling transaction
    }

    /**
     * Demonstrates REQUIRES_NEW - audit log saved even if main fails.
     */
    @Transactional
    public void demonstrateRequiresNew(Long userId, String newEmail, boolean simulateError) {
        log.info("Main transaction started");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setEmail(newEmail);
        userRepository.save(user);

        // This runs in a SEPARATE transaction
        // It will be COMMITTED even if we throw exception below
        createAuditLogInNewTransaction(userId, "EMAIL_UPDATE_ATTEMPTED",
                "Attempted to change email to: " + newEmail);

        if (simulateError) {
            throw new RuntimeException("Main transaction failed!");
            // User update is ROLLED BACK
            // But audit log is ALREADY COMMITTED (different transaction)
        }
    }

    // ============================================================
    // EXAMPLE 6: Transaction with Timeout
    // ============================================================
    /**
     * Transaction with 5 second timeout.
     * If operation takes longer, transaction is rolled back.
     */
    @Transactional(timeout = 5)
    public User updateUserWithTimeout(Long userId, String newEmail) {
        log.info("Transaction with 5 second timeout");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setEmail(newEmail);

        // If this takes more than 5 seconds, transaction times out
        return userRepository.save(user);
    }

    // ============================================================
    // EXAMPLE 7: Selective Rollback
    // ============================================================
    /**
     * Only rollback for specific exceptions.
     *
     * - RuntimeException: Rolls back (default)
     * - Checked Exception: Does NOT rollback (default)
     * - rollbackFor: Force rollback for specific exceptions
     * - noRollbackFor: Prevent rollback for specific exceptions
     */
    @Transactional(
            rollbackFor = {IllegalArgumentException.class},
            noRollbackFor = {UserNotFoundException.class}
    )
    public User updateUserSelectiveRollback(Long userId, String newEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        // UserNotFoundException does NOT rollback

        if (newEmail == null || newEmail.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
            // IllegalArgumentException DOES rollback
        }

        user.setEmail(newEmail);
        return userRepository.save(user);
    }
}
