package com.xxwn.pitchfeed.naver;

import java.time.LocalDateTime;

public record NaverNewsItem(
        String title,
        String link,
        String description,
        LocalDateTime publishedAt
) {}
