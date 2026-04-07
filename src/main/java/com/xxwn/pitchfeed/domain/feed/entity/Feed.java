package com.xxwn.pitchfeed.domain.feed.entity;

import jakarta.persistence.*;
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

    @Column(unique = true)
    private String url;

    private String category;

    private String keywords;

    private String query;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'RSS'")
    private SourceType sourceType = SourceType.RSS;

    private boolean active = true;

    private LocalDateTime lastFetchedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Feed(String name, String url, String category, String keywords, String query, SourceType sourceType) {
        this.name = name;
        this.url = url;
        this.category = category;
        this.keywords = keywords;
        this.query = query;
        this.sourceType = sourceType != null ? sourceType : SourceType.RSS;
    }

public void updateLastFetchedAt() {
        this.lastFetchedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
    }
}
