package com.example.usermanagement.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Scheduler Configuration for @Scheduled tasks.
 *
 * @EnableScheduling: Enables Spring's scheduled task execution capability.
 * Methods annotated with @Scheduled will run according to their schedule.
 *
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    @SCHEDULED ANNOTATION OPTIONS                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                              ║
 * ║  1. FIXED RATE (fixedRate = 5000)                                           ║
 * ║     • Runs every X milliseconds from START of previous execution            ║
 * ║     • May cause overlap if task takes longer than interval                  ║
 * ║     • Example: Task takes 3s, fixedRate=5s → next starts at 5s mark         ║
 * ║                                                                              ║
 * ║  2. FIXED DELAY (fixedDelay = 5000)                                         ║
 * ║     • Runs X milliseconds after END of previous execution                   ║
 * ║     • No overlap possible                                                   ║
 * ║     • Example: Task takes 3s, fixedDelay=5s → next starts at 8s mark        ║
 * ║                                                                              ║
 * ║  3. CRON EXPRESSION (cron = "0 0 * * * *")                                  ║
 * ║     • Complex schedules (daily at midnight, weekdays only, etc.)            ║
 * ║     • Format: second minute hour day-of-month month day-of-week             ║
 * ║     • Examples:                                                              ║
 * ║       "0 0 0 * * *"     = Every day at midnight                             ║
 * ║       "0 0 9 * * MON"   = Every Monday at 9:00 AM                           ║
 * ║       "0 0/30 * * * *"  = Every 30 minutes                                  ║
 * ║       "0 0 9-17 * * MON-FRI" = Every hour 9AM-5PM on weekdays               ║
 * ║                                                                              ║
 * ║  4. INITIAL DELAY (initialDelay = 10000)                                    ║
 * ║     • Wait X milliseconds before first execution                            ║
 * ║     • Use with fixedRate or fixedDelay                                      ║
 * ║                                                                              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * WHY CUSTOM SCHEDULER?
 * =====================
 * Default scheduler uses single thread! This means:
 * - If Task A takes 10 seconds, Task B is blocked for 10 seconds
 * - Long-running tasks delay ALL other scheduled tasks
 *
 * Custom ThreadPoolTaskScheduler with multiple threads:
 * - Tasks run in parallel
 * - One slow task doesn't block others
 * - Better resource utilization
 */
@Configuration
@EnableScheduling
@Slf4j
public class SchedulerConfig implements SchedulingConfigurer {

    /**
     * Custom scheduler with thread pool for running scheduled tasks.
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // Number of threads for scheduled tasks
        // Set based on number of concurrent scheduled tasks expected
        scheduler.setPoolSize(5);

        // Thread name prefix for easy identification in logs
        scheduler.setThreadNamePrefix("Scheduled-");

        // Handle errors in scheduled tasks
        scheduler.setErrorHandler(throwable -> {
            log.error("Error in scheduled task: {}", throwable.getMessage(), throwable);
        });

        // Wait for tasks to complete on shutdown
        scheduler.setWaitForTasksToCompleteOnShutdown(true);

        // Max wait time on shutdown (seconds)
        scheduler.setAwaitTerminationSeconds(30);

        // Remove cancelled tasks from queue immediately
        scheduler.setRemoveOnCancelPolicy(true);

        scheduler.initialize();

        log.info("Task Scheduler initialized with pool size: {}", scheduler.getPoolSize());
        return scheduler;
    }

    /**
     * Configure the ScheduledTaskRegistrar with our custom scheduler.
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler());
    }
}
