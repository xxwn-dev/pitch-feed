package com.xxwn.pitchfeed.domain.article.controller;

import com.xxwn.pitchfeed.domain.article.entity.Article;

import java.time.LocalDateTime;

public record ArticleResponse(
        Long id,
        String feedName,
        String category,
        String title,
        String url,
        String summary,
        String tags,
        LocalDateTime publishedAt
) {
    public static ArticleResponse from(Article article) {
        return new ArticleResponse(
                article.getId(),
                article.getFeed().getName(),
                article.getFeed().getCategory(),
                article.getTitle(),
                article.getUrl(),
                article.getSummary(),
                article.getTags(),
                article.getPublishedAt()
        );
    }
}
