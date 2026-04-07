package com.xxwn.pitchfeed.rss;

import com.xxwn.pitchfeed.rss.service.RssFetchService;
import com.xxwn.pitchfeed.domain.article.entity.Article;
import com.xxwn.pitchfeed.domain.article.repository.ArticleRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
@Tag("integration")
public class RssFetchJobTest {

    @Autowired
    private RssFetchService rssFetchService;

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    void KBO_NEWS_COLLECT_TEST() {
        rssFetchService.run();

        List<Article> articles = articleRepository.findAll();
        log.info("size of Collected articles: {}", articles.size());
        articles.forEach(i -> {
            log.info("title: {}", i.getTitle());
            log.info("summary: {}", i.getSummary());
            log.info("tags: {}", i.getTags());
        });
    }
}
