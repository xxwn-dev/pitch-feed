package com.xxwn.pitchfeed.domain.article.repository;

import com.xxwn.pitchfeed.domain.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    boolean existsByUrl(String url);

    List<Article> findAllByOrderByPublishedAtDesc();
    List<Article> findByFeedCategoryOrderByPublishedAtDesc(String category);
}
