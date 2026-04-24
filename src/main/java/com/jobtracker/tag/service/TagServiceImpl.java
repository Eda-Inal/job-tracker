package com.jobtracker.tag.service;

import com.jobtracker.application.dto.JobApplicationResponse;
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
import com.jobtracker.user.entity.User;
import com.jobtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final JobApplicationRepository applicationRepository;
    private final TagMapper tagMapper;
    private final JobApplicationMapper applicationMapper;

    @Override
    public List<TagResponse> getAll(Long userId) {
        return tagRepository.findByUserIdOrderByNameAsc(userId)
                .stream().map(tagMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public TagResponse create(Long userId, CreateTagRequest request) {
        if (tagRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new BadRequestException("Tag with name '" + request.getName() + "' already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Tag tag = Tag.builder()
                .user(user)
                .name(request.getName())
                .color(request.getColor())
                .build();

        return tagMapper.toResponse(tagRepository.save(tag));
    }

    @Override
    @Transactional
    public TagResponse patch(Long userId, Long id, PatchTagRequest request) {
        Tag tag = tagRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));

        if (request.getName() != null && !request.getName().equals(tag.getName())) {
            if (tagRepository.existsByUserIdAndName(userId, request.getName())) {
                throw new BadRequestException("Tag with name '" + request.getName() + "' already exists");
            }
            tag.setName(request.getName());
        }

        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }

        return tagMapper.toResponse(tagRepository.save(tag));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        if (!tagRepository.existsByIdAndUserId(id, userId)) {
            throw new ResourceNotFoundException("Tag", id);
        }
        tagRepository.deleteById(id);
    }

    @Override
    @Transactional
    public JobApplicationResponse addTagToApplication(Long userId, Long appId, Long tagId) {
        JobApplication application = applicationRepository.findByIdAndUserId(appId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", appId));

        Tag tag = tagRepository.findByIdAndUserId(tagId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", tagId));

        application.getTags().add(tag);
        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    @Override
    @Transactional
    public void removeTagFromApplication(Long userId, Long appId, Long tagId) {
        JobApplication application = applicationRepository.findByIdAndUserId(appId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", appId));

        if (!tagRepository.existsByIdAndUserId(tagId, userId)) {
            throw new ResourceNotFoundException("Tag", tagId);
        }

        application.getTags().removeIf(t -> t.getId().equals(tagId));
        applicationRepository.save(application);
    }
}
