package com.jobtracker.interview.event;

import com.jobtracker.interview.entity.InterviewType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class InterviewScheduledEvent {

    private final Long userId;
    private final Long interviewId;
    private final Long applicationId;
    private final String companyName;
    private final LocalDateTime scheduledAt;
    private final InterviewType interviewType;
}
