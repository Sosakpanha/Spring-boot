package com.example.usermanagement.infrastructure.scheduling;

import com.example.usermanagement.domain.model.AuditLog;
import com.example.usermanagement.domain.model.User;
import com.example.usermanagement.domain.repository.AuditLogRepository;
import com.example.usermanagement.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Async Service demonstrating @Async annotation usage.
 *
 * @Async BEHAVIOR:
 * ================
 * - Method runs in a SEPARATE THREAD (from thread pool)
 * - Caller does NOT wait for completion (fire-and-forget OR CompletableFuture)
 * - Main thread continues immediately after calling async method
 *
 * IMPORTANT RULES:
 * ================
 * 1. @Async methods must be PUBLIC
 * 2. @Async methods must be called from OUTSIDE the class (proxy limitation)
 * 3. Self-invocation (this.asyncMethod()) will NOT work - runs synchronously
 * 4. Return void or CompletableFuture<T> (not regular objects)
 *
 * USE CASES:
 * ==========
 * - Sending emails (don't block user request)
 * - Writing audit logs
 * - Generating reports
 * - Calling external APIs
 * - Processing uploaded files
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncUserService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    /**
     * Fire-and-forget async method (returns void).
     * Caller doesn't wait for completion or result.
     *
     * Uses default "taskExecutor" bean.
     */
    @Async
    public void sendWelcomeEmail(User user) {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] Sending welcome email to: {}", threadName, user.getEmail());

        try {
            // Simulate email sending delay
            TimeUnit.SECONDS.sleep(3);
            log.info("[{}] Welcome email sent successfully to: {}", threadName, user.getEmail());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[{}] Email sending interrupted for: {}", threadName, user.getEmail());
        }
    }

    /**
     * Async method using specific executor (emailExecutor).
     * Useful when you want dedicated thread pool for certain tasks.
     */
    @Async("emailExecutor")
    public void sendPasswordResetEmail(String email, String resetToken) {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] Sending password reset email to: {}", threadName, email);

        try {
            // Simulate email sending
            TimeUnit.SECONDS.sleep(2);
            log.info("[{}] Password reset email sent to: {}", threadName, email);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[{}] Password reset email interrupted for: {}", threadName, email);
        }
    }

    /**
     * Async method that returns a result via CompletableFuture.
     * Caller can:
     * - Block and wait: future.get()
     * - Chain callbacks: future.thenApply(...)
     * - Combine multiple futures: CompletableFuture.allOf(...)
     */
    @Async
    public CompletableFuture<List<User>> findAllUsersAsync() {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] Starting async user retrieval", threadName);

        try {
            // Simulate slow database query
            TimeUnit.SECONDS.sleep(2);
            List<User> users = userRepository.findAll();
            log.info("[{}] Found {} users asynchronously", threadName, users.size());
            return CompletableFuture.completedFuture(users);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Async report generation using dedicated reportExecutor.
     * Returns CompletableFuture so caller can track completion.
     */
    @Async("reportExecutor")
    public CompletableFuture<String> generateUserReport() {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] Starting user report generation", threadName);

        try {
            // Simulate CPU-intensive report generation
            TimeUnit.SECONDS.sleep(5);

            List<User> users = userRepository.findAll();
            StringBuilder report = new StringBuilder();
            report.append("=== USER REPORT ===\n");
            report.append("Generated at: ").append(LocalDateTime.now()).append("\n");
            report.append("Total users: ").append(users.size()).append("\n");
            report.append("==================\n");

            for (User user : users) {
                report.append(String.format("- %s %s (%s)\n",
                        user.getFirstName(), user.getLastName(), user.getEmail()));
            }

            log.info("[{}] User report generated successfully", threadName);
            return CompletableFuture.completedFuture(report.toString());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Async audit logging - don't slow down main request.
     */
    @Async
    public void logUserActivityAsync(Long userId, String action, String details) {
        String threadName = Thread.currentThread().getName();
        log.debug("[{}] Logging activity for user {}: {}", threadName, userId, action);

        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .details(details)
                .createdAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.info("[{}] Audit log saved for user {}: {}", threadName, userId, action);
    }

    /**
     * Demonstrate combining multiple async operations.
     * All operations run in parallel, then combine results.
     */
    @Async
    public CompletableFuture<String> processUserDataAsync(Long userId) {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] Processing user data for ID: {}", threadName, userId);

        return CompletableFuture.supplyAsync(() -> {
            // This would normally call external services in parallel
            return "Processed data for user: " + userId;
        });
    }
}
