package com.jobtracker.application.service;

import com.jobtracker.application.dto.CreateJobApplicationRequest;
import com.jobtracker.application.dto.JobApplicationResponse;
import com.jobtracker.application.dto.PatchJobApplicationRequest;
import com.jobtracker.application.dto.UpdateJobApplicationRequest;
import com.jobtracker.application.entity.ApplicationStatus;
import com.jobtracker.application.entity.JobApplication;
import com.jobtracker.application.event.JobApplicationCreatedEvent;
import com.jobtracker.application.event.StatusChangedEvent;
import com.jobtracker.application.mapper.JobApplicationMapper;
import com.jobtracker.application.repository.JobApplicationRepository;
import com.jobtracker.application.specification.JobApplicationSpecification;
import com.jobtracker.common.exception.ResourceNotFoundException;
import com.jobtracker.user.entity.Role;
import com.jobtracker.user.entity.User;
import com.jobtracker.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock private JobApplicationRepository applicationRepository;
    @Mock private UserRepository userRepository;
    @Mock private JobApplicationMapper applicationMapper;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private JobApplicationServiceImpl applicationService;

    private User user;
    private JobApplication application;
    private JobApplicationResponse response;

    @BeforeEach
    void setUp() {
        user = User.builder().email("user@test.com").password("pwd").role(Role.USER).build();
        ReflectionTestUtils.setField(user, "id", 1L);

        application = JobApplication.builder()
                .user(user).companyName("Google").position("SWE")
                .status(ApplicationStatus.APPLIED)
                .applicationDate(LocalDate.now()).currency("TRY").build();
        ReflectionTestUtils.setField(application, "id", 10L);

        response = JobApplicationResponse.builder()
                .id(10L).userId(1L).companyName("Google").position("SWE")
                .status(ApplicationStatus.APPLIED)
                .applicationDate(LocalDate.now())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    // --- create ---

    @Test
    void create_publishesJobApplicationCreatedEvent() {
        CreateJobApplicationRequest request = new CreateJobApplicationRequest();
        ReflectionTestUtils.setField(request, "companyName", "Google");
        ReflectionTestUtils.setField(request, "position", "SWE");
        ReflectionTestUtils.setField(request, "applicationDate", LocalDate.now());

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(applicationMapper.toEntity(request)).willReturn(application);
        given(applicationRepository.save(application)).willReturn(application);
        given(applicationMapper.toResponse(application)).willReturn(response);

        applicationService.create(1L, request);

        ArgumentCaptor<JobApplicationCreatedEvent> captor =
                ArgumentCaptor.forClass(JobApplicationCreatedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        JobApplicationCreatedEvent event = captor.getValue();
        assertThat(event.getUserId()).isEqualTo(1L);
        assertThat(event.getApplicationId()).isEqualTo(10L);
        assertThat(event.getCompanyName()).isEqualTo("Google");
        assertThat(event.getPosition()).isEqualTo("SWE");
    }

    @Test
    void create_userNotFound_throwsAndNoEventPublished() {
        CreateJobApplicationRequest request = new CreateJobApplicationRequest();
        ReflectionTestUtils.setField(request, "companyName", "Google");
        ReflectionTestUtils.setField(request, "position", "SWE");
        ReflectionTestUtils.setField(request, "applicationDate", LocalDate.now());

        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.create(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(eventPublisher, never()).publishEvent(any());
    }

    // --- update (status change) ---

    @Test
    void update_statusChanges_publishesStatusChangedEvent() {
        UpdateJobApplicationRequest request = new UpdateJobApplicationRequest();
        ReflectionTestUtils.setField(request, "companyName", "Google");
        ReflectionTestUtils.setField(request, "position", "SWE");
        ReflectionTestUtils.setField(request, "status", ApplicationStatus.INTERVIEWING);
        ReflectionTestUtils.setField(request, "applicationDate", LocalDate.now());

        JobApplicationResponse updatedResponse = JobApplicationResponse.builder()
                .id(10L).userId(1L).companyName("Google").position("SWE")
                .status(ApplicationStatus.INTERVIEWING)
                .applicationDate(LocalDate.now())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        given(applicationRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(application));
        given(applicationRepository.save(application)).willReturn(application);
        given(applicationMapper.toResponse(application)).willReturn(updatedResponse);

        applicationService.update(1L, 10L, request);

        ArgumentCaptor<StatusChangedEvent> captor = ArgumentCaptor.forClass(StatusChangedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        StatusChangedEvent event = captor.getValue();
        assertThat(event.getPreviousStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(event.getNewStatus()).isEqualTo(ApplicationStatus.INTERVIEWING);
        assertThat(event.getApplicationId()).isEqualTo(10L);
    }

    @Test
    void update_statusUnchanged_noStatusChangedEvent() {
        UpdateJobApplicationRequest request = new UpdateJobApplicationRequest();
        ReflectionTestUtils.setField(request, "companyName", "Google");
        ReflectionTestUtils.setField(request, "position", "SWE");
        ReflectionTestUtils.setField(request, "status", ApplicationStatus.APPLIED); // same
        ReflectionTestUtils.setField(request, "applicationDate", LocalDate.now());

        given(applicationRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(application));
        given(applicationRepository.save(application)).willReturn(application);
        given(applicationMapper.toResponse(application)).willReturn(response);

        applicationService.update(1L, 10L, request);

        verify(eventPublisher, never()).publishEvent(any(StatusChangedEvent.class));
    }

    // --- patch (status change) ---

    @Test
    void patch_statusChanges_publishesStatusChangedEvent() {
        PatchJobApplicationRequest request = new PatchJobApplicationRequest();
        ReflectionTestUtils.setField(request, "status", ApplicationStatus.OFFER);

        JobApplicationResponse offerResponse = JobApplicationResponse.builder()
                .id(10L).userId(1L).companyName("Google").position("SWE")
                .status(ApplicationStatus.OFFER)
                .applicationDate(LocalDate.now())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        given(applicationRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(application));
        given(applicationRepository.save(application)).willReturn(application);
        given(applicationMapper.toResponse(application)).willReturn(offerResponse);

        applicationService.patch(1L, 10L, request);

        ArgumentCaptor<StatusChangedEvent> captor = ArgumentCaptor.forClass(StatusChangedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getNewStatus()).isEqualTo(ApplicationStatus.OFFER);
        assertThat(captor.getValue().getPreviousStatus()).isEqualTo(ApplicationStatus.APPLIED);
    }

    @Test
    void patch_noStatusInRequest_noStatusChangedEvent() {
        PatchJobApplicationRequest request = new PatchJobApplicationRequest();
        ReflectionTestUtils.setField(request, "companyName", "Google Updated");
        // status is null — not being changed

        given(applicationRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(application));
        given(applicationRepository.save(application)).willReturn(application);
        given(applicationMapper.toResponse(application)).willReturn(response);

        applicationService.patch(1L, 10L, request);

        verify(eventPublisher, never()).publishEvent(any(StatusChangedEvent.class));
    }

    // --- delete ---

    @Test
    void delete_existingApplication_deletesWithNoEvent() {
        given(applicationRepository.existsByIdAndUserId(10L, 1L)).willReturn(true);

        applicationService.delete(1L, 10L);

        verify(applicationRepository).deleteById(10L);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void delete_notFound_throwsAndNoDelete() {
        given(applicationRepository.existsByIdAndUserId(10L, 1L)).willReturn(false);

        assertThatThrownBy(() -> applicationService.delete(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(applicationRepository, never()).deleteById(any());
    }
}
