package com.jobtracker.interview.dto;

import com.jobtracker.interview.entity.InterviewOutcome;
import com.jobtracker.interview.entity.InterviewType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PatchInterviewRequest {

    private LocalDateTime scheduledAt;
    private InterviewType type;
    private Integer durationMinutes;
    private String interviewerName;
    private String meetingLink;
    private String notes;
    private InterviewOutcome outcome;
}
