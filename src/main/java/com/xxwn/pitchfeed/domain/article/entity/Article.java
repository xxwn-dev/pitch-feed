package com.xxwn.pitchfeed.domain.article.entity;

import com.xxwn.pitchfeed.domain.feed.entity.Feed;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "articles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id")
    private Feed feed;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String url;

    @Column(columnDefinition = "TEXT")
    private String author;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String tags;

    private LocalDateTime publishedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Article(Feed feed, String title, String url, String author, String content, LocalDateTime publishedAt) {
        this.feed = feed;
        this.title = title;
        this.url = url;
        this.author = author;
        this.content = content;
        this.publishedAt = publishedAt;
    }

    public void addSummary(String summary, String tags){
        this.summary = summary;
        this.tags = tags;
    }
}
