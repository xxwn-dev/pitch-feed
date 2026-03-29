package com.xxwn.pitchfeed.domain.feed.entity;

import jakarta.persistence.*;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "feeds")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String url;

    private String category;

    private String keywords;

    private boolean active = true;

    private LocalDateTime lastFetchedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Feed(String name, String url, String category, String keywords) {
        this.name = name;
        this.url = url;
        this.category = category;
        this.keywords = keywords;
    }

    public boolean matchesKeywords(String title, String content) {
        if (keywords == null || keywords.isBlank()) return true;
        String text = (title == null ? "" : title) + " " + (content == null ? "" : content);
        return Arrays.stream(keywords.split(","))
                .map(String::trim)
                .filter(k -> !k.isBlank())
                .anyMatch(text::contains);
    }

    public void updateLastFetchedAt() {
        this.lastFetchedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
    }
}
