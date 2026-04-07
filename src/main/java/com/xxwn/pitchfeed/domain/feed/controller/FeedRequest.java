package com.xxwn.pitchfeed.domain.feed.controller;

import com.xxwn.pitchfeed.domain.feed.entity.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeedRequest(
        @NotBlank String name,
        String url,
        String category,
        String keywords,
        String query,
        @NotNull SourceType sourceType
) {
}
