package com.jobtracker.interview.repository;

import com.jobtracker.application.entity.ApplicationStatus;
import com.jobtracker.application.entity.JobApplication;
import com.jobtracker.application.repository.JobApplicationRepository;
import com.jobtracker.interview.entity.Interview;
import com.jobtracker.interview.entity.InterviewOutcome;
import com.jobtracker.interview.entity.InterviewType;
import com.jobtracker.user.entity.Role;
import com.jobtracker.user.entity.User;
import com.jobtracker.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InterviewRepositoryTest {

    @Autowired InterviewRepository interviewRepository;
    @Autowired JobApplicationRepository applicationRepository;
    @Autowired UserRepository userRepository;

    private User user;
    private User otherUser;
    private JobApplication application;
    private JobApplication otherApplication;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .email("user@test.com").password("pwd").role(Role.USER).build());

        otherUser = userRepository.save(User.builder()
                .email("other@test.com").password("pwd").role(Role.USER).build());

        application = applicationRepository.save(JobApplication.builder()
                .user(user).companyName("Google").position("SWE")
                .status(ApplicationStatus.INTERVIEWING)
                .applicationDate(LocalDate.now()).currency("TRY").build());

        otherApplication = applicationRepository.save(JobApplication.builder()
                .user(otherUser).companyName("Meta").position("SWE")
                .status(ApplicationStatus.APPLIED)
                .applicationDate(LocalDate.now()).currency("TRY").build());
    }

    // --- findByJobApplicationIdAndJobApplicationUserIdOrderByScheduledAtAsc ---

    @Test
    void findByApplication_returnsInterviewsInChronologicalOrder() {
        LocalDateTime base = LocalDateTime.now().plusDays(5);

        saveInterview(application, base.plusHours(3), InterviewType.HR, InterviewOutcome.PENDING);
        saveInterview(application, base.plusHours(1), InterviewType.PHONE, InterviewOutcome.PASSED);
        saveInterview(application, base.plusHours(2), InterviewType.TECHNICAL, InterviewOutcome.PENDING);

        List<Interview> result = interviewRepository
                .findByJobApplicationIdAndJobApplicationUserIdOrderByScheduledAtAsc(
                        application.getId(), user.getId());

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getType()).isEqualTo(InterviewType.PHONE);
        assertThat(result.get(1).getType()).isEqualTo(InterviewType.TECHNICAL);
        assertThat(result.get(2).getType()).isEqualTo(InterviewType.HR);
    }

    @Test
    void findByApplication_doesNotReturnOtherUsersInterviews() {
        saveInterview(application, LocalDateTime.now().plusDays(1), InterviewType.PHONE, InterviewOutcome.PENDING);
        saveInterview(otherApplication, LocalDateTime.now().plusDays(2), InterviewType.HR, InterviewOutcome.PENDING);

        List<Interview> result = interviewRepository
                .findByJobApplicationIdAndJobApplicationUserIdOrderByScheduledAtAsc(
                        application.getId(), user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getJobApplication().getId()).isEqualTo(application.getId());
    }

    @Test
    void findByApplication_noInterviews_returnsEmptyList() {
        List<Interview> result = interviewRepository
                .findByJobApplicationIdAndJobApplicationUserIdOrderByScheduledAtAsc(
                        application.getId(), user.getId());

        assertThat(result).isEmpty();
    }

    // --- findByIdAndJobApplicationUserId ---

    @Test
    void findByIdAndUser_ownedInterview_returnsInterview() {
        Interview saved = saveInterview(application, LocalDateTime.now().plusDays(1),
                InterviewType.TECHNICAL, InterviewOutcome.PENDING);

        Optional<Interview> result = interviewRepository
                .findByIdAndJobApplicationUserId(saved.getId(), user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void findByIdAndUser_otherUsersInterview_returnsEmpty() {
        Interview saved = saveInterview(otherApplication, LocalDateTime.now().plusDays(1),
                InterviewType.HR, InterviewOutcome.PENDING);

        Optional<Interview> result = interviewRepository
                .findByIdAndJobApplicationUserId(saved.getId(), user.getId());

        assertThat(result).isEmpty();
    }

    // --- findUpcoming ---

    @Test
    void findUpcoming_returnsInterviewsWithinRange() {
        LocalDateTime now = LocalDateTime.now();

        saveInterview(application, now.plusDays(1), InterviewType.PHONE, InterviewOutcome.PENDING);
        saveInterview(application, now.plusDays(5), InterviewType.TECHNICAL, InterviewOutcome.PENDING);
        saveInterview(application, now.plusDays(10), InterviewType.HR, InterviewOutcome.PENDING); // outside range

        List<Interview> result = interviewRepository.findUpcoming(user.getId(), now, now.plusDays(7));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getType()).isEqualTo(InterviewType.PHONE);
        assertThat(result.get(1).getType()).isEqualTo(InterviewType.TECHNICAL);
    }

    @Test
    void findUpcoming_doesNotReturnOtherUsersInterviews() {
        LocalDateTime now = LocalDateTime.now();

        saveInterview(application, now.plusDays(2), InterviewType.PHONE, InterviewOutcome.PENDING);
        saveInterview(otherApplication, now.plusDays(3), InterviewType.HR, InterviewOutcome.PENDING);

        List<Interview> result = interviewRepository.findUpcoming(user.getId(), now, now.plusDays(7));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getJobApplication().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void findUpcoming_pastInterviews_notReturned() {
        LocalDateTime now = LocalDateTime.now();

        saveInterview(application, now.minusDays(1), InterviewType.PHONE, InterviewOutcome.PASSED);
        saveInterview(application, now.plusDays(2), InterviewType.HR, InterviewOutcome.PENDING);

        List<Interview> result = interviewRepository.findUpcoming(user.getId(), now, now.plusDays(7));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(InterviewType.HR);
    }

    @Test
    void findUpcoming_noInterviews_returnsEmptyList() {
        List<Interview> result = interviewRepository.findUpcoming(
                user.getId(), LocalDateTime.now(), LocalDateTime.now().plusDays(7));

        assertThat(result).isEmpty();
    }

    // --- helpers ---

    private Interview saveInterview(JobApplication app, LocalDateTime scheduledAt,
                                    InterviewType type, InterviewOutcome outcome) {
        return interviewRepository.save(Interview.builder()
                .jobApplication(app)
                .scheduledAt(scheduledAt)
                .type(type)
                .outcome(outcome)
                .build());
    }
}
