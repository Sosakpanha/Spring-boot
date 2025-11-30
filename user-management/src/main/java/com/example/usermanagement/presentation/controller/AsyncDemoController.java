package com.example.usermanagement.presentation.controller;

import com.example.usermanagement.domain.model.User;
import com.example.usermanagement.infrastructure.scheduling.AsyncUserService;
import com.example.usermanagement.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Controller demonstrating @Async behavior.
 * Test these endpoints to see how async methods work.
 */
@RestController
@RequestMapping("/api/demo/async")
@RequiredArgsConstructor
@Tag(name = "Async Demo", description = "Endpoints to test @Async behavior")
@Slf4j
public class AsyncDemoController {

    private final AsyncUserService asyncUserService;
    private final UserService userService;

    @Operation(summary = "Test fire-and-forget async",
            description = "Sends welcome email asynchronously. Returns immediately while email sends in background.")
    @PostMapping("/send-email/{userId}")
    public ResponseEntity<Map<String, Object>> sendWelcomeEmail(@PathVariable Long userId) {
        String mainThread = Thread.currentThread().getName();
        log.info("[{}] Received request to send welcome email for user {}", mainThread, userId);

        User user = userService.getUserById(userId);

        // This returns IMMEDIATELY - email sends in background
        asyncUserService.sendWelcomeEmail(user);

        log.info("[{}] Controller returning response (email sending in background)", mainThread);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Email sending initiated");
        response.put("userId", userId);
        response.put("note", "Check server logs to see async execution");
        response.put("mainThread", mainThread);

        return ResponseEntity.accepted().body(response);
    }

    @Operation(summary = "Test async with CompletableFuture",
            description = "Fetches users asynchronously and waits for result")
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsersAsync()
            throws ExecutionException, InterruptedException, TimeoutException {

        String mainThread = Thread.currentThread().getName();
        log.info("[{}] Starting async user fetch", mainThread);

        long startTime = System.currentTimeMillis();

        // Start async operation
        CompletableFuture<List<User>> futureUsers = asyncUserService.findAllUsersAsync();

        // Do other work while waiting (in real app)
        log.info("[{}] Doing other work while async operation runs...", mainThread);

        // Wait for result (with timeout)
        List<User> users = futureUsers.get(10, TimeUnit.SECONDS);

        long duration = System.currentTimeMillis() - startTime;

        Map<String, Object> response = new HashMap<>();
        response.put("userCount", users.size());
        response.put("executionTimeMs", duration);
        response.put("mainThread", mainThread);
        response.put("note", "Check logs to see async thread name");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Generate report asynchronously",
            description = "Starts report generation in background and returns immediately")
    @PostMapping("/generate-report")
    public ResponseEntity<Map<String, Object>> generateReport() {
        String mainThread = Thread.currentThread().getName();
        log.info("[{}] Report generation requested", mainThread);

        // Start report generation (non-blocking)
        CompletableFuture<String> futureReport = asyncUserService.generateUserReport();

        // Add callback for when report is ready
        futureReport.thenAccept(report -> {
            log.info("Report ready! First 100 chars: {}",
                    report.substring(0, Math.min(100, report.length())));
        });

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Report generation started");
        response.put("status", "PROCESSING");
        response.put("note", "In real app, you'd store a job ID and provide status endpoint");

        return ResponseEntity.accepted().body(response);
    }

    @Operation(summary = "Test parallel async operations",
            description = "Demonstrates running multiple async operations in parallel")
    @GetMapping("/parallel-demo")
    public ResponseEntity<Map<String, Object>> parallelDemo()
            throws ExecutionException, InterruptedException, TimeoutException {

        String mainThread = Thread.currentThread().getName();
        log.info("[{}] Starting parallel async operations", mainThread);

        long startTime = System.currentTimeMillis();

        // Start multiple async operations in parallel
        CompletableFuture<List<User>> usersFuture = asyncUserService.findAllUsersAsync();
        CompletableFuture<String> reportFuture = asyncUserService.generateUserReport();

        // Wait for ALL to complete
        CompletableFuture.allOf(usersFuture, reportFuture).get(15, TimeUnit.SECONDS);

        long duration = System.currentTimeMillis() - startTime;

        // Get results
        List<User> users = usersFuture.get();
        String report = reportFuture.get();

        Map<String, Object> response = new HashMap<>();
        response.put("userCount", users.size());
        response.put("reportLength", report.length());
        response.put("totalExecutionTimeMs", duration);
        response.put("note", "Both operations ran in parallel, not sequentially");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Log activity asynchronously",
            description = "Logs user activity in background without blocking")
    @PostMapping("/log-activity/{userId}")
    public ResponseEntity<Map<String, Object>> logActivity(
            @PathVariable Long userId,
            @RequestParam String action) {

        String mainThread = Thread.currentThread().getName();
        log.info("[{}] Logging activity for user {}", mainThread, userId);

        // Fire-and-forget - don't wait for logging to complete
        asyncUserService.logUserActivityAsync(userId, action, "Activity logged via API");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Activity logging initiated");
        response.put("userId", userId);
        response.put("action", action);

        return ResponseEntity.accepted().body(response);
    }
}
