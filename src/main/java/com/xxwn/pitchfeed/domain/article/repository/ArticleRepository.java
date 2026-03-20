package com.xxwn.pitchfeed.domain.article.repository;

import com.xxwn.pitchfeed.domain.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    boolean existsByUrl(String url);

    List<Article> findAllByOrderByPublishedAtDesc();
    List<Article> findByFeedCategoryOrderByPublishedAtDesc(String category);
    List<Article> findByPublishedAtBetween(LocalDateTime start, LocalDateTime end);
    void deleteByFeedId(Long feedId);
}
