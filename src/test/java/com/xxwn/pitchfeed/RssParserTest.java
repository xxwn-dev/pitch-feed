package com.xxwn.pitchfeed;

import com.xxwn.pitchfeed.rss.parser.RssItem;
import com.xxwn.pitchfeed.rss.parser.RssParser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Slf4j
@Tag("integration")
public class RssParserTest {

    @Autowired
    private RssParser rssParser;

    @Autowired
    private org.springframework.web.client.RestClient restClient;

    @Test
    void MLB_RSS_PARSING_TEST() {
        List<RssItem> items = rssParser.parse("https://www.mlb.com/feeds/news/rss.xml");

        assertThat(items).isNotEmpty();
        items.forEach(item -> {
            log.info("Summary: {}", item.getTitle());
            log.info("url: {}", item.getUrl());
            log.info("publishedAt: {}", item.getPublishedAt());
        });
    }

    @Test
    void KBO_연합뉴스_카테고리_확인_TEST() {
        com.rometools.rome.io.SyndFeedInput input = new com.rometools.rome.io.SyndFeedInput();
        String raw = restClient.get()
                .uri("https://www.yna.co.kr/rss/sports.xml")
                .header("User-Agent", "Mozilla/5.0")
                .retrieve()
                .body(String.class);
        try {
            com.rometools.rome.feed.synd.SyndFeed feed = input.build(new java.io.StringReader(raw));
            feed.getEntries().stream().limit(10).forEach(entry -> {
                log.info("title: {} | categories: {}", entry.getTitle(),
                        entry.getCategories().stream()
                                .map(c -> c.getName())
                                .toList());
            });
        } catch (Exception e) {
            log.error("파싱 실패: {}", e.getMessage());
        }
    }

    @Test
    void KBO_연합뉴스_RSS_PARSING_TEST() {
        List<RssItem> items = rssParser.parse("https://www.yna.co.kr/rss/sports.xml");

        log.info("파싱 결과: {}건", items.size());
        assertThat(items).isNotEmpty();
        items.forEach(item -> {
            log.info("title: {}", item.getTitle());
            log.info("url: {}", item.getUrl());
            log.info("publishedAt: {}", item.getPublishedAt());
        });
    }
}
