package com.jobtracker.interview.dto;

import com.jobtracker.interview.entity.InterviewOutcome;
import com.jobtracker.interview.entity.InterviewType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InterviewResponse {

    private Long id;
    private Long jobApplicationId;
    private LocalDateTime scheduledAt;
    private InterviewType type;
    private Integer durationMinutes;
    private String interviewerName;
    private String meetingLink;
    private String notes;
    private InterviewOutcome outcome;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
