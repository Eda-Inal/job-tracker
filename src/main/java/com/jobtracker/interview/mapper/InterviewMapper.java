package com.jobtracker.interview.mapper;

import com.jobtracker.interview.dto.CreateInterviewRequest;
import com.jobtracker.interview.dto.InterviewResponse;
import com.jobtracker.interview.dto.PatchInterviewRequest;
import com.jobtracker.interview.entity.Interview;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InterviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobApplication", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Interview toEntity(CreateInterviewRequest request);

    @Mapping(target = "jobApplicationId", source = "jobApplication.id")
    InterviewResponse toResponse(Interview interview);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobApplication", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchFromRequest(PatchInterviewRequest request, @MappingTarget Interview interview);
}
