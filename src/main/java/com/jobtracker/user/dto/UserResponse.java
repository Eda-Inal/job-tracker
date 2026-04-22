package com.jobtracker.user.dto;

import com.jobtracker.user.entity.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private boolean emailVerified;
    private LocalDateTime createdAt;
}
