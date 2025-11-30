package com.example.usermanagement.domain.repository;

import com.example.usermanagement.domain.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for AuditLog entity.
 *
 * Part of the DOMAIN LAYER - defines the contract for audit log data access.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find all audit logs for a specific user, ordered by creation time (newest first).
     */
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find all audit logs by action type.
     */
    List<AuditLog> findByAction(String action);
}
