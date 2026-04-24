package com.jobtracker.application.dto;

import com.jobtracker.application.entity.ApplicationSource;
import com.jobtracker.application.entity.ApplicationStatus;
import com.jobtracker.application.entity.WorkType;
import com.jobtracker.tag.dto.TagResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    private List<TagResponse> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
