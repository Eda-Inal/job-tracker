package com.jobtracker.application.dto;

import com.jobtracker.application.entity.ApplicationSource;
import com.jobtracker.application.entity.ApplicationStatus;
import com.jobtracker.application.entity.WorkType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UpdateJobApplicationRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Position is required")
    private String position;

    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    @NotNull(message = "Application date is required")
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
