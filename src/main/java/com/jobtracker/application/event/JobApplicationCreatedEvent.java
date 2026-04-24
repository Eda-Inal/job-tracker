package com.jobtracker.application.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class JobApplicationCreatedEvent {

    private final Long userId;
    private final Long applicationId;
    private final String companyName;
    private final String position;
}
