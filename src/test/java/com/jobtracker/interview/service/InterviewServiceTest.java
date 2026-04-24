package com.jobtracker.interview.service;

import com.jobtracker.application.entity.JobApplication;
import com.jobtracker.application.repository.JobApplicationRepository;
import com.jobtracker.common.exception.ResourceNotFoundException;
import com.jobtracker.interview.dto.CreateInterviewRequest;
import com.jobtracker.interview.dto.InterviewResponse;
import com.jobtracker.interview.dto.PatchInterviewRequest;
import com.jobtracker.interview.entity.Interview;
import com.jobtracker.interview.entity.InterviewOutcome;
import com.jobtracker.interview.event.InterviewScheduledEvent;
import com.jobtracker.interview.entity.InterviewType;
import com.jobtracker.interview.mapper.InterviewMapper;
import com.jobtracker.interview.repository.InterviewRepository;
import com.jobtracker.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock private InterviewRepository interviewRepository;
    @Mock private JobApplicationRepository applicationRepository;
    @Mock private InterviewMapper interviewMapper;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private InterviewServiceImpl interviewService;

    private User user;
    private JobApplication application;
    private Interview interview;
    private InterviewResponse response;

    @BeforeEach
    void setUp() {
        user = User.builder().build();
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", 1L);

        application = JobApplication.builder().user(user).companyName("Google").position("SWE").build();
        org.springframework.test.util.ReflectionTestUtils.setField(application, "id", 10L);

        interview = Interview.builder()
                .jobApplication(application)
                .scheduledAt(LocalDateTime.now().plusDays(2))
                .type(InterviewType.TECHNICAL)
                .outcome(InterviewOutcome.PENDING)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(interview, "id", 100L);

        response = InterviewResponse.builder()
                .id(100L)
                .jobApplicationId(10L)
                .type(InterviewType.TECHNICAL)
                .outcome(InterviewOutcome.PENDING)
                .build();
    }

    // --- getAllByApplication ---

    @Test
    void getAllByApplication_applicationBelongsToUser_returnsList() {
        given(applicationRepository.existsByIdAndUserId(10L, 1L)).willReturn(true);
        given(interviewRepository.findByJobApplicationIdAndJobApplicationUserIdOrderByScheduledAtAsc(10L, 1L))
                .willReturn(List.of(interview));
        given(interviewMapper.toResponse(interview)).willReturn(response);

        List<InterviewResponse> result = interviewService.getAllByApplication(1L, 10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getJobApplicationId()).isEqualTo(10L);
    }

    @Test
    void getAllByApplication_applicationNotFound_throwsResourceNotFoundException() {
        given(applicationRepository.existsByIdAndUserId(10L, 1L)).willReturn(false);

        assertThatThrownBy(() -> interviewService.getAllByApplication(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- create ---

    @Test
    void create_validRequest_savesAndReturnsInterview() {
        CreateInterviewRequest request = new CreateInterviewRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "scheduledAt", LocalDateTime.now().plusDays(3));
        org.springframework.test.util.ReflectionTestUtils.setField(request, "type", InterviewType.HR);

        given(applicationRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(application));
        given(interviewMapper.toEntity(request)).willReturn(interview);
        given(interviewRepository.save(interview)).willReturn(interview);
        given(interviewMapper.toResponse(interview)).willReturn(response);

        InterviewResponse result = interviewService.create(1L, 10L, request);

        assertThat(result).isNotNull();
        verify(interviewRepository).save(interview);
        ArgumentCaptor<InterviewScheduledEvent> eventCaptor = ArgumentCaptor.forClass(InterviewScheduledEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getUserId()).isEqualTo(1L);
        assertThat(eventCaptor.getValue().getApplicationId()).isEqualTo(10L);
    }

    @Test
    void create_applicationNotFound_throwsResourceNotFoundException() {
        CreateInterviewRequest request = new CreateInterviewRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "scheduledAt", LocalDateTime.now().plusDays(1));

        given(applicationRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.create(1L, 10L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(interviewRepository, never()).save(any());
    }

    @Test
    void create_outcomeNullInMapper_defaultsToPending() {
        CreateInterviewRequest request = new CreateInterviewRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "scheduledAt", LocalDateTime.now().plusDays(1));

        Interview noOutcome = Interview.builder()
                .jobApplication(application)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .outcome(null)  // override @Builder.Default to simulate mapper returning null outcome
                .build();

        given(applicationRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(application));
        given(interviewMapper.toEntity(request)).willReturn(noOutcome);
        given(interviewRepository.save(noOutcome)).willReturn(noOutcome);
        given(interviewMapper.toResponse(noOutcome)).willReturn(response);

        interviewService.create(1L, 10L, request);

        assertThat(noOutcome.getOutcome()).isEqualTo(InterviewOutcome.PENDING);
    }

    // --- getById ---

    @Test
    void getById_ownedByUser_returnsInterview() {
        given(interviewRepository.findByIdAndJobApplicationUserId(100L, 1L)).willReturn(Optional.of(interview));
        given(interviewMapper.toResponse(interview)).willReturn(response);

        InterviewResponse result = interviewService.getById(1L, 100L);

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void getById_notOwnedByUser_throwsResourceNotFoundException() {
        given(interviewRepository.findByIdAndJobApplicationUserId(100L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.getById(1L, 100L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- patch ---

    @Test
    void patch_validFields_updatesInterview() {
        PatchInterviewRequest patchRequest = new PatchInterviewRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(patchRequest, "outcome", InterviewOutcome.PASSED);

        given(interviewRepository.findByIdAndJobApplicationUserId(100L, 1L)).willReturn(Optional.of(interview));
        given(interviewRepository.save(interview)).willReturn(interview);
        given(interviewMapper.toResponse(interview)).willReturn(response);

        InterviewResponse result = interviewService.patch(1L, 100L, patchRequest);

        assertThat(result).isNotNull();
        verify(interviewMapper).patchFromRequest(patchRequest, interview);
        verify(interviewRepository).save(interview);
    }

    @Test
    void patch_interviewNotFound_throwsResourceNotFoundException() {
        PatchInterviewRequest patchRequest = new PatchInterviewRequest();
        given(interviewRepository.findByIdAndJobApplicationUserId(100L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.patch(1L, 100L, patchRequest))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(interviewRepository, never()).save(any());
    }

    // --- delete ---

    @Test
    void delete_ownedByUser_deletesInterview() {
        given(interviewRepository.findByIdAndJobApplicationUserId(100L, 1L)).willReturn(Optional.of(interview));

        interviewService.delete(1L, 100L);

        verify(interviewRepository).deleteById(100L);
    }

    @Test
    void delete_notOwnedByUser_throwsResourceNotFoundException() {
        given(interviewRepository.findByIdAndJobApplicationUserId(100L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.delete(1L, 100L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(interviewRepository, never()).deleteById(any());
    }

    // --- getUpcoming ---

    @Test
    void getUpcoming_returnsInterviewsInNext7Days() {
        given(interviewRepository.findUpcoming(eq(1L), any(), any())).willReturn(List.of(interview));
        given(interviewMapper.toResponse(interview)).willReturn(response);

        List<InterviewResponse> result = interviewService.getUpcoming(1L);

        assertThat(result).hasSize(1);

        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(interviewRepository).findUpcoming(eq(1L), fromCaptor.capture(), toCaptor.capture());

        assertThat(toCaptor.getValue()).isAfter(fromCaptor.getValue());
        assertThat(toCaptor.getValue()).isBefore(fromCaptor.getValue().plusDays(8));
    }

    @Test
    void getUpcoming_noInterviews_returnsEmptyList() {
        given(interviewRepository.findUpcoming(eq(1L), any(), any())).willReturn(List.of());

        List<InterviewResponse> result = interviewService.getUpcoming(1L);

        assertThat(result).isEmpty();
    }
}
