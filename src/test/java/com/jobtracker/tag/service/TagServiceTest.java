package com.jobtracker.tag.service;

import com.jobtracker.application.dto.JobApplicationResponse;
import com.jobtracker.application.entity.ApplicationStatus;
import com.jobtracker.application.entity.JobApplication;
import com.jobtracker.application.mapper.JobApplicationMapper;
import com.jobtracker.application.repository.JobApplicationRepository;
import com.jobtracker.common.exception.BadRequestException;
import com.jobtracker.common.exception.ResourceNotFoundException;
import com.jobtracker.tag.dto.CreateTagRequest;
import com.jobtracker.tag.dto.PatchTagRequest;
import com.jobtracker.tag.dto.TagResponse;
import com.jobtracker.tag.entity.Tag;
import com.jobtracker.tag.mapper.TagMapper;
import com.jobtracker.tag.repository.TagRepository;
import com.jobtracker.user.entity.Role;
import com.jobtracker.user.entity.User;
import com.jobtracker.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock private TagRepository tagRepository;
    @Mock private UserRepository userRepository;
    @Mock private JobApplicationRepository applicationRepository;
    @Mock private TagMapper tagMapper;
    @Mock private JobApplicationMapper applicationMapper;

    @InjectMocks private TagServiceImpl tagService;

    private User user;
    private Tag tag;
    private TagResponse tagResponse;
    private JobApplication application;

    @BeforeEach
    void setUp() {
        user = User.builder().email("user@test.com").password("pwd").role(Role.USER).build();
        ReflectionTestUtils.setField(user, "id", 1L);

        tag = Tag.builder().user(user).name("remote").color("#FF5733").build();
        ReflectionTestUtils.setField(tag, "id", 10L);

        tagResponse = TagResponse.builder().id(10L).userId(1L).name("remote").color("#FF5733").build();

        application = JobApplication.builder()
                .user(user).companyName("Google").position("SWE")
                .status(ApplicationStatus.APPLIED)
                .applicationDate(LocalDate.now()).currency("TRY").build();
        ReflectionTestUtils.setField(application, "id", 100L);
    }

    // --- getAll ---

    @Test
    void getAll_returnsSortedTagsForUser() {
        given(tagRepository.findByUserIdOrderByNameAsc(1L)).willReturn(List.of(tag));
        given(tagMapper.toResponse(tag)).willReturn(tagResponse);

        List<TagResponse> result = tagService.getAll(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("remote");
    }

    // --- create ---

    @Test
    void create_newTag_savesAndReturns() {
        CreateTagRequest request = new CreateTagRequest();
        ReflectionTestUtils.setField(request, "name", "remote");
        ReflectionTestUtils.setField(request, "color", "#FF5733");

        given(tagRepository.existsByUserIdAndName(1L, "remote")).willReturn(false);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(tagRepository.save(any(Tag.class))).willReturn(tag);
        given(tagMapper.toResponse(tag)).willReturn(tagResponse);

        TagResponse result = tagService.create(1L, request);

        assertThat(result.getName()).isEqualTo("remote");
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void create_duplicateName_throwsBadRequestException() {
        CreateTagRequest request = new CreateTagRequest();
        ReflectionTestUtils.setField(request, "name", "remote");

        given(tagRepository.existsByUserIdAndName(1L, "remote")).willReturn(true);

        assertThatThrownBy(() -> tagService.create(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("remote");
        verify(tagRepository, never()).save(any());
    }

    // --- patch ---

    @Test
    void patch_nameChange_updatesAndReturns() {
        PatchTagRequest request = new PatchTagRequest();
        ReflectionTestUtils.setField(request, "name", "hybrid");

        given(tagRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(tag));
        given(tagRepository.existsByUserIdAndName(1L, "hybrid")).willReturn(false);
        given(tagRepository.save(tag)).willReturn(tag);
        given(tagMapper.toResponse(tag)).willReturn(tagResponse);

        tagService.patch(1L, 10L, request);

        assertThat(tag.getName()).isEqualTo("hybrid");
        verify(tagRepository).save(tag);
    }

    @Test
    void patch_sameName_doesNotCheckDuplicate() {
        PatchTagRequest request = new PatchTagRequest();
        ReflectionTestUtils.setField(request, "name", "remote"); // same as current

        given(tagRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(tag));
        given(tagRepository.save(tag)).willReturn(tag);
        given(tagMapper.toResponse(tag)).willReturn(tagResponse);

        tagService.patch(1L, 10L, request);

        verify(tagRepository, never()).existsByUserIdAndName(any(), any());
    }

    @Test
    void patch_duplicateName_throwsBadRequestException() {
        PatchTagRequest request = new PatchTagRequest();
        ReflectionTestUtils.setField(request, "name", "frontend");

        given(tagRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(tag));
        given(tagRepository.existsByUserIdAndName(1L, "frontend")).willReturn(true);

        assertThatThrownBy(() -> tagService.patch(1L, 10L, request))
                .isInstanceOf(BadRequestException.class);
        verify(tagRepository, never()).save(any());
    }

    @Test
    void patch_tagNotFound_throwsResourceNotFoundException() {
        PatchTagRequest request = new PatchTagRequest();
        given(tagRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.patch(1L, 10L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- delete ---

    @Test
    void delete_existingTag_deletesIt() {
        given(tagRepository.existsByIdAndUserId(10L, 1L)).willReturn(true);

        tagService.delete(1L, 10L);

        verify(tagRepository).deleteById(10L);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        given(tagRepository.existsByIdAndUserId(10L, 1L)).willReturn(false);

        assertThatThrownBy(() -> tagService.delete(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(tagRepository, never()).deleteById(any());
    }

    // --- addTagToApplication ---

    @Test
    void addTag_validApplicationAndTag_addsAndReturnsApplication() {
        JobApplicationResponse appResponse = JobApplicationResponse.builder()
                .id(100L).userId(1L).companyName("Google").position("SWE")
                .status(ApplicationStatus.APPLIED).tags(List.of(tagResponse)).build();

        given(applicationRepository.findByIdAndUserId(100L, 1L)).willReturn(Optional.of(application));
        given(tagRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(tag));
        given(applicationRepository.save(application)).willReturn(application);
        given(applicationMapper.toResponse(application)).willReturn(appResponse);

        JobApplicationResponse result = tagService.addTagToApplication(1L, 100L, 10L);

        assertThat(result.getTags()).hasSize(1);
        assertThat(application.getTags()).contains(tag);
    }

    @Test
    void addTag_applicationNotFound_throwsResourceNotFoundException() {
        given(applicationRepository.findByIdAndUserId(100L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.addTagToApplication(1L, 100L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(tagRepository, never()).findByIdAndUserId(any(), any());
    }

    @Test
    void addTag_tagNotFound_throwsResourceNotFoundException() {
        given(applicationRepository.findByIdAndUserId(100L, 1L)).willReturn(Optional.of(application));
        given(tagRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.addTagToApplication(1L, 100L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(applicationRepository, never()).save(any());
    }

    // --- removeTagFromApplication ---

    @Test
    void removeTag_tagAttached_removesFromApplication() {
        application.getTags().add(tag);

        given(applicationRepository.findByIdAndUserId(100L, 1L)).willReturn(Optional.of(application));
        given(tagRepository.existsByIdAndUserId(10L, 1L)).willReturn(true);
        given(applicationRepository.save(application)).willReturn(application);

        tagService.removeTagFromApplication(1L, 100L, 10L);

        assertThat(application.getTags()).doesNotContain(tag);
        verify(applicationRepository).save(application);
    }

    @Test
    void removeTag_tagNotFound_throwsResourceNotFoundException() {
        given(applicationRepository.findByIdAndUserId(100L, 1L)).willReturn(Optional.of(application));
        given(tagRepository.existsByIdAndUserId(10L, 1L)).willReturn(false);

        assertThatThrownBy(() -> tagService.removeTagFromApplication(1L, 100L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(applicationRepository, never()).save(any());
    }
}
