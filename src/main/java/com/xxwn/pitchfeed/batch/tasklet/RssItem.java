package com.xxwn.pitchfeed.batch.tasklet;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RssItem {
    private String title;
    private String url;
    private String author;
    private String content;
    private LocalDateTime publishedAt;
}
