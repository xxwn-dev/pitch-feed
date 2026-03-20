package com.xxwn.pitchfeed.batch.controller;

import com.xxwn.pitchfeed.batch.service.RssFetchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final RssFetchService rssFetchService;

    @Value("${batch.secret}")
    private String batchSecret;

    @PostMapping("/trigger")
    public ResponseEntity<String> trigger(
            @RequestHeader("X-Batch-Secret") String secret) {
        if (!batchSecret.equals(secret)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        rssFetchService.run();
        return ResponseEntity.ok("Triggered");
    }
}
