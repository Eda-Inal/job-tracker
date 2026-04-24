package com.jobtracker.interview.service;

import com.jobtracker.interview.dto.CreateInterviewRequest;
import com.jobtracker.interview.dto.InterviewResponse;
import com.jobtracker.interview.dto.PatchInterviewRequest;

import java.util.List;

public interface InterviewService {

    List<InterviewResponse> getAllByApplication(Long userId, Long appId);

    InterviewResponse create(Long userId, Long appId, CreateInterviewRequest request);

    InterviewResponse getById(Long userId, Long id);

    InterviewResponse patch(Long userId, Long id, PatchInterviewRequest request);

    void delete(Long userId, Long id);

    List<InterviewResponse> getUpcoming(Long userId);
}
