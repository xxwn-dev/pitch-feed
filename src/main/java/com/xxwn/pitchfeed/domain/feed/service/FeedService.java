package com.xxwn.pitchfeed.domain.feed.service;

import com.xxwn.pitchfeed.domain.article.repository.ArticleRepository;
import com.xxwn.pitchfeed.domain.feed.controller.FeedRequest;
import com.xxwn.pitchfeed.domain.feed.controller.FeedResponse;
import com.xxwn.pitchfeed.domain.feed.entity.Feed;
import com.xxwn.pitchfeed.domain.feed.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final ArticleRepository articleRepository;

    public List<FeedResponse> getFeeds() {
        return feedRepository.findAll().stream()
                .map(FeedResponse::from)
                .toList();
    }

    public FeedResponse addFeed(FeedRequest request) {
        Feed feed = Feed.builder()
                .name(request.name())
                .url(request.url())
                .category(request.category())
                .build();
        return FeedResponse.from(feedRepository.save(feed));
    }

    @Transactional
    public void deleteFeed(Long id) {
        articleRepository.deleteByFeedId(id);
        feedRepository.deleteById(id);
    }
}
