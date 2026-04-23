package com.jobtracker.application.dto;

import com.jobtracker.application.entity.ApplicationStatus;

import java.time.LocalDate;

public record JobApplicationFilter(
        String search,
        ApplicationStatus status,
        LocalDate startDate,
        LocalDate endDate,
        String location
) {}
