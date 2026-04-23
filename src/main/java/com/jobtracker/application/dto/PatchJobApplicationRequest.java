package com.jobtracker.application.dto;

import com.jobtracker.application.entity.ApplicationSource;
import com.jobtracker.application.entity.ApplicationStatus;
import com.jobtracker.application.entity.WorkType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class PatchJobApplicationRequest {

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
}
