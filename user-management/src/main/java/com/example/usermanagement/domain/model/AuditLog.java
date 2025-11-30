package com.example.usermanagement.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Domain Entity for tracking user actions (audit trail).
 *
 * Part of the DOMAIN LAYER - represents audit log entries.
 *
 * Used for:
 * - Tracking user activities
 * - Demonstrating @Transactional with multiple tables
 * - Compliance and security auditing
 */
@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 500)
    private String details;

    @Column(name = "old_value", length = 1000)
    private String oldValue;

    @Column(name = "new_value", length = 1000)
    private String newValue;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ==================== BUSINESS METHODS ====================

    /**
     * Check if this is a critical action that needs attention.
     */
    public boolean isCriticalAction() {
        return "DELETE".equalsIgnoreCase(action) ||
                "PASSWORD_CHANGE".equalsIgnoreCase(action) ||
                "ROLE_CHANGE".equalsIgnoreCase(action);
    }
}
