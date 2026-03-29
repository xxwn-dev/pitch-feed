package com.xxwn.pitchfeed.rss.controller;

import com.xxwn.pitchfeed.ai.service.DiscordWebhookService;
import com.xxwn.pitchfeed.rss.service.RssFetchService;
import com.xxwn.pitchfeed.domain.article.entity.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xxwn.pitchfeed.rss.service.FetchResult;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rss")
@RequiredArgsConstructor
public class RssController {

    private final RssFetchService rssFetchService;
    private final DiscordWebhookService discordWebhookService;

    @Value("${rss.secret}")
    private String rssSecret;

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> trigger(
            @RequestHeader("X-Batch-Secret") String secret) {
        if (!rssSecret.equals(secret)) {
            return ResponseEntity.status(401).build();
        }
        FetchResult result = rssFetchService.run();
        Map<String, Object> payload = discordWebhookService.buildPayload(result.articles());
        if (!result.errors().isEmpty()) {
            payload.put("errors", result.errors());
        }
        return ResponseEntity.ok(payload);
    }
}
