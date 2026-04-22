package com.jobtracker.auth.service;

import com.jobtracker.auth.dto.LoginRequest;
import com.jobtracker.auth.dto.RegisterRequest;
import com.jobtracker.auth.dto.TokenResponse;
import com.jobtracker.auth.entity.RefreshToken;
import com.jobtracker.auth.repository.RefreshTokenRepository;
import com.jobtracker.common.exception.BadRequestException;
import com.jobtracker.user.entity.Role;
import com.jobtracker.user.entity.User;
import com.jobtracker.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", 604_800_000L);

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded")
                .role(Role.USER)
                .build();
    }

    // --- register ---

    @Test
    void register_newEmail_savesUserAndReturnsTokens() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");
        req.setFullName("Test User");

        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("encoded");
        given(userRepository.save(any(User.class))).willReturn(testUser);
        given(jwtService.generateAccessToken(any())).willReturn("access-token");
        given(refreshTokenRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        TokenResponse result = authService.register(req);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isNotBlank();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_existingEmail_throwsBadRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");
        req.setFullName("Test User");

        given(userRepository.existsByEmail("test@example.com")).willReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already in use");

        verify(userRepository, never()).save(any());
    }

    // --- login ---

    @Test
    void login_validCredentials_returnsTokens() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));
        given(jwtService.generateAccessToken(testUser)).willReturn("access-token");
        given(refreshTokenRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        TokenResponse result = authService.login(req);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isNotBlank();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_invalidCredentials_throwsBadCredentials() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("wrong");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }

    // --- refresh ---

    @Test
    void refresh_validToken_revokesOldAndReturnsNewTokens() {
        RefreshToken stored = RefreshToken.builder()
                .token("old-refresh")
                .user(testUser)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        given(refreshTokenRepository.findByToken("old-refresh")).willReturn(Optional.of(stored));
        given(jwtService.generateAccessToken(testUser)).willReturn("new-access-token");
        given(refreshTokenRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        TokenResponse result = authService.refresh("old-refresh");

        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        assertThat(stored.isRevoked()).isTrue();
    }

    @Test
    void refresh_revokedToken_throwsBadRequest() {
        RefreshToken stored = RefreshToken.builder()
                .token("revoked-token")
                .user(testUser)
                .revoked(true)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        given(refreshTokenRepository.findByToken("revoked-token")).willReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refresh("revoked-token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("revoked");
    }

    @Test
    void refresh_expiredToken_throwsBadRequest() {
        RefreshToken stored = RefreshToken.builder()
                .token("expired-token")
                .user(testUser)
                .revoked(false)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        given(refreshTokenRepository.findByToken("expired-token")).willReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refresh("expired-token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void refresh_unknownToken_throwsBadRequest() {
        given(refreshTokenRepository.findByToken("unknown")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("unknown"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid");
    }

    // --- logout ---

    @Test
    void logout_validToken_revokesToken() {
        RefreshToken stored = RefreshToken.builder()
                .token("valid-refresh")
                .user(testUser)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        given(refreshTokenRepository.findByToken("valid-refresh")).willReturn(Optional.of(stored));

        authService.logout("valid-refresh");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().isRevoked()).isTrue();
    }

    @Test
    void logout_unknownToken_doesNothing() {
        given(refreshTokenRepository.findByToken("unknown")).willReturn(Optional.empty());

        authService.logout("unknown");

        verify(refreshTokenRepository, never()).save(any());
    }
}
