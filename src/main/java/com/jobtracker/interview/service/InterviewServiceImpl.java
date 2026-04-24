package com.jobtracker.interview.service;

import com.jobtracker.application.entity.JobApplication;
import com.jobtracker.application.repository.JobApplicationRepository;
import com.jobtracker.audit.annotation.Auditable;
import com.jobtracker.audit.entity.AuditAction;
import com.jobtracker.common.exception.ResourceNotFoundException;
import com.jobtracker.interview.dto.CreateInterviewRequest;
import com.jobtracker.interview.dto.InterviewResponse;
import com.jobtracker.interview.dto.PatchInterviewRequest;
import com.jobtracker.interview.entity.Interview;
import com.jobtracker.interview.entity.InterviewOutcome;
import com.jobtracker.interview.event.InterviewScheduledEvent;
import com.jobtracker.interview.mapper.InterviewMapper;
import com.jobtracker.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final JobApplicationRepository applicationRepository;
    private final InterviewMapper interviewMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<InterviewResponse> getAllByApplication(Long userId, Long appId) {
        if (!applicationRepository.existsByIdAndUserId(appId, userId)) {
            throw new ResourceNotFoundException("JobApplication", appId);
        }
        return interviewRepository
                .findByJobApplicationIdAndJobApplicationUserIdOrderByScheduledAtAsc(appId, userId)
                .stream().map(interviewMapper::toResponse).toList();
    }

    @Override
    @Transactional
    @Auditable(entityType = "Interview", action = AuditAction.CREATE)
    public InterviewResponse create(Long userId, Long appId, CreateInterviewRequest request) {
        JobApplication application = applicationRepository.findByIdAndUserId(appId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", appId));

        Interview interview = interviewMapper.toEntity(request);
        interview.setJobApplication(application);

        if (interview.getOutcome() == null) {
            interview.setOutcome(InterviewOutcome.PENDING);
        }

        InterviewResponse response = interviewMapper.toResponse(interviewRepository.save(interview));
        eventPublisher.publishEvent(new InterviewScheduledEvent(
                userId, response.getId(), appId,
                application.getCompanyName(), response.getScheduledAt(), response.getType()));
        return response;
    }

    @Override
    public InterviewResponse getById(Long userId, Long id) {
        return interviewMapper.toResponse(findOrThrow(userId, id));
    }

    @Override
    @Transactional
    @Auditable(entityType = "Interview", action = AuditAction.UPDATE)
    public InterviewResponse patch(Long userId, Long id, PatchInterviewRequest request) {
        Interview interview = findOrThrow(userId, id);
        interviewMapper.patchFromRequest(request, interview);
        return interviewMapper.toResponse(interviewRepository.save(interview));
    }

    @Override
    @Transactional
    @Auditable(entityType = "Interview", action = AuditAction.DELETE)
    public void delete(Long userId, Long id) {
        if (interviewRepository.findByIdAndJobApplicationUserId(id, userId).isEmpty()) {
            throw new ResourceNotFoundException("Interview", id);
        }
        interviewRepository.deleteById(id);
    }

    @Override
    public List<InterviewResponse> getUpcoming(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return interviewRepository.findUpcoming(userId, now, now.plusDays(7))
                .stream().map(interviewMapper::toResponse).toList();
    }

    private Interview findOrThrow(Long userId, Long id) {
        return interviewRepository.findByIdAndJobApplicationUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", id));
    }
}
