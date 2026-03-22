package com.xxwn.pitchfeed.domain.article.service;

import com.xxwn.pitchfeed.domain.article.controller.ArticleResponse;
import com.xxwn.pitchfeed.domain.article.entity.Article;
import com.xxwn.pitchfeed.domain.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;

    public List<ArticleResponse> getArticles(String category) {
        List<Article> articles = (category != null)
                ? articleRepository.findByFeedCategoryOrderByPublishedAtDesc(category)
                : articleRepository.findAllByOrderByPublishedAtDesc();
        return articles.stream().map(ArticleResponse::from).toList();
    }

    public ArticleResponse getArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));
        return ArticleResponse.from(article);
    }

    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }
}
