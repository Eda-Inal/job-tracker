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
import com.jobtracker.application.event.JobApplicationCreatedEvent;
import com.jobtracker.application.event.StatusChangedEvent;
import com.jobtracker.audit.annotation.Auditable;
import com.jobtracker.audit.entity.AuditAction;
import com.jobtracker.common.dto.PageResponse;
import com.jobtracker.common.exception.ResourceNotFoundException;
import com.jobtracker.user.entity.User;
import com.jobtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

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
    @Auditable(entityType = "JobApplication", action = AuditAction.CREATE)
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

        JobApplicationResponse response = applicationMapper.toResponse(applicationRepository.save(application));
        eventPublisher.publishEvent(new JobApplicationCreatedEvent(
                userId, response.getId(), response.getCompanyName(), response.getPosition()));
        return response;
    }

    @Override
    @Transactional
    @Auditable(entityType = "JobApplication", action = AuditAction.UPDATE)
    public JobApplicationResponse update(Long userId, Long id, UpdateJobApplicationRequest request) {
        JobApplication application = findOrThrow(userId, id);
        ApplicationStatus previousStatus = application.getStatus();
        applicationMapper.updateFromRequest(request, application);
        JobApplicationResponse response = applicationMapper.toResponse(applicationRepository.save(application));
        if (!request.getStatus().equals(previousStatus)) {
            eventPublisher.publishEvent(new StatusChangedEvent(
                    userId, id, previousStatus, request.getStatus(), application.getCompanyName()));
        }
        return response;
    }

    @Override
    @Transactional
    @Auditable(entityType = "JobApplication", action = AuditAction.UPDATE)
    public JobApplicationResponse patch(Long userId, Long id, PatchJobApplicationRequest request) {
        JobApplication application = findOrThrow(userId, id);
        ApplicationStatus previousStatus = application.getStatus();
        applicationMapper.patchFromRequest(request, application);
        JobApplicationResponse response = applicationMapper.toResponse(applicationRepository.save(application));
        if (request.getStatus() != null && !request.getStatus().equals(previousStatus)) {
            eventPublisher.publishEvent(new StatusChangedEvent(
                    userId, id, previousStatus, request.getStatus(), application.getCompanyName()));
        }
        return response;
    }

    @Override
    @Transactional
    @Auditable(entityType = "JobApplication", action = AuditAction.DELETE)
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
