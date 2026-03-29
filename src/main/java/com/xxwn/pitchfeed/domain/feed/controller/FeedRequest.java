package com.xxwn.pitchfeed.domain.feed.controller;

import jakarta.validation.constraints.NotBlank;

public record FeedRequest(
        @NotBlank String name,
        @NotBlank String url,
        String category,
        String keywords
) {
}
