package com.xxwn.pitchfeed.auth.controller;

import com.xxwn.pitchfeed.auth.dto.LoginRequest;
import com.xxwn.pitchfeed.auth.dto.TokenResponse;
import com.xxwn.pitchfeed.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        if (!adminUsername.equals(request.getUsername()) || !adminPassword.equals(request.getPassword())) {
            return ResponseEntity.status(401).build();
        }
        String token = jwtTokenProvider.createToken(request.getUsername());
        return ResponseEntity.ok(new TokenResponse(token));
    }
}
