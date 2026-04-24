package com.jobtracker.interview.controller;

import com.jobtracker.common.dto.ApiResponse;
import com.jobtracker.interview.dto.CreateInterviewRequest;
import com.jobtracker.interview.dto.InterviewResponse;
import com.jobtracker.interview.dto.PatchInterviewRequest;
import com.jobtracker.interview.service.InterviewService;
import com.jobtracker.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping("/api/v1/applications/{appId}/interviews")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getAllByApplication(
            @AuthenticationPrincipal User user,
            @PathVariable Long appId) {
        return ResponseEntity.ok(ApiResponse.success(
                interviewService.getAllByApplication(user.getId(), appId)));
    }

    @PostMapping("/api/v1/applications/{appId}/interviews")
    public ResponseEntity<ApiResponse<InterviewResponse>> create(
            @AuthenticationPrincipal User user,
            @PathVariable Long appId,
            @Valid @RequestBody CreateInterviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(interviewService.create(user.getId(), appId, request)));
    }

    @GetMapping("/api/v1/interviews/upcoming")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getUpcoming(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(
                interviewService.getUpcoming(user.getId())));
    }

    @GetMapping("/api/v1/interviews/{id}")
    public ResponseEntity<ApiResponse<InterviewResponse>> getById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                interviewService.getById(user.getId(), id)));
    }

    @PatchMapping("/api/v1/interviews/{id}")
    public ResponseEntity<ApiResponse<InterviewResponse>> patch(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody PatchInterviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                interviewService.patch(user.getId(), id, request)));
    }

    @DeleteMapping("/api/v1/interviews/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        interviewService.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
