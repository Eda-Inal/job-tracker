package com.jobtracker.event.listener;

import com.jobtracker.application.event.JobApplicationCreatedEvent;
import com.jobtracker.application.event.StatusChangedEvent;
import com.jobtracker.application.entity.ApplicationStatus;
import com.jobtracker.interview.event.InterviewScheduledEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationEventListener {

    @Async
    @EventListener
    public void onApplicationCreated(JobApplicationCreatedEvent event) {
        log.info("Application created: userId={}, appId={}, company='{}', position='{}'",
                event.getUserId(), event.getApplicationId(),
                event.getCompanyName(), event.getPosition());
        // TODO 13: send confirmation email
        // TODO 15: create in-app notification
    }

    @Async
    @EventListener
    public void onStatusChanged(StatusChangedEvent event) {
        log.info("Status changed: userId={}, appId={}, '{}' {} → {}",
                event.getUserId(), event.getApplicationId(), event.getCompanyName(),
                event.getPreviousStatus(), event.getNewStatus());
        // TODO 15: create in-app notification for status change
        if (event.getNewStatus() == ApplicationStatus.OFFER) {
            log.info("Offer received at '{}' for userId={}! TODO 13: send celebration email",
                    event.getCompanyName(), event.getUserId());
        }
    }

    @Async
    @EventListener
    public void onInterviewScheduled(InterviewScheduledEvent event) {
        log.info("Interview scheduled: userId={}, interviewId={}, company='{}', type={}, at={}",
                event.getUserId(), event.getInterviewId(), event.getCompanyName(),
                event.getInterviewType(), event.getScheduledAt());
        // TODO 13: send interview reminder email (1 day before, via scheduler)
        // TODO 15: create in-app notification
    }
}
