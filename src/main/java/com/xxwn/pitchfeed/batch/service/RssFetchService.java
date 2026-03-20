package com.xxwn.pitchfeed.batch.service;

import com.xxwn.pitchfeed.ai.service.ArticleSummaryService;
import com.xxwn.pitchfeed.ai.service.DiscordWebhookService;
import com.xxwn.pitchfeed.batch.tasklet.RssItem;
import com.xxwn.pitchfeed.batch.tasklet.RssParser;
import com.xxwn.pitchfeed.domain.article.entity.Article;
import com.xxwn.pitchfeed.domain.article.repository.ArticleRepository;
import com.xxwn.pitchfeed.domain.feed.entity.Feed;
import com.xxwn.pitchfeed.domain.feed.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RssFetchService {

    private final FeedRepository feedRepository;
    private final ArticleRepository articleRepository;
    private final RssParser rssParser;
    private final ArticleSummaryService summaryService;
    private final DiscordWebhookService discordWebhookService;

    @Value("${batch.fetch-limit:5}")
    private int fetchLimit;

    public void run() {
        List<Feed> feeds = feedRepository.findAllByActiveTrue();
        List<Article> newArticles = new ArrayList<>();

        for (Feed feed : feeds) {
            List<RssItem> items = rssParser.parse(feed.getUrl());
            List<RssItem> limited = items.size() > fetchLimit ? items.subList(0, fetchLimit) : items;
            log.info("feeds: {} | Parsed: {}건 → 처리 대상: {}건", feed.getName(), items.size(), limited.size());

            for (RssItem item : limited) {
                if (articleRepository.existsByUrl(item.getUrl())) {
                    continue;
                }

                ArticleSummaryService.SummaryResult result =
                        summaryService.summarize(item.getTitle(), item.getContent());

                LocalDateTime dayStart = item.getPublishedAt().toLocalDate().atStartOfDay();
                LocalDateTime dayEnd = dayStart.plusDays(1);
                List<Article> sameDayArticles = articleRepository.findByPublishedAtBetween(dayStart, dayEnd);
                if (isDuplicateByTags(result.tags(), sameDayArticles)) {
                    log.info("태그 중복으로 건너뜀: {}", item.getTitle());
                    continue;
                }

                String imageUrl = rssParser.extractOgImage(item.getUrl());
                Article article = Article.builder()
                        .feed(feed)
                        .title(item.getTitle())
                        .url(item.getUrl())
                        .imageUrl(imageUrl)
                        .author(item.getAuthor())
                        .content(item.getContent())
                        .publishedAt(item.getPublishedAt())
                        .build();
                article.addSummary(result.summary(), result.tags());
                articleRepository.save(article);
                newArticles.add(article);
            }
            feed.updateLastFetchedAt();
            feedRepository.save(feed);
        }

        discordWebhookService.send(newArticles);
    }

    private boolean isDuplicateByTags(String newTags, List<Article> sameDayArticles) {
        if (newTags == null || newTags.isBlank()) return false;
        Set<String> newTagSet = Arrays.stream(newTags.split(","))
                .map(String::trim)
                .filter(t -> !t.isBlank())
                .collect(Collectors.toSet());
        if (newTagSet.isEmpty()) return false;

        for (Article existing : sameDayArticles) {
            if (existing.getTags() == null) continue;
            Set<String> existingTagSet = Arrays.stream(existing.getTags().split(","))
                    .map(String::trim)
                    .filter(t -> !t.isBlank())
                    .collect(Collectors.toSet());
            long overlap = newTagSet.stream().filter(existingTagSet::contains).count();
            if (overlap >= 2) {
                return true;
            }
        }
        return false;
    }
}
