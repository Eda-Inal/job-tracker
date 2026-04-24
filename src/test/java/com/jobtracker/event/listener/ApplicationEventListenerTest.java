package com.jobtracker.event.listener;

import com.jobtracker.application.entity.ApplicationStatus;
import com.jobtracker.application.event.JobApplicationCreatedEvent;
import com.jobtracker.application.event.StatusChangedEvent;
import com.jobtracker.interview.entity.InterviewType;
import com.jobtracker.interview.event.InterviewScheduledEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class ApplicationEventListenerTest {

    @InjectMocks
    private ApplicationEventListener listener;

    @Test
    void onApplicationCreated_doesNotThrow() {
        JobApplicationCreatedEvent event = new JobApplicationCreatedEvent(1L, 10L, "Google", "SWE");
        assertDoesNotThrow(() -> listener.onApplicationCreated(event));
    }

    @Test
    void onStatusChanged_doesNotThrow() {
        StatusChangedEvent event = new StatusChangedEvent(
                1L, 10L, ApplicationStatus.APPLIED, ApplicationStatus.INTERVIEWING, "Google");
        assertDoesNotThrow(() -> listener.onStatusChanged(event));
    }

    @Test
    void onStatusChanged_offerStatus_doesNotThrow() {
        StatusChangedEvent event = new StatusChangedEvent(
                1L, 10L, ApplicationStatus.INTERVIEWING, ApplicationStatus.OFFER, "Google");
        assertDoesNotThrow(() -> listener.onStatusChanged(event));
    }

    @Test
    void onInterviewScheduled_doesNotThrow() {
        InterviewScheduledEvent event = new InterviewScheduledEvent(
                1L, 100L, 10L, "Google", LocalDateTime.now().plusDays(2), InterviewType.TECHNICAL);
        assertDoesNotThrow(() -> listener.onInterviewScheduled(event));
    }
}
