package com.example.usermanagement.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Async Configuration with Custom ThreadPool Executor.
 *
 * @EnableAsync: Enables Spring's asynchronous method execution capability.
 * Methods annotated with @Async will run in a separate thread.
 *
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    THREAD POOL CONFIGURATION EXPLAINED                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                              ║
 * ║  CORE POOL SIZE (corePoolSize = 5)                                          ║
 * ║  ─────────────────────────────────                                          ║
 * ║  • Minimum threads always kept alive (even when idle)                       ║
 * ║  • These threads handle normal workload                                     ║
 * ║  • Set based on: CPU cores, I/O wait time, expected concurrent tasks        ║
 * ║                                                                              ║
 * ║  MAX POOL SIZE (maxPoolSize = 10)                                           ║
 * ║  ────────────────────────────────                                           ║
 * ║  • Maximum threads that can be created                                      ║
 * ║  • Extra threads created when queue is FULL                                 ║
 * ║  • These extra threads are terminated after keepAliveSeconds                ║
 * ║                                                                              ║
 * ║  QUEUE CAPACITY (queueCapacity = 100)                                       ║
 * ║  ────────────────────────────────────                                       ║
 * ║  • Tasks wait here when all core threads are busy                           ║
 * ║  • Only AFTER queue is full, new threads (up to max) are created            ║
 * ║  • Larger queue = more memory usage, longer wait times                      ║
 * ║                                                                              ║
 * ║  TASK EXECUTION FLOW:                                                       ║
 * ║  ────────────────────                                                       ║
 * ║  1. Task arrives → Core thread available? → Execute immediately             ║
 * ║  2. Core threads busy → Add to queue                                        ║
 * ║  3. Queue full → Create new thread (if < maxPoolSize)                       ║
 * ║  4. Max threads reached + queue full → RejectedExecutionHandler             ║
 * ║                                                                              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * HOW THREAD POOLS AFFECT PERFORMANCE:
 * ====================================
 *
 * 1. TOO FEW THREADS (corePoolSize too small):
 *    - Tasks queue up, increasing response latency
 *    - CPU may be underutilized (especially for I/O-bound tasks)
 *    - Users experience slow responses
 *
 * 2. TOO MANY THREADS (maxPoolSize too large):
 *    - Context switching overhead increases
 *    - Memory consumption grows (each thread ~1MB stack)
 *    - CPU spends more time switching than working
 *    - Diminishing returns: more threads ≠ faster execution
 *
 * 3. QUEUE TOO LARGE:
 *    - Memory consumption increases
 *    - Tasks wait longer before execution
 *    - May hide performance problems (backlog builds up)
 *
 * 4. QUEUE TOO SMALL:
 *    - Tasks rejected more frequently
 *    - More threads created (up to max)
 *    - Higher resource usage spikes
 *
 * SIZING GUIDELINES:
 * ==================
 * • CPU-bound tasks: corePoolSize = CPU cores + 1
 * • I/O-bound tasks: corePoolSize = CPU cores × 2 (or more)
 * • Mixed workload: corePoolSize = CPU cores × (1 + wait_time/compute_time)
 *
 * MONITORING (add actuator):
 * ==========================
 * - Active threads count
 * - Queue size
 * - Completed task count
 * - Rejected task count
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Main executor for general async tasks.
     * Used by default when @Async is used without specifying an executor.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        log.info("Creating Async Task Executor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size: threads always kept alive
        executor.setCorePoolSize(5);

        // Max pool size: maximum threads when queue is full
        executor.setMaxPoolSize(10);

        // Queue capacity: tasks waiting when core threads are busy
        executor.setQueueCapacity(100);

        // Thread name prefix (useful for debugging/logging)
        executor.setThreadNamePrefix("Async-");

        // Keep alive time for threads beyond core pool (seconds)
        executor.setKeepAliveSeconds(60);

        // Allow core threads to time out (saves resources when idle)
        executor.setAllowCoreThreadTimeOut(true);

        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // Max wait time on shutdown (seconds)
        executor.setAwaitTerminationSeconds(30);

        // Rejection policy when queue is full and max threads reached
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();

        log.info("Async Task Executor initialized: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }

    /**
     * Dedicated executor for email/notification tasks.
     * Separate pool prevents email delays from affecting other async tasks.
     */
    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Smaller pool for email tasks (typically I/O bound)
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("Email-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        log.info("Email Executor initialized: corePoolSize={}, maxPoolSize={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }

    /**
     * Dedicated executor for report generation.
     * Reports are CPU-intensive, so we limit concurrent reports.
     */
    @Bean(name = "reportExecutor")
    public Executor reportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Small pool for CPU-intensive report generation
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("Report-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        log.info("Report Executor initialized: corePoolSize={}, maxPoolSize={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }

    /**
     * Default executor for @Async methods without explicit executor name.
     */
    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }

    /**
     * Handle exceptions thrown by async methods.
     * Without this, async exceptions are lost (not propagated to caller).
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

    /**
     * Custom exception handler for async methods.
     * Logs the exception since it can't be caught by the caller.
     */
    @Slf4j
    static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            log.error("Async method '{}' threw exception: {}", method.getName(), ex.getMessage());
            log.error("Method parameters: {}", params);
            log.error("Exception details:", ex);

            // Here you could:
            // - Send alert to monitoring system
            // - Store in database for later analysis
            // - Send notification to admin
        }
    }
}
