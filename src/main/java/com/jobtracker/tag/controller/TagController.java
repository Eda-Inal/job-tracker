package com.jobtracker.tag.controller;

import com.jobtracker.application.dto.JobApplicationResponse;
import com.jobtracker.common.dto.ApiResponse;
import com.jobtracker.tag.dto.AddTagRequest;
import com.jobtracker.tag.dto.CreateTagRequest;
import com.jobtracker.tag.dto.PatchTagRequest;
import com.jobtracker.tag.dto.TagResponse;
import com.jobtracker.tag.service.TagService;
import com.jobtracker.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getAll(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(tagService.getAll(user.getId())));
    }

    @PostMapping("/tags")
    public ResponseEntity<ApiResponse<TagResponse>> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateTagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(tagService.create(user.getId(), request)));
    }

    @PatchMapping("/tags/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> patch(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody PatchTagRequest request) {
        return ResponseEntity.ok(ApiResponse.success(tagService.patch(user.getId(), id, request)));
    }

    @DeleteMapping("/tags/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        tagService.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/applications/{appId}/tags")
    public ResponseEntity<ApiResponse<JobApplicationResponse>> addTag(
            @AuthenticationPrincipal User user,
            @PathVariable Long appId,
            @Valid @RequestBody AddTagRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                tagService.addTagToApplication(user.getId(), appId, request.getTagId())));
    }

    @DeleteMapping("/applications/{appId}/tags/{tagId}")
    public ResponseEntity<Void> removeTag(
            @AuthenticationPrincipal User user,
            @PathVariable Long appId,
            @PathVariable Long tagId) {
        tagService.removeTagFromApplication(user.getId(), appId, tagId);
        return ResponseEntity.noContent().build();
    }
}
