package com.jobtracker.user.mapper;

import com.jobtracker.user.dto.UserResponse;
import com.jobtracker.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
