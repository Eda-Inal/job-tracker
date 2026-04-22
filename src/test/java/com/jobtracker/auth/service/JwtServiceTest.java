package com.jobtracker.auth.service;

import com.jobtracker.user.entity.Role;
import com.jobtracker.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET =
            "bXktc2VjcmV0LWtleS1mb3Itand0LXRva2VuLWdlbmVyYXRpb24tc2hvdWxkLWJlLXZlcnktbG9uZw==";

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900_000L);

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPass")
                .role(Role.USER)
                .build();
    }

    @Test
    void generateAccessToken_returnsNonBlankToken() {
        String token = jwtService.generateAccessToken(user);
        assertThat(token).isNotBlank();
    }

    @Test
    void extractUsername_returnsCorrectEmail() {
        String token = jwtService.generateAccessToken(user);
        assertThat(jwtService.extractUsername(token)).isEqualTo("test@example.com");
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtService.generateAccessToken(user);
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_wrongUser_returnsFalse() {
        User other = User.builder()
                .id(2L)
                .email("other@example.com")
                .password("pass")
                .role(Role.USER)
                .build();

        String token = jwtService.generateAccessToken(user);
        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        JwtService shortLived = new JwtService();
        ReflectionTestUtils.setField(shortLived, "secretKey", SECRET);
        ReflectionTestUtils.setField(shortLived, "accessTokenExpiration", -1000L);

        String token = shortLived.generateAccessToken(user);
        assertThat(shortLived.isTokenValid(token, user)).isFalse();
    }

    @Test
    void isTokenValid_tamperedToken_returnsFalse() {
        String token = jwtService.generateAccessToken(user) + "tampered";
        assertThat(jwtService.isTokenValid(token, user)).isFalse();
    }
}
