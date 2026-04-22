package com.jobtracker.auth.service;

import com.jobtracker.auth.dto.LoginRequest;
import com.jobtracker.auth.dto.RegisterRequest;
import com.jobtracker.auth.dto.TokenResponse;

public interface AuthService {

    TokenResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(String refreshToken);

    void logout(String refreshToken);
}
