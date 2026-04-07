package com.xxwn.pitchfeed.naver;

import com.xxwn.pitchfeed.ai.service.ArticleSummaryService;
import com.xxwn.pitchfeed.domain.article.entity.Article;
import com.xxwn.pitchfeed.domain.article.repository.ArticleRepository;
import com.xxwn.pitchfeed.domain.feed.entity.Feed;
import com.xxwn.pitchfeed.domain.feed.entity.SourceType;
import com.xxwn.pitchfeed.domain.feed.repository.FeedRepository;
import com.xxwn.pitchfeed.rss.service.FetchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverFetchService {

    private final FeedRepository feedRepository;
    private final ArticleRepository articleRepository;
    private final NaverSearchClient naverSearchClient;
    private final ArticleSummaryService articleSummaryService;

    @Value("${rss.save-limit:5}")
    private int saveLimit;

    public FetchResult run() {
        List<Feed> feeds = feedRepository.findAllBySourceTypeAndActiveTrue(SourceType.NAVER_API);
        List<Article> newArticles = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Feed feed : feeds) {
            if (feed.getQuery() == null || feed.getQuery().isBlank()) {
                log.warn("feeds: {} | query가 없어 건너뜀", feed.getName());
                continue;
            }
            List<NaverNewsItem> items;
            try {
                items = naverSearchClient.search(feed.getQuery());
            } catch (RuntimeException e) {
                errors.add(e.getMessage());
                continue;
            }

            int savedCount = 0;
            for (NaverNewsItem item : items) {
                if (savedCount >= saveLimit) break;
                if (articleRepository.existsByUrl(item.link())) continue;
                if (articleSummaryService.classify(item.title(), item.description(), feed.getCategory())) continue;

                Article article = Article.builder()
                        .feed(feed)
                        .title(item.title())
                        .url(item.link())
                        .content(null)
                        .publishedAt(item.publishedAt())
                        .build();
                article.addSummary(item.description(), null);
                articleRepository.save(article);
                newArticles.add(article);
                savedCount++;
            }

            log.info("feeds: {} | 검색: {}건 → 저장: {}건", feed.getName(), items.size(), savedCount);
            feed.updateLastFetchedAt();
            feedRepository.save(feed);
        }

        return new FetchResult(newArticles, errors);
    }
}
