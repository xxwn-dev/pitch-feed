package com.xxwn.pitchfeed.rss.parser;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RssParser {

    private final RestClient restClient;

    public List<RssItem> parse(String feedUrl) {
        try {
            String feedContent = restClient.get()
                    .uri(feedUrl)
                    .header("User-Agent", "Mozilla/5.0")
                    .retrieve()
                    .body(String.class);

            String sanitized = sanitizeXml(feedContent);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new StringReader(sanitized));

            return feed.getEntries().stream()
                    .map(this::toRssItem)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("RSS 파싱 실패 - url: {}, error: {}", feedUrl, e.getMessage());
            throw new RuntimeException("RSS 파싱 실패 [" + feedUrl + "]: " + e.getMessage(), e);
        }
    }

    private RssItem toRssItem(SyndEntry entry) {
        return RssItem.builder()
                .title(entry.getTitle())
                .url(entry.getLink())
                .author(entry.getAuthor())
                .content(extractContents(entry))
                .publishedAt(convertToLocalDateTime(entry.getPublishedDate()))
                .build();
    }

    private String extractContents(SyndEntry entry) {
        if (entry.getContents() != null && !entry.getContents().isEmpty()) {
            return entry.getContents().get(0).getValue();
        }
        if (entry.getDescription() != null) {
            return entry.getDescription().getValue();
        }
        return null;
    }

    private String sanitizeXml(String xml) {
        if (xml == null) return "";
        xml = wrapFieldInCdata(xml, "description");
        xml = wrapFieldInCdata(xml, "content:encoded");
        return xml;
    }

    private String wrapFieldInCdata(String xml, String tag) {
        return Pattern.compile(
                "<" + tag + ">(?!\\s*<!\\[CDATA\\[)(.*?)</" + tag + ">",
                Pattern.DOTALL
        ).matcher(xml).replaceAll("<" + tag + "><![CDATA[$1]]></" + tag + ">");
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) return LocalDateTime.now();
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
