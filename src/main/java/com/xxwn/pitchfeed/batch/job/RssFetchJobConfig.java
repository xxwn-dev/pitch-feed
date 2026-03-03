package com.xxwn.pitchfeed.batch.job;

import com.xxwn.pitchfeed.ai.service.ArticleSummaryService;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

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
            log.info("Size of Feeds: {}", feeds.size());

            for(Feed feed : feeds){
                List<RssItem> items = rssParser.parse(feed.getUrl());
                int savedCount = 0;

                for(RssItem item : items){
                    if(articleRepository.existsByUrl(item.getUrl())){
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

                    ArticleSummaryService.SummaryResult result =
                            summaryService.summarize(item.getTitle(), item.getContent());
                    article.addSummary(result.summary(), result.tags());
                    articleRepository.save(article);
                    savedCount++;
                }
                feed.updateLastFetchedAt();
                feedRepository.save(feed);
                log.info("feed: {} | saved new: {} ", feed.getName(), savedCount );
            }
            return RepeatStatus.FINISHED;
        });
    }
}
