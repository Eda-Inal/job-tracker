package com.jobtracker.tag.repository;

import com.jobtracker.application.entity.ApplicationStatus;
import com.jobtracker.application.entity.JobApplication;
import com.jobtracker.application.repository.JobApplicationRepository;
import com.jobtracker.tag.entity.Tag;
import com.jobtracker.user.entity.Role;
import com.jobtracker.user.entity.User;
import com.jobtracker.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TagRepositoryTest {

    @Autowired TagRepository tagRepository;
    @Autowired UserRepository userRepository;
    @Autowired JobApplicationRepository applicationRepository;

    private User user;
    private User otherUser;
    private Tag remoteTag;
    private Tag frontendTag;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .email("user@test.com").password("pwd").role(Role.USER).build());

        otherUser = userRepository.save(User.builder()
                .email("other@test.com").password("pwd").role(Role.USER).build());

        remoteTag = saveTag(user, "remote", "#00FF00");
        frontendTag = saveTag(user, "frontend", "#0000FF");
        saveTag(otherUser, "backend", "#FF0000");
    }

    // --- findByUserIdOrderByNameAsc ---

    @Test
    void findByUserId_returnsOnlyUserTagsAlphabetically() {
        List<Tag> result = tagRepository.findByUserIdOrderByNameAsc(user.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("frontend");
        assertThat(result.get(1).getName()).isEqualTo("remote");
    }

    @Test
    void findByUserId_doesNotReturnOtherUserTags() {
        List<Tag> result = tagRepository.findByUserIdOrderByNameAsc(user.getId());

        assertThat(result).extracting(Tag::getName).doesNotContain("backend");
    }

    // --- findByIdAndUserId ---

    @Test
    void findByIdAndUserId_ownedTag_returnsTag() {
        Optional<Tag> result = tagRepository.findByIdAndUserId(remoteTag.getId(), user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("remote");
    }

    @Test
    void findByIdAndUserId_otherUsersTag_returnsEmpty() {
        List<Tag> otherTags = tagRepository.findByUserIdOrderByNameAsc(otherUser.getId());
        Long otherTagId = otherTags.get(0).getId();

        Optional<Tag> result = tagRepository.findByIdAndUserId(otherTagId, user.getId());

        assertThat(result).isEmpty();
    }

    // --- existsByUserIdAndName ---

    @Test
    void existsByUserIdAndName_existingName_returnsTrue() {
        assertThat(tagRepository.existsByUserIdAndName(user.getId(), "remote")).isTrue();
    }

    @Test
    void existsByUserIdAndName_nonExistingName_returnsFalse() {
        assertThat(tagRepository.existsByUserIdAndName(user.getId(), "unknown")).isFalse();
    }

    @Test
    void existsByUserIdAndName_otherUsersName_returnsFalse() {
        assertThat(tagRepository.existsByUserIdAndName(user.getId(), "backend")).isFalse();
    }

    // --- many-to-many: job_application_tags ---

    @Test
    void addTagToApplication_tagAppearsInApplicationTags() {
        JobApplication application = applicationRepository.save(JobApplication.builder()
                .user(user).companyName("Google").position("SWE")
                .status(ApplicationStatus.APPLIED)
                .applicationDate(LocalDate.now()).currency("TRY").build());

        application.getTags().add(remoteTag);
        applicationRepository.save(application);
        applicationRepository.flush();

        JobApplication loaded = applicationRepository.findByIdAndUserId(
                application.getId(), user.getId()).orElseThrow();

        assertThat(loaded.getTags()).contains(remoteTag);
    }

    @Test
    void removeTagFromApplication_tagRemovedFromApplication() {
        JobApplication application = applicationRepository.save(JobApplication.builder()
                .user(user).companyName("Meta").position("SWE")
                .status(ApplicationStatus.APPLIED)
                .applicationDate(LocalDate.now()).currency("TRY").build());

        application.getTags().add(remoteTag);
        application.getTags().add(frontendTag);
        applicationRepository.save(application);
        applicationRepository.flush();

        application.getTags().removeIf(t -> t.getId().equals(remoteTag.getId()));
        applicationRepository.save(application);
        applicationRepository.flush();

        JobApplication loaded = applicationRepository.findByIdAndUserId(
                application.getId(), user.getId()).orElseThrow();

        assertThat(loaded.getTags()).doesNotContain(remoteTag);
        assertThat(loaded.getTags()).contains(frontendTag);
    }

    // --- helpers ---

    private Tag saveTag(User owner, String name, String color) {
        return tagRepository.save(Tag.builder().user(owner).name(name).color(color).build());
    }
}
