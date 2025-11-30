package com.example.usermanagement.presentation.controller;

import com.example.usermanagement.application.dto.user.UserResponseDTO;
import com.example.usermanagement.domain.model.AuditLog;
import com.example.usermanagement.domain.model.User;
import com.example.usermanagement.application.mapper.UserMapper;
import com.example.usermanagement.domain.repository.AuditLogRepository;
import com.example.usermanagement.application.service.TransactionalDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for demonstrating @Transactional behavior.
 * Test these endpoints to see how transactions work.
 */
@RestController
@RequestMapping("/api/demo/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Demo", description = "Endpoints to test @Transactional behavior")
public class TransactionDemoController {

    private final TransactionalDemoService transactionalDemoService;
    private final AuditLogRepository auditLogRepository;
    private final UserMapper userMapper;

    @Operation(summary = "Update user email with audit log",
            description = "Updates user email AND creates audit log in ONE transaction")
    @PutMapping("/users/{id}/email")
    public ResponseEntity<UserResponseDTO> updateEmailWithAudit(
            @PathVariable Long id,
            @RequestParam String newEmail) {
        User user = transactionalDemoService.updateUserEmailWithAudit(id, newEmail);
        return ResponseEntity.ok(userMapper.toResponseDTO(user));
    }

    @Operation(summary = "Test rollback scenario",
            description = "Set simulateError=true to see rollback in action")
    @PutMapping("/users/{id}/email-with-rollback")
    public ResponseEntity<UserResponseDTO> testRollback(
            @PathVariable Long id,
            @RequestParam String newEmail,
            @RequestParam(defaultValue = "false") boolean simulateError) {
        User user = transactionalDemoService.updateUserWithPossibleRollback(id, newEmail, simulateError);
        return ResponseEntity.ok(userMapper.toResponseDTO(user));
    }

    @Operation(summary = "Test REQUIRES_NEW propagation",
            description = "Audit log saves even if main transaction fails")
    @PutMapping("/users/{id}/requires-new")
    public ResponseEntity<String> testRequiresNew(
            @PathVariable Long id,
            @RequestParam String newEmail,
            @RequestParam(defaultValue = "false") boolean simulateError) {
        transactionalDemoService.demonstrateRequiresNew(id, newEmail, simulateError);
        return ResponseEntity.ok("Transaction completed successfully");
    }

    @Operation(summary = "Get audit logs for user")
    @GetMapping("/audit-logs/user/{userId}")
    public ResponseEntity<List<AuditLog>> getAuditLogs(@PathVariable Long userId) {
        List<AuditLog> logs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(logs);
    }

    @Operation(summary = "Get all audit logs")
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        return ResponseEntity.ok(auditLogRepository.findAll());
    }
}
