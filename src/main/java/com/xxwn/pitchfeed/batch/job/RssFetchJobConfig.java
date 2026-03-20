package com.xxwn.pitchfeed.batch.job;

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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RssFetchJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final FeedRepository feedRepository;
    private final ArticleRepository articleRepository;
    private final RssParser rssParser;
    private final ArticleSummaryService summaryService;
    private final DiscordWebhookService discordWebhookService;

    @Value("${batch.fetch-limit:5}")
    private int fetchLimit;

    @Bean
    public Job rssFetchJob() {
        return new JobBuilder("rssFetchJob", jobRepository)
                .start(rssFetchStep())
                .build();
    }

    @Bean
    public Step rssFetchStep() {
        return new StepBuilder("rssFetchStep", jobRepository)
                .tasklet(rssFetchTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet rssFetchTasklet() {
        return ((contribution, chunkContext) -> {
            List<Feed> feeds = feedRepository.findAllByActiveTrue();

            List<Article> newArticles = new ArrayList<>();
            for (Feed feed : feeds) {
                List<RssItem> items = rssParser.parse(feed.getUrl());
                List<RssItem> limited = items.size() > fetchLimit ? items.subList(0, fetchLimit) : items;
                log.info("feeds: {} | Parsed: {}건 → 처리 대상: {}건", feed.getName(), items.size(), limited.size());

                for (RssItem item : limited) {
                    // 1. URL 중복 체크
                    if (articleRepository.existsByUrl(item.getUrl())) {
                        continue;
                    }

                    // 2. AI 요약 먼저 실행 (태그 추출)
                    ArticleSummaryService.SummaryResult result =
                            summaryService.summarize(item.getTitle(), item.getContent());

                    // 3. 같은 날 태그 기반 중복 체크
                    LocalDateTime dayStart = item.getPublishedAt().toLocalDate().atStartOfDay();
                    LocalDateTime dayEnd = dayStart.plusDays(1);
                    List<Article> sameDayArticles = articleRepository.findByPublishedAtBetween(dayStart, dayEnd);
                    if (isDuplicateByTags(result.tags(), sameDayArticles)) {
                        log.info("태그 중복으로 건너뜀: {}", item.getTitle());
                        continue;
                    }

                    // 4. OG 이미지 추출 및 저장
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
            return RepeatStatus.FINISHED;
        });
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
