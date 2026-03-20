package com.xxwn.pitchfeed;

import com.xxwn.pitchfeed.batch.tasklet.RssItem;
import com.xxwn.pitchfeed.batch.tasklet.RssParser;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Slf4j
@Disabled("로컬 수동 테스트 전용")
public class RssParserTest {

    @Autowired
    private RssParser rssParser;

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
}
