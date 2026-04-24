package com.jobtracker.interview.dto;

import com.jobtracker.interview.entity.InterviewOutcome;
import com.jobtracker.interview.entity.InterviewType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CreateInterviewRequest {

    @NotNull(message = "Scheduled date/time is required")
    private LocalDateTime scheduledAt;

    private InterviewType type;
    private Integer durationMinutes;
    private String interviewerName;
    private String meetingLink;
    private String notes;
    private InterviewOutcome outcome;
}
