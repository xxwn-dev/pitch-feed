package com.xxwn.pitchfeed.auth.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "test-jwt-secret-key-for-ci-must-be-at-least-32-chars";
    private static final long EXPIRATION_MS = 86400000L;

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(SECRET, EXPIRATION_MS);
    }

    @Test
    void createToken_생성된_토큰이_유효해야_한다() {
        String token = provider.createToken("testuser");

        assertThat(token).isNotBlank();
        assertThat(provider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_잘못된_토큰은_false를_반환한다() {
        assertThat(provider.validateToken("invalid.token.value")).isFalse();
        assertThat(provider.validateToken("")).isFalse();
    }

    @Test
    void getAuthentication_올바른_username과_ADMIN_권한을_반환한다() {
        String token = provider.createToken("testuser");

        Authentication auth = provider.getAuthentication(token);

        assertThat(auth.getName()).isEqualTo("testuser");
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }
}
