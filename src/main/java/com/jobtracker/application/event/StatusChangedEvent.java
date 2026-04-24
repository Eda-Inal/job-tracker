package com.jobtracker.application.event;

import com.jobtracker.application.entity.ApplicationStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StatusChangedEvent {

    private final Long userId;
    private final Long applicationId;
    private final ApplicationStatus previousStatus;
    private final ApplicationStatus newStatus;
    private final String companyName;
}
