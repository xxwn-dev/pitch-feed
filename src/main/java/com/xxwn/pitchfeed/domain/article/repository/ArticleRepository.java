package com.xxwn.pitchfeed.domain.article.repository;

import com.xxwn.pitchfeed.domain.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    boolean existsByUrl(String url);

    List<Article> findAllByOrderByPublishedAtDesc();
    List<Article> findByFeedCategoryOrderByPublishedAtDesc(String category);
    List<Article> findByPublishedAtBetween(LocalDateTime start, LocalDateTime end);
    void deleteByFeedId(Long feedId);

    @Query("SELECT a.titleHash FROM Article a WHERE a.publishedAt BETWEEN :start AND :end AND a.titleHash IS NOT NULL")
    List<Long> findTitleHashesByPublishedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
