package com.jobtracker.application.service;

import com.jobtracker.application.dto.CreateJobApplicationRequest;
import com.jobtracker.application.dto.JobApplicationFilter;
import com.jobtracker.application.dto.JobApplicationResponse;
import com.jobtracker.application.dto.PatchJobApplicationRequest;
import com.jobtracker.application.dto.UpdateJobApplicationRequest;
import com.jobtracker.application.entity.ApplicationStatus;
import com.jobtracker.application.entity.JobApplication;
import com.jobtracker.application.mapper.JobApplicationMapper;
import com.jobtracker.application.repository.JobApplicationRepository;
import com.jobtracker.application.specification.JobApplicationSpecification;
import com.jobtracker.common.dto.PageResponse;
import com.jobtracker.common.exception.ResourceNotFoundException;
import com.jobtracker.user.entity.User;
import com.jobtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobApplicationMapper applicationMapper;

    @Override
    public PageResponse<JobApplicationResponse> getAll(Long userId, JobApplicationFilter filter, Pageable pageable) {
        Specification<JobApplication> spec = JobApplicationSpecification.withFilter(userId, filter);
        return PageResponse.of(applicationRepository.findAll(spec, pageable).map(applicationMapper::toResponse));
    }

    @Override
    public JobApplicationResponse getById(Long userId, Long id) {
        return applicationMapper.toResponse(findOrThrow(userId, id));
    }

    @Override
    @Transactional
    public JobApplicationResponse create(Long userId, CreateJobApplicationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        JobApplication application = applicationMapper.toEntity(request);
        application.setUser(user);

        if (application.getStatus() == null) {
            application.setStatus(ApplicationStatus.APPLIED);
        }
        if (application.getCurrency() == null) {
            application.setCurrency("TRY");
        }

        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    @Override
    @Transactional
    public JobApplicationResponse update(Long userId, Long id, UpdateJobApplicationRequest request) {
        JobApplication application = findOrThrow(userId, id);
        applicationMapper.updateFromRequest(request, application);
        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    @Override
    @Transactional
    public JobApplicationResponse patch(Long userId, Long id, PatchJobApplicationRequest request) {
        JobApplication application = findOrThrow(userId, id);
        applicationMapper.patchFromRequest(request, application);
        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        if (!applicationRepository.existsByIdAndUserId(id, userId)) {
            throw new ResourceNotFoundException("JobApplication", id);
        }
        applicationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public JobApplicationResponse restore(Long userId, Long id) {
        JobApplication application = applicationRepository.findTrashedByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", id));

        application.setDeleted(false);
        application.setDeletedAt(null);

        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    @Override
    public PageResponse<JobApplicationResponse> getTrash(Long userId, Pageable pageable) {
        return PageResponse.of(
                applicationRepository.findTrashedByUserId(userId, pageable).map(applicationMapper::toResponse));
    }

    private JobApplication findOrThrow(Long userId, Long id) {
        return applicationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", id));
    }
}
