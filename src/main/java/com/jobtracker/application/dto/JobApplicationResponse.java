package com.jobtracker.application.dto;

import com.jobtracker.application.entity.ApplicationSource;
import com.jobtracker.application.entity.ApplicationStatus;
import com.jobtracker.application.entity.WorkType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class JobApplicationResponse {

    private Long id;
    private Long userId;
    private String companyName;
    private String position;
    private ApplicationStatus status;
    private LocalDate applicationDate;
    private String jobUrl;
    private String location;
    private WorkType workType;
    private Integer salaryMin;
    private Integer salaryMax;
    private String currency;
    private String notes;
    private ApplicationSource source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
