package com.xxwn.pitchfeed.naver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverSearchClient {

    private static final DateTimeFormatter PUB_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    private final RestClient restClient;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.search-display:20}")
    private int searchDisplay;

    public List<NaverNewsItem> search(String query) {
        try {
            NaverApiResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("openapi.naver.com")
                            .path("/v1/search/news.json")
                            .queryParam("query", query)
                            .queryParam("display", searchDisplay)
                            .queryParam("sort", "date")
                            .build())
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .retrieve()
                    .body(NaverApiResponse.class);

            if (response == null || response.items() == null) return List.of();

            return response.items().stream()
                    .map(this::toNaverNewsItem)
                    .toList();
        } catch (Exception e) {
            log.error("Naver API 호출 실패 - query: {}, error: {}", query, e.getMessage());
            throw new RuntimeException("Naver API 호출 실패 [" + query + "]: " + e.getMessage(), e);
        }
    }

    private NaverNewsItem toNaverNewsItem(NaverApiItem item) {
        return new NaverNewsItem(
                cleanHtml(item.title()),
                item.link(),
                cleanHtml(item.description()),
                parseDate(item.pubDate())
        );
    }

    private String cleanHtml(String text) {
        if (text == null) return null;
        String stripped = text.replaceAll("<[^>]+>", "");
        return HtmlUtils.htmlUnescape(stripped);
    }

    private LocalDateTime parseDate(String pubDate) {
        if (pubDate == null) return LocalDateTime.now();
        try {
            return ZonedDateTime.parse(pubDate, PUB_DATE_FORMATTER)
                    .withZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (Exception e) {
            log.warn("Naver 날짜 파싱 실패: {}", pubDate);
            return LocalDateTime.now();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record NaverApiResponse(List<NaverApiItem> items) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record NaverApiItem(String title, String link, String description, String pubDate) {}
}
