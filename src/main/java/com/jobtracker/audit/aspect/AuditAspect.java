package com.jobtracker.audit.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtracker.audit.annotation.Auditable;
import com.jobtracker.audit.entity.AuditAction;
import com.jobtracker.audit.entity.AuditLog;
import com.jobtracker.audit.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        Object result;
        try {
            result = pjp.proceed();
        } catch (Throwable t) {
            // Only audit successful operations
            throw t;
        }

        try {
            persistAuditLog(auditable, pjp.getArgs(), result);
        } catch (Exception e) {
            log.warn("Failed to save audit log for {} {}: {}",
                    auditable.action(), auditable.entityType(), e.getMessage());
        }

        return result;
    }

    private void persistAuditLog(Auditable auditable, Object[] args, Object result) {
        AuditLog auditLog = AuditLog.builder()
                .userId(extractUserId(args))
                .entityType(auditable.entityType())
                .entityId(resolveEntityId(auditable.action(), args, result))
                .action(auditable.action())
                .newValue(serializeNewValue(auditable.action(), result))
                .ipAddress(resolveIpAddress())
                .build();

        auditLogRepository.save(auditLog);
    }

    private Long extractUserId(Object[] args) {
        if (args.length > 0 && args[0] instanceof Long id) return id;
        return null;
    }

    private Long resolveEntityId(AuditAction action, Object[] args, Object result) {
        if (action == AuditAction.CREATE && result != null) {
            try {
                JsonNode node = objectMapper.valueToTree(result);
                if (node.has("id") && !node.get("id").isNull()) {
                    return node.get("id").asLong();
                }
            } catch (Exception ignored) {}
        }
        // For UPDATE / DELETE: entity id is the second argument
        if (args.length > 1 && args[1] instanceof Long id) return id;
        return null;
    }

    private String serializeNewValue(AuditAction action, Object result) {
        if (action == AuditAction.DELETE || result == null) return null;
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            log.debug("Could not serialize audit new value: {}", e.getMessage());
            return null;
        }
    }

    private String resolveIpAddress() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
