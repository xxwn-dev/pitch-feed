package com.xxwn.pitchfeed.batch.tasklet;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RssParser {

    public List<RssItem> parse(String feedUrl) {
        try {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(URI.create(feedUrl).toURL()));

            return feed.getEntries().stream()
                    .map(this::toRssItem)
                    .collect(Collectors.toList());

        } catch (Exception e) {
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

    private LocalDateTime convertToLocalDateTime(Date date) {
        if(date == null) return LocalDateTime.now();
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
