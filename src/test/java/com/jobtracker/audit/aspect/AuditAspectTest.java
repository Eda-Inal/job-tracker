package com.jobtracker.audit.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtracker.application.dto.JobApplicationResponse;
import com.jobtracker.application.entity.ApplicationStatus;
import com.jobtracker.audit.annotation.Auditable;
import com.jobtracker.audit.entity.AuditAction;
import com.jobtracker.audit.entity.AuditLog;
import com.jobtracker.audit.repository.AuditLogRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Spy  private ObjectMapper objectMapper;
    @Mock private ProceedingJoinPoint pjp;
    @Mock private Auditable auditable;

    @InjectMocks private AuditAspect auditAspect;

    private JobApplicationResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = JobApplicationResponse.builder()
                .id(42L).userId(1L)
                .companyName("Google").position("SWE")
                .status(ApplicationStatus.APPLIED)
                .applicationDate(LocalDate.now())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    // --- CREATE ---

    @Test
    void audit_create_savesLogWithEntityIdFromReturnValue() throws Throwable {
        given(auditable.entityType()).willReturn("JobApplication");
        given(auditable.action()).willReturn(AuditAction.CREATE);
        given(pjp.getArgs()).willReturn(new Object[]{1L, new Object()});
        given(pjp.proceed()).willReturn(sampleResponse);

        auditAspect.audit(pjp, auditable);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getEntityType()).isEqualTo("JobApplication");
        assertThat(saved.getAction()).isEqualTo(AuditAction.CREATE);
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getEntityId()).isEqualTo(42L);
        assertThat(saved.getNewValue()).isNotNull();
        assertThat(saved.getOldValue()).isNull();
    }

    // --- UPDATE ---

    @Test
    void audit_update_savesLogWithEntityIdFromArgs() throws Throwable {
        given(auditable.entityType()).willReturn("JobApplication");
        given(auditable.action()).willReturn(AuditAction.UPDATE);
        given(pjp.getArgs()).willReturn(new Object[]{1L, 99L, new Object()});
        given(pjp.proceed()).willReturn(sampleResponse);

        auditAspect.audit(pjp, auditable);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getAction()).isEqualTo(AuditAction.UPDATE);
        assertThat(saved.getEntityId()).isEqualTo(99L);
        assertThat(saved.getNewValue()).isNotNull();
    }

    // --- DELETE ---

    @Test
    void audit_delete_savesLogWithNoNewValue() throws Throwable {
        given(auditable.entityType()).willReturn("Interview");
        given(auditable.action()).willReturn(AuditAction.DELETE);
        given(pjp.getArgs()).willReturn(new Object[]{1L, 77L});
        given(pjp.proceed()).willReturn(null);

        auditAspect.audit(pjp, auditable);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getEntityType()).isEqualTo("Interview");
        assertThat(saved.getAction()).isEqualTo(AuditAction.DELETE);
        assertThat(saved.getEntityId()).isEqualTo(77L);
        assertThat(saved.getNewValue()).isNull();
    }

    // --- Error propagation ---

    @Test
    void audit_methodThrows_exceptionPropagatedAndNoLogSaved() throws Throwable {
        given(auditable.entityType()).willReturn("JobApplication");
        given(auditable.action()).willReturn(AuditAction.CREATE);
        given(pjp.getArgs()).willReturn(new Object[]{1L});
        given(pjp.proceed()).willThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> auditAspect.audit(pjp, auditable))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");

        verify(auditLogRepository, never()).save(any());
    }

    // --- Audit failure resilience ---

    @Test
    void audit_saveThrows_mainResultStillReturned() throws Throwable {
        given(auditable.entityType()).willReturn("JobApplication");
        given(auditable.action()).willReturn(AuditAction.CREATE);
        given(pjp.getArgs()).willReturn(new Object[]{1L, new Object()});
        given(pjp.proceed()).willReturn(sampleResponse);
        given(auditLogRepository.save(any())).willThrow(new RuntimeException("DB unavailable"));

        Object result = auditAspect.audit(pjp, auditable);

        assertThat(result).isEqualTo(sampleResponse);
    }

    // --- userId extraction ---

    @Test
    void audit_firstArgIsUserId_recordedCorrectly() throws Throwable {
        given(auditable.entityType()).willReturn("JobApplication");
        given(auditable.action()).willReturn(AuditAction.UPDATE);
        given(pjp.getArgs()).willReturn(new Object[]{55L, 10L});
        given(pjp.proceed()).willReturn(sampleResponse);

        auditAspect.audit(pjp, auditable);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertThat(captor.getValue().getUserId()).isEqualTo(55L);
    }
}
