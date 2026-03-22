package com.xxwn.pitchfeed.global.config;

import com.xxwn.pitchfeed.auth.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, e) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/articles/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/feeds").permitAll()
                .requestMatchers("/articles/**").permitAll()  // Thymeleaf 프리뷰
                .requestMatchers(HttpMethod.POST, "/api/rss/trigger").permitAll()  // secret 헤더 방식 유지
                .requestMatchers(HttpMethod.POST, "/api/feeds").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/feeds/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/articles/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
