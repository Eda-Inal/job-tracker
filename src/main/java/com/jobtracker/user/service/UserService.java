package com.jobtracker.user.service;

import com.jobtracker.user.dto.ChangePasswordRequest;
import com.jobtracker.user.dto.UpdateUserRequest;
import com.jobtracker.user.dto.UserResponse;

public interface UserService {

    UserResponse getById(Long userId);

    UserResponse update(Long userId, UpdateUserRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    void delete(Long userId);
}
