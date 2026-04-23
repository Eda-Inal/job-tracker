package com.jobtracker.application.service;

import com.jobtracker.application.dto.CreateJobApplicationRequest;
import com.jobtracker.application.dto.JobApplicationResponse;
import com.jobtracker.application.dto.PatchJobApplicationRequest;
import com.jobtracker.application.dto.UpdateJobApplicationRequest;
import com.jobtracker.application.dto.JobApplicationFilter;
import com.jobtracker.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface JobApplicationService {

    PageResponse<JobApplicationResponse> getAll(Long userId, JobApplicationFilter filter, Pageable pageable);

    JobApplicationResponse getById(Long userId, Long id);

    JobApplicationResponse create(Long userId, CreateJobApplicationRequest request);

    JobApplicationResponse update(Long userId, Long id, UpdateJobApplicationRequest request);

    JobApplicationResponse patch(Long userId, Long id, PatchJobApplicationRequest request);

    void delete(Long userId, Long id);

    JobApplicationResponse restore(Long userId, Long id);

    PageResponse<JobApplicationResponse> getTrash(Long userId, Pageable pageable);
}
