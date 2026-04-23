package com.jobtracker.application.controller;

import com.jobtracker.application.dto.CreateJobApplicationRequest;
import com.jobtracker.application.dto.JobApplicationResponse;
import com.jobtracker.application.dto.PatchJobApplicationRequest;
import com.jobtracker.application.dto.UpdateJobApplicationRequest;
import com.jobtracker.application.dto.JobApplicationFilter;
import com.jobtracker.application.entity.ApplicationStatus;
import com.jobtracker.application.service.JobApplicationService;
import com.jobtracker.common.dto.ApiResponse;
import com.jobtracker.common.dto.PageResponse;
import com.jobtracker.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService applicationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<JobApplicationResponse>>> getAll(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String location,
            @PageableDefault(size = 20, sort = "applicationDate", direction = Sort.Direction.DESC) Pageable pageable) {
        JobApplicationFilter filter = new JobApplicationFilter(search, status, startDate, endDate, location);
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.getAll(user.getId(), filter, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobApplicationResponse>> getById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.getById(user.getId(), id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobApplicationResponse>> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateJobApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(applicationService.create(user.getId(), request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobApplicationResponse>> update(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobApplicationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.update(user.getId(), id, request)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<JobApplicationResponse>> patch(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody PatchJobApplicationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.patch(user.getId(), id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        applicationService.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trash")
    public ResponseEntity<ApiResponse<PageResponse<JobApplicationResponse>>> getTrash(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.getTrash(user.getId(), pageable)));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<JobApplicationResponse>> restore(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.restore(user.getId(), id)));
    }
}
