package com.example.usermanagement.service;

import com.example.usermanagement.repository.AuditLogRepository;
import com.example.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Scheduled Tasks Service demonstrating @Scheduled annotation.
 *
 * SCHEDULING PATTERNS:
 * ====================
 * 1. fixedRate: Execute every X ms from START of previous run
 * 2. fixedDelay: Execute X ms after END of previous run
 * 3. cron: Complex schedules using cron expressions
 *
 * CRON EXPRESSION FORMAT:
 * =======================
 * ┌───────────── second (0-59)
 * │ ┌───────────── minute (0-59)
 * │ │ ┌───────────── hour (0-23)
 * │ │ │ ┌───────────── day of month (1-31)
 * │ │ │ │ ┌───────────── month (1-12 or JAN-DEC)
 * │ │ │ │ │ ┌───────────── day of week (0-7 or SUN-SAT, 0 and 7 are Sunday)
 * │ │ │ │ │ │
 * * * * * * *
 *
 * Special Characters:
 * - * : any value
 * - , : list separator (MON,WED,FRI)
 * - - : range (1-5)
 * - / : step values (0/15 = every 15 starting from 0)
 *
 * COMMON CRON EXAMPLES:
 * =====================
 * "0 0 * * * *"        = Every hour (at minute 0, second 0)
 * "0 0 0 * * *"        = Every day at midnight
 * "0 0 9 * * MON-FRI"  = Weekdays at 9:00 AM
 * "0 0/30 * * * *"     = Every 30 minutes
 * "0 0 0 1 * *"        = First day of every month at midnight
 * "0 0 9,17 * * *"     = Daily at 9:00 AM and 5:00 PM
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Health check - runs every 30 seconds.
     * Uses fixedRate: starts counting from START of previous execution.
     *
     * Good for: Monitoring, heartbeats, status checks
     */
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void healthCheck() {
        String threadName = Thread.currentThread().getName();
        log.debug("[{}] Health check at {}", threadName, LocalDateTime.now().format(formatter));
    }

    /**
     * Cache statistics - runs every 5 minutes with initial delay.
     * Uses fixedDelay: starts counting from END of previous execution.
     *
     * Good for: Tasks where overlap would cause issues
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 60000) // 5 min delay, 1 min initial wait
    public void logCacheStatistics() {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] === Cache Statistics ===", threadName);
        log.info("[{}] Time: {}", threadName, LocalDateTime.now().format(formatter));
        // In production, you'd get actual cache stats from RedisCacheManager
        log.info("[{}] Cache statistics logged", threadName);
    }

    /**
     * User count summary - runs every minute.
     * Demonstrates simple periodic task.
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void logUserCount() {
        String threadName = Thread.currentThread().getName();
        long userCount = userRepository.count();
        log.info("[{}] Current user count: {}", threadName, userCount);
    }

    /**
     * Audit log cleanup - runs daily at 2:00 AM.
     * Uses cron expression for specific time scheduling.
     *
     * Good for: Maintenance tasks, cleanup jobs, report generation
     */
    @Scheduled(cron = "0 0 2 * * *") // Every day at 2:00 AM
    public void cleanupOldAuditLogs() {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] Starting audit log cleanup at {}", threadName, LocalDateTime.now().format(formatter));

        // Delete audit logs older than 90 days
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        // In production: auditLogRepository.deleteByCreatedAtBefore(cutoffDate);

        log.info("[{}] Audit log cleanup completed. Deleted logs older than {}",
                threadName, cutoffDate.format(formatter));
    }

    /**
     * Weekly report - runs every Monday at 9:00 AM.
     */
    @Scheduled(cron = "0 0 9 * * MON") // Every Monday at 9:00 AM
    public void generateWeeklyReport() {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] Generating weekly report at {}", threadName, LocalDateTime.now().format(formatter));

        long totalUsers = userRepository.count();
        long totalAuditLogs = auditLogRepository.count();

        log.info("[{}] === WEEKLY REPORT ===", threadName);
        log.info("[{}] Total Users: {}", threadName, totalUsers);
        log.info("[{}] Total Audit Logs: {}", threadName, totalAuditLogs);
        log.info("[{}] Report generated successfully", threadName);
    }

    /**
     * Database connection check - runs every 10 seconds.
     * Short interval for demo purposes; use longer intervals in production.
     */
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void checkDatabaseConnection() {
        String threadName = Thread.currentThread().getName();
        try {
            // Simple query to check DB connection
            userRepository.count();
            log.debug("[{}] Database connection OK", threadName);
        } catch (Exception e) {
            log.error("[{}] Database connection FAILED: {}", threadName, e.getMessage());
            // In production: trigger alert, attempt reconnection, etc.
        }
    }

    /**
     * End of day summary - runs at 11:59 PM every day.
     */
    @Scheduled(cron = "0 59 23 * * *") // Every day at 11:59 PM
    public void endOfDaySummary() {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] === END OF DAY SUMMARY ===", threadName);
        log.info("[{}] Date: {}", threadName, LocalDateTime.now().format(formatter));
        log.info("[{}] Total users in system: {}", threadName, userRepository.count());
        log.info("[{}] End of day processing complete", threadName);
    }

    /**
     * Demonstrates that long-running tasks don't block others.
     * This task takes 5 seconds but other tasks continue running.
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void simulateLongRunningTask() {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] Starting long-running task at {}", threadName, LocalDateTime.now().format(formatter));

        try {
            // Simulate 5 second task
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[{}] Long-running task interrupted", threadName);
        }

        log.info("[{}] Long-running task completed at {}", threadName, LocalDateTime.now().format(formatter));
    }
}
