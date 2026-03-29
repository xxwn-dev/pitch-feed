package com.xxwn.pitchfeed.rss.service;

import com.xxwn.pitchfeed.domain.article.entity.Article;

import java.util.List;

public record FetchResult(List<Article> articles, List<String> errors) {}
