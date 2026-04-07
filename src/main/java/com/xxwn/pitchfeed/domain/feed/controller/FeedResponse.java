package com.xxwn.pitchfeed.domain.feed.controller;

import com.xxwn.pitchfeed.domain.feed.entity.Feed;
import com.xxwn.pitchfeed.domain.feed.entity.SourceType;

import java.time.LocalDateTime;

public record FeedResponse(
        Long id,
        String name,
        String url,
        String category,
        String keywords,
        String query,
        SourceType sourceType,
        boolean active,
        LocalDateTime lastFetchedAt
) {
    public static FeedResponse from(Feed feed) {
        return new FeedResponse(
                feed.getId(),
                feed.getName(),
                feed.getUrl(),
                feed.getCategory(),
                feed.getKeywords(),
                feed.getQuery(),
                feed.getSourceType(),
                feed.isActive(),
                feed.getLastFetchedAt()
        );
    }
}
