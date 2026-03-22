package com.xxwn.pitchfeed.auth.controller;

import com.xxwn.pitchfeed.auth.jwt.JwtAuthenticationFilter;
import com.xxwn.pitchfeed.auth.jwt.JwtTokenProvider;
import com.xxwn.pitchfeed.global.config.SecurityConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    @Test
    void login_올바른_자격증명으로_토큰을_반환한다() throws Exception {
        given(jwtTokenProvider.createToken("testadmin")).willReturn("mocked-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "testadmin", "password": "testpassword"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    void login_잘못된_비밀번호는_401을_반환한다() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "testadmin", "password": "wrongpassword"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_존재하지_않는_유저는_401을_반환한다() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "unknown", "password": "testpassword"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
