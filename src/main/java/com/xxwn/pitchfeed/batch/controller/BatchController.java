package com.xxwn.pitchfeed.batch.controller;

import com.xxwn.pitchfeed.ai.service.DiscordWebhookService;
import com.xxwn.pitchfeed.batch.service.RssFetchService;
import com.xxwn.pitchfeed.domain.article.entity.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final RssFetchService rssFetchService;
    private final DiscordWebhookService discordWebhookService;

    @Value("${batch.secret}")
    private String batchSecret;

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> trigger(
            @RequestHeader("X-Batch-Secret") String secret) {
        if (!batchSecret.equals(secret)) {
            return ResponseEntity.status(401).build();
        }
        List<Article> newArticles = rssFetchService.run();
        Map<String, Object> payload = discordWebhookService.buildPayload(newArticles);
        return ResponseEntity.ok(payload);
    }
}
