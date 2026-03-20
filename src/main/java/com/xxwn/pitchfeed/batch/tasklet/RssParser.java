package com.xxwn.pitchfeed.batch.tasklet;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
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
import java.util.regex.Matcher;
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

            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new StringReader(feedContent));

            return feed.getEntries().stream()
                    .map(this::toRssItem)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("RSS 파싱 실패 - url: {}, error: {}", feedUrl, e.getMessage());
            return Collections.emptyList();
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
        if(entry.getContents() != null && !entry.getContents().isEmpty()){
            return entry.getContents().get(0).getValue();
        }
        if(entry.getDescription() != null){
            return entry.getDescription().getValue();
        }
        return null;
    }

    public String extractOgImage(String articleUrl) {
        try {
            String html = restClient.get()
                    .uri(articleUrl)
                    .header("User-Agent", "Mozilla/5.0")
                    .retrieve()
                    .body(String.class);

            Pattern p1 = Pattern.compile("<meta[^>]*property=[\"']og:image[\"'][^>]*content=[\"']([^\"']+)[\"']");
            Matcher m1 = p1.matcher(html);
            if (m1.find()) return m1.group(1);

            Pattern p2 = Pattern.compile("<meta[^>]*content=[\"']([^\"']+)[\"'][^>]*property=[\"']og:image[\"']");
            Matcher m2 = p2.matcher(html);
            return m2.find() ? m2.group(1) : null;

        } catch (Exception e) {
            log.warn("og:image 추출 실패 - url: {}", articleUrl);
            return null;
        }
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        if(date == null) return LocalDateTime.now();
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
