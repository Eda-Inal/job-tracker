package com.jobtracker.tag.service;

import com.jobtracker.application.dto.JobApplicationResponse;
import com.jobtracker.tag.dto.CreateTagRequest;
import com.jobtracker.tag.dto.PatchTagRequest;
import com.jobtracker.tag.dto.TagResponse;

import java.util.List;

public interface TagService {

    List<TagResponse> getAll(Long userId);

    TagResponse create(Long userId, CreateTagRequest request);

    TagResponse patch(Long userId, Long id, PatchTagRequest request);

    void delete(Long userId, Long id);

    JobApplicationResponse addTagToApplication(Long userId, Long appId, Long tagId);

    void removeTagFromApplication(Long userId, Long appId, Long tagId);
}
