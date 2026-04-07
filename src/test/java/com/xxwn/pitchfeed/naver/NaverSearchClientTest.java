package com.xxwn.pitchfeed.naver;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Tag("integration")
public class NaverSearchClientTest {

    private NaverSearchClient naverSearchClient;

    @BeforeEach
    void setUp() throws IOException {
        Map<String, String> env = loadDotEnv();
        naverSearchClient = new NaverSearchClient(RestClient.create());
        ReflectionTestUtils.setField(naverSearchClient, "clientId", env.get("NAVER_CLIENT_ID"));
        ReflectionTestUtils.setField(naverSearchClient, "clientSecret", env.get("NAVER_CLIENT_SECRET"));
        ReflectionTestUtils.setField(naverSearchClient, "searchDisplay", 20);
    }

    private Map<String, String> loadDotEnv() throws IOException {
        Path envFile = Path.of(".env");
        if (!Files.exists(envFile)) return Map.of();
        return Files.readAllLines(envFile).stream()
                .filter(line -> !line.isBlank() && !line.startsWith("#") && line.contains("="))
                .collect(Collectors.toMap(
                        line -> line.substring(0, line.indexOf('=')).trim(),
                        line -> line.substring(line.indexOf('=') + 1).trim()
                ));
    }

    @Test
    void NAVER_NEWS_SEARCH_TEST() {
        List<NaverNewsItem> items = naverSearchClient.search("KBO 프로야구");

        log.info("검색 결과: {}건", items.size());
        items.forEach(item -> {
            log.info("title: {}", item.title());
            log.info("link: {}", item.link());
            log.info("description: {}", item.description());
            log.info("publishedAt: {}", item.publishedAt());
            log.info("---");
        });
    }
}
