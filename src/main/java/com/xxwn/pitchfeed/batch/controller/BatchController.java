package com.xxwn.pitchfeed.batch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job rssFetchJob;

    @Value("${batch.secret}")
    private String batchSecret;

    @PostMapping("/trigger")
    public ResponseEntity<String> trigger(
            @RequestHeader("X-Batch-Secret") String secret) throws  Exception {
        if(!batchSecret.equals(secret)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        JobParameters params = new JobParametersBuilder()
                .addLocalDateTime("runAt", LocalDateTime.now())
                .toJobParameters();

        jobLauncher.run(rssFetchJob, params);
        return ResponseEntity.ok("Batch triggered");
    }
}
