package com.jobtracker.application.repository;

import com.jobtracker.application.dto.JobApplicationFilter;
import com.jobtracker.application.entity.ApplicationSource;
import com.jobtracker.application.entity.ApplicationStatus;
import com.jobtracker.application.entity.JobApplication;
import com.jobtracker.application.entity.WorkType;
import com.jobtracker.application.specification.JobApplicationSpecification;
import com.jobtracker.user.entity.Role;
import com.jobtracker.user.entity.User;
import com.jobtracker.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JobApplicationRepositoryTest {

    @Autowired JobApplicationRepository applicationRepository;
    @Autowired UserRepository userRepository;

    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .email("user@test.com").password("pwd").role(Role.USER).build());

        otherUser = userRepository.save(User.builder()
                .email("other@test.com").password("pwd").role(Role.USER).build());

        save(user, "Google", "Backend Developer", ApplicationStatus.APPLIED,
                LocalDate.of(2024, 1, 10), "Istanbul", WorkType.REMOTE, ApplicationSource.LINKEDIN);

        save(user, "Meta", "Frontend Developer", ApplicationStatus.INTERVIEWING,
                LocalDate.of(2024, 2, 15), "Ankara", WorkType.HYBRID, ApplicationSource.INDEED);

        save(user, "Apple", "Backend Engineer", ApplicationStatus.OFFER,
                LocalDate.of(2024, 3, 20), "Istanbul", WorkType.ONSITE, ApplicationSource.REFERRAL);

        save(user, "Netflix", "Data Scientist", ApplicationStatus.REJECTED,
                LocalDate.of(2024, 4, 5), "Remote", WorkType.REMOTE, ApplicationSource.OTHER);

        // Belongs to another user — must not appear in results
        save(otherUser, "Amazon", "SRE", ApplicationStatus.APPLIED,
                LocalDate.of(2024, 1, 1), "Istanbul", WorkType.HYBRID, ApplicationSource.LINKEDIN);
    }

    @Test
    void noFilter_returnsOnlyCurrentUsersActiveApplications() {
        Page<JobApplication> result = findAll(user.getId(), null, null, null, null, null);

        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent())
                .extracting(a -> a.getUser().getId())
                .containsOnly(user.getId());
    }

    @Test
    void statusFilter_returnsOnlyMatchingStatus() {
        Page<JobApplication> result = findAll(user.getId(), null, ApplicationStatus.INTERVIEWING, null, null, null);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getCompanyName()).isEqualTo("Meta");
    }

    @Test
    void searchByCompanyName_caseInsensitive_returnsMatch() {
        Page<JobApplication> result = findAll(user.getId(), "google", null, null, null, null);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getCompanyName()).isEqualTo("Google");
    }

    @Test
    void searchByPosition_partialMatch_returnsMultiple() {
        Page<JobApplication> result = findAll(user.getId(), "backend", null, null, null, null);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(JobApplication::getPosition)
                .containsExactlyInAnyOrder("Backend Developer", "Backend Engineer");
    }

    @Test
    void dateRange_startDate_returnsApplicationsOnOrAfter() {
        Page<JobApplication> result = findAll(user.getId(), null, null,
                LocalDate.of(2024, 3, 1), null, null);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(JobApplication::getCompanyName)
                .containsExactlyInAnyOrder("Apple", "Netflix");
    }

    @Test
    void dateRange_endDate_returnsApplicationsOnOrBefore() {
        Page<JobApplication> result = findAll(user.getId(), null, null,
                null, LocalDate.of(2024, 2, 28), null);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(JobApplication::getCompanyName)
                .containsExactlyInAnyOrder("Google", "Meta");
    }

    @Test
    void dateRange_startAndEnd_returnsApplicationsInRange() {
        Page<JobApplication> result = findAll(user.getId(), null, null,
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 31), null);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(JobApplication::getCompanyName)
                .containsExactlyInAnyOrder("Meta", "Apple");
    }

    @Test
    void locationFilter_partialMatch_returnsMatchingApplications() {
        Page<JobApplication> result = findAll(user.getId(), null, null, null, null, "istanbul");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(JobApplication::getCompanyName)
                .containsExactlyInAnyOrder("Google", "Apple");
    }

    @Test
    void combinedFilter_searchAndStatus_returnsIntersection() {
        Page<JobApplication> result = findAll(user.getId(), "developer", ApplicationStatus.INTERVIEWING,
                null, null, null);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getCompanyName()).isEqualTo("Meta");
    }

    @Test
    void softDeletedApplications_notReturnedBySpecification() {
        JobApplication google = findAll(user.getId(), "Google", null, null, null, null)
                .getContent().get(0);
        applicationRepository.deleteById(google.getId());
        applicationRepository.flush();

        Page<JobApplication> result = findAll(user.getId(), null, null, null, null, null);

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent())
                .extracting(JobApplication::getCompanyName)
                .doesNotContain("Google");
    }

    // --- helpers ---

    private Page<JobApplication> findAll(Long userId, String search, ApplicationStatus status,
                                         LocalDate startDate, LocalDate endDate, String location) {
        JobApplicationFilter filter = new JobApplicationFilter(search, status, startDate, endDate, location);
        Specification<JobApplication> spec = JobApplicationSpecification.withFilter(userId, filter);
        return applicationRepository.findAll(spec, PageRequest.of(0, 20));
    }

    private void save(User owner, String company, String position, ApplicationStatus status,
                      LocalDate date, String location, WorkType workType, ApplicationSource source) {
        applicationRepository.save(JobApplication.builder()
                .user(owner)
                .companyName(company)
                .position(position)
                .status(status)
                .applicationDate(date)
                .location(location)
                .workType(workType)
                .source(source)
                .currency("TRY")
                .build());
    }
}
