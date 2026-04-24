package com.jobtracker.audit.repository;

import com.jobtracker.audit.entity.AuditAction;
import com.jobtracker.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    List<AuditLog> findByUserIdAndEntityTypeAndActionOrderByCreatedAtDesc(
            Long userId, String entityType, AuditAction action);
}
