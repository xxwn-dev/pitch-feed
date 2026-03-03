package com.xxwn.pitchfeed.domain.feed.controller;

import com.xxwn.pitchfeed.domain.feed.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

        private final FeedService feedService;

        @GetMapping
        public ResponseEntity<List<FeedResponse>> getFeeds() {
            return ResponseEntity.ok(feedService.getFeeds());
        }

        @PostMapping
        public ResponseEntity<FeedResponse> addFeed(@RequestBody @Valid FeedRequest request) {
            return ResponseEntity.ok(feedService.addFeed(request));
        }

        @DeleteMapping
        public ResponseEntity<Void> deleteFeed(@PathVariable Long id) {
            feedService.deleteFeed(id);
            return ResponseEntity.noContent().build();
        }

}
