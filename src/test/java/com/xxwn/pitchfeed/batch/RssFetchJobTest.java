package com.xxwn.pitchfeed.batch;

import com.xxwn.pitchfeed.domain.article.entity.Article;
import com.xxwn.pitchfeed.domain.article.repository.ArticleRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
public class RssFetchJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job rssFetchJob;

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    void KBO_NEWS_COLLECT_TEST() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLocalDateTime("runAt", LocalDateTime.now())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(rssFetchJob, params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<Article> articles = articleRepository.findAll();
        log.info("size of Collected articles: {}" , articles.size());
        articles.forEach(i -> {
            log.info("Summary: {}", i.getTitle());
            log.info("summary: {}", i.getSummary());
            log.info("tags: {}", i.getTags());
        });
    }
}
