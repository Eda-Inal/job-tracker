package com.jobtracker.application.mapper;

import com.jobtracker.application.dto.CreateJobApplicationRequest;
import com.jobtracker.application.dto.JobApplicationResponse;
import com.jobtracker.application.dto.PatchJobApplicationRequest;
import com.jobtracker.application.dto.UpdateJobApplicationRequest;
import com.jobtracker.application.entity.JobApplication;
import com.jobtracker.tag.mapper.TagMapper;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {TagMapper.class})
public interface JobApplicationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    JobApplication toEntity(CreateJobApplicationRequest request);

    @Mapping(target = "userId", source = "user.id")
    JobApplicationResponse toResponse(JobApplication application);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromRequest(UpdateJobApplicationRequest request, @MappingTarget JobApplication application);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchFromRequest(PatchJobApplicationRequest request, @MappingTarget JobApplication application);
}
