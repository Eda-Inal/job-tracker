package com.jobtracker.tag.mapper;

import com.jobtracker.tag.dto.TagResponse;
import com.jobtracker.tag.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TagMapper {

    @Mapping(target = "userId", source = "user.id")
    TagResponse toResponse(Tag tag);
}
