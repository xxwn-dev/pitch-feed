package com.xxwn.pitchfeed.rss.service;

import com.xxwn.pitchfeed.ai.service.ArticleSummaryService;
import com.xxwn.pitchfeed.ai.service.SummaryResult;
import com.xxwn.pitchfeed.rss.parser.RssItem;
import com.xxwn.pitchfeed.rss.parser.RssParser;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RssFetchService {

    private final FeedRepository feedRepository;
    private final ArticleRepository articleRepository;
    private final RssParser rssParser;
    private final ArticleSummaryService summaryService;

    @Value("${rss.fetch-limit:5}")
    private int fetchLimit;

    public FetchResult run() {
        List<Feed> feeds = feedRepository.findAllByActiveTrue();
        List<Article> newArticles = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Feed feed : feeds) {
            List<RssItem> items;
            try {
                items = rssParser.parse(feed.getUrl());
            } catch (RuntimeException e) {
                errors.add(e.getMessage());
                continue;
            }
            List<RssItem> limited = items.size() > fetchLimit ? items.subList(0, fetchLimit) : items;
            log.info("feeds: {} | Parsed: {}건 → 처리 대상: {}건", feed.getName(), items.size(), limited.size());

            // 1단계: URL 중복 필터링
            List<RssItem> candidates = limited.stream()
                    .filter(item -> !articleRepository.existsByUrl(item.getUrl()))
                    .toList();

            // 2단계: AI 호출 병렬 실행
            List<Callable<SummaryResult>> tasks = candidates.stream()
                    .map(item -> (Callable<SummaryResult>) () ->
                            summaryService.summarize(item.getTitle(), item.getContent(), feed.getCategory()))
                    .toList();

            List<SummaryResult> summaryResults;
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<SummaryResult>> futures = executor.invokeAll(tasks);
                summaryResults = futures.stream()
                        .map(f -> {
                            try { return f.get(); }
                            catch (InterruptedException | ExecutionException e) {
                                log.error("AI 요약 실패: {}", e.getMessage());
                                return new SummaryResult(null, null, true);
                            }
                        })
                        .toList();
            } catch (InterruptedException e) {
                log.error("AI 병렬 요약 중단: {}", e.getMessage());
                Thread.currentThread().interrupt();
                continue;
            }

            // 3단계: 중복 체크 & 저장 (순차)
            for (int i = 0; i < candidates.size(); i++) {
                RssItem item = candidates.get(i);
                SummaryResult result = summaryResults.get(i);

                if (result.skip()) {
                    log.info("AI 분류 스킵: {}", item.getTitle());
                    continue;
                }

                LocalDateTime dayStart = item.getPublishedAt().toLocalDate().atStartOfDay();
                LocalDateTime dayEnd = dayStart.plusDays(1);
                List<Article> sameDayArticles = articleRepository.findByPublishedAtBetween(dayStart, dayEnd);
                if (isDuplicateByTags(result.tags(), sameDayArticles)) {
                    log.info("태그 중복으로 건너뜀: {}", item.getTitle());
                    continue;
                }

                Article article = Article.builder()
                        .feed(feed)
                        .title(item.getTitle())
                        .url(item.getUrl())
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

        return new FetchResult(newArticles, errors);
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
