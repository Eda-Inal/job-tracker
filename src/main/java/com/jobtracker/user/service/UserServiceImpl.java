package com.jobtracker.user.service;

import com.jobtracker.common.exception.BadRequestException;
import com.jobtracker.common.exception.ResourceNotFoundException;
import com.jobtracker.user.dto.ChangePasswordRequest;
import com.jobtracker.user.dto.UpdateUserRequest;
import com.jobtracker.user.dto.UserResponse;
import com.jobtracker.user.entity.User;
import com.jobtracker.user.mapper.UserMapper;
import com.jobtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getById(Long userId) {
        return userMapper.toResponse(findOrThrow(userId));
    }

    @Override
    @Transactional
    public UserResponse update(Long userId, UpdateUserRequest request) {
        User user = findOrThrow(userId);
        user.setFullName(request.getFullName());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findOrThrow(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        userRepository.deleteById(userId);
    }

    private User findOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
