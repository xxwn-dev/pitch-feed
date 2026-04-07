package com.xxwn.pitchfeed.naver;

import com.xxwn.pitchfeed.domain.article.entity.Article;
import com.xxwn.pitchfeed.rss.service.FetchResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
@Tag("integration")
public class NaverFetchServiceTest {

    @Autowired
    private NaverFetchService naverFetchService;

    @Test
    void NAVER_FETCH_PIPELINE_TEST() {
        FetchResult result = naverFetchService.run();

        log.info("=== 저장된 기사: {}건 ===", result.articles().size());
        for (Article article : result.articles()) {
            log.info("title: {}", article.getTitle());
            log.info("summary: {}", article.getSummary());
            log.info("url: {}", article.getUrl());
            log.info("---");
        }

        if (!result.errors().isEmpty()) {
            log.warn("=== 에러: {}건 ===", result.errors().size());
            result.errors().forEach(e -> log.warn("error: {}", e));
        }
    }
}
